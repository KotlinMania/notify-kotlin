// port-lint: source inotify.rs (real inotify FFI via platform.linux)
// Actual for all Linux targets (linuxX64, linuxArm64).
// Uses the built-in Kotlin/Native platform library for Linux, which provides
// inotify functions via platform.linux.
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.kotlinmania.notify

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.*

/**
 * Wraps an inotify file descriptor as a Kotlin handle.
 */
public actual class InotifyFdHandle {
    internal var fd: Int = -1
}

// IN_NONBLOCK = O_NONBLOCK on Linux = 0o4000 = 2048.
private const val IN_NONBLOCK: Int = 2048

public actual object NativeInotify {
    public actual fun init(): InotifyFdHandle? {
        val fd = inotify_init1(IN_NONBLOCK)
        if (fd < 0) return null
        val handle = InotifyFdHandle()
        handle.fd = fd
        return handle
    }

    public actual fun addWatch(fd: InotifyFdHandle, path: String, mask: UInt): Int {
        return path.encodeToByteArray().usePinned { pinnedPath ->
            inotify_add_watch(fd.fd, pinnedPath.addressOf(0), mask)
        }
    }

    public actual fun removeWatch(fd: InotifyFdHandle, wd: Int): Boolean {
        val result = inotify_rm_watch(fd.fd, wd)
        return result == 0
    }

    public actual fun readEvents(fd: InotifyFdHandle): List<InotifyEvent> {
        val bufferSize = 4096
        val events = mutableListOf<InotifyEvent>()

        memScoped {
            val buffer = allocArray<ByteVar>(bufferSize)
            val bytesRead = read(fd.fd, buffer, bufferSize.toULong())

            if (bytesRead <= 0) return emptyList()

            val bufferPtr: CPointer<ByteVar> = buffer.getPointer(this)
            var offset = 0
            val structSize = sizeOf<inotify_event>().toInt()
            while (offset < bytesRead.toInt()) {
                // Cast the buffer at the current offset to an inotify_event pointer.
                @Suppress("UNCHECKED_CAST")
                val eventPtr = (bufferPtr + offset) as CPointer<inotify_event>
                val event = eventPtr.pointed

                val nameBytes = event.len.toInt()
                val name = if (nameBytes > 0) {
                    // The name field starts right after the fixed fields of inotify_event.
                    val nameStart = offset + structSize
                    val nameEnd = nameStart + nameBytes
                    var nameLen = 0
                    while (nameStart + nameLen < nameEnd && buffer[nameStart + nameLen].toInt() != 0) {
                        nameLen++
                    }
                    if (nameLen > 0) {
                        (bufferPtr + nameStart)!!.reinterpret<ByteVar>().toKString()
                    } else {
                        ""
                    }
                } else {
                    ""
                }

                events.add(
                    InotifyEvent(
                        wd = event.wd,
                        mask = event.mask,
                        cookie = event.cookie,
                        name = name,
                    ),
                )

                // Advance past the fixed part + the name (including NUL padding).
                offset += structSize + nameBytes
            }
        }

        return events
    }

    public actual fun close(fd: InotifyFdHandle) {
        if (fd.fd >= 0) {
            platform.posix.close(fd.fd)
            fd.fd = -1
        }
    }

    public actual fun translateMask(mask: UInt): EventKind {
        // Match in the order the upstream Rust code checks.
        if (mask and InotifyEventMask.CREATE != 0u) return EventKind.Create(CreateKind.Any)
        if (mask and InotifyEventMask.DELETE != 0u) return EventKind.Remove(RemoveKind.Any)
        if (mask and InotifyEventMask.DELETE_SELF != 0u) return EventKind.Remove(RemoveKind.Any)
        if (mask and InotifyEventMask.MOVED_FROM != 0u) return EventKind.Remove(RemoveKind.Any)
        if (mask and InotifyEventMask.MOVED_TO != 0u) return EventKind.Create(CreateKind.Any)
        if (mask and InotifyEventMask.MOVE_SELF != 0u) return EventKind.Modify(ModifyKind.Name(RenameMode.Both))
        if (mask and InotifyEventMask.MODIFY != 0u) return EventKind.Modify(ModifyKind.Data(DataChange.Any))
        if (mask and InotifyEventMask.ATTRIB != 0u) return EventKind.Modify(ModifyKind.Metadata(MetadataKind.Any))
        return EventKind.Other
    }
}