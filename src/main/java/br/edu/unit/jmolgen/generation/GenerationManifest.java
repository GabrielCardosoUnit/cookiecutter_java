package br.edu.unit.jmolgen.generation;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Registro de uma geracao: modelo, parametros usados, arquivos criados e erros.
 *
 * <p>Nao contem datas nem caminhos absolutos, para que a mesma entrada produza sempre o
 * mesmo manifesto e nenhum diretorio pessoal seja gravado.
 *
 * @param generator       nome do gerador
 * @param templateName    nome do modelo
 * @param templateVersion versao do modelo
 * @param parameters      parametros informados pelo usuario, ordenados pela chave
 * @param files           arquivos gravados, na ordem de gravacao
 * @param errors          erros que interromperam a geracao (vazio em caso de sucesso)
 */
public record GenerationManifest(
    String generator,
    String templateName,
    String templateVersion,
    Map<String, String> parameters,
    List<GeneratedFile> files,
    List<String> errors) {

  /** Nome do arquivo gravado na raiz do projeto gerado. */
  public static final String FILE_NAME = "jmolgen-manifest.json";

  public GenerationManifest {
    parameters = new TreeMap<>(parameters);
    files = List.copyOf(files);
    errors = List.copyOf(errors);
  }

  /** {@code true} se todos os arquivos foram gravados. */
  public boolean successful() {
    return errors.isEmpty();
  }

  /** Copia deste manifesto com um erro a mais. */
  public GenerationManifest withError(String error) {
    List<String> newErrors = new ArrayList<>(errors);
    newErrors.add(error);
    return new GenerationManifest(generator, templateName, templateVersion, parameters, files,
        newErrors);
  }

  /** Grava o manifesto em JSON, sempre com LF, independentemente do sistema operacional. */
  public void writeJson(Path file) throws IOException {
    DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter("  ", "\n"))
        .withArrayIndenter(new DefaultIndenter("  ", "\n"));
    ObjectMapper mapper = new ObjectMapper()
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    String json = mapper.writer(printer).writeValueAsString(this) + "\n";
    Files.writeString(file, json, StandardCharsets.UTF_8);
  }
}
