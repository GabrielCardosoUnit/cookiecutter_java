package br.edu.unit.jmolgen.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unit.jmolgen.Specs;
import br.edu.unit.jmolgen.model.ProjectSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectSpecJsonCodecTest {

  private final ProjectSpecJsonCodec codec = new ProjectSpecJsonCodec();

  @TempDir
  Path temp;

  @Test
  void writesAndReadsPortableSpecWithSpdxLicense() throws Exception {
    ProjectSpec expected = Specs.valid();
    Path file = temp.resolve("project-spec.json");

    codec.write(expected, file);

    String json = Files.readString(file);
    assertTrue(json.contains("\"license\" : \"MIT\""));
    assertEquals(expected, codec.read(file));
  }

  @Test
  void rejectsUnknownLicense() throws Exception {
    Path file = temp.resolve("project-spec.json");
    Files.writeString(file, """
        {"projectName":"P","artifactId":"p","basePackage":"br.edu.p",
        "author":"A","description":"D","javaVersion":"21",
        "license":"Unknown","firstClassName":"Starter"}
        """);

    assertThrows(IOException.class, () -> codec.read(file));
  }

  @Test
  void rejectsMissingFields() throws Exception {
    Path file = temp.resolve("project-spec.json");
    Files.writeString(file, "{\"projectName\":\"P\"}");

    assertThrows(IOException.class, () -> codec.read(file));
  }
}