package br.edu.unit.jmolgen.validation;

import br.edu.unit.jmolgen.model.ProjectSpec;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;

/**
 * Regras de validade dos parametros e do destino (equivalente ao hook {@code pre_gen_project}).
 *
 * <p>artifactId, pacote e nome de classe viram nomes de pastas e arquivos, entao sao validados
 * separadamente e com regras mais estritas que as do Java, para que nenhum texto do usuario
 * vire um caminho sem passar por aqui.
 */
public class ProjectSpecValidator {

  /** Versoes LTS do Java aceitas para o projeto gerado. */
  public static final List<String> SUPPORTED_JAVA_VERSIONS = List.of("17", "21", "25");

  private static final int MAX_NAME_LENGTH = 100;
  private static final int MAX_DESCRIPTION_LENGTH = 300;
  private static final int MAX_ARTIFACT_ID_LENGTH = 64;

  /** Minusculas, digitos e hifens simples entre partes: "meu-projeto-2". */
  private static final Pattern ARTIFACT_ID = Pattern.compile("[a-z][a-z0-9]*(-[a-z0-9]+)*");

  /** Cada segmento do pacote, por convencao em minusculas. */
  private static final Pattern PACKAGE_SEGMENT = Pattern.compile("[a-z_][a-z0-9_]*");

  /** Nome de classe em UpperCamelCase. */
  private static final Pattern CLASS_NAME = Pattern.compile("[A-Z][A-Za-z0-9_]*");

  /** Nomes que o Windows nao aceita como arquivo ou pasta, com ou sem extensao. */
  private static final Pattern WINDOWS_RESERVED =
      Pattern.compile("(?i)(con|prn|aux|nul|com[0-9]|lpt[0-9])");

  /** Nomes que colidiriam com tipos usados pelo codigo gerado. */
  private static final List<String> RESERVED_CLASS_NAMES = List.of("Test", "Assertions");

  /**
   * Valida todos os campos e devolve a lista completa de erros (vazia se o spec for valido).
   */
  public List<ValidationError> validate(ProjectSpec spec) {
    List<ValidationError> errors = new ArrayList<>();
    if (spec == null) {
      errors.add(new ValidationError("spec", "parametros ausentes"));
      return errors;
    }

    validateText(errors, "projectName", spec.projectName(), MAX_NAME_LENGTH);
    validateText(errors, "author", spec.author(), MAX_NAME_LENGTH);
    validateText(errors, "description", spec.description(), MAX_DESCRIPTION_LENGTH);
    validateArtifactId(errors, spec.artifactId());
    validateBasePackage(errors, spec.basePackage());
    validateClassName(errors, spec.firstClassName());

    if (!SUPPORTED_JAVA_VERSIONS.contains(spec.javaVersion())) {
      errors.add(new ValidationError("javaVersion",
          "versao do Java deve ser uma de " + SUPPORTED_JAVA_VERSIONS));
    }
    if (spec.license() == null) {
      errors.add(new ValidationError("license", "escolha uma licenca"));
    }
    return errors;
  }

  /** Igual a {@link #validate}, mas lanca excecao se houver qualquer erro. */
  public void validateOrThrow(ProjectSpec spec) throws InvalidProjectSpecException {
    List<ValidationError> errors = validate(spec);
    if (!errors.isEmpty()) {
      throw new InvalidProjectSpecException(errors);
    }
  }

  /**
   * Resolve a pasta do projeto dentro da pasta base e garante que ela nao escapa dela.
   *
   * @return caminho absoluto e normalizado da pasta do projeto
   */
  public Path resolveTarget(Path baseDir, String artifactId) throws InvalidProjectSpecException {
    if (baseDir == null) {
      throw destinationError("escolha a pasta base");
    }
    Path base = baseDir.toAbsolutePath().normalize();
    if (!Files.isDirectory(base)) {
      throw destinationError("a pasta base nao existe ou nao e um diretorio: " + base);
    }

    Path target;
    try {
      target = base.resolve(artifactId).normalize();
    } catch (InvalidPathException | NullPointerException e) {
      throw destinationError("artifactId nao forma um nome de pasta valido");
    }
    if (target.equals(base) || !target.startsWith(base)) {
      throw destinationError("destino fora da pasta escolhida");
    }
    if (Files.exists(target) && !Files.isDirectory(target)) {
      throw destinationError("ja existe um arquivo com o nome " + artifactId);
    }
    return target;
  }

