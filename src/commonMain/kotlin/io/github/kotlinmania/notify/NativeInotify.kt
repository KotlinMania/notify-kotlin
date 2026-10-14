// port-lint: source inotify.rs (expect/actual boundary for inotify FFI)
package io.github.kotlinmania.notify

/**
 * Platform-native inotify file descriptor handle.
 * On Linux this wraps an `inotify_init1` fd; on non-Linux it is unused.
 */
public expect class InotifyFdHandle()

/**
 * inotify event mask constants, mapped from `<sys/inotify.h>`.
 */
public object InotifyEventMask {
    public const val ACCESS: UInt = 0x00000001u
    public const val MODIFY: UInt = 0x00000002u
    public const val ATTRIB: UInt = 0x00000004u
    public const val CLOSE_WRITE: UInt = 0x00000008u
    public const val CLOSE_NOWRITE: UInt = 0x00000010u
    public const val OPEN: UInt = 0x00000020u
    public const val MOVED_FROM: UInt = 0x00000040u
    public const val MOVED_TO: UInt = 0x00000080u
    public const val CREATE: UInt = 0x00000100u
    public const val DELETE: UInt = 0x00000200u
    public const val DELETE_SELF: UInt = 0x00000400u
    public const val MOVE_SELF: UInt = 0x00000800u
    public const val UNMOUNT: UInt = 0x00002000u
    public const val Q_OVERFLOW: UInt = 0x00004000u
    public const val IGNORED: UInt = 0x00008000u
    public const val ISDIR: UInt = 0x40000000u
}

/**
 * inotify watch mask constants.
 */
public object InotifyWatchMask {
    public const val ACCESS: UInt = 0x00000001u
    public const val ATTRIB: UInt = 0x00000004u
    public const val CLOSE_WRITE: UInt = 0x00000008u
    public const val CLOSE_NOWRITE: UInt = 0x00000010u
    public const val CREATE: UInt = 0x00000100u
    public const val DELETE: UInt = 0x00000200u
    public const val DELETE_SELF: UInt = 0x00000400u
    public const val MODIFY: UInt = 0x00000002u
    public const val MOVE_SELF: UInt = 0x00000800u
    public const val MOVED_FROM: UInt = 0x00000040u
    public const val MOVED_TO: UInt = 0x00000080u
    public const val OPEN: UInt = 0x00000020u
    public const val ALL_EVENTS: UInt = 0x00000FFFu
}

/**
 * A single inotify event read from the fd.
 */
public data class InotifyEvent(
    public val wd: Int,
    public val mask: UInt,
    public val cookie: UInt,
    public val name: String,
)

/**
 * Linux `errno` values used by inotify.
 */
public object LinuxErrno {
    public const val ENOSPC: Int = 28
    public const val EMFILE: Int = 24
    public const val ENFILE: Int = 23
}

/**
 * Platform-native inotify operations, behind an expect/actual boundary.
 *
 * On Linux, the actuals call `inotify_init1`, `inotify_add_watch`,
 * `inotify_rm_watch`, and `read` via cinterop with `<sys/inotify.h>`.
 * On non-Linux, the actuals are no-ops or throw — inotify is Linux-only.
 */
public expect object NativeInotify {
    /**
     * Create a new inotify instance (`inotify_init1(IN_NONBLOCK)`).
     * Returns a file descriptor handle, or null on failure.
     */
    public fun init(): InotifyFdHandle?

    /**
     * Add a watch for [path] with the given [mask].
     * Returns a watch descriptor (positive int), or -1 on failure.
     */
    public fun addWatch(fd: InotifyFdHandle, path: String, mask: UInt): Int

    /**
     * Remove a watch by descriptor.
     * Returns true on success.
     */
    public fun removeWatch(fd: InotifyFdHandle, wd: Int): Boolean

    /**
     * Read pending events from the inotify fd.
     * Returns a list of [InotifyEvent], or empty if no events available.
     */
    public fun readEvents(fd: InotifyFdHandle): List<InotifyEvent>

    /**
     * Close the inotify file descriptor.
     */
    public fun close(fd: InotifyFdHandle)

    /**
     * Translate inotify event mask into [EventKind].
     */
    public fun translateMask(mask: UInt): EventKind
}
