package br.edu.unit.jmolgen;

import br.edu.unit.jmolgen.generation.GenerationManifest;
import br.edu.unit.jmolgen.generation.GenerationPlan;
import br.edu.unit.jmolgen.generation.ProjectGenerator;
import br.edu.unit.jmolgen.generation.TargetNotEmptyException;
import br.edu.unit.jmolgen.json.ProjectSpecJsonCodec;
import br.edu.unit.jmolgen.model.ProjectSpec;
import br.edu.unit.jmolgen.process.MavenVerificationResult;
import br.edu.unit.jmolgen.process.MavenVerifier;
import br.edu.unit.jmolgen.template.TemplateException;
import br.edu.unit.jmolgen.validation.InvalidProjectSpecException;
import br.edu.unit.jmolgen.validation.ValidationError;
import br.edu.unit.jmolgen.ui.MainFrame;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;

/** Ponto de entrada para a interface Swing e os comandos de linha. */
public final class App {

  private App() {
  }

  public static void main(String[] args) {
    if (args.length == 0 || args.length == 1 && args[0].equals("gui")) {
      if (java.awt.GraphicsEnvironment.isHeadless()) {
        System.err.println("A interface grafica nao esta disponivel neste ambiente.");
        System.exit(2);
      }
      SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
      return;
    }
    int exitCode = run(args, System.out, System.err);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  /** Executa um comando sem encerrar a JVM, tambem usado pelos testes. */
  public static int run(String[] args, PrintStream out, PrintStream err) {
    ProjectGenerator generator = new ProjectGenerator();
    ProjectSpecJsonCodec codec = new ProjectSpecJsonCodec();
    try {
      if (args.length == 2 && args[0].equals("preview")) {
        ProjectSpec spec = codec.read(Path.of(args[1]));
        GenerationPlan plan = generator.plan(spec);
        out.print(plan.treeView());
        return 0;
      }
      if (args.length >= 3 && args[0].equals("generate")) {
        Set<String> options = new HashSet<>(Arrays.asList(args).subList(3, args.length));
        if (options.size() != args.length - 3
            || !Set.of("--overwrite", "--verify").containsAll(options)) {
          return usage(err);
        }
        ProjectSpec spec = codec.read(Path.of(args[1]));
        Path baseDirectory = Path.of(args[2]);
        GenerationManifest manifest = generator.generate(spec, baseDirectory,
            options.contains("--overwrite"));
        out.println("Projeto gerado em " + baseDirectory.resolve(spec.artifactId()));
        out.println("Manifesto: " + (manifest.successful() ? "sucesso" : "incompleto"));
        if (!manifest.successful()) {
          manifest.errors().forEach(err::println);
          return 1;
        }
        if (options.contains("--verify")) {
          MavenVerificationResult verification = new MavenVerifier()
              .verify(baseDirectory.resolve(spec.artifactId()));
          printVerification(verification, out, err);
          return verification.successful() ? 0 : 1;
        }
        return 0;
      }
      if (args.length == 2 && args[0].equals("verify")) {
        MavenVerificationResult result = new MavenVerifier().verify(Path.of(args[1]));
        printVerification(result, out, err);
        return result.successful() ? 0 : 1;
      }
      return usage(err);
    } catch (InvalidProjectSpecException e) {
      for (ValidationError validationError : e.getErrors()) {
        err.println(validationError);
      }
      return 2;
    } catch (TargetNotEmptyException e) {
      err.println(e.getMessage() + "; use --overwrite depois de confirmar o destino.");
      return 2;
    } catch (TemplateException | java.io.IOException e) {
      err.println(e.getMessage());
      return 2;
    }
  }

  private static void printVerification(MavenVerificationResult result, PrintStream out,
      PrintStream err) {
    out.println(result.successful() ? "BUILD SUCCESS" : "BUILD FAILED");
    if (!result.stdout().isBlank()) {
      out.print(result.stdout());
    }
    if (!result.stderr().isBlank()) {
      err.print(result.stderr());
    }
    if (result.timedOut()) {
      err.println("A verificacao excedeu o tempo limite.");
    }
  }

  private static int usage(PrintStream err) {
    err.println("Uso:");
    err.println("  java -jar jmol-project-generator.jar [gui]");
    err.println("  java -jar jmol-project-generator.jar preview <project-spec.json>");
    err.println("  java -jar jmol-project-generator.jar generate <project-spec.json> <pasta-base>"
        + " [--overwrite] [--verify]");
    err.println("  java -jar jmol-project-generator.jar verify <pasta-do-projeto>");
    return 2;
  }
}