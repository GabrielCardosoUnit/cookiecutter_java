package br.edu.unit.jmolgen.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeoutException;

/** Executa {@code mvn --batch-mode test} sem ocultar a saida nem bloquear indefinidamente. */
public final class MavenVerifier {

  private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);
  private final List<String> command;
  private final Duration timeout;

  public MavenVerifier() {
    this(defaultCommand(), DEFAULT_TIMEOUT);
  }

  public MavenVerifier(List<String> command, Duration timeout) {
    if (command == null || command.isEmpty() || timeout == null || timeout.isNegative()
        || timeout.isZero()) {
      throw new IllegalArgumentException("comando e timeout devem ser validos");
    }
    this.command = List.copyOf(command);
    this.timeout = timeout;
  }

  /** Executa o Maven no diretorio do projeto e devolve falhas como resultado, nao excecao. */
  public MavenVerificationResult verify(Path projectDirectory) {
    if (projectDirectory == null || !Files.isDirectory(projectDirectory)) {
      return failedToStart("Diretorio do projeto nao existe ou nao e uma pasta");
    }

    Process process;
    try {
      process = new ProcessBuilder(command).directory(projectDirectory.toFile()).start();
    } catch (IOException e) {
      return failedToStart("Nao foi possivel iniciar o Maven (" + e.getClass().getSimpleName()
          + "): " + e.getMessage());
    }

    CompletableFuture<String> stdout = readAsync(process.getInputStream());
    CompletableFuture<String> stderr = readAsync(process.getErrorStream());
    try {
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        process.waitFor();
        return new MavenVerificationResult(true, false, true, null, output(stdout),
            output(stderr));
      }
      int exitCode = process.exitValue();
      return new MavenVerificationResult(true, exitCode == 0, false, exitCode, output(stdout),
          output(stderr));
    } catch (InterruptedException e) {
      process.destroyForcibly();
      Thread.currentThread().interrupt();
      return new MavenVerificationResult(true, false, false, null, output(stdout),
          append(output(stderr), "Verificacao interrompida"));
    }
  }

  private static List<String> defaultCommand() {
    List<String> command = new ArrayList<>();
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      command.addAll(List.of("cmd.exe", "/d", "/c", "mvn.cmd"));
    } else {
      command.add("mvn");
    }
    command.addAll(List.of("--batch-mode", "test"));
    return command;
  }

  private static CompletableFuture<String> readAsync(InputStream stream) {
    return CompletableFuture.supplyAsync(() -> {
      try (stream) {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        return "Falha ao ler saida do processo: " + e.getMessage();
      }
    });
  }

  private static String output(CompletableFuture<String> output) {
    try {
      return output.get(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "Saida do processo indisponivel";
    } catch (ExecutionException | TimeoutException e) {
      return "Saida do processo indisponivel";
    }
  }

  private static MavenVerificationResult failedToStart(String message) {
    return new MavenVerificationResult(false, false, false, null, "", message);
  }

  private static String append(String text, String extra) {
    return text.isBlank() ? extra : text + System.lineSeparator() + extra;
  }
}