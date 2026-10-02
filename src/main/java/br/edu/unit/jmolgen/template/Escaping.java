package br.edu.unit.jmolgen.template;

import java.util.Map;
import java.util.TreeMap;

/**
 * Escape aplicado aos valores antes de inseri-los em um arquivo, conforme o tipo de destino.
 *
 * <p>Os modelos Java so recebem identificadores ja validados, e Markdown, YAML e texto de
 * licenca nao exigem escape para os campos permitidos (uma linha, sem caracteres de
 * controle). O pom.xml, porem, quebraria com um autor como "Silva &amp; Souza".
 */
public enum Escaping {

  NONE,
  XML;

  /** Escolhe o escape pela extensao do arquivo de destino. */
  public static Escaping forPath(String path) {
    return path.endsWith(".xml") ? XML : NONE;
  }

  public String apply(String value) {
    return switch (this) {
      case NONE -> value;
      case XML -> value
          .replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;")
          .replace("\"", "&quot;")
          .replace("'", "&apos;");
    };
  }

  /** Aplica o escape a todos os valores do mapa. */
  public Map<String, String> applyAll(Map<String, String> values) {
    Map<String, String> escaped = new TreeMap<>();
    values.forEach((key, value) -> escaped.put(key, apply(value)));
    return escaped;
  }
}
