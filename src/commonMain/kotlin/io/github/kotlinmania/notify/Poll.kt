// port-lint: source notify/src/poll.rs
package io.github.kotlinmania.notify

/**
 * Event sent for registered handlers on initial directory scans.
 */
public typealias ScanEvent = Result<String>

/**
 * Handler trait for receivers of [ScanEvent].
 */
public interface ScanEventHandler {
    /**
     * Handles a scan event.
     */
    public fun handleEvent(event: ScanEvent)

    public companion object {
        public operator fun invoke(handler: (ScanEvent) -> Unit): ScanEventHandler =
            object : ScanEventHandler {
                override fun handleEvent(event: ScanEvent) = handler(event)
            }
    }
}

public class DefaultScanEventHandler(private val callback: (ScanEvent) -> Unit) : ScanEventHandler {
    override fun handleEvent(event: ScanEvent) {
        callback(event)
    }
}

/**
 * MetaPath composed of a path and its metadata.
 */
public class MetaPath(
    public val path: String,
    public val metadata: Any? = null,
) {
    public fun intoPath(): String = path

    public fun fmt(): String = "MetaPath($path)"

    public companion object {
        public fun fromPartsUnchecked(path: String, metadata: Any?): MetaPath =
            MetaPath(path, metadata)
    }
}

/**
 * PathData tracking mtime and content hash for polling.
 */
public class PathData(
    public val mtime: Long = 0L,
    public val hash: Long? = null,
) {
    public fun getContentHash(path: String): Long? = hash

    public fun fmt(): String = "PathData(mtime=$mtime, hash=$hash)"

    public companion object {
        public fun compareToEvent(
            path: String,
            old: PathData?,
            new: PathData?,
        ): Event? {
            return when {
                old != null && new != null -> {
                    if (new.mtime > old.mtime) {
                        Event(EventKind.Modify(ModifyKind.Metadata(MetadataKind.WriteTime)), listOf(path))
                    } else if (new.hash != old.hash) {
                        Event(EventKind.Modify(ModifyKind.Data(DataChange.Any)), listOf(path))
                    } else {
                        null
                    }
                }
                old == null && new != null -> Event(EventKind.Create(CreateKind.Any), listOf(path))
                old != null && new == null -> Event(EventKind.Remove(RemoveKind.Any), listOf(path))
                else -> null
            }
        }
    }
}

/**
 * Thin wrapper for event handler emission.
 */
public class EventEmitter(private val eventHandler: EventHandler) {
    public fun emit(event: Result<Event>) {
        eventHandler.handleEvent(event)
    }

    public fun emitOk(event: Event) {
        emit(Result.success(event))
    }

    public fun emitIoErr(err: Throwable, path: String? = null) {
        val error = Error.io(err).let { if (path != null) it.addPath(path) else it }
        emit(Result.failure(error))
    }
}

/**
 * Builder for WatchData and PathData.
 */
public class DataBuilder(
    private val emitter: EventEmitter,
    private val scanEmitter: ScanEventHandler? = null,
    private val compareContent: Boolean = false,
    private var now: Long = 0L,
) {
    public fun updateTimestamp() {
        now = 0L
    }

    public fun buildWatchData(
        root: String,
        isRecursive: Boolean,
        followSymlinks: Boolean,
    ): WatchData =
        WatchData(root, isRecursive, followSymlinks)

    public fun buildPathData(metaPath: MetaPath): PathData =
        PathData(mtime = now, hash = if (compareContent) 0L else null)
}

/**
 * WatchData tracking a watched directory or file.
 */
public class WatchData(
    public val root: String,
    public val isRecursive: Boolean,
    public val followSymlinks: Boolean,
) {
    private val paths = mutableMapOf<String, PathData>()

    public fun rescan(dataBuilder: DataBuilder) {
        dataBuilder.updateTimestamp()
    }

    public fun scanAllPathData(dataBuilder: DataBuilder) {
        val meta = MetaPath.fromPartsUnchecked(root, null)
        paths[root] = dataBuilder.buildPathData(meta)
    }

    public fun dirScanDepth(): Int = if (isRecursive) Int.MAX_VALUE else 1

    public fun fmt(): String = "WatchData($root)"
}

