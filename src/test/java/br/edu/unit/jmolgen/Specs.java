package br.edu.unit.jmolgen;

import br.edu.unit.jmolgen.model.LicenseType;
import br.edu.unit.jmolgen.model.ProjectSpec;

/** Specs prontos para os testes. */
public final class Specs {

  private Specs() {
  }

  public static ProjectSpec valid() {
    return new ProjectSpec("Analise Molecular", "analise-molecular", "br.edu.unit.quimica",
        "Maria Silva", "Ferramentas de geometria molecular", "21", LicenseType.MIT,
        "MolecularGeometry");
  }

  public static ProjectSpec withArtifactId(String artifactId) {
    ProjectSpec s = valid();
    return new ProjectSpec(s.projectName(), artifactId, s.basePackage(), s.author(),
        s.description(), s.javaVersion(), s.license(), s.firstClassName());
  }

  public static ProjectSpec withBasePackage(String basePackage) {
    ProjectSpec s = valid();
    return new ProjectSpec(s.projectName(), s.artifactId(), basePackage, s.author(),
        s.description(), s.javaVersion(), s.license(), s.firstClassName());
  }

  public static ProjectSpec withFirstClassName(String firstClassName) {
    ProjectSpec s = valid();
    return new ProjectSpec(s.projectName(), s.artifactId(), s.basePackage(), s.author(),
        s.description(), s.javaVersion(), s.license(), firstClassName);
  }

  public static ProjectSpec withAuthorAndDescription(String author, String description) {
    ProjectSpec s = valid();
    return new ProjectSpec(s.projectName(), s.artifactId(), s.basePackage(), author,
        description, s.javaVersion(), s.license(), s.firstClassName());
  }

  public static ProjectSpec withJavaVersion(String javaVersion) {
    ProjectSpec s = valid();
    return new ProjectSpec(s.projectName(), s.artifactId(), s.basePackage(), s.author(),
        s.description(), javaVersion, s.license(), s.firstClassName());
  }

  public static ProjectSpec withLicense(LicenseType license) {
    ProjectSpec s = valid();
    return new ProjectSpec(s.projectName(), s.artifactId(), s.basePackage(), s.author(),
        s.description(), s.javaVersion(), license, s.firstClassName());
  }
}
