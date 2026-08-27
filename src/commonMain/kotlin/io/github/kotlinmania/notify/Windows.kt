// port-lint: source windows.rs
package io.github.kotlinmania.notify

public class ReadData(public val buffer: ByteArray = ByteArray(0))

public class ReadDirectoryRequest(
    public val path: String,
    public val recursive: Boolean,
)

public sealed class Action {
    public data class Watch(public val path: String, public val recursive: Boolean) : Action()
    public data class Unwatch(public val path: String) : Action()
    public data object Stop : Action()
    public data class ConfigureRaw(public val raw: Boolean) : Action()
}

public class MetaEvent(public val event: Event)

public enum class WatchState {
    Active,
    Stopped,
}

public class ReadDirectoryChangesServer(
    private val eventHandler: EventHandler,
) {
    private var state: WatchState = WatchState.Stopped
    private val watches = mutableMapOf<String, Boolean>()

    public fun start() {
        state = WatchState.Active
    }

    public fun run() {}

    public fun addWatch(path: String, recursiveBoolean: Boolean) {
        watches[path] = recursiveBoolean
    }

    public fun removeWatch(path: String) {
        watches.remove(path)
    }

    public fun configureRawMode(rawMode: Boolean) {}

    public fun stopWatch(path: String) {
        removeWatch(path)
    }

    public fun startRead(request: ReadDirectoryRequest) {}

    public fun handleEvent(action: Action) {
        when (action) {
            is Action.Watch -> addWatch(action.path, action.recursive)
            is Action.Unwatch -> removeWatch(action.path)
            is Action.Stop -> state = WatchState.Stopped
            is Action.ConfigureRaw -> configureRawMode(action.raw)
        }
    }

    public fun emitEvent(metaEvent: MetaEvent) {
        eventHandler.handleEvent(Result.success(metaEvent.event))
    }

    public fun wakeupServer() {}

    public fun sendActionRequireAck(action: Action): Result<Unit> {
        handleEvent(action)
        return Result.success(Unit)
    }

    public companion object {
        public fun create(eventHandler: EventHandler): ReadDirectoryChangesServer =
            ReadDirectoryChangesServer(eventHandler)

        public fun create(eventHandler: (Result<Event>) -> Unit): ReadDirectoryChangesServer =
            ReadDirectoryChangesServer(EventHandler { eventHandler(it) })
    }
}

/**
 * Watcher implementation for Windows ReadDirectoryChangesW API.
 */
public class ReadDirectoryChangesWatcher private constructor(
    private val eventHandler: EventHandler,
    private var config: Config,
) : Watcher {
    private val server = ReadDirectoryChangesServer.create(eventHandler)

    override fun watch(path: String, recursiveMode: RecursiveMode): Result<Unit> =
        watchInner(path, recursiveMode)

    override fun unwatch(path: String): Result<Unit> =
        unwatchInner(path)

    public fun watchInner(path: String, recursiveMode: RecursiveMode): Result<Unit> {
        val isRecursive = recursiveMode == RecursiveMode.Recursive
        return server.sendActionRequireAck(Action.Watch(path, isRecursive))
    }

    public fun unwatchInner(path: String): Result<Unit> {
        return server.sendActionRequireAck(Action.Unwatch(path))
    }

    public fun drop() {
        server.sendActionRequireAck(Action.Stop)
    }

    override fun configure(option: Config): Result<Boolean> {
        this.config = option
        return Result.success(true)
    }

    override fun kind(): WatcherKind = WatcherKind.ReadDirectoryChangesWatcher

    public companion object {
        public fun new(eventHandler: EventHandler, config: Config): Result<ReadDirectoryChangesWatcher> =
            Result.success(ReadDirectoryChangesWatcher(eventHandler, config))

        public fun new(eventHandler: (Result<Event>) -> Unit, config: Config): Result<ReadDirectoryChangesWatcher> =
            new(EventHandler { eventHandler(it) }, config)

        public fun new(eventHandler: EventHandler): Result<ReadDirectoryChangesWatcher> =
            new(eventHandler, Config.default())

        public fun new(eventHandler: (Result<Event>) -> Unit): Result<ReadDirectoryChangesWatcher> =
            new(EventHandler { eventHandler(it) })

        public fun kind(): WatcherKind = WatcherKind.ReadDirectoryChangesWatcher
    }
}
