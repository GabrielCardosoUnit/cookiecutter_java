package br.edu.unit.jmolgen.template;

/**
 * Origem dos arquivos de um modelo. Permite trocar o classpath por outra fonte (por exemplo,
 * uma pasta no disco ou um mapa em memoria nos testes) sem alterar o gerador.
 */
public interface TemplateSource {

  /**
   * Le um arquivo do modelo como texto UTF-8, com quebras de linha normalizadas para LF.
   *
   * @param name caminho relativo dentro do modelo, separado por "/"
   * @throws TemplateException se o arquivo nao existir ou nao puder ser lido
   */
  String read(String name) throws TemplateException;
}
