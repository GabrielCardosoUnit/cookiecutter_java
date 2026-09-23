# ${projectName}

${description}

Autor: ${author}

## Requisitos

- JDK ${javaVersion} ou superior
- Maven 3.9 ou superior

## Build e testes

```bash
mvn test
```

Para gerar o JAR em `target/`:

```bash
mvn package
```

## Estrutura

```
${artifactId}/
|-- pom.xml                          configuracao do Maven (Java ${javaVersion}, JUnit 5)
|-- src/main/java/                   codigo do projeto (pacote ${basePackage})
|-- src/main/resources/              arquivos de dados e configuracao
|-- src/test/java/                   testes JUnit
|-- docs/                            documentacao
`-- .github/workflows/build.yml      integracao continua (mvn test no Linux)
```

A classe inicial `${firstClassName}` traz um exemplo de calculo (distancia entre posicoes
atomicas) e o teste `${firstClassName}Test` mostra como verifica-lo.

## Licenca

Distribuido sob a ${licenseName}. Veja o arquivo `LICENSE`.

---

Gerado pelo JMol Project Generator (modelo ${templateName} ${templateVersion}).
Os parametros usados estao em `jmolgen-manifest.json`.