  private static InvalidProjectSpecException destinationError(String message) {
    return new InvalidProjectSpecException(List.of(new ValidationError("destination", message)));
  }

  private static void validateText(List<ValidationError> errors, String field, String value,
      int maxLength) {
    if (value == null || value.isBlank()) {
      errors.add(new ValidationError(field, "campo obrigatorio"));
    } else if (value.length() > maxLength) {
      errors.add(new ValidationError(field, "maximo de " + maxLength + " caracteres"));
    } else if (value.chars().anyMatch(Character::isISOControl)) {
      errors.add(new ValidationError(field, "nao pode conter quebras de linha ou tabulacoes"));
    }
  }

  private static void validateArtifactId(List<ValidationError> errors, String artifactId) {
    if (artifactId == null || artifactId.isEmpty()) {
      errors.add(new ValidationError("artifactId", "campo obrigatorio"));
    } else if (artifactId.length() > MAX_ARTIFACT_ID_LENGTH) {
      errors.add(new ValidationError("artifactId",
          "maximo de " + MAX_ARTIFACT_ID_LENGTH + " caracteres"));
    } else if (!ARTIFACT_ID.matcher(artifactId).matches()) {
      errors.add(new ValidationError("artifactId",
          "use letras minusculas, digitos e hifens (ex.: analise-molecular)"));
    } else if (WINDOWS_RESERVED.matcher(artifactId).matches()) {
      errors.add(new ValidationError("artifactId", "nome reservado pelo sistema operacional"));
    }
  }

  private static void validateBasePackage(List<ValidationError> errors, String basePackage) {
    if (basePackage == null || basePackage.isEmpty()) {
      errors.add(new ValidationError("basePackage", "campo obrigatorio"));
      return;
    }
    if (basePackage.length() > MAX_NAME_LENGTH) {
      errors.add(new ValidationError("basePackage",
          "maximo de " + MAX_NAME_LENGTH + " caracteres"));
      return;
    }
    // limite -1 preserva segmentos vazios, para detectar "br..unit" e "br.unit."
    for (String segment : basePackage.split("\\.", -1)) {
      if (!PACKAGE_SEGMENT.matcher(segment).matches()) {
        errors.add(new ValidationError("basePackage", "segmento invalido '" + segment
            + "': use minusculas, digitos e _ separados por ponto (ex.: br.edu.unit.quimica)"));
        return;
      }
      if (SourceVersion.isKeyword(segment)) {
        errors.add(new ValidationError("basePackage",
            "'" + segment + "' e palavra reservada do Java"));
        return;
      }
      if (WINDOWS_RESERVED.matcher(segment).matches()) {
        errors.add(new ValidationError("basePackage",
            "'" + segment + "' e nome reservado pelo sistema operacional"));
        return;
      }
    }
    String first = basePackage.split("\\.")[0];
    if (first.equals("java") || first.equals("javax")) {
      errors.add(new ValidationError("basePackage", "pacotes java e javax sao do proprio JDK"));
    }
  }

  private static void validateClassName(List<ValidationError> errors, String className) {
    if (className == null || className.isEmpty()) {
      errors.add(new ValidationError("firstClassName", "campo obrigatorio"));
    } else if (className.length() > MAX_NAME_LENGTH) {
      errors.add(new ValidationError("firstClassName",
          "maximo de " + MAX_NAME_LENGTH + " caracteres"));
    } else if (!CLASS_NAME.matcher(className).matches()) {
      errors.add(new ValidationError("firstClassName",
          "comece com maiuscula e use apenas letras, digitos e _ (ex.: MolecularGeometry)"));
    } else if (RESERVED_CLASS_NAMES.contains(className) || isJavaLangType(className)) {
      errors.add(new ValidationError("firstClassName",
          "'" + className + "' colide com uma classe do Java ou do JUnit"));
    } else if (WINDOWS_RESERVED.matcher(className).matches()) {
      errors.add(new ValidationError("firstClassName",
          "nome reservado pelo sistema operacional"));
    }
  }

  /** Uma classe chamada, por exemplo, Math esconderia java.lang.Math no codigo gerado. */
  private static boolean isJavaLangType(String simpleName) {
    try {
      Class.forName("java.lang." + simpleName, false, ClassLoader.getPlatformClassLoader());
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
