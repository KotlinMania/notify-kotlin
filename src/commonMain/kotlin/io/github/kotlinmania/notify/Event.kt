// port-lint: source lib.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.notify

import kotlin.native.HiddenFromObjC

@HiddenFromObjC
public enum class AccessMode {
    Any,
    Execute,
    Read,
    Write,
    Other,
}

@HiddenFromObjC
public sealed class AccessKind {
    public data object Any : AccessKind()
    public data object Read : AccessKind()
    public data class Open(public val mode: AccessMode) : AccessKind()
    public data class Close(public val mode: AccessMode) : AccessKind()
    public data object Other : AccessKind()
}

@HiddenFromObjC
public sealed class CreateKind {
    public data object Any : CreateKind()
    public data object File : CreateKind()
    public data object Folder : CreateKind()
    public data object Other : CreateKind()
}

@HiddenFromObjC
public enum class DataChange {
    Any,
    Size,
    Content,
    Other,
}

@HiddenFromObjC
public enum class MetadataKind {
    Any,
    AccessTime,
    WriteTime,
    Permissions,
    Ownership,
    Extended,
    Other,
}

@HiddenFromObjC
public enum class RenameMode {
    Any,
    To,
    From,
    Both,
    Other,
}

@HiddenFromObjC
public sealed class ModifyKind {
    public data object Any : ModifyKind()
    public data class Data(public val change: DataChange) : ModifyKind()
    public data class Metadata(public val kind: MetadataKind) : ModifyKind()
    public data class Name(public val mode: RenameMode) : ModifyKind()
    public data object Other : ModifyKind()
}

@HiddenFromObjC
public sealed class RemoveKind {
    public data object Any : RemoveKind()
    public data object File : RemoveKind()
    public data object Folder : RemoveKind()
    public data object Other : RemoveKind()
}

@HiddenFromObjC
public sealed class EventKind {
    public data object Any : EventKind()
    public data class Access(public val kind: AccessKind) : EventKind()
    public data class Create(public val kind: CreateKind) : EventKind()
    public data class Modify(public val kind: ModifyKind) : EventKind()
    public data class Remove(public val kind: RemoveKind) : EventKind()
    public data object Other : EventKind()

    public val isAccess: Boolean get() = this is Access
    public val isCreate: Boolean get() = this is Create
    public val isModify: Boolean get() = this is Modify
    public val isRemove: Boolean get() = this is Remove
    public val isOther: Boolean get() = this is Other
    public val isAny: Boolean get() = this is Any
}

@HiddenFromObjC
public data class EventAttributes(
    public val tracker: Long? = null,
    public val flag: Long? = null,
    public val info: String? = null,
    public val source: String? = null,
)

@HiddenFromObjC
public data class Event(
    public val kind: EventKind,
    public val paths: List<String> = emptyList(),
    public val attrs: EventAttributes = EventAttributes(),
) {
    public constructor(kind: EventKind) : this(kind, emptyList(), EventAttributes())

    public fun needRescan(): Boolean = false

    public fun tracker(): Long? = attrs.tracker

    public fun flag(): Long? = attrs.flag

    public fun info(): String? = attrs.info

    public fun source(): String? = attrs.source

    public companion object {
        public fun new(kind: EventKind): Event = Event(kind)
    }
}
