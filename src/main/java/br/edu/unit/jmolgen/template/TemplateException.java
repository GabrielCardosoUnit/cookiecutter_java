package br.edu.unit.jmolgen.template;

/** Falha no modelo: recurso ausente, descritor invalido ou variavel nao resolvida. */
public class TemplateException extends Exception {

  public TemplateException(String message) {
    super(message);
  }

  public TemplateException(String message, Throwable cause) {
    super(message, cause);
  }
}
