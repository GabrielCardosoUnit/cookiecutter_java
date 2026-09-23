package br.edu.unit.jmolgen.generation;

/**
 * Arquivo ja renderizado em memoria, ainda nao gravado.
 *
 * @param path    caminho relativo a pasta do projeto, separado por "/"
 * @param content conteudo final, com quebras de linha LF
 */
public record PlannedFile(String path, String content) {
}
