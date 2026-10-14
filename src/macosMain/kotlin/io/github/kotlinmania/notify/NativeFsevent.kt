// port-lint: source fsevent.rs (real FSEvents FFI via cinterop with CoreServices/CoreFoundation)
// Actual for all Darwin targets (macOS, iOS, tvOS, watchOS — all have FSEvents API).
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.kotlinmania.notify

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.CoreServices.*

/**
 * Wraps a CoreFoundation `FSEventStreamRef` as a Kotlin handle.
 */
public actual class FseventStreamHandle {
    internal var streamRef: FSEventStreamRef? = null
}

public actual class FseventPathsArrayHandle {
    internal var arrayRef: CFMutableArrayRef? = null
}

public actual class FseventRunLoopHandle {
    internal var runLoopRef: CFRunLoopRef? = null
}

/**
 * FSEvents callback context — bridges C callback to Kotlin lambda.
 */
private class FseventCallbackContext(
    val callback: (flags: UInt, path: String) -> Unit,
)

/**
 * FSEvents C callback trampoline — invoked by CoreFoundation.
 * Signature matches `FSEventStreamCallback` in <CoreServices/FSEvents.h>.
 */
@OptIn(ExperimentalForeignApi::class)
private fun fseventCallbackTrampoline(
    streamRef: FSEventStreamRef?,
    info: CPointer<out CPointed>?,
    numEvents: ULong,
    eventPaths: CPointer<out CPointed>?,
    eventFlags: CPointer<UIntVarOf<UInt>>?,
    eventIds: CPointer<ULongVarOf<ULong>>?,
) {
    val context = info?.asStableRef<FseventCallbackContext>()?.get() ?: return
    if (eventPaths == null || eventFlags == null) return

    val pathArrayRef = eventPaths.reinterpret<CPointerVarOf<CFArrayRef>>()

    for (i in 0 until numEvents.toInt()) {
        val pathArray = pathArrayRef[i]
        if (pathArray == null) continue
        val pathObj = CFArrayGetValueAtIndex(pathArray, i.toLong())
        if (pathObj == null) continue
        val path = pathObj.toKStringFromCFString() ?: continue
        val flags = eventFlags[i]
        context.callback(flags, path)
    }
}

/**
 * Convert a CFStringRef-like opaque pointer to a Kotlin String.
 */
@OptIn(ExperimentalForeignApi::class)
private fun COpaquePointer?.toKStringFromCFString(): String? {
    if (this == null) return null
    @Suppress("UNCHECKED_CAST")
    val cfString = this as CFStringRef
    val length = CFStringGetLength(cfString)
    if (length.toInt() == 0) return ""
    val maxBuf = CFStringGetMaximumSizeForEncoding(length, kCFStringEncodingUTF8) + 1
    memScoped {
        val bytes = allocArray<ByteVar>(maxBuf.toInt())
        val converted = CFStringGetCString(cfString, bytes, maxBuf, kCFStringEncodingUTF8)
        if (converted) return bytes.toKString()
    }
    return null
}

