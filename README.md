# JMol Project Generator

Gerador de projetos Maven prontos para testes, inspirado no
[Cookiecutter for Computational Molecular Sciences](https://github.com/MolSSI/cookiecutter-cms)
(Naden et al., *J. Chem. Educ.* 2024, 101, 5105-5109, DOI 10.1021/acs.jchemed.4c00793).

Projeto do Grupo 3 (Artigo 6) da disciplina de Projeto de Programacao, Universidade Tiradentes.

A partir de poucas respostas (nome, artifactId, pacote, autor, descricao, licenca, versao do
Java e nome da primeira classe), o gerador cria um projeto Maven que compila e passa nos
testes sem ajustes manuais.

## Requisitos

- JDK 21 ou superior (para rodar o gerador)
- Maven 3.9 ou superior

Confira as versoes instaladas:

```bash
java -version
mvn -version
```

## Como executar

### 1. Obter o codigo

```bash
git clone https://github.com/GabrielCardosoUnit/cookiecutter_java.git
cd cookiecutter_java
```

### 2. Compilar e rodar os testes

Na raiz do repositorio:

```bash
mvn test
```

O Maven baixa as dependencias na primeira execucao, compila o gerador e roda todos os
testes JUnit. O resultado esperado e `BUILD SUCCESS`. Os relatorios de cada classe de teste
ficam em `target/surefire-reports/`.

Para rodar apenas uma classe de teste:

```bash
mvn test -Dtest=ProjectGeneratorTest
```

### 3. Gerar o JAR

```bash
mvn package
```

O arquivo `target/jmol-project-generator-0.1.0-SNAPSHOT.jar` inclui as dependencias e pode
ser executado diretamente.

### Interface grafica

Inicie a interface com:

```bash
java -jar target/jmol-project-generator-0.1.0-SNAPSHOT.jar
```

A janela tem abas para dados, opcoes, previa e resultado. A previa acompanha os campos; a
geracao confirma antes de sobrescrever uma pasta nao vazia e executa `mvn test` em segundo
plano. Os botoes da janela importam e exportam um `ProjectSpec` JSON.

### Linha de comando e JSON

O exemplo de especificacao esta em `examples/project-spec.json`:

```bash
java -jar target/jmol-project-generator-0.1.0-SNAPSHOT.jar preview examples/project-spec.json
java -jar target/jmol-project-generator-0.1.0-SNAPSHOT.jar generate examples/project-spec.json examples/generated-project/ --verify
java -jar target/jmol-project-generator-0.1.0-SNAPSHOT.jar verify examples/generated-project/projeto-cientifico
```

Para sobrescrever uma pasta nao vazia, acrescente `--overwrite` ao comando `generate`.
O campo `license` no JSON usa identificadores SPDX (`MIT`, `BSD-3-Clause` ou `Apache-2.0`).

### Executar um projeto gerado

Dentro da pasta de um projeto criado pelo gerador:

```bash
mvn test
```

O projeto gerado ja vem com uma classe de exemplo e quatro testes JUnit, e deve terminar com
`BUILD SUCCESS` sem nenhum ajuste manual.

Para gerar o relatorio de cobertura, executar Checkstyle ou criar a documentacao Javadoc:

```bash
mvn verify
mvn checkstyle:check
mvn javadoc:javadoc
```

### Projeto de exemplo (`projeto-cientifico`)

O repositorio traz um projeto de referencia criado pelo proprio gerador a partir de
`examples/project-spec.json`, sem edicoes manuais. Ele fica em
`examples/generated-project/projeto-cientifico/`.

Para roda-lo a partir deste repositorio:

```bash
cd examples/generated-project/projeto-cientifico
mvn test
```

O mesmo projeto esta publicado em um repositorio proprio, para que o workflow gerado
(`.github/workflows/build.yml`) seja executado pelo GitHub Actions. O GitHub so executa
workflows que estao na raiz de um repositorio, entao dentro de `examples/` ele nao rodaria:

- Repositorio: <https://github.com/GabrielCardosoUnit/projeto-cientifico>
- Execucoes do CI: <https://github.com/GabrielCardosoUnit/projeto-cientifico/actions>
- Evidencia: [execucao do workflow `Build` com sucesso](https://github.com/GabrielCardosoUnit/projeto-cientifico/actions/runs/36897588953)

Para roda-lo em outra maquina (requer JDK 21 e Maven 3.9 ou superiores):

```bash
git clone https://github.com/GabrielCardosoUnit/projeto-cientifico.git
cd projeto-cientifico
mvn test
```

O resultado esperado e `BUILD SUCCESS` com 4 testes. O projeto de exemplo e uma biblioteca
(uma classe com um calculo de geometria molecular), sem metodo `main`: "rodar" o projeto
significa compilar e executar os testes.

Para recriar o exemplo depois de alterar o JSON ou os modelos:

```bash
java -jar target/jmol-project-generator-0.1.0-SNAPSHOT.jar generate examples/project-spec.json examples/generated-project/ --overwrite --verify
```

## Projeto gerado (modelo `maven-basic`)

```
<artifactId>/
|-- .github/workflows/build.yml      checkout, setup-java e mvn --batch-mode test (Linux)
|-- .gitignore
|-- LICENSE                          MIT, BSD-3-Clause ou Apache-2.0
|-- README.md                        requisitos, build, testes e estrutura
|-- docs/index.md
|-- jmolgen-manifest.json            parametros usados, arquivos criados (SHA-256) e erros
|-- pom.xml                          Java 17/21/25, JUnit 5, JaCoCo, Checkstyle e Javadoc
`-- src/
    |-- main/java/<pacote>/<Classe>.java
    |-- main/resources/
    `-- test/java/<pacote>/<Classe>Test.java
```

## Arquitetura

| Pacote | Responsabilidade |
|---|---|
| `model` | `ProjectSpec` (record imutavel com as respostas) e `LicenseType` |
| `validation` | `ProjectSpecValidator`: regras de nomes, pacote, versao e destino |
| `template` | `TemplateRenderer` (substitui `${variavel}`), `TemplateSource`, `TemplateDescriptor`, `Escaping` |
| `generation` | `ProjectGenerator`, `GenerationPlan` (previa) e `GenerationManifest` |
| `json` | Importacao/exportacao portavel do `ProjectSpec` |
| `process` | `MavenVerifier` e resultado com stdout/stderr separados |
| `ui` | Interface Swing com previa e verificacao em segundo plano |

Os modelos ficam em `src/main/resources/templates/maven-basic/`, descritos por
`template.properties`.

### Correspondencia com o Cookiecutter original

| Cookiecutter (Python) | JMol Project Generator (Java) |
|---|---|
| `cookiecutter.json` | `ProjectSpec` |
| `{{ cookiecutter.valor }}` | `${valor}`, substituido por `TemplateRenderer` |
| hook `pre_gen_project` | `ProjectSpecValidator` |
| `os.makedirs` / `open` | `Files.createDirectories` / `Files.writeString` |
| pytest e diretorios de referencia | JUnit 5 com `@TempDir` |
| `pyproject.toml` | `pom.xml` |
| GitHub Actions (Python) | workflow Maven com `setup-java` e `mvn test` |

## Garantias e limites

- **Validacao**: artifactId, pacote e nome da classe sao validados separadamente; palavras
  reservadas do Java, nomes reservados do Windows (`con`, `nul`...) e nomes que colidem com
  `java.lang` sao recusados. Todos os erros sao reportados de uma vez.
- **Destino seguro**: a pasta do projeto e resolvida dentro da pasta escolhida; caminhos
  absolutos e `..` sao recusados. Uma pasta nao vazia so recebe arquivos com confirmacao
  explicita.
- **Nada incompleto**: todos os arquivos sao renderizados em memoria antes da gravacao;
  qualquer variavel sem valor interrompe a geracao antes de gravar o primeiro arquivo.
- **Deterministico**: as mesmas respostas geram bytes identicos, com quebras de linha LF e
  sem datas, valores aleatorios ou caminhos pessoais (inclusive no manifesto).
- **Escape**: valores inseridos no `pom.xml` passam por escape XML (`&`, `<`, ...). Os campos
  de texto aceitam uma unica linha.
- O workflow gerado nao usa segredos nem publica pacotes.
- Testes, licenca, documentacao e GitHub Actions sao incluidos por padrao; a GUI permite
  selecionar Java e licenca, mas ainda nao oferece controles para desativar cada recurso.

## Andamento

- [x] Fase 1: estrutura do repositorio (JDK 21, pacote `br.edu.unit.jmolgen`)
- [x] Fase 2: nucleo (modelo, validacao, modelo de arquivos, gerador, manifesto) e testes
- [x] Linha de comando e importacao/exportacao do `ProjectSpec` em JSON
- [x] `MavenVerifier` (executa `mvn test` no projeto gerado)
- [x] Interface Swing (Dados, Opcoes, Previa, Resultado)
- [x] Evidencia de CI: workflow gerado executado com sucesso no repositorio `projeto-cientifico`
- [ ] Relatorio tecnico comparativo (Python x Java)

## Fluxo de branches

Codigo novo entra pela branch `develop`; `main` recebe as versoes estaveis.
