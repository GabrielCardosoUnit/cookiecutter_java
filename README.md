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

O arquivo `target/jmol-project-generator-0.1.0-SNAPSHOT.jar` e criado com as classes e os
modelos do gerador.

### Estado atual da execucao

Nesta fase o gerador ainda **nao tem ponto de entrada** (classe `App` com `main`): a geracao
de projetos e exercitada pelos testes, que criam projetos completos em pastas temporarias
(`@TempDir`) e verificam arvore, conteudo, licenca, manifesto e determinismo. A execucao
direta pelo terminal (com arquivo JSON) e pela interface Swing sera adicionada nas proximas
fases (ver [Andamento](#andamento)).

### Executar um projeto gerado

Dentro da pasta de um projeto criado pelo gerador:

```bash
mvn test
```

O projeto gerado ja vem com uma classe de exemplo e quatro testes JUnit, e deve terminar com
`BUILD SUCCESS` sem nenhum ajuste manual.

## Projeto gerado (modelo `maven-basic`)

```
<artifactId>/
|-- .github/workflows/build.yml      checkout, setup-java e mvn --batch-mode test (Linux)
|-- .gitignore
|-- LICENSE                          MIT, BSD-3-Clause ou Apache-2.0
|-- README.md                        requisitos, build, testes e estrutura
|-- docs/index.md
|-- jmolgen-manifest.json            parametros usados, arquivos criados (SHA-256) e erros
|-- pom.xml                          Java 17/21/25, UTF-8, JUnit 5 e Surefire
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

## Andamento

- [x] Fase 1: estrutura do repositorio (JDK 21, pacote `br.edu.unit.jmolgen`)
- [x] Fase 2: nucleo (modelo, validacao, modelo de arquivos, gerador, manifesto) e testes
- [ ] Linha de comando e importacao/exportacao do `ProjectSpec` em JSON
- [ ] `MavenVerifier` (executa `mvn test` no projeto gerado)
- [ ] Interface Swing (Dados, Opcoes, Previa, Resultado)
- [ ] Entregaveis: JAR executavel, `examples/` e relatorio tecnico

## Fluxo de branches

Codigo novo entra pela branch `develop`; `main` recebe as versoes estaveis.
