/**
 * NOTE: This is entirely optional and basics can be done in `settings.gradle.kts`
 */

import java.io.File

repositories {
    // Any external repositories besides: MavenLocal, MavenCentral, HytaleMaven, and CurseMaven
}

dependencies {
    // Any external dependency you also want to include
}

// Fix runServer when project path contains spaces: use a junction (Windows) so --mods has no spaces.
val isWindows = System.getProperty("os.name", "").lowercase().contains("win")
val modsJunctionPath: String? = if (isWindows) System.getenv("LOCALAPPDATA")?.let { File(it, "HyGripMods").absolutePath } else null

tasks.register("createModsJunction") {
    onlyIf { modsJunctionPath != null }
    doLast {
        val linkPath = modsJunctionPath!!
        val targetDir = layout.projectDirectory.dir("src/main").asFile
        val link = File(linkPath)
        if (link.exists()) {
            if (!link.isDirectory || link.canonicalPath != targetDir.canonicalPath) {
                link.deleteRecursively()
            }
        }
        if (!link.exists()) {
            val proc = ProcessBuilder("cmd", "/c", "mklink", "/J", linkPath, targetDir.absolutePath)
                .inheritIO()
                .start()
            if (proc.waitFor() != 0) throw RuntimeException("mklink failed with code ${proc.exitValue()}")
        }
    }
}

tasks.configureEach {
    if (name == "runServer" && modsJunctionPath != null) {
        dependsOn("createModsJunction")
        (this as? org.gradle.api.tasks.JavaExec)?.let { javaExec ->
            doFirst {
                val argsList = javaExec.args.toMutableList()
                val newArgs = argsList.map { arg ->
                    if (arg.startsWith("--mods=")) "--mods=$modsJunctionPath" else arg
                }
                javaExec.args = newArgs
            }
        }
    }
}