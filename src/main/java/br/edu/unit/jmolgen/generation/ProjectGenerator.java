package br.edu.unit.jmolgen.generation;

import br.edu.unit.jmolgen.model.ProjectSpec;
import br.edu.unit.jmolgen.template.ClasspathTemplateSource;
import br.edu.unit.jmolgen.template.Escaping;
import br.edu.unit.jmolgen.template.TemplateDescriptor;
import br.edu.unit.jmolgen.template.TemplateException;
import br.edu.unit.jmolgen.template.TemplateRenderer;
import br.edu.unit.jmolgen.template.TemplateSource;
import br.edu.unit.jmolgen.validation.InvalidProjectSpecException;
import br.edu.unit.jmolgen.validation.ProjectSpecValidator;
import br.edu.unit.jmolgen.validation.ValidationError;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Gera um projeto a partir de um {@link ProjectSpec} em duas etapas.
 *
 * <ol>
 *   <li>{@link #plan}: valida os parametros e renderiza todos os arquivos em memoria. Nada e
 *       gravado; se alguma variavel ficar sem valor, a geracao falha aqui.</li>
 *   <li>{@link #generate}: verifica o destino, grava os arquivos e o manifesto.</li>
 * </ol>
 */
public class ProjectGenerator {

  public static final String GENERATOR_NAME = "jmol-project-generator";
  public static final String DEFAULT_TEMPLATE = "maven-basic";

  private final TemplateSource source;
  private final TemplateRenderer renderer;
  private final ProjectSpecValidator validator;

  /** Gerador com o modelo padrao empacotado no classpath. */
  public ProjectGenerator() {
    this(new ClasspathTemplateSource(DEFAULT_TEMPLATE), new TemplateRenderer(),
        new ProjectSpecValidator());
  }

  public ProjectGenerator(TemplateSource source, TemplateRenderer renderer,
      ProjectSpecValidator validator) {
    this.source = source;
    this.renderer = renderer;
    this.validator = validator;
  }

  /**
   * Valida o spec e renderiza todos os arquivos em memoria, sem tocar no disco.
   */
  public GenerationPlan plan(ProjectSpec spec) throws InvalidProjectSpecException,
      TemplateException {
    validator.validateOrThrow(spec);
    TemplateDescriptor descriptor = TemplateDescriptor.load(source);
    Map<String, String> values = templateValues(spec, descriptor);

    List<PlannedFile> files = new ArrayList<>();
    for (Map.Entry<String, String> entry : descriptor.files().entrySet()) {
      String path = renderer.render(entry.getKey(), values);
      String sourceName = renderer.render(entry.getValue(), values);
      checkRelativePath(path);
      Map<String, String> escaped = Escaping.forPath(path).applyAll(values);
      files.add(new PlannedFile(path, renderer.render(source.read(sourceName), escaped)));
    }
    files.sort(Comparator.comparing(PlannedFile::path));

    return new GenerationPlan(descriptor.name(), descriptor.version(), spec.artifactId(), files);
  }

  /**
   * Gera o projeto em {@code baseDir/<artifactId>}.
   *
   * @param overwriteConfirmed {@code true} somente se o usuario confirmou explicitamente que
   *                           uma pasta de destino nao vazia pode receber os arquivos
   * @return manifesto da geracao; se {@link GenerationManifest#successful()} for falso, a
   *         gravacao foi interrompida e o resultado esta incompleto
   * @throws InvalidProjectSpecException parametros ou destino invalidos
   * @throws TargetNotEmptyException     destino com conteudo e sem confirmacao
   * @throws TemplateException           modelo inconsistente
   */
  public GenerationManifest generate(ProjectSpec spec, Path baseDir, boolean overwriteConfirmed)
      throws InvalidProjectSpecException, TargetNotEmptyException, TemplateException {
    GenerationPlan plan = plan(spec);
    Path target = validator.resolveTarget(baseDir, spec.artifactId());
    if (!overwriteConfirmed && isNonEmptyDirectory(target)) {
      throw new TargetNotEmptyException(target);
    }

    // Resolve todos os caminhos antes de gravar o primeiro arquivo.
    List<Path> outputs = new ArrayList<>();
    for (PlannedFile file : plan.files()) {
      outputs.add(resolveInside(target, file.path()));
    }

    List<GeneratedFile> written = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    for (int i = 0; i < outputs.size(); i++) {
      PlannedFile file = plan.files().get(i);
      try {
        Files.createDirectories(outputs.get(i).getParent());
        Files.writeString(outputs.get(i), file.content(), StandardCharsets.UTF_8);
        written.add(new GeneratedFile(file.path(), sha256(file.content())));
      } catch (IOException e) {
        // So o caminho relativo: a mensagem da excecao traria o caminho absoluto do usuario.
        errors.add("Falha ao gravar " + file.path() + " (" + e.getClass().getSimpleName() + ")");
        break;
      }
    }

    GenerationManifest manifest = new GenerationManifest(GENERATOR_NAME, plan.templateName(),
        plan.templateVersion(), parameters(spec), written, errors);
    try {
      Files.createDirectories(target);
      manifest.writeJson(target.resolve(GenerationManifest.FILE_NAME));
    } catch (IOException e) {
      manifest = manifest.withError(
          "Falha ao gravar " + GenerationManifest.FILE_NAME + " (" + e.getClass().getSimpleName()
              + ")");
    }
    return manifest;
  }

  /** Parametros informados pelo usuario, como aparecem no manifesto. */
  static Map<String, String> parameters(ProjectSpec spec) {
    Map<String, String> parameters = new TreeMap<>();
    parameters.put("projectName", spec.projectName());
    parameters.put("artifactId", spec.artifactId());
    parameters.put("basePackage", spec.basePackage());
    parameters.put("author", spec.author());
    parameters.put("description", spec.description());
    parameters.put("javaVersion", spec.javaVersion());
    parameters.put("license", spec.license().spdxId());
    parameters.put("firstClassName", spec.firstClassName());
    return parameters;
  }

  /** Conjunto fechado de variaveis disponiveis nos modelos. */
  private static Map<String, String> templateValues(ProjectSpec spec,
      TemplateDescriptor descriptor) {
    Map<String, String> values = parameters(spec);
    values.put("groupId", spec.basePackage());
    values.put("packagePath", spec.basePackage().replace('.', '/'));
    values.put("licenseName", spec.license().displayName());
    values.put("licenseSpdx", spec.license().spdxId());
    values.put("licenseUrl", spec.license().url());
    values.put("templateName", descriptor.name());
    values.put("templateVersion", descriptor.version());
    return values;
  }

  /** Recusa caminhos absolutos, com barra invertida, letra de unidade, "." ou "..". */
  private static void checkRelativePath(String path) throws TemplateException {
    if (path.isEmpty() || path.startsWith("/") || path.contains("\\") || path.contains(":")) {
      throw new TemplateException("Caminho de destino invalido no modelo: " + path);
    }
    for (String segment : path.split("/", -1)) {
      if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
        throw new TemplateException("Caminho de destino invalido no modelo: " + path);
      }
    }
  }

  /** Resolve segmento por segmento e confirma que o resultado continua dentro do destino. */
  private static Path resolveInside(Path target, String relativePath) throws TemplateException {
    Path resolved = target;
    for (String segment : relativePath.split("/")) {
      resolved = resolved.resolve(segment);
    }
    resolved = resolved.normalize();
    if (!resolved.startsWith(target)) {
      throw new TemplateException("Arquivo fora da pasta do projeto: " + relativePath);
    }
    return resolved;
  }

  private static boolean isNonEmptyDirectory(Path dir) throws InvalidProjectSpecException {
    if (!Files.isDirectory(dir)) {
      return false;
    }
    try (Stream<Path> entries = Files.list(dir)) {
      return entries.findAny().isPresent();
    } catch (IOException e) {
      throw new InvalidProjectSpecException(List.of(
          new ValidationError("destination", "nao foi possivel ler a pasta de destino")));
    }
  }

  private static String sha256(String content) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 e obrigatorio em toda JVM", e);
    }
  }
}
