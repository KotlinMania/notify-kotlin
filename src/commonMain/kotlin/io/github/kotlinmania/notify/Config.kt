// port-lint: source config.rs
package io.github.kotlinmania.notify

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Indicates whether only the provided directory or its sub-directories as well should be watched.
 */
enum class RecursiveMode {
    /**
     * Watch all sub-directories as well, including directories created after installing the watch.
     */
    Recursive,

    /**
     * Watch only the provided directory.
     */
    NonRecursive,
    ;

    internal fun isRecursive(): Boolean =
        when (this) {
            Recursive -> true
            NonRecursive -> false
        }
}

/**
 * Watcher backend configuration.
 *
 * This contains multiple settings that may relate to only one specific backend,
 * such as to correctly configure each backend regardless of what is selected during runtime.
 *
 * ```kotlin
 * val config = Config()
 *     .withPollInterval(2.seconds)
 *     .withCompareContents(true)
 * ```
 *
 * Some options can be changed during runtime, others have to be set when creating the watcher backend.
 */
data class Config(
    private val pollIntervalValue: Duration? = 30.seconds,
    private val compareContentsValue: Boolean = false,
    private val followSymlinksValue: Boolean = true,
) {
    /**
     * For the `PollWatcher` backend.
     *
     * Interval between each re-scan attempt. This can be extremely expensive for large
     * file trees so it is recommended to measure and tune accordingly.
     *
     * The default poll frequency is 30 seconds.
     *
     * This will enable automatic polling, overwriting [withManualPolling].
     */
    fun withPollInterval(duration: Duration): Config {
        // Version 7.0 can break the signature to use an optional duration.
        return copy(pollIntervalValue = duration)
    }

    /**
     * Returns current setting.
     */
    fun pollInterval(): Duration? = pollIntervalValue

    /**
     * For the `PollWatcher` backend.
     *
     * Disable automatic polling. Requires calling `PollWatcher.poll()` manually.
     *
     * This will disable automatic polling, overwriting [withPollInterval].
     */
    fun withManualPolling(): Config = copy(pollIntervalValue = null)

    /**
     * For the `PollWatcher` backend.
     *
     * Optional feature that will evaluate the contents of changed files to determine if
     * they have indeed changed using a fast hashing algorithm. This is especially important
     * for pseudo filesystems like those on Linux under /sys and /proc which are not obligated
     * to respect any other filesystem norms such as modification timestamps, file sizes, etc.
     * By enabling this feature, performance will be significantly impacted as all files will
     * need to be read and hashed at each [pollInterval].
     *
     * This cannot be changed during runtime. Off by default.
     */
    fun withCompareContents(compareContents: Boolean): Config = copy(compareContentsValue = compareContents)

    /**
     * Returns current setting.
     */
    fun compareContents(): Boolean = compareContentsValue

    /**
     * For the `INotifyWatcher`, `KqueueWatcher`, and `PollWatcher`.
     *
     * Determine if symbolic links should be followed when recursively watching a directory.
     *
     * This cannot be changed during runtime. On by default.
     */
    fun withFollowSymlinks(followSymlinks: Boolean): Config = copy(followSymlinksValue = followSymlinks)

    /**
     * Returns current setting.
     */
    fun followSymlinks(): Boolean = followSymlinksValue

    companion object {
        /**
         * Creates the default watcher backend configuration.
         */
        fun default(): Config = Config()
    }
}
