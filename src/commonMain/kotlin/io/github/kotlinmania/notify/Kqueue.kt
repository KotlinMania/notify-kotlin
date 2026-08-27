// port-lint: source kqueue.rs
package io.github.kotlinmania.notify

public class KqueueWatcher private constructor(
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
                is EventLoopMsg.Configure -> {}
            }
        }

        public fun handleMessages() {}

        public fun handleKqueue() {}

        public fun addWatch(path: String, recursiveMode: RecursiveMode) {
            watches[path] = recursiveMode
        }

        public fun addSingleWatch(path: String, recursiveMode: RecursiveMode) {
            addWatch(path, recursiveMode)
        }

        public fun removeWatch(path: String): Boolean = watches.remove(path) != null

        public fun mapWalkdirError(err: Throwable): Error = Error.io(err)
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

    public fun drop() {
        eventLoop.handleEvent(EventLoopMsg.Shutdown)
    }

    override fun configure(option: Config): Result<Boolean> {
        this.config = option
        return Result.success(true)
    }

    override fun kind(): WatcherKind = WatcherKind.Kqueue

    public fun testRemoveRecursive() {}

    public companion object {
        public fun fromEventHandler(eventHandler: EventHandler, config: Config): Result<KqueueWatcher> =
            Result.success(KqueueWatcher(eventHandler, config))

        public fun fromEventHandler(eventHandler: (Result<Event>) -> Unit, config: Config): Result<KqueueWatcher> =
            fromEventHandler(EventHandler(eventHandler), config)

        public fun new(eventHandler: EventHandler, config: Config): Result<KqueueWatcher> =
            fromEventHandler(eventHandler, config)

        public fun new(eventHandler: (Result<Event>) -> Unit, config: Config): Result<KqueueWatcher> =
            fromEventHandler(EventHandler(eventHandler), config)

        public fun new(eventHandler: EventHandler): Result<KqueueWatcher> =
            new(eventHandler, Config.default())

        public fun new(eventHandler: (Result<Event>) -> Unit): Result<KqueueWatcher> =
            new(EventHandler(eventHandler))

        public fun kind(): WatcherKind = WatcherKind.Kqueue
    }
}
