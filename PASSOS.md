# Passos para concluir o JMol Project Generator

Acompanhamento do projeto em relacao ao roteiro do Grupo 3 (Artigo 6).

Legenda: ✅ feito · ⚠️ parcial · ❌ falta · 👥 tarefa do grupo (fora do codigo)

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
| 7 | Gerar pom e codigo inicial | ✅ | pom com Java, UTF-8, JUnit e Surefire; classe cientifica e teste no pacote informado |
| 8 | Gerar licenca, README e gitignore | ✅ | 3 licencas (MIT, BSD-3-Clause, Apache-2.0), README, `.gitignore` que nao esconde dados, `docs/` |
| 9 | Gerar integracao continua | ✅ | `.github/workflows/build.yml` com `mvn --batch-mode test`, sem segredos |
| 10 | Manifesto de geracao | ✅ | `jmolgen-manifest.json`: versao do modelo, parametros, SHA-256 e erros |
| 11 | Interface Swing | ❌ | `MainFrame` com as abas Dados, Opcoes, Previa e Resultado; `JFileChooser`; previa atualizada ao digitar; erros por campo; confirmacao para sobrescrever; barra de progresso |
| 12 | Reproducao por JSON | ❌ | Exportar e importar o `ProjectSpec` em JSON + modo linha de comando (`App` com `main`) |
| 13 | Testar a arvore e o conteudo | ✅ | 95 testes com `@TempDir`. Faltam os testes de JSON e do `MavenVerifier` quando existirem |
| 14 | Validar o projeto gerado | ⚠️ | Verificado a mao (Java 17, 21 e 25 dao `BUILD SUCCESS`). Falta o `MavenVerifier`: `ProcessBuilder`, captura de stdout, stderr e codigo de saida, e o resultado na tela via `SwingWorker` |

---

## 2. Entregaveis (secao 11)

| Item | Situacao |
|---|---|
| Codigo do gerador e templates versionados | ✅ |
| `README.md` | ⚠️ Atualizar quando houver CLI e interface |
| `examples/project-spec.json` | ❌ Depende do passo 12 |
| `examples/generated-project/` | ❌ Gerar com o proprio gerador |
| `JMol-Project-Generator.jar` executavel | ❌ Precisa de `main` e do Jackson embutido (plugin shade) |
| Evidencia de execucao do CI gerado | ❌ Subir o projeto de exemplo para o GitHub e guardar o link ou print do workflow verde |
| `docs/relatorio_tecnico.pdf` | ❌ 👥 Comparacao Python x Java, usando o passo 1 |

---

## 3. Lista de verificacao antes da entrega (secao 13)

- [ ] O gerador abre com o JDK indicado *(depende de `App` e da interface)*
- [ ] O mesmo JSON produz a mesma arvore *(o determinismo ja esta testado; falta o JSON)*
- [x] artifactId e package sao validados separadamente
- [x] O destino nao permite travessia de diretorios
- [x] Pastas nao vazias nao sao sobrescritas silenciosamente *(falta o dialogo na interface)*
- [x] Nenhuma variavel fica sem substituicao
- [ ] O projeto gerado executa `mvn test` *(verificado a mao; falta o `MavenVerifier`)*
- [x] O workflow nao contem segredo nem publicacao automatica
- [x] Os testes usam diretorio temporario
- [x] O README descreve requisitos e limites

---

## 4. Ordem de execucao

### ✅ Fase 1: reestruturacao
- [x] Remover o Maven Archetype e o codigo de exemplo antigo
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

### ❌ Fase 4: linha de comando, JSON e verificacao
- [ ] Exportar e importar o `ProjectSpec` em JSON (Jackson)
- [ ] Classe `App` com `main`: gerar a partir de um `project-spec.json`
- [ ] Opcao para confirmar a sobrescrita de pasta nao vazia pela linha de comando
- [ ] `process/MavenVerifier`: rodar `mvn test` com `ProcessBuilder` (argumentos em lista, diretorio definido, captura de stdout, stderr e codigo de saida, tratamento para Maven ausente)
- [ ] Testes: o mesmo JSON gera a mesma arvore; o `MavenVerifier` com e sem Maven
- [ ] Atualizar README e `learn.md`

### ❌ Fase 5: interface Swing
- [ ] `ui/MainFrame` com as abas Dados, Opcoes, Previa e Resultado
- [ ] Campos para os 8 parametros; licenca e versao do Java em listas
- [ ] Erros exibidos no campo certo (usando `ValidationError.field`)
- [ ] Previa da arvore atualizada quando os campos validos mudam (`GenerationPlan.treeView`)
- [ ] `JFileChooser` apenas para a pasta base
- [ ] Dialogo de confirmacao para pasta nao vazia
- [ ] Gerar e rodar o `MavenVerifier` em `SwingWorker`, com progresso, sem congelar a janela
- [ ] Botoes para importar e exportar JSON
- [ ] Atualizar README e `learn.md`

### ❌ Fase 6: entrega
- [ ] JAR executavel `JMol-Project-Generator.jar` (Main-Class + dependencias embutidas)
- [ ] `examples/project-spec.json`
- [ ] `examples/generated-project/` gerado pelo proprio gerador
- [ ] Subir o projeto de exemplo para o GitHub e registrar o CI verde
- [ ] README final (uso do JAR, CLI e interface)
- [ ] Decidir sobre `templates/` na raiz (o roteiro mostra essa pasta; hoje os modelos ficam em `src/main/resources/templates/`, onde precisam estar para irem dentro do JAR)

### 👥 Em paralelo: tarefas do grupo
- [ ] Passo 1: rodar o Cookiecutter original e montar a matriz de recursos
- [ ] Relatorio tecnico `docs/relatorio_tecnico.pdf` comparando Python e Java
- [ ] Preparar a apresentacao (o `learn.md` ajuda a explicar o codigo)

### Conferencia final
- [ ] Repassar a lista de verificacao da secao 3 deste arquivo
- [ ] Testar o JAR em outra maquina: preencher, visualizar, gerar, abrir e rodar `mvn test`
