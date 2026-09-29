# Passos para concluir o JMol Project Generator

Acompanhamento do projeto em relacao ao roteiro do Grupo 3 (Artigo 6).

Legenda: ✅ feito · ⚠️ parcial · ❌ falta · 👥 tarefa do grupo (fora do codigo)

Nota: a GUI permite escolher a versao Java e a licenca. Testes, documentacao e GitHub Actions
sao gerados por padrao; controles para desativar esses recursos ainda nao existem.

---

## 1. Passos do roteiro (secao 9)

| # | Passo | Situacao | Detalhe |
|---|---|---|---|
| 1 | Executar o Cookiecutter original | ❌ 👥 | Instalar Python e `cookiecutter`, gerar 2 projetos com opcoes diferentes e comparar arquivos removidos, licenca, testes e workflows. O resultado e a matriz de recursos que vai para o relatorio |
| 2 | Definir o escopo do template Java | ✅ | Modelo `maven-basic`: 9 arquivos + o manifesto |
| 3 | Modelar os parametros | ✅ | Record `ProjectSpec` + enum `LicenseType` |
| 4 | Validar nomes e caminhos | ✅ | `ProjectSpecValidator`: artifactId e pacote validados separadamente, bloqueio de `..` e caminho absoluto, pasta nao vazia |
| 5 | Criar a arvore de diretorios | ✅ | `resolve` segmento por segmento + `Files.createDirectories` |
| 6 | Renderizador de texto | ✅ | `TemplateRenderer`: conjunto fechado de variaveis, falha antes de gravar |
| 7 | Gerar pom e codigo inicial | ✅ | pom com Java, UTF-8, JUnit, Surefire, JaCoCo, Checkstyle e Javadoc; classe cientifica e teste no pacote informado |
| 8 | Gerar licenca, README e gitignore | ✅ | 3 licencas (MIT, BSD-3-Clause, Apache-2.0), README, `.gitignore` que nao esconde dados, `docs/` |
| 9 | Gerar integracao continua | ✅ | `.github/workflows/build.yml` com `mvn --batch-mode test`, sem segredos |
| 10 | Manifesto de geracao | ✅ | `jmolgen-manifest.json`: versao do modelo, parametros, SHA-256 e erros |
| 11 | Interface Swing | ✅ | `MainFrame` com Dados, Opcoes, Previa e Resultado; seletor de pasta; previa dinamica; erros por campo; confirmacao; progresso e verificacao em `SwingWorker` |
| 12 | Reproducao por JSON | ✅ | Importar/exportar `ProjectSpec` em JSON; `App` oferece `preview`, `generate` e `verify` |
| 13 | Testar a arvore e o conteudo | ✅ | Testes JUnit com `@TempDir`, incluindo JSON, CLI, nome de classe informado e Maven ausente |
| 14 | Validar o projeto gerado | ✅ | `MavenVerifier` executa `mvn --batch-mode test`, captura stdout/stderr, aplica timeout e informa falha ao iniciar |

---

## 2. Entregaveis (secao 11)

| Item | Situacao |
|---|---|
| Codigo do gerador e templates versionados | ✅ |
| `README.md` | ✅ Atualizado com GUI e CLI |
| `examples/project-spec.json` | ✅ Exemplo importavel |
| `examples/generated-project/` | ✅ Gerado pelo proprio gerador |
| JAR executavel | ✅ `mvn package` inclui dependencias e `Main-Class` |
| Evidencia de execucao do CI gerado | ❌ Subir o projeto de exemplo para o GitHub e guardar o link ou print do workflow verde |
| `docs/relatorio_tecnico.pdf` | ❌ 👥 Comparacao Python x Java, usando o passo 1 |

---

## 3. Lista de verificacao antes da entrega (secao 13)

- [x] O gerador abre com o JDK indicado (`App` inicia a interface)
- [x] O mesmo JSON produz a mesma arvore
- [x] artifactId e package sao validados separadamente
- [x] O destino nao permite travessia de diretorios
- [x] Pastas nao vazias nao sao sobrescritas silenciosamente
- [x] Nenhuma variavel fica sem substituicao
- [x] O projeto gerado executa `mvn test` pelo `MavenVerifier`
- [x] O workflow nao contem segredo nem publicacao automatica
- [x] Os testes usam diretorio temporario
- [x] O README descreve requisitos e limites

---

## 4. Ordem de execucao

### ✅ Fase 1: reestruturacao
- [x] Remover o Maven Archetype e o codigo de exemplo antigo; a geracao usa templates proprios
- [x] pom como `br.edu.unit:jmol-project-generator`, JDK 21
- [x] CI do gerador com JDK 21 e `mvn test`
- [x] README reescrito, com a secao "Como executar"
- [x] Commit

### ✅ Fase 2: nucleo do gerador
- [x] `model`: `ProjectSpec`, `LicenseType`
- [x] `validation`: `ProjectSpecValidator`, `ValidationError`, `InvalidProjectSpecException`
- [x] `template`: `TemplateRenderer`, `TemplateSource`, `ClasspathTemplateSource`, `TemplateDescriptor`, `Escaping`
- [x] `generation`: `ProjectGenerator`, `GenerationPlan` (previa), `GenerationManifest`
- [x] Modelo `maven-basic` com 3 licencas
- [x] 95 testes JUnit com `@TempDir`
- [x] Commit

### ✅ Fase 4: linha de comando, JSON e verificacao
- [x] Exportar e importar o `ProjectSpec` em JSON com identificador SPDX da licenca
- [x] Classe `App` com `main`: GUI sem argumentos e comandos CLI
- [x] Confirmar sobrescrita com `--overwrite`
- [x] `process/MavenVerifier`: `ProcessBuilder`, diretorio definido, stdout/stderr, timeout e falha quando Maven ausente
- [x] Testes de JSON, CLI e Maven ausente
- [x] Atualizar README

### ✅ Fase 5: interface Swing
- [x] `ui/MainFrame` com Dados, Opcoes, Previa e Resultado
- [x] Campos do `ProjectSpec`; licenca e Java em listas
- [x] Erros de validacao associados aos campos
- [x] Previa atualizada conforme os campos validos mudam
- [x] `JFileChooser` para pasta base e arquivos JSON
- [x] Confirmacao de sobrescrita
- [x] Geracao e `MavenVerifier` em `SwingWorker`, com progresso
- [x] Importar e exportar JSON
- [x] Atualizar README

### 🟠 Fase 6: entrega
- [x] JAR executavel com Main-Class e dependencias embutidas (`mvn package`)
- [x] `examples/project-spec.json`
- [x] `examples/generated-project/` gerado pelo proprio gerador
- [ ] Subir o projeto de exemplo para o GitHub e registrar o CI verde
- [x] README final (uso do JAR, CLI e interface)
- [ ] Decidir sobre `templates/` na raiz (o roteiro mostra essa pasta; hoje os modelos ficam em `src/main/resources/templates/`, onde precisam estar para irem dentro do JAR)

### 👥 Em paralelo: tarefas do grupo
- [ ] Passo 1: rodar o Cookiecutter original e montar a matriz de recursos
- [ ] Relatorio tecnico `docs/relatorio_tecnico.pdf` comparando Python e Java
- [ ] Preparar a apresentacao (o `learn.md` ajuda a explicar o codigo)

### Conferencia final
- [ ] Repassar a lista de verificacao da secao 3 deste arquivo
- [ ] Testar o JAR em outra maquina: preencher, visualizar, gerar, abrir e rodar `mvn test`
