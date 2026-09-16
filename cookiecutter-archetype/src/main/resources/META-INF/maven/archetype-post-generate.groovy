import java.io.File

def props = request.getProperties()
def outputDir = new File(request.getOutputDirectory(), request.getArtifactId())

def isYes = { String key ->
    def v = props.getProperty(key, "S")
    return v != null && v.trim().equalsIgnoreCase("S")
}

// Testes: remove a pasta src/test inteira quando o usuario responde N
if (!isYes("includeTests")) {
    def testDir = new File(outputDir, "src/test")
    if (testDir.exists()) {
        testDir.deleteDir()
    }
}

// Licenca: remove o arquivo LICENSE quando o usuario responde N
if (!isYes("includeLicense")) {
    def license = new File(outputDir, "LICENSE")
    if (license.exists()) {
        license.delete()
    }
}

// Git: remove .gitignore e o workflow de CI quando o usuario responde N
if (!isYes("includeGit")) {
    def gitignore = new File(outputDir, ".gitignore")
    if (gitignore.exists()) {
        gitignore.delete()
    }
    def githubDir = new File(outputDir, ".github")
    if (githubDir.exists()) {
        githubDir.deleteDir()
    }
}

println "Projeto '" + request.getArtifactId() + "' gerado com sucesso."
println "  Autor: " + props.getProperty("author")
println "  Testes: " + props.getProperty("includeTests")
println "  Dependencias extras (checkstyle): " + props.getProperty("includeDependencies")
println "  Documentacao (javadoc): " + props.getProperty("includeDocs")
println "  Git/CI: " + props.getProperty("includeGit")
println "  Licenca: " + props.getProperty("includeLicense")
