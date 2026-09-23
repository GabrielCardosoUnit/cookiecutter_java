package br.edu.unit.jmolgen.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unit.jmolgen.Specs;
import br.edu.unit.jmolgen.model.ProjectSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProjectSpecValidatorTest {

  private final ProjectSpecValidator validator = new ProjectSpecValidator();

  @Test
  void acceptsValidSpec() {
    assertEquals(List.of(), validator.validate(Specs.valid()));
  }

  @Test
  void stripsSurroundingSpaces() {
    ProjectSpec spec = Specs.withArtifactId("  analise-molecular  ");
    assertEquals("analise-molecular", spec.artifactId());
    assertEquals(List.of(), validator.validate(spec));
  }

  @Test
  void reportsEveryInvalidFieldAtOnce() {
    ProjectSpec spec = new ProjectSpec("", "Bad Id", "br..x", " ", "", "11", null, "lower");
    List<String> fields = validator.validate(spec).stream().map(ValidationError::field).toList();
    assertEquals(List.of("projectName", "author", "description", "artifactId", "basePackage",
        "firstClassName", "javaVersion", "license"), fields);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "Analise", "analise_molecular", "analise molecular", "-analise",
      "analise-", "analise--molecular", "../analise", "a/b", "a\\b", "..", "/abs", "C:analise",
      "con", "lpt1", "1analise"})
  void rejectsInvalidArtifactId(String artifactId) {
    assertInvalid(Specs.withArtifactId(artifactId), "artifactId");
  }

  @ParameterizedTest
  @ValueSource(strings = {"quimica", "analise-molecular-2", "a1"})
  void acceptsValidArtifactId(String artifactId) {
    assertEquals(List.of(), validator.validate(Specs.withArtifactId(artifactId)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "br..unit", "br.unit.", ".br", "Br.unit", "br.edu-unit", "br.1edu",
      "br.class", "br.true", "br/unit", "br.unit..", "java.quimica", "javax", "br.con",
      "br.unit\n.quimica", "br.unit quimica"})
  void rejectsInvalidBasePackage(String basePackage) {
    assertInvalid(Specs.withBasePackage(basePackage), "basePackage");
  }

  @ParameterizedTest
  @ValueSource(strings = {"quimica", "br.edu.unit.quimica", "br.edu.unit_se.q2", "_interno"})
  void acceptsValidBasePackage(String basePackage) {
    assertEquals(List.of(), validator.validate(Specs.withBasePackage(basePackage)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "molecule", "1Molecule", "Molecular-Geometry", "Molecular Geometry",
      "Math", "String", "Object", "Test", "Assertions", "Con", "Molecule$Inner"})
  void rejectsInvalidClassName(String className) {
    assertInvalid(Specs.withFirstClassName(className), "firstClassName");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "8", "11", "21.0", "latest"})
  void rejectsUnsupportedJavaVersion(String javaVersion) {
    assertInvalid(Specs.withJavaVersion(javaVersion), "javaVersion");
  }

  @Test
  void rejectsLineBreakInText() {
    assertInvalid(Specs.withAuthorAndDescription("Maria", "linha 1\nlinha 2"), "description");
    assertInvalid(Specs.withAuthorAndDescription("Maria\tSilva", "ok"), "author");
  }

  @Test
  void rejectsTooLongText() {
    assertInvalid(Specs.withAuthorAndDescription("a".repeat(101), "ok"), "author");
  }

  @Test
  void resolvesTargetInsideBase(@TempDir Path base) throws Exception {
    Path target = validator.resolveTarget(base, "analise-molecular");
    assertEquals(base.toAbsolutePath().normalize().resolve("analise-molecular"), target);
  }

  @ParameterizedTest
  @ValueSource(strings = {"..", "../fora", "sub/../../fora", "."})
  void rejectsTargetOutsideBase(String artifactId, @TempDir Path base) {
    InvalidProjectSpecException e = assertThrows(InvalidProjectSpecException.class,
        () -> validator.resolveTarget(base, artifactId));
    assertEquals("destination", e.getErrors().get(0).field());
  }

  @Test
  void rejectsAbsoluteArtifactIdAsTarget(@TempDir Path base, @TempDir Path other) {
    assertThrows(InvalidProjectSpecException.class,
        () -> validator.resolveTarget(base, other.toAbsolutePath().toString()));
  }

  @Test
  void rejectsMissingBaseDirectory(@TempDir Path base) {
    assertThrows(InvalidProjectSpecException.class,
        () -> validator.resolveTarget(base.resolve("nao-existe"), "analise-molecular"));
  }

  @Test
  void rejectsTargetThatIsAFile(@TempDir Path base) throws IOException {
    Files.writeString(base.resolve("analise-molecular"), "arquivo");
    assertThrows(InvalidProjectSpecException.class,
        () -> validator.resolveTarget(base, "analise-molecular"));
  }

  private void assertInvalid(ProjectSpec spec, String field) {
    List<ValidationError> errors = validator.validate(spec);
    assertTrue(errors.stream().anyMatch(e -> e.field().equals(field)),
        "esperava erro em " + field + ", obteve " + errors);
  }
}
