// port-lint: stub actual — Apple platforms have no inotify (they use FSEvents instead)
package io.github.kotlinmania.notify

public actual class InotifyFdHandle actual constructor()

public actual object NativeInotify {
    public actual fun init(): InotifyFdHandle? = null

    public actual fun addWatch(fd: InotifyFdHandle, path: String, mask: UInt): Int = -1

    public actual fun removeWatch(fd: InotifyFdHandle, wd: Int): Boolean = false

    public actual fun readEvents(fd: InotifyFdHandle): List<InotifyEvent> = emptyList()

    public actual fun close(fd: InotifyFdHandle) {}

    public actual fun translateMask(mask: UInt): EventKind = EventKind.Other
}
