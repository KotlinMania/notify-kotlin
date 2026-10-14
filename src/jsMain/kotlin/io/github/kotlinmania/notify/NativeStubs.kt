// port-lint: stub actual — JS has no FSEvents or inotify FFI
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

public actual class InotifyFdHandle actual constructor()

public actual object NativeInotify {
    public actual fun init(): InotifyFdHandle? = null
    public actual fun addWatch(fd: InotifyFdHandle, path: String, mask: UInt): Int = -1
    public actual fun removeWatch(fd: InotifyFdHandle, wd: Int): Boolean = false
    public actual fun readEvents(fd: InotifyFdHandle): List<InotifyEvent> = emptyList()
    public actual fun close(fd: InotifyFdHandle) {}
    public actual fun translateMask(mask: UInt): EventKind = EventKind.Other
}