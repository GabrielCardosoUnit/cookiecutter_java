package br.edu.unit.jmolgen.model;

/**
 * Licencas que o gerador sabe aplicar ao projeto criado.
 *
 * <p>O texto de cada licenca fica versionado como recurso em
 * {@code templates/<modelo>/licenses/<spdxId>.txt}.
 */
public enum LicenseType {

  MIT("MIT", "MIT License", "https://opensource.org/licenses/MIT"),
  BSD_3_CLAUSE("BSD-3-Clause", "BSD 3-Clause License", "https://opensource.org/licenses/BSD-3-Clause"),
  APACHE_2_0("Apache-2.0", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0");

  private final String spdxId;
  private final String displayName;
  private final String url;

  LicenseType(String spdxId, String displayName, String url) {
    this.spdxId = spdxId;
    this.displayName = displayName;
    this.url = url;
  }

  /** Identificador SPDX, usado tambem como nome do arquivo de texto da licenca. */
  public String spdxId() {
    return spdxId;
  }

  public String displayName() {
    return displayName;
  }

  public String url() {
    return url;
  }
}
