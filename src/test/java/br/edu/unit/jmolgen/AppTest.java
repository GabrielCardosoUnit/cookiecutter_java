package br.edu.unit.jmolgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unit.jmolgen.json.ProjectSpecJsonCodec;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

  @TempDir
  Path temp;

  @Test
  void previewsProjectFromJsonSpec() throws Exception {
    Path specFile = temp.resolve("project-spec.json");
    new ProjectSpecJsonCodec().write(Specs.valid(), specFile);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    int exitCode = App.run(new String[] {"preview", specFile.toString()}, output,
        new PrintStream(new ByteArrayOutputStream()));

    assertEquals(0, exitCode);
    assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("MolecularGeometry.java"));
  }

  @Test
  void generatesProjectFromJsonSpec() throws Exception {
    Path specFile = temp.resolve("project-spec.json");
    Path outputDirectory = temp.resolve("output");
    java.nio.file.Files.createDirectory(outputDirectory);
    new ProjectSpecJsonCodec().write(Specs.valid(), specFile);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    int exitCode = App.run(new String[] {"generate", specFile.toString(),
        outputDirectory.toString()}, new PrintStream(bytes, true, StandardCharsets.UTF_8),
        new PrintStream(new ByteArrayOutputStream()));

    assertEquals(0, exitCode);
    assertTrue(java.nio.file.Files.exists(outputDirectory.resolve(
        "analise-molecular/jmolgen-manifest.json")));
    assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Projeto gerado"));
  }
}