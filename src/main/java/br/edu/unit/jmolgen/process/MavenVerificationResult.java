package br.edu.unit.jmolgen.process;

/** Resultado da execucao de {@code mvn test} no projeto gerado. */
public record MavenVerificationResult(
    boolean started,
    boolean successful,
    boolean timedOut,
    Integer exitCode,
    String stdout,
    String stderr) {
}