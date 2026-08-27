// port-lint: source notify/src/null.rs
package io.github.kotlinmania.notify

/**
 * Stub Watcher implementation.
 *
 * Events are never delivered from this watcher.
 */
public class NullWatcher private constructor() : Watcher {

    override fun watch(path: String, recursiveMode: RecursiveMode): Result<Unit> =
        Result.success(Unit)

    override fun unwatch(path: String): Result<Unit> =
        Result.success(Unit)

    override fun configure(option: Config): Result<Boolean> =
        Result.success(false)

    override fun kind(): WatcherKind = WatcherKind.NullWatcher

    public companion object {
        public fun new(eventHandler: EventHandler, config: Config): Result<NullWatcher> =
            Result.success(NullWatcher())

        public fun new(eventHandler: (Result<Event>) -> Unit, config: Config): Result<NullWatcher> =
            Result.success(NullWatcher())

        public fun new(eventHandler: EventHandler): Result<NullWatcher> =
            new(eventHandler, Config.default())

        public fun new(eventHandler: (Result<Event>) -> Unit): Result<NullWatcher> =
            new(EventHandler(eventHandler))

        public fun kind(): WatcherKind = WatcherKind.NullWatcher
    }
}
