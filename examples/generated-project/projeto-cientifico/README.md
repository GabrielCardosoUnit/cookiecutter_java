# Projeto Cientifico

Projeto de exemplo gerado pelo JMol Project Generator

Autor: Grupo 3 - Universidade Tiradentes

## Requisitos

- JDK 21 ou superior
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
projeto-cientifico/
|-- pom.xml                          configuracao do Maven (Java 21, JUnit 5)
|-- src/main/java/                   codigo do projeto (pacote br.edu.unit.quimica)
|-- src/main/resources/              arquivos de dados e configuracao
|-- src/test/java/                   testes JUnit
|-- docs/                            documentacao
`-- .github/workflows/build.yml      integracao continua (mvn test no Linux)
```

A classe inicial `MolecularGeometry` traz um exemplo de calculo (distancia entre posicoes
atomicas) e o teste `MolecularGeometryTest` mostra como verifica-lo.

## Licenca

Distribuido sob a MIT License. Veja o arquivo `LICENSE`.

---

Gerado pelo JMol Project Generator (modelo maven-basic 1.0.0).
Os parametros usados estao em `jmolgen-manifest.json`.
