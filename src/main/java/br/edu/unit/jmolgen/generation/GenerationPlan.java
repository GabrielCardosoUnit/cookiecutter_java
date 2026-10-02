package br.edu.unit.jmolgen.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Resultado da renderizacao em memoria: tudo o que sera gravado, em ordem deterministica.
 * Serve tanto para a previa da arvore quanto para a gravacao.
 *
 * @param templateName     nome do modelo usado
 * @param templateVersion  versao do modelo usado
 * @param projectDirectory nome da pasta do projeto (o artifactId)
 * @param files            arquivos ordenados pelo caminho
 */
public record GenerationPlan(
    String templateName,
    String templateVersion,
    String projectDirectory,
    List<PlannedFile> files) {

  public GenerationPlan {
    files = List.copyOf(files);
  }

  /** Caminhos relativos de todos os arquivos, na ordem de gravacao. */
  public List<String> paths() {
    return files.stream().map(PlannedFile::path).toList();
  }

  /**
   * Arvore em texto (ASCII, para funcionar em qualquer console), incluindo o manifesto
   * que sera gravado junto com os arquivos.
   */
  public String treeView() {
    Node root = new Node();
    for (String path : paths()) {
      root.add(path.split("/"));
    }
    root.add(new String[] {GenerationManifest.FILE_NAME});

    StringBuilder out = new StringBuilder(projectDirectory).append("/\n");
    root.print(out, "");
    return out.toString();
  }

  /** No da arvore; TreeMap garante ordem alfabetica estavel. */
  private static final class Node {
    private final Map<String, Node> children = new TreeMap<>();

    void add(String[] segments) {
      Node current = this;
      for (String segment : segments) {
        current = current.children.computeIfAbsent(segment, s -> new Node());
      }
    }

    void print(StringBuilder out, String indent) {
      List<Map.Entry<String, Node>> entries = new ArrayList<>(children.entrySet());
      for (int i = 0; i < entries.size(); i++) {
        boolean last = i == entries.size() - 1;
        Node child = entries.get(i).getValue();
        boolean directory = !child.children.isEmpty();
        out.append(indent).append(last ? "`-- " : "|-- ")
            .append(entries.get(i).getKey()).append(directory ? "/" : "").append('\n');
        child.print(out, indent + (last ? "    " : "|   "));
      }
    }
  }
}
