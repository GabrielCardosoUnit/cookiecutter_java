package br.edu.unit.jmolgen.generation;

/**
 * Arquivo gravado com sucesso.
 *
 * @param path   caminho relativo a pasta do projeto
 * @param sha256 hash SHA-256 do conteudo, em hexadecimal
 */
public record GeneratedFile(String path, String sha256) {
}
