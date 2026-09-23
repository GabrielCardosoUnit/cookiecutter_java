package br.edu.unit.jmolgen.generation;

import java.nio.file.Path;

/** A pasta de destino ja tem conteudo e o usuario nao confirmou a sobrescrita. */
public class TargetNotEmptyException extends Exception {

  private final Path target;

  public TargetNotEmptyException(Path target) {
    super("A pasta de destino nao esta vazia: " + target);
    this.target = target;
  }

  public Path getTarget() {
    return target;
  }
}
