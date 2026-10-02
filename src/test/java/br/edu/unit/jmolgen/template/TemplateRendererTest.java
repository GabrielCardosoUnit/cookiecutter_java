package br.edu.unit.jmolgen.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateRendererTest {

  private final TemplateRenderer renderer = new TemplateRenderer();

  @Test
  void replacesKnownVariables() throws Exception {
    String result = renderer.render("package ${pkg};\nclass ${name} {}",
        Map.of("pkg", "br.unit", "name", "Molecule"));
    assertEquals("package br.unit;\nclass Molecule {}", result);
  }

  @Test
  void keepsTextWithoutVariables() throws Exception {
    assertEquals("sem variaveis $ { }", renderer.render("sem variaveis $ { }", Map.of()));
  }

  @Test
  void failsOnUnknownVariablesListingAllOfThem() {
    TemplateException e = assertThrows(TemplateException.class,
        () -> renderer.render("${a} ${b} ${c} ${a}", Map.of("b", "ok")));
    assertTrue(e.getMessage().contains("[a, c]"), e.getMessage());
  }

  @Test
  void failsOnEmptyVariable() {
    assertThrows(TemplateException.class, () -> renderer.render("${}", Map.of()));
  }

  @Test
  void doesNotReinterpretValues() throws Exception {
    // Um valor com ${...} ou com caracteres especiais de regex entra literalmente.
    String result = renderer.render("${x}|${y}", Map.of("x", "${y}", "y", "$1\\"));
    assertEquals("${y}|$1\\", result);
  }

  @Test
  void escapesXmlSpecialCharacters() {
    assertEquals("Silva &amp; Souza &lt;lab&gt; &quot;q&quot; &apos;a&apos;",
        Escaping.XML.apply("Silva & Souza <lab> \"q\" 'a'"));
    assertEquals("a & b", Escaping.NONE.apply("a & b"));
    assertEquals(Escaping.XML, Escaping.forPath("pom.xml"));
    assertEquals(Escaping.NONE, Escaping.forPath("README.md"));
  }
}
