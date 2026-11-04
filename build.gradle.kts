plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
}

extra["androidMinSdkVersion"] = 31
extra["androidTargetSdkVersion"] = 37
extra["androidCompileSdkVersion"] = 37
extra["androidCompileSdkVersionMinor"] = 0
extra["androidBuildToolsVersion"] = "37.0.0"
extra["androidCompileNdkVersion"] = libs.versions.ndk.get()
extra["androidSourceCompatibility"] = JavaVersion.VERSION_21
extra["androidTargetCompatibility"] = JavaVersion.VERSION_21
extra["managerVersionCode"] = getVersionCode()
extra["managerVersionName"] = getVersionName()

fun safeExec(vararg cmd: String): String? {
    return try {
        val process = Runtime.getRuntime().exec(cmd)
        val out = process.inputStream.bufferedReader().use { it.readText().trim() }
        process.waitFor()
        out
    } catch (_: Throwable) {
        null
    }
}

fun getGitCommitCount(): Int {
    return safeExec("git", "rev-list", "--count", "HEAD")?.toIntOrNull() ?: 0
}

fun getGitDescribe(): String {
    return safeExec("git", "describe", "--tags", "--always") ?: "mclient"
}

fun getVersionCode(): Int {
    val commitCount = getGitCommitCount()
    return 30000 + commitCount
}

fun getVersionName(): String {
    return getGitDescribe()
}
