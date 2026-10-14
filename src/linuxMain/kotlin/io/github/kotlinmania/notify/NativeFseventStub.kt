// port-lint: stub actual — Linux has no FSEvents (it uses inotify instead)
package io.github.kotlinmania.notify

public actual class FseventStreamHandle actual constructor()

public actual class FseventPathsArrayHandle actual constructor()

public actual class FseventRunLoopHandle actual constructor()

public actual object NativeFsevent {
    public actual fun createPathsArray(): FseventPathsArrayHandle = FseventPathsArrayHandle()

    public actual fun appendPath(paths: FseventPathsArrayHandle, path: String): Boolean = false

    public actual fun removePath(paths: FseventPathsArrayHandle, path: String): Int = 0

    public actual fun pathCount(paths: FseventPathsArrayHandle): Int = 0

    public actual fun releasePathsArray(paths: FseventPathsArrayHandle) {}

    public actual fun createStream(
        paths: FseventPathsArrayHandle,
        sinceWhen: ULong,
        latency: Double,
        flags: UInt,
        callback: (flags: UInt, path: String) -> Unit,
    ): FseventStreamHandle? = null

    public actual fun startStream(stream: FseventStreamHandle): FseventRunLoopHandle = FseventRunLoopHandle()

    public actual fun stopStream(stream: FseventStreamHandle, runLoop: FseventRunLoopHandle) {}

    public actual fun translateFlags(flags: UInt, precise: Boolean): List<EventKind> = emptyList()
}