@OptIn(ExperimentalForeignApi::class)
public actual object NativeFsevent {
    public actual fun createPathsArray(): FseventPathsArrayHandle {
        val handle = FseventPathsArrayHandle()
        handle.arrayRef = CFArrayCreateMutable(null, 0L, null)
        return handle
    }

    public actual fun appendPath(paths: FseventPathsArrayHandle, path: String): Boolean {
        val array = paths.arrayRef ?: return false
        val cfString = CFStringCreateWithCString(null, path, kCFStringEncodingUTF8) ?: return false
        CFArrayAppendValue(array, cfString)
        CFRelease(cfString)
        return true
    }

    public actual fun removePath(paths: FseventPathsArrayHandle, path: String): Int {
        val array = paths.arrayRef ?: return 0
        val count = CFArrayGetCount(array).toInt()
        var removed = 0
        // CFArray is not ordered; remove all matching entries (case-insensitive).
        var i = count - 1
        while (i >= 0) {
            val item = CFArrayGetValueAtIndex(array, i.toLong())
            val itemString = item?.toKStringFromCFString()
            if (itemString != null && itemString.equals(path, ignoreCase = true)) {
                CFArrayRemoveValueAtIndex(array, i.toLong())
                removed++
            }
            i--
        }
        return removed
    }

    public actual fun pathCount(paths: FseventPathsArrayHandle): Int {
        return paths.arrayRef?.let { CFArrayGetCount(it).toInt() } ?: 0
    }

    public actual fun releasePathsArray(paths: FseventPathsArrayHandle) {
        paths.arrayRef?.let { CFRelease(it) }
        paths.arrayRef = null
    }

    public actual fun createStream(
        paths: FseventPathsArrayHandle,
        sinceWhen: ULong,
        latency: Double,
        flags: UInt,
        callback: (flags: UInt, path: String) -> Unit,
    ): FseventStreamHandle? {
        val array = paths.arrayRef ?: return null
        if (CFArrayGetCount(array) == 0L) return null

        val context = FseventCallbackContext(callback)
        val stableRef = StableRef.create(context)

        // Build the FSEventStreamContext struct in scoped memory.
        val stream = memScoped {
            val ctx = alloc<FSEventStreamContext>()
            ctx.version = 0L
            ctx.info = stableRef.asCPointer()
            ctx.retain = null
            ctx.release = null
            ctx.copyDescription = null

            FSEventStreamCreate(
                allocator = null,
                callback = staticCFunction(::fseventCallbackTrampoline),
                context = ctx.ptr,
                pathsToWatch = array,
                sinceWhen = sinceWhen,
                latency = latency,
                flags = flags,
            )
        }

        if (stream == null) {
            stableRef.dispose()
            return null
        }

        val handle = FseventStreamHandle()
        handle.streamRef = stream
        return handle
    }

    public actual fun startStream(stream: FseventStreamHandle): FseventRunLoopHandle {
        val streamRef = stream.streamRef ?: return FseventRunLoopHandle()

        // Get the current run loop (main thread).
        val runLoop = CFRunLoopGetCurrent()
        val mode = kCFRunLoopDefaultMode

        FSEventStreamScheduleWithRunLoop(streamRef, runLoop, mode)
        FSEventStreamStart(streamRef)

        // Block on the run loop — this is the event loop.
        CFRunLoopRun()

        // After the run loop stops, flush remaining events.
        FSEventStreamFlushSync(streamRef)

        val handle = FseventRunLoopHandle()
        handle.runLoopRef = runLoop
        return handle
    }

    public actual fun stopStream(stream: FseventStreamHandle, runLoop: FseventRunLoopHandle) {
        val streamRef = stream.streamRef ?: return
        val mode = kCFRunLoopDefaultMode

        FSEventStreamStop(streamRef)
        FSEventStreamUnscheduleFromRunLoop(streamRef, CFRunLoopGetCurrent(), mode)
        FSEventStreamInvalidate(streamRef)
        FSEventStreamRelease(streamRef)

        // Stop the run loop if it's running.
        val rl = runLoop.runLoopRef
        if (rl != null) {
            CFRunLoopStop(rl)
        }

        stream.streamRef = null
        runLoop.runLoopRef = null
    }

    public actual fun translateFlags(flags: UInt, precise: Boolean): List<EventKind> {
        val events = mutableListOf<EventKind>()

        if (flags and FseventStreamFlags.ITEM_CREATED != 0u) {
            events.add(EventKind.Create(CreateKind.Any))
        }
        if (flags and FseventStreamFlags.ITEM_REMOVED != 0u) {
            events.add(EventKind.Remove(RemoveKind.Any))
        }
        if (flags and FseventStreamFlags.ITEM_RENAMED != 0u) {
            events.add(EventKind.Modify(ModifyKind.Name(RenameMode.Both)))
        }
        if (flags and FseventStreamFlags.ITEM_MODIFIED != 0u) {
            events.add(EventKind.Modify(ModifyKind.Data(DataChange.Any)))
        }
        if (flags and FseventStreamFlags.INODE_META_MOD != 0u) {
            events.add(EventKind.Modify(ModifyKind.Metadata(MetadataKind.Any)))
        }

        // If no specific flags matched, return Other.
        if (events.isEmpty()) {
            events.add(EventKind.Other)
        }

        return events
    }
}