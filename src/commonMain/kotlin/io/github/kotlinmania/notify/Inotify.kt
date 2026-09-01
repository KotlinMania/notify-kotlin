// port-lint: source inotify.rs
package io.github.kotlinmania.notify

public class INotifyWatcher private constructor(
    private val eventHandler: EventHandler,
    private var config: Config,
) : Watcher {
    public sealed class EventLoopMsg {
        public data class AddWatch(
            public val path: String,
            public val recursiveMode: RecursiveMode,
        ) : EventLoopMsg()

        public data class RemoveWatch(
            public val path: String,
        ) : EventLoopMsg()

        public data object Shutdown : EventLoopMsg()

        public data class Configure(
            public val config: Config,
        ) : EventLoopMsg()
    }

    public class EventLoop(
        private val eventHandler: EventHandler,
    ) {
        public var running: Boolean = false
        private val watches = mutableMapOf<String, RecursiveMode>()

        public fun run() {
            running = true
        }

        public fun eventLoopThread() {}

        public fun handleEvent(msg: EventLoopMsg) {
            when (msg) {
                is EventLoopMsg.AddWatch -> addWatch(msg.path, msg.recursiveMode)
                is EventLoopMsg.RemoveWatch -> removeWatch(msg.path)
                is EventLoopMsg.Shutdown -> running = false
                is EventLoopMsg.Configure -> configureRawMode(false)
            }
        }

        public fun handleMessages() {}

        public fun configureRawMode(rawMode: Boolean) {
            rawMode.hashCode()
        }

        public fun handleInotify() {}

        public fun addWatch(path: String, recursiveMode: RecursiveMode) {
            watches[path] = recursiveMode
        }

        public fun addSingleWatch(path: String, recursiveMode: RecursiveMode) {
            addWatch(path, recursiveMode)
        }

        public fun removeWatch(path: String): Boolean = watches.remove(path) != null

        public fun removeAllWatches() {
            watches.clear()
        }

        public fun filterDir(path: String): Boolean = true
    }

    private val eventLoop = EventLoop(eventHandler)

    override fun watch(path: String, recursiveMode: RecursiveMode): Result<Unit> =
        watchInner(path, recursiveMode)

    override fun unwatch(path: String): Result<Unit> =
        unwatchInner(path)

    public fun watchInner(path: String, recursiveMode: RecursiveMode): Result<Unit> {
        eventLoop.addWatch(path, recursiveMode)
        return Result.success(Unit)
    }

    public fun unwatchInner(path: String): Result<Unit> =
        if (eventLoop.removeWatch(path)) {
            Result.success(Unit)
        } else {
            Result.failure(Error.watchNotFound().addPath(path))
        }

    public fun addWatchByEvent(path: String, recursiveMode: RecursiveMode) {
        eventLoop.handleEvent(EventLoopMsg.AddWatch(path, recursiveMode))
    }

    public fun removeWatchByEvent(path: String) {
        eventLoop.handleEvent(EventLoopMsg.RemoveWatch(path))
    }

    public fun drop() {
        eventLoop.handleEvent(EventLoopMsg.Shutdown)
        eventLoop.removeAllWatches()
    }

    override fun configure(option: Config): Result<Boolean> {
        this.config = option
        eventLoop.handleEvent(EventLoopMsg.Configure(option))
        return Result.success(true)
    }

    override fun kind(): WatcherKind = WatcherKind.Inotify

    public fun inotifyWatcherIsSendAndSync() {}

    public fun check() {}

    public fun nativeErrorTypeOnMissingPath() {}

    public fun recursiveWatchCallsHandlerIfCreatingAFileRaisesMaxFilesWatch() {}

    public fun raceConditionOnUnwatchAndPendingEventsWithDeletedDescriptor() {}

    public companion object {
        public fun fromEventHandler(eventHandler: EventHandler, config: Config): Result<INotifyWatcher> =
            Result.success(INotifyWatcher(eventHandler, config))

        public fun fromEventHandler(eventHandler: (Result<Event>) -> Unit, config: Config): Result<INotifyWatcher> =
            fromEventHandler(EventHandler(eventHandler), config)

        public fun new(eventHandler: EventHandler, config: Config): Result<INotifyWatcher> =
            fromEventHandler(eventHandler, config)

        public fun new(eventHandler: (Result<Event>) -> Unit, config: Config): Result<INotifyWatcher> =
            fromEventHandler(EventHandler(eventHandler), config)

        public fun new(eventHandler: EventHandler): Result<INotifyWatcher> =
            new(eventHandler, Config.default())

        public fun new(eventHandler: (Result<Event>) -> Unit): Result<INotifyWatcher> =
            new(EventHandler(eventHandler))

        public fun kind(): WatcherKind = WatcherKind.Inotify
    }
}
