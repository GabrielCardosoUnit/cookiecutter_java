package br.edu.unit.jmolgen.generation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unit.jmolgen.Specs;
import br.edu.unit.jmolgen.model.LicenseType;
import br.edu.unit.jmolgen.model.ProjectSpec;
import br.edu.unit.jmolgen.template.TemplateException;
import br.edu.unit.jmolgen.template.TemplateRenderer;
import br.edu.unit.jmolgen.template.TemplateSource;
import br.edu.unit.jmolgen.validation.InvalidProjectSpecException;
import br.edu.unit.jmolgen.validation.ProjectSpecValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProjectGeneratorTest {

  private static final List<String> EXPECTED_FILES = List.of(
      ".github/workflows/build.yml",
      ".gitignore",
      "LICENSE",
      "README.md",
      "docs/index.md",
      "pom.xml",
      "src/main/java/br/edu/unit/quimica/MolecularGeometry.java",
      "src/main/resources/.gitkeep",
      "src/test/java/br/edu/unit/quimica/MolecularGeometryTest.java");

  private final ProjectGenerator generator = new ProjectGenerator();

  @TempDir
  Path base;

  @Test
  void planListsFilesInDeterministicOrder() throws Exception {
    GenerationPlan plan = generator.plan(Specs.valid());
    assertEquals(EXPECTED_FILES, plan.paths());
    assertEquals("maven-basic", plan.templateName());
  }

  @Test
  void treeViewShowsProjectStructure() throws Exception {
    String tree = generator.plan(Specs.valid()).treeView();
    assertTrue(tree.startsWith("analise-molecular/\n|-- .github/\n|   `-- workflows/\n"), tree);
    assertTrue(tree.contains("|-- " + GenerationManifest.FILE_NAME + "\n"), tree);
    assertTrue(tree.contains("`-- MolecularGeometry.java\n"), tree);
    assertTrue(tree.contains("`-- MolecularGeometryTest.java\n"), tree);
  }

  @Test
  void generatesExpectedTree() throws Exception {
    GenerationManifest manifest = generator.generate(Specs.valid(), base, false);

    assertTrue(manifest.successful(), manifest.errors()::toString);
    List<String> expected = new ArrayList<>(EXPECTED_FILES);
    expected.add(GenerationManifest.FILE_NAME);
    expected.sort(null);
    assertEquals(expected, listFiles(base.resolve("analise-molecular")));
    assertEquals(EXPECTED_FILES, manifest.files().stream().map(GeneratedFile::path).toList());
  }

  @Test
  void pomDeclaresCoordinatesJavaVersionAndJunit() throws Exception {
    generator.generate(Specs.valid(), base, false);
    String pom = read("pom.xml");

    assertTrue(pom.contains("<groupId>br.edu.unit.quimica</groupId>"));
    assertTrue(pom.contains("<artifactId>analise-molecular</artifactId>"));
    assertTrue(pom.contains("<name>Analise Molecular</name>"));
    assertTrue(pom.contains("<maven.compiler.release>21</maven.compiler.release>"));
    assertTrue(pom.contains("<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>"));
    assertTrue(pom.contains("<artifactId>junit-jupiter</artifactId>"));
    assertTrue(pom.contains("<artifactId>maven-surefire-plugin</artifactId>"));
    assertTrue(pom.contains("<name>MIT License</name>"));
  }

  @Test
  void javaSourcesUseInformedPackageAndClass() throws Exception {
    generator.generate(Specs.valid(), base, false);
    String main = read("src/main/java/br/edu/unit/quimica/MolecularGeometry.java");
    String test = read("src/test/java/br/edu/unit/quimica/MolecularGeometryTest.java");

    assertTrue(main.startsWith("package br.edu.unit.quimica;\n"));
    assertTrue(main.contains("public final class MolecularGeometry {"));
    assertTrue(test.startsWith("package br.edu.unit.quimica;\n"));
    assertTrue(test.contains("class MolecularGeometryTest {"));
    assertTrue(test.contains("MolecularGeometry.distance("));
  }

  @Test
  void workflowRunsMavenTestsWithoutSecretsOrPublishing() throws Exception {
    generator.generate(Specs.withJavaVersion("17"), base, false);
    String workflow = read(".github/workflows/build.yml");

    assertTrue(workflow.contains("runs-on: ubuntu-latest"));
    assertTrue(workflow.contains("uses: actions/setup-java@v4"));
    assertTrue(workflow.contains("java-version: '17'"));
    assertTrue(workflow.contains("run: mvn --batch-mode test"));
    for (String forbidden : List.of("secrets", "token", "deploy", "publish", "password")) {
      assertFalse(workflow.toLowerCase().contains(forbidden), "workflow contem " + forbidden);
    }
  }

  @Test
  void readmeDescribesRequirementsAndBuild() throws Exception {
    generator.generate(Specs.valid(), base, false);
    String readme = read("README.md");

    assertTrue(readme.startsWith("# Analise Molecular\n"));
    assertTrue(readme.contains("JDK 21 ou superior"));
    assertTrue(readme.contains("mvn test"));
  }

  @Test
  void noVariableIsLeftUnresolved() throws Exception {
    generator.generate(Specs.valid(), base, false);
    Path project = base.resolve("analise-molecular");
    for (String file : listFiles(project)) {
      assertFalse(Files.readString(project.resolve(file)).contains("${"), file);
    }
  }

  @ParameterizedTest
  @EnumSource(LicenseType.class)
  void generatesChosenLicense(LicenseType license) throws Exception {
    generator.generate(Specs.withLicense(license), base, false);
    String text = read("LICENSE");
    String pom = read("pom.xml");

    switch (license) {
      case MIT -> assertTrue(text.startsWith("MIT License\n\nCopyright (c) Maria Silva\n"));
      case BSD_3_CLAUSE ->
          assertTrue(text.startsWith("BSD 3-Clause License\n\nCopyright (c) Maria Silva\n"));
      case APACHE_2_0 -> assertTrue(text.contains("Apache License\n"));
    }
    assertTrue(pom.contains("<url>" + license.url() + "</url>"));
  }

  @Test
  void escapesSpecialCharactersOnlyInXml() throws Exception {
    ProjectSpec spec = Specs.withAuthorAndDescription("Silva & Souza <lab>", "Usa \"aspas\"");
    generator.generate(spec, base, false);

    String pom = read("pom.xml");
    assertTrue(pom.contains("<name>Silva &amp; Souza &lt;lab&gt;</name>"));
    assertTrue(pom.contains("<description>Usa &quot;aspas&quot;</description>"));
    // o pom continua sendo XML valido
    DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(base.resolve("analise-molecular/pom.xml").toFile());

    assertTrue(read("LICENSE").contains("Copyright (c) Silva & Souza <lab>"));
    assertTrue(read("README.md").contains("Usa \"aspas\""));
  }

  @Test
  void refusesNonEmptyTargetWithoutConfirmation() throws Exception {
    Path target = Files.createDirectory(base.resolve("analise-molecular"));
    Files.writeString(target.resolve("dados.csv"), "x,y\n1,2\n");

    assertThrows(TargetNotEmptyException.class,
        () -> generator.generate(Specs.valid(), base, false));
    assertEquals(List.of("dados.csv"), listFiles(target));
  }

  @Test
  void writesIntoNonEmptyTargetOnlyWhenConfirmed() throws Exception {
    Path target = Files.createDirectory(base.resolve("analise-molecular"));
    Files.writeString(target.resolve("dados.csv"), "x,y\n1,2\n");

    GenerationManifest manifest = generator.generate(Specs.valid(), base, true);

    assertTrue(manifest.successful());
    assertTrue(Files.exists(target.resolve("pom.xml")));
    assertEquals("x,y\n1,2\n", Files.readString(target.resolve("dados.csv")));
  }

  @Test
  void acceptsExistingEmptyTarget() throws Exception {
    Files.createDirectory(base.resolve("analise-molecular"));
    assertTrue(generator.generate(Specs.valid(), base, false).successful());
  }

  @Test
  void invalidSpecWritesNothing() throws IOException {
    assertThrows(InvalidProjectSpecException.class,
        () -> generator.generate(Specs.withArtifactId("../fora"), base, false));
    assertEquals(List.of(), listFiles(base));
  }

  @Test
  void sameSpecProducesIdenticalOutput(@TempDir Path otherBase) throws Exception {
    generator.generate(Specs.valid(), base, false);
    generator.generate(Specs.valid(), otherBase, false);

    Map<String, byte[]> first = snapshot(base.resolve("analise-molecular"));
    Map<String, byte[]> second = snapshot(otherBase.resolve("analise-molecular"));
    assertEquals(first.keySet(), second.keySet());
    for (String file : first.keySet()) {
      assertArrayEquals(first.get(file), second.get(file), file);
    }
  }

  @Test
  void manifestRecordsParametersAndHashesWithoutPersonalPaths() throws Exception {
    generator.generate(Specs.valid(), base, false);
    String json = read(GenerationManifest.FILE_NAME);
    JsonNode manifest = new ObjectMapper().readTree(json);

    assertEquals("maven-basic", manifest.get("templateName").asText());
    assertEquals("1.0.0", manifest.get("templateVersion").asText());
    assertEquals("Maria Silva", manifest.get("parameters").get("author").asText());
    assertEquals("MIT", manifest.get("parameters").get("license").asText());
    assertEquals(EXPECTED_FILES.size(), manifest.get("files").size());
    assertEquals(64, manifest.get("files").get(0).get("sha256").asText().length());
    assertEquals(0, manifest.get("errors").size());

    assertFalse(json.contains(base.toAbsolutePath().toString()));
    assertFalse(json.contains(base.toAbsolutePath().toString().replace('\\', '/')));
    assertFalse(json.contains("\r"));
  }

  @Test
  void unresolvedVariableFailsBeforeWritingAnything() {
    TemplateSource source = inMemory(Map.of(
        "template.properties", "name=t\nversion=1\nfile.ok.txt=ok.tpl\nfile.bad.txt=bad.tpl\n",
        "ok.tpl", "ok ${artifactId}",
        "bad.tpl", "falta ${naoExiste}"));
    ProjectGenerator custom = new ProjectGenerator(source, new TemplateRenderer(),
        new ProjectSpecValidator());

    TemplateException e = assertThrows(TemplateException.class,
        () -> custom.generate(Specs.valid(), base, false));
    assertTrue(e.getMessage().contains("naoExiste"));
    assertFalse(Files.exists(base.resolve("analise-molecular")));
  }

  @Test
  void templateCannotWriteOutsideProject() {
    TemplateSource source = inMemory(Map.of(
        "template.properties", "name=t\nversion=1\nfile.../fora.txt=x.tpl\n",
        "x.tpl", "x"));
    ProjectGenerator custom = new ProjectGenerator(source, new TemplateRenderer(),
        new ProjectSpecValidator());

    assertThrows(TemplateException.class, () -> custom.generate(Specs.valid(), base, false));
    assertFalse(Files.exists(base.resolve("fora.txt")));
  }

  private static TemplateSource inMemory(Map<String, String> files) {
    return name -> {
      String content = files.get(name);
      if (content == null) {
        throw new TemplateException("nao encontrado: " + name);
      }
      return content;
    };
  }

  private String read(String relativePath) throws IOException {
    return Files.readString(base.resolve("analise-molecular").resolve(relativePath));
  }

  /** Caminhos relativos de todos os arquivos, com "/" e em ordem alfabetica. */
  private static List<String> listFiles(Path root) throws IOException {
    try (Stream<Path> paths = Files.walk(root)) {
      return paths.filter(Files::isRegularFile)
          .map(p -> root.relativize(p).toString().replace('\\', '/'))
          .sorted()
          .toList();
    }
  }

  private static Map<String, byte[]> snapshot(Path root) throws IOException {
    Map<String, byte[]> contents = new TreeMap<>();
    for (String file : listFiles(root)) {
      contents.put(file, Files.readAllBytes(root.resolve(file)));
    }
    return contents;
  }
}
