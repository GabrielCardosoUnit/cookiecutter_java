package br.edu.unit.jmolgen.validation;

/**
 * Erro de validacao associado a um campo, para que a interface possa destacar o campo certo.
 *
 * @param field   nome do componente de {@code ProjectSpec} (ou {@code "destination"})
 * @param message mensagem legivel para o usuario
 */
public record ValidationError(String field, String message) {

  @Override
  public String toString() {
    return field + ": " + message;
  }
}
