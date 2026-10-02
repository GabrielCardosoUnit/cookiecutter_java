package br.edu.unit.jmolgen.validation;

import java.util.List;

/** Entrada invalida: o projeto nao pode ser gerado com os parametros informados. */
public class InvalidProjectSpecException extends Exception {

  private final List<ValidationError> errors;

  public InvalidProjectSpecException(List<ValidationError> errors) {
    super("Parametros invalidos: " + errors);
    this.errors = List.copyOf(errors);
  }

  public List<ValidationError> getErrors() {
    return errors;
  }
}
