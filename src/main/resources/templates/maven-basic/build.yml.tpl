name: Build

on:
  push:
  pull_request:

permissions:
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Configurar JDK ${javaVersion}
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '${javaVersion}'
          cache: 'maven'

      - name: Compilar e testar
        run: mvn --batch-mode test
