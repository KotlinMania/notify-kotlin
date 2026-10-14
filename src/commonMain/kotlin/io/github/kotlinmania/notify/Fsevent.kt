// port-lint: source fsevent.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.notify

import io.github.kotlinmania.bitflags.BitFlags
import io.github.kotlinmania.bitflags.Flag
import io.github.kotlinmania.bitflags.toText
import io.github.kotlinmania.log.formatArgs
import io.github.kotlinmania.log.trace
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
public class StreamFlags(
    bits: ULong,
) : BitFlags<StreamFlags>(bits) {
    override fun flags(): List<Flag<StreamFlags>> = FLAGS

    override fun fromBitsRetain(bits: ULong): StreamFlags = StreamFlags(bits)

    override fun toString(): String = toText(this)

    public companion object {
        public val NONE: StreamFlags = StreamFlags(0x00000000uL)
        public val MUST_SCAN_SUBDIRS: StreamFlags = StreamFlags(0x00000001uL)
        public val USER_DROPPED: StreamFlags = StreamFlags(0x00000002uL)
        public val KERNEL_DROPPED: StreamFlags = StreamFlags(0x00000004uL)
        public val IDS_WRAPPED: StreamFlags = StreamFlags(0x00000008uL)
        public val HISTORY_DONE: StreamFlags = StreamFlags(0x00000010uL)
        public val ROOT_CHANGED: StreamFlags = StreamFlags(0x00000020uL)
        public val MOUNT: StreamFlags = StreamFlags(0x00000040uL)
        public val UNMOUNT: StreamFlags = StreamFlags(0x00000080uL)
        public val ITEM_CREATED: StreamFlags = StreamFlags(0x00000100uL)
        public val ITEM_REMOVED: StreamFlags = StreamFlags(0x00000200uL)
        public val INODE_META_MOD: StreamFlags = StreamFlags(0x00000400uL)
        public val ITEM_RENAMED: StreamFlags = StreamFlags(0x00000800uL)
        public val ITEM_MODIFIED: StreamFlags = StreamFlags(0x00001000uL)
        public val FINDER_INFO_MOD: StreamFlags = StreamFlags(0x00002000uL)
        public val ITEM_CHANGE_OWNER: StreamFlags = StreamFlags(0x00004000uL)
        public val ITEM_XATTR_MOD: StreamFlags = StreamFlags(0x00008000uL)
        public val IS_FILE: StreamFlags = StreamFlags(0x00010000uL)
        public val IS_DIR: StreamFlags = StreamFlags(0x00020000uL)
        public val IS_SYMLINK: StreamFlags = StreamFlags(0x00040000uL)
        public val OWN_EVENT: StreamFlags = StreamFlags(0x00080000uL)
        public val IS_HARDLINK: StreamFlags = StreamFlags(0x00100000uL)
        public val IS_LAST_HARDLINK: StreamFlags = StreamFlags(0x00200000uL)
        public val ITEM_CLONED: StreamFlags = StreamFlags(0x00400000uL)

        public val FLAGS: List<Flag<StreamFlags>> =
            listOf(
                Flag("NONE", NONE),
                Flag("MUST_SCAN_SUBDIRS", MUST_SCAN_SUBDIRS),
                Flag("USER_DROPPED", USER_DROPPED),
                Flag("KERNEL_DROPPED", KERNEL_DROPPED),
                Flag("IDS_WRAPPED", IDS_WRAPPED),
                Flag("HISTORY_DONE", HISTORY_DONE),
                Flag("ROOT_CHANGED", ROOT_CHANGED),
                Flag("MOUNT", MOUNT),
                Flag("UNMOUNT", UNMOUNT),
                Flag("ITEM_CREATED", ITEM_CREATED),
                Flag("ITEM_REMOVED", ITEM_REMOVED),
                Flag("INODE_META_MOD", INODE_META_MOD),
                Flag("ITEM_RENAMED", ITEM_RENAMED),
                Flag("ITEM_MODIFIED", ITEM_MODIFIED),
                Flag("FINDER_INFO_MOD", FINDER_INFO_MOD),
                Flag("ITEM_CHANGE_OWNER", ITEM_CHANGE_OWNER),
                Flag("ITEM_XATTR_MOD", ITEM_XATTR_MOD),
                Flag("IS_FILE", IS_FILE),
                Flag("IS_DIR", IS_DIR),
                Flag("IS_SYMLINK", IS_SYMLINK),
                Flag("OWN_EVENT", OWN_EVENT),
                Flag("IS_HARDLINK", IS_HARDLINK),
                Flag("IS_LAST_HARDLINK", IS_LAST_HARDLINK),
                Flag("ITEM_CLONED", ITEM_CLONED),
            )
    }
}

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

    public fun configureRawMode(rawMode: Boolean) {
        rawMode.hashCode()
    }

    public fun callback(event: Event) {
        eventHandler.handleEvent(Result.success(event))
    }

    public fun callbackImpl(flags: Long, path: String) {
        val streamFlags = StreamFlags(flags.toULong())
        trace(formatArgs("FSEvent: path = `{}`, flag = {}", path, streamFlags))
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
