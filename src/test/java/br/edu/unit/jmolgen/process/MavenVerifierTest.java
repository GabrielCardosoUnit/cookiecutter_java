package br.edu.unit.jmolgen.process;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MavenVerifierTest {

  @TempDir
  Path temp;

  @Test
  void reportsMissingMavenWithoutThrowing() {
    MavenVerifier verifier = new MavenVerifier(
        List.of(temp.resolve("missing-maven").toString()), Duration.ofSeconds(1));

    MavenVerificationResult result = verifier.verify(temp);

    assertFalse(result.started());
    assertFalse(result.successful());
    assertNull(result.exitCode());
    assertTrue(result.stderr().contains("Nao foi possivel iniciar o Maven"));
  }

  @Test
  void reportsInvalidProjectDirectory() {
    MavenVerifier verifier = new MavenVerifier(List.of("mvn"), Duration.ofSeconds(1));

    MavenVerificationResult result = verifier.verify(temp.resolve("missing"));

    assertFalse(result.started());
    assertTrue(result.stderr().contains("Diretorio do projeto"));
  }

  @Test
  void capturesBothStreamsAndExitCode() throws Exception {
    Path fixture = temp.resolve("FakeMaven.java");
    Files.writeString(fixture, """
        class FakeMaven {
          public static void main(String[] args) {
            System.out.println("stdout-marker");
            System.err.println("stderr-marker");
            System.exit(7);
          }
        }
        """, StandardCharsets.UTF_8);
    String java = Path.of(System.getProperty("java.home"), "bin",
        System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java")
        .toString();
    MavenVerifier verifier = new MavenVerifier(List.of(java, fixture.toString()),
        Duration.ofSeconds(20));

    MavenVerificationResult result = verifier.verify(temp);

    assertTrue(result.started());
    assertFalse(result.successful());
    assertEquals(7, result.exitCode());
    assertTrue(result.stdout().contains("stdout-marker"));
    assertTrue(result.stderr().contains("stderr-marker"));
  }
}