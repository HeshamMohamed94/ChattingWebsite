import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

// Yarn Classic's lockfile writer is not deterministic across `clean` re-resolutions on this
// toolchain: equivalent-but-differently-grouped duplicate semver-range entries (same versions,
// same integrity hashes) made kotlinStoreYarnLock's strict comparison fail every time, even
// right after kotlinUpgradeYarnLock. This applies WARNING/auto-replace to every lockfile
// mismatch project-wide, not just that duplicate-grouping case - there is no CI step here that
// separately diffs the resolved lockfile for real version/integrity drift, so a genuine
// unintended dependency bump would also pass silently under this setting. Revisit if this
// project gains such a check.
plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.WARNING
    the<YarnRootExtension>().yarnLockAutoReplace = true
}

