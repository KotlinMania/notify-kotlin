// port-lint: source lib.rs
package io.github.kotlinmania.notify

/**
 * Watcher kind enumeration.
 */
public enum class WatcherKind {
    /** inotify backend (linux) */
    Inotify,

    /** FS-Event backend (mac) */
    Fsevent,

    /** KQueue backend (bsd, optionally mac) */
    Kqueue,

    /** Polling based backend (fallback) */
    PollWatcher,

    /** Windows backend */
    ReadDirectoryChangesWatcher,

    /** Fake watcher for testing */
    NullWatcher,
}

/**
 * The set of requirements for watcher event handling functions.
 */
public interface EventHandler {
    /**
     * Handles an event.
     */
    public fun handleEvent(event: Result<Event>)

    public companion object {
        public operator fun invoke(handler: (Result<Event>) -> Unit): EventHandler =
            object : EventHandler {
                override fun handleEvent(event: Result<Event>) = handler(event)
            }
    }
}

/**
 * Providing methods for adding and removing paths to watch.
 */
public interface PathsMut {
    /**
     * Add a new path to watch.
     */
    public fun add(path: String, recursiveMode: RecursiveMode): Result<Unit>

    /**
     * Remove a path from watching.
     */
    public fun remove(path: String): Result<Unit>

    /**
     * Ensure added/removed paths are applied.
     */
    public fun commit(): Result<Unit>
}

/**
 * Type that can deliver file activity notifications.
 *
 * Watcher is implemented per platform using the best implementation available on that platform.
 */
public interface Watcher {
    /**
     * Begin watching a new path.
     */
    public fun watch(path: String, recursiveMode: RecursiveMode): Result<Unit>

    /**
     * Stop watching a path.
     */
    public fun unwatch(path: String): Result<Unit>

    /**
     * Add/remove paths to watch.
     */
    public fun pathsMut(): PathsMut = DefaultPathsMut(this)

    /**
     * Configure the watcher at runtime.
     */
    public fun configure(option: Config): Result<Boolean> = Result.success(false)

    /**
     * Returns the watcher kind.
     */
    public fun kind(): WatcherKind
}

/**
 * Default PathsMut implementation wrapping a Watcher.
 */
public class DefaultPathsMut(
    private val watcher: Watcher,
) : PathsMut {
    override fun add(path: String, recursiveMode: RecursiveMode): Result<Unit> =
        watcher.watch(path, recursiveMode)

    override fun remove(path: String): Result<Unit> =
        watcher.unwatch(path)

    override fun commit(): Result<Unit> =
        Result.success(Unit)
}

/**
 * The recommended Watcher implementation for the current platform.
 */
public typealias RecommendedWatcher = PollWatcher

/**
 * Convenience method for creating the RecommendedWatcher for the current platform.
 */
public fun recommendedWatcher(eventHandler: EventHandler): Result<RecommendedWatcher> =
    RecommendedWatcher.new(eventHandler, Config.default())

/**
 * Convenience method for creating the RecommendedWatcher with a lambda event handler.
 */
public fun recommendedWatcher(eventHandler: (Result<Event>) -> Unit): Result<RecommendedWatcher> =
    recommendedWatcher(EventHandler(eventHandler))

public fun iterWithTimeout(events: List<Event>): List<Event> = events
