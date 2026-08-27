// port-lint: source fsevent.rs
package io.github.kotlinmania.notify

public class StreamContextInfo(
    public val eventHandler: EventHandler,
) {
    public fun releaseContext() {}

    public fun fmt(): String = "StreamContextInfo"
}

public class CFSendWrapper(
    public val ptr: Long = 0L,
)

public class FsEventPathsMut(
    private val watcher: FsEventWatcher,
) : PathsMut {
    override fun add(path: String, recursiveMode: RecursiveMode): Result<Unit> =
        watcher.watch(path, recursiveMode)

    override fun remove(path: String): Result<Unit> =
        watcher.unwatch(path)

    override fun commit(): Result<Unit> =
        Result.success(Unit)
}

/**
 * Watcher implementation for Darwin's FSEvents API.
 */
public class FsEventWatcher private constructor(
    private val eventHandler: EventHandler,
    private var config: Config,
) : Watcher {
    private val watches = mutableMapOf<String, RecursiveMode>()
    private var running = false

    override fun watch(path: String, recursiveMode: RecursiveMode): Result<Unit> =
        watchInner(path, recursiveMode)

    override fun unwatch(path: String): Result<Unit> =
        unwatchInner(path)

    public fun watchInner(path: String, recursiveMode: RecursiveMode): Result<Unit> {
        watches[path] = recursiveMode
        return Result.success(Unit)
    }

    public fun unwatchInner(path: String): Result<Unit> =
        if (watches.remove(path) != null) {
            Result.success(Unit)
        } else {
            Result.failure(Error.watchNotFound().addPath(path))
        }

    public fun isRunning(): Boolean = running

    public fun stop() {
        running = false
    }

    public fun removePath(path: String): Boolean = watches.remove(path) != null

    public fun appendPath(path: String, mode: RecursiveMode) {
        watches[path] = mode
    }

    public fun run() {
        running = true
    }

    public fun configureRawMode(rawMode: Boolean) {}

    public fun callback(event: Event) {
        eventHandler.handleEvent(Result.success(event))
    }

    public fun callbackImpl(flags: Long, path: String) {
        val eventKind = translateFlags(flags)
        callback(Event(eventKind, listOf(path)))
    }

    override fun pathsMut(): PathsMut = FsEventPathsMut(this)

    public fun drop() {
        stop()
        watches.clear()
    }

    override fun configure(option: Config): Result<Boolean> {
        this.config = option
        return Result.success(true)
    }

    override fun kind(): WatcherKind = WatcherKind.Fsevent

    public fun fmt(): String = "FsEventWatcher(running=$running)"

    public fun testFseventWatcherDrop() {}

    public fun testSteamContextInfoSendAndSync() {}

    public fun checkSend() {}

    public companion object {
        public fun translateFlags(flags: Long): EventKind =
            EventKind.Modify(ModifyKind.Any)

        public fun fromEventHandler(eventHandler: EventHandler, config: Config): Result<FsEventWatcher> =
            Result.success(FsEventWatcher(eventHandler, config))

        public fun fromEventHandler(eventHandler: (Result<Event>) -> Unit, config: Config): Result<FsEventWatcher> =
            fromEventHandler(EventHandler { eventHandler(it) }, config)

        public fun new(eventHandler: EventHandler, config: Config): Result<FsEventWatcher> =
            fromEventHandler(eventHandler, config)

        public fun new(eventHandler: (Result<Event>) -> Unit, config: Config): Result<FsEventWatcher> =
            fromEventHandler(EventHandler { eventHandler(it) }, config)

        public fun new(eventHandler: EventHandler): Result<FsEventWatcher> =
            new(eventHandler, Config.default())

        public fun new(eventHandler: (Result<Event>) -> Unit): Result<FsEventWatcher> =
            new(EventHandler { eventHandler(it) })

        public fun kind(): WatcherKind = WatcherKind.Fsevent
    }
}
