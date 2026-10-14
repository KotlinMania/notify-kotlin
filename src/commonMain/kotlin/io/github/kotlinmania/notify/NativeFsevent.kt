// port-lint: source fsevent.rs (expect/actual boundary for FSEvents FFI)
package io.github.kotlinmania.notify

/**
 * Platform-native FSEvents stream handle.
 * On Darwin this wraps a `FSEventStreamRef`; on non-Darwin it is unused.
 */
public expect class FseventStreamHandle()

/**
 * Platform-native CFMutableArray handle for paths.
 * On Darwin this wraps a `CFMutableArrayRef`; on non-Darwin it is unused.
 */
public expect class FseventPathsArrayHandle()

/**
 * Platform-native CFRunLoop handle.
 * On Darwin this wraps a `CFRunLoopRef`; on non-Darwin it is unused.
 */
public expect class FseventRunLoopHandle()

/**
 * Semantic FSEvents stream flags, mapped from the C constants.
 * Values mirror the upstream `FSEventStreamEventFlags` constants.
 */
public object FseventStreamFlags {
    public const val NONE: UInt = 0x00000000u
    public const val MUST_SCAN_SUBDIRS: UInt = 0x00000001u
    public const val USER_DROPPED: UInt = 0x00000002u
    public const val KERNEL_DROPPED: UInt = 0x00000004u
    public const val IDS_WRAPPED: UInt = 0x00000008u
    public const val HISTORY_DONE: UInt = 0x00000010u
    public const val ROOT_CHANGED: UInt = 0x00000020u
    public const val MOUNT: UInt = 0x00000040u
    public const val UNMOUNT: UInt = 0x00000080u
    public const val ITEM_CREATED: UInt = 0x00000100u
    public const val ITEM_REMOVED: UInt = 0x00000200u
    public const val INODE_META_MOD: UInt = 0x00000400u
    public const val ITEM_RENAMED: UInt = 0x00000800u
    public const val ITEM_MODIFIED: UInt = 0x00001000u
    public const val FINDER_INFO_MOD: UInt = 0x00002000u
    public const val ITEM_CHANGE_OWNER: UInt = 0x00004000u
    public const val ITEM_XATTR_MOD: UInt = 0x00008000u
    public const val IS_FILE: UInt = 0x00010000u
    public const val IS_DIR: UInt = 0x00020000u
    public const val IS_SYMLINK: UInt = 0x00040000u
    public const val OWN_EVENT: UInt = 0x00080000u
    public const val IS_HARDLINK: UInt = 0x00100000u
    public const val IS_LAST_HARDLINK: UInt = 0x00200000u
    public const val ITEM_CLONED: UInt = 0x00400000u
}

/**
 * FSEvents stream creation flags.
 * Values mirror the upstream `FSEventStreamCreateFlags` constants.
 */
public object FseventCreateFlags {
    public const val NONE: UInt = 0x00000000u
    public const val WATCH_ROOT: UInt = 0x00000004u
    public const val IGNORE_SELF: UInt = 0x00000008u
    public const val FILE_EVENTS: UInt = 0x00000010u
    public const val NO_DEFER: UInt = 0x00000020u
}

/**
 * FSEventStreamEventId — `kFSEventStreamEventIdSinceNow` is `0xFFFFFFFFFFFFFFFF`.
 */
public const val FSEVENT_STREAM_EVENT_ID_SINCE_NOW: ULong = 0xFFFFFFFFFFFFFFFFu

/**
 * Platform-native FSEvents operations, behind an expect/actual boundary.
 *
 * On Darwin, the actuals call CoreFoundation/CoreServices C functions via cinterop.
 * On non-Darwin, the actuals are no-ops or throw — FSEvents is Apple-only.
 *
 * The common FseventWatcher calls these to manage the native stream lifecycle.
 */
public expect object NativeFsevent {
    /**
     * Create a new empty CFMutableArray for paths.
     * Returns a handle that must be released with [releasePathsArray].
     */
    public fun createPathsArray(): FseventPathsArrayHandle

    /**
     * Append a path string to the paths array.
     * Returns false if the path could not be converted to a CFString.
     */
    public fun appendPath(paths: FseventPathsArrayHandle, path: String): Boolean

    /**
     * Remove a path string from the paths array (case-insensitive).
     * Returns the number of removed entries.
     */
    public fun removePath(paths: FseventPathsArrayHandle, path: String): Int

    /**
     * Get the number of paths in the array.
     */
    public fun pathCount(paths: FseventPathsArrayHandle): Int

    /**
     * Release the paths array (CFRelease).
     */
    public fun releasePathsArray(paths: FseventPathsArrayHandle)

    /**
     * Create an FSEventStream watching the given paths array.
     * The [callback] is invoked for each event with (flags, path) pairs.
     * Returns a stream handle, or null if creation failed.
     */
    public fun createStream(
        paths: FseventPathsArrayHandle,
        sinceWhen: ULong,
        latency: Double,
        flags: UInt,
        callback: (flags: UInt, path: String) -> Unit,
    ): FseventStreamHandle?

    /**
     * Schedule the stream on a run loop, start it, and return the run loop handle.
     * The run loop runs on the calling thread — this blocks until [stopStream] is called.
     */
    public fun startStream(stream: FseventStreamHandle): FseventRunLoopHandle

    /**
     * Stop the stream and its run loop, then release all native resources.
     */
    public fun stopStream(stream: FseventStreamHandle, runLoop: FseventRunLoopHandle)

    /**
     * Translate FSEvents stream flags into [EventKind] values.
     */
    public fun translateFlags(flags: UInt, precise: Boolean): List<EventKind>
}
