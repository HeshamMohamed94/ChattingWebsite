import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

// Yarn Classic's lockfile writer is not deterministic across `clean` re-resolutions on this
// toolchain: equivalent-but-differently-grouped duplicate semver-range entries (same versions,
// same integrity hashes) make kotlinStoreYarnLock's strict comparison fail every time, even
// right after kotlinUpgradeYarnLock. Warn and auto-replace instead of failing the build.
plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.WARNING
    the<YarnRootExtension>().yarnLockAutoReplace = true
}

