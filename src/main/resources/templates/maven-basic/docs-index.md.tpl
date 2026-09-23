# Documentacao de ${projectName}

${description}

## Documentacao da API

A documentacao das classes e gerada a partir dos comentarios Javadoc:

```bash
mvn javadoc:javadoc
```

O resultado fica na pasta `apidocs`, dentro de `target/`.

## Como contribuir

1. Escreva o codigo em `src/main/java/${packagePath}/`.
2. Escreva os testes correspondentes em `src/test/java/${packagePath}/`.
3. Rode `mvn test` antes de enviar alteracoes; o mesmo comando roda na integracao continua.
