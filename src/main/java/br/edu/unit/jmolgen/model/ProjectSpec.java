package br.edu.unit.jmolgen.model;

/**
 * Parametros imutaveis de um projeto a ser gerado (equivalente ao {@code cookiecutter.json}).
 *
 * <p>O record apenas transporta as respostas do usuario, sem depender da interface grafica.
 * As regras de validade ficam em {@code ProjectSpecValidator}, para que todos os erros
 * possam ser reportados de uma vez em vez de parar no primeiro.
 *
 * @param projectName    nome visivel do projeto (pode conter espacos)
 * @param artifactId     identificador Maven e nome da pasta do projeto
 * @param basePackage    pacote Java base (tambem usado como groupId)
 * @param author         autor, usado no pom e na licenca
 * @param description    descricao curta de uma linha
 * @param javaVersion    versao minima do Java do projeto gerado (ex.: "21")
 * @param license        licenca do projeto gerado
 * @param firstClassName nome da primeira classe cientifica
 */
public record ProjectSpec(
    String projectName,
    String artifactId,
    String basePackage,
    String author,
    String description,
    String javaVersion,
    LicenseType license,
    String firstClassName) {

  /** Remove espacos nas pontas de todos os textos; valores nulos sao mantidos para o validador. */
  public ProjectSpec {
    projectName = strip(projectName);
    artifactId = strip(artifactId);
    basePackage = strip(basePackage);
    author = strip(author);
    description = strip(description);
    javaVersion = strip(javaVersion);
    firstClassName = strip(firstClassName);
  }

  private static String strip(String value) {
    return value == null ? null : value.strip();
  }
}
