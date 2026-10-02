package br.edu.unit.jmolgen.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Le os arquivos de um modelo empacotado em {@code src/main/resources/templates/<nome>}. */
public class ClasspathTemplateSource implements TemplateSource {

  private final String root;

  /**
   * @param templateName nome da pasta do modelo, por exemplo {@code maven-basic}
   */
  public ClasspathTemplateSource(String templateName) {
    this.root = "templates/" + templateName + "/";
  }

  @Override
  public String read(String name) throws TemplateException {
    if (name.startsWith("/") || name.contains("..") || name.contains("\\")) {
      throw new TemplateException("Nome de recurso invalido no modelo: " + name);
    }
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(root + name)) {
      if (in == null) {
        throw new TemplateException("Arquivo do modelo nao encontrado: " + root + name);
      }
      // Normaliza CRLF para que a saida nao dependa de como o Git fez o checkout no Windows.
      return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
    } catch (IOException e) {
      throw new TemplateException("Falha ao ler " + root + name, e);
    }
  }
}
