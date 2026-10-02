package br.edu.unit.jmolgen.json;

import br.edu.unit.jmolgen.model.LicenseType;
import br.edu.unit.jmolgen.model.ProjectSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Leitura e escrita do formato JSON portavel de {@link ProjectSpec}. */
public final class ProjectSpecJsonCodec {

  private final ObjectMapper mapper = new ObjectMapper();

  /** Grava o spec em UTF-8, usando o identificador SPDX da licenca. */
  public void write(ProjectSpec spec, Path file) throws IOException {
    ObjectNode json = mapper.createObjectNode();
    json.put("projectName", spec.projectName());
    json.put("artifactId", spec.artifactId());
    json.put("basePackage", spec.basePackage());
    json.put("author", spec.author());
    json.put("description", spec.description());
    json.put("javaVersion", spec.javaVersion());
    json.put("license", spec.license() == null ? null : spec.license().spdxId());
    json.put("firstClassName", spec.firstClassName());
    String content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json) + "\n";
    Files.writeString(file, content, StandardCharsets.UTF_8);
  }

  /** Lê um spec JSON; campos ausentes ou uma licença desconhecida produzem {@link IOException}. */
  public ProjectSpec read(Path file) throws IOException {
    JsonNode json = mapper.readTree(file.toFile());
    if (json == null || !json.isObject()) {
      throw new IOException("O arquivo JSON deve conter um objeto ProjectSpec");
    }
    try {
      return new ProjectSpec(text(json, "projectName"), text(json, "artifactId"),
          text(json, "basePackage"), text(json, "author"), text(json, "description"),
          text(json, "javaVersion"), license(text(json, "license")),
          text(json, "firstClassName"));
    } catch (IllegalArgumentException e) {
      throw new IOException("ProjectSpec JSON invalido: " + e.getMessage(), e);
    }
  }

  private static String text(JsonNode json, String field) throws IOException {
    JsonNode value = json.get(field);
    if (value == null || !value.isTextual()) {
      throw new IOException("Campo obrigatorio ausente ou invalido: " + field);
    }
    return value.textValue();
  }

  private static LicenseType license(String spdxId) throws JsonProcessingException {
    for (LicenseType value : LicenseType.values()) {
      if (value.spdxId().equals(spdxId)) {
        return value;
      }
    }
    throw new JsonProcessingException("licenca SPDX desconhecida: " + spdxId) { };
  }
}