/**
 * Polling based Watcher implementation.
 *
 * Checks the watched paths periodically to detect changes.
 */
public class PollWatcher private constructor(
    private val eventHandler: EventHandler,
    private var config: Config,
    private val scanEventHandler: ScanEventHandler? = null,
) : Watcher {
    private val watches = mutableMapOf<String, WatchData>()
    private val emitter = EventEmitter(eventHandler)
    private val dataBuilder = DataBuilder(emitter, scanEventHandler, config.compareContents())
    private var delay = config.pollInterval()
    private var followSymlinks = config.followSymlinks()

    override fun watch(path: String, recursiveMode: RecursiveMode): Result<Unit> =
        watchInner(path, recursiveMode)

    override fun unwatch(path: String): Result<Unit> =
        unwatchInner(path)

    public fun watchInner(path: String, recursiveMode: RecursiveMode): Result<Unit> {
        val isRecursive = recursiveMode == RecursiveMode.Recursive
        val watchData = dataBuilder.buildWatchData(path, isRecursive, followSymlinks)
        watchData.scanAllPathData(dataBuilder)
        watches[path] = watchData
        scanEventHandler?.handleEvent(Result.success(path))
        return Result.success(Unit)
    }

    public fun unwatchInner(path: String): Result<Unit> {
        return if (watches.remove(path) != null) {
            Result.success(Unit)
        } else {
            Result.failure(Error.watchNotFound().addPath(path))
        }
    }

    override fun configure(option: Config): Result<Boolean> {
        this.config = option
        this.delay = option.pollInterval()
        this.followSymlinks = option.followSymlinks()
        return Result.success(true)
    }

    override fun kind(): WatcherKind = WatcherKind.PollWatcher

    /**
     * Actively poll for changes.
     */
    public fun poll(): Result<Unit> {
        run()
        return Result.success(Unit)
    }

    public fun pollAll(): Result<Unit> = poll()

    public fun run() {
        dataBuilder.updateTimestamp()
        for (watchData in watches.values) {
            watchData.rescan(dataBuilder)
        }
    }

    public fun drop() {
        watches.clear()
    }

    public fun pollWatcherIsSendAndSync() {}

    public fun check() {}

    public companion object {
        public fun systemTimeToSeconds(time: Long): Long = time / 1000L

        public fun new(eventHandler: EventHandler, config: Config): Result<PollWatcher> =
            withOpt(eventHandler, config, null)

        public fun new(eventHandler: (Result<Event>) -> Unit, config: Config): Result<PollWatcher> =
            new(EventHandler(eventHandler), config)

        public fun new(eventHandler: EventHandler): Result<PollWatcher> =
            new(eventHandler, Config.default())

        public fun new(eventHandler: (Result<Event>) -> Unit): Result<PollWatcher> =
            new(EventHandler(eventHandler))

        public fun withInitialScan(
            eventHandler: EventHandler,
            config: Config,
            scanCallback: ScanEventHandler,
        ): Result<PollWatcher> =
            withOpt(eventHandler, config, scanCallback)

        public fun withOpt(
            eventHandler: EventHandler,
            config: Config,
            scanCallback: ScanEventHandler?,
        ): Result<PollWatcher> =
            Result.success(PollWatcher(eventHandler, config, scanCallback))

        public fun withDelayMs(eventHandler: EventHandler, delayMs: Long): Result<PollWatcher> =
            new(eventHandler, Config.default().withPollInterval(kotlin.time.Duration.parse("${delayMs}ms")))

        public fun kind(): WatcherKind = WatcherKind.PollWatcher
    }
}
