package br.edu.unit.jmolgen.template;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Descricao de um modelo, lida de {@code template.properties}.
 *
 * <p>Formato do arquivo:
 * <pre>
 * name=maven-basic
 * version=1.0.0
 * file.&lt;caminho de destino&gt;=&lt;arquivo de origem no modelo&gt;
 * </pre>
 * Destino e origem podem conter variaveis, por exemplo
 * {@code file.src/main/java/${packagePath}/${firstClassName}.java=FirstClass.java.tpl}.
 *
 * @param name    nome do modelo
 * @param version versao do modelo, registrada no manifesto
 * @param files   destino (com variaveis) para origem (com variaveis), ordenado pelo destino
 */
public record TemplateDescriptor(String name, String version, Map<String, String> files) {

  public static final String DESCRIPTOR_FILE = "template.properties";
  private static final String FILE_PREFIX = "file.";

  public TemplateDescriptor {
    files = Map.copyOf(files);
  }

  /** Le e valida o descritor do modelo. */
  public static TemplateDescriptor load(TemplateSource source) throws TemplateException {
    Properties properties = new Properties();
    try {
      properties.load(new StringReader(source.read(DESCRIPTOR_FILE)));
    } catch (IOException e) {
      throw new TemplateException("Descritor do modelo invalido", e);
    }

    String name = properties.getProperty("name");
    String version = properties.getProperty("version");
    if (name == null || version == null) {
      throw new TemplateException(DESCRIPTOR_FILE + " precisa declarar name e version");
    }

    Map<String, String> files = new TreeMap<>();
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith(FILE_PREFIX)) {
        files.put(key.substring(FILE_PREFIX.length()), properties.getProperty(key));
      }
    }
    if (files.isEmpty()) {
      throw new TemplateException(DESCRIPTOR_FILE + " nao declara nenhum arquivo");
    }
    return new TemplateDescriptor(name, version, files);
  }
}
