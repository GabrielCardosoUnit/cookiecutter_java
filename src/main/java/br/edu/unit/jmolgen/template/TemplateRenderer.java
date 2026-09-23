package br.edu.unit.jmolgen.template;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Substitui variaveis {@code ${nome}} em textos de modelo (equivalente ao
 * {@code {{ cookiecutter.nome }}} do Jinja).
 *
 * <p>A substituicao e feita em uma unica passagem: um valor que contenha {@code ${...}} e
 * copiado literalmente e nunca e reinterpretado. Qualquer variavel sem valor faz a
 * renderizacao falhar, para que nenhum arquivo incompleto seja gravado.
 */
public class TemplateRenderer {

  private static final Pattern VARIABLE = Pattern.compile("\\$\\{([^}]*)}");

  /**
   * @param template texto com variaveis
   * @param values   valores disponiveis, ja escapados para o tipo de arquivo de destino
   * @return texto com todas as variaveis substituidas
   * @throws TemplateException se alguma variavel nao tiver valor
   */
  public String render(String template, Map<String, String> values) throws TemplateException {
    Matcher matcher = VARIABLE.matcher(template);
    StringBuilder result = new StringBuilder(template.length());
    Set<String> unresolved = new TreeSet<>();

    while (matcher.find()) {
      String key = matcher.group(1);
      String value = values.get(key);
      if (value == null) {
        unresolved.add(key);
        value = matcher.group();
      }
      matcher.appendReplacement(result, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(result);

    if (!unresolved.isEmpty()) {
      throw new TemplateException("Variaveis de modelo nao resolvidas: " + unresolved);
    }
    return result.toString();
  }
}
