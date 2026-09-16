# cookiecutter-java

Gerador de projetos Maven com boas praticas (testes, cobertura, lint, documentacao e CI),
inspirado no [Cookiecutter CC-CMS da MolSSI](https://github.com/MolSSI/cookiecutter-cms).
Projeto desenvolvido para a disciplina de Projeto de Programacao (Universidade Tiradentes).

O repositorio tem duas partes:

- **Projeto base** (raiz do repositorio) — um projeto Maven de exemplo, com todas as
  ferramentas ja configuradas: JUnit 5, JaCoCo (cobertura), Checkstyle (lint), Javadoc e CI.
- **`cookiecutter-archetype/`** — o "gerador" propriamente dito: um Maven Archetype que cria
  novos projetos com essa mesma estrutura, perguntando de forma interativa autor, descricao
  e quais recursos incluir.

## Pre-requisitos

- JDK 17
- Maven 3.9+

## Como rodar o projeto base

Na raiz do repositorio:

```bash
mvn clean verify
```

Esse comando compila o codigo, roda os testes (JUnit 5), gera o relatorio de cobertura
(JaCoCo, exigindo minimo de 60% de cobertura de linhas) e aplica o lint (Checkstyle, no
padrao Google). O relatorio de cobertura em HTML fica em `target/site/jacoco/index.html`
depois do build.

Para executar a aplicacao (imprime "Hello World!"):

```bash
mvn -q exec:java -Dexec.mainClass="br.com.isadora.ccjava.App"
```

ou, depois do `mvn clean verify`, direto pelo jar gerado:

```bash
java -cp target/classes br.com.isadora.ccjava.App
```

## Como usar o gerador (Maven Archetype)

### 1. Instalar o archetype localmente

Dentro da pasta `cookiecutter-archetype/`:

```bash
cd cookiecutter-archetype
mvn install
```

### 2. Gerar um novo projeto a partir dele

Em qualquer outra pasta (fora deste repositorio):

```bash
mvn archetype:generate \
  -DarchetypeGroupId=br.com.isadora.ccjava \
  -DarchetypeArtifactId=cookiecutter-archetype \
  -DarchetypeVersion=1.0-SNAPSHOT
```

O Maven vai pedir, em ordem:

| Pergunta | O que controla |
|---|---|
| `groupId`, `artifactId`, `version`, `package` | Identidade do novo projeto (padrao do Maven) |
| `author` | Nome do autor, vai para o `pom.xml` gerado |
| `description` | Descricao do projeto, vai para o `pom.xml` gerado |
| `includeDependencies` (S/N) | Inclui o Checkstyle (lint) no projeto gerado |
| `includeTests` (S/N) | Inclui JUnit 5, a classe de teste e o JaCoCo (cobertura) |
| `includeDocs` (S/N) | Inclui o plugin de Javadoc |
| `includeGit` (S/N) | Inclui `.gitignore` e o workflow de CI (`.github/workflows`) |
| `includeLicense` (S/N) | Inclui o arquivo `LICENSE` |

Basta apertar Enter para aceitar o valor padrao mostrado entre colchetes, ou digitar
outro valor (S/N, texto, etc). Ao final, o projeto novo e criado numa pasta com o nome
do `artifactId` informado, ja pronto para `mvn clean verify`.

Para gerar sem perguntas (usando so os valores padrao ou passados via `-D`), adicione
`-DinteractiveMode=false` e os `-D` com os valores desejados, por exemplo:

```bash
mvn archetype:generate -DinteractiveMode=false \
  -DarchetypeGroupId=br.com.isadora.ccjava \
  -DarchetypeArtifactId=cookiecutter-archetype \
  -DarchetypeVersion=1.0-SNAPSHOT \
  -DgroupId=com.exemplo.meuprojeto \
  -DartifactId=meu-novo-projeto \
  -Dversion=1.0-SNAPSHOT \
  -Dauthor="Seu Nome" \
  -Ddescription="Meu novo projeto" \
  -DincludeDependencies=S \
  -DincludeTests=S \
  -DincludeDocs=S \
  -DincludeGit=S \
  -DincludeLicense=S
```

## Integracao continua

Todo push ou pull request para as branches `main` e `develop` dispara o workflow em
`.github/workflows/ci.yml`, que roda `mvn clean verify` num runner Ubuntu com JDK 17 e
publica os relatorios de teste e cobertura como artefatos da execucao.

## Fluxo de branches

O repositorio segue um fluxo simples de Git-flow: codigo novo entra pela branch
`develop`; `main` fica reservada para versoes estaveis/merges.
