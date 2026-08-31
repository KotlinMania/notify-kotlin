// port-lint: source notify/src/error.rs
package io.github.kotlinmania.notify

/**
 * Type alias to use this library's Error type in a Result.
 */
public typealias Result<T> = kotlin.Result<T>

/**
 * Error kinds supported by notify.
 */
public sealed class ErrorKind {
    /**
     * Generic error.
     *
     * May be used in cases where a platform specific error is mapped to this type, or for opaque internal errors.
     */
    public data class Generic(
        public val message: String,
    ) : ErrorKind()

    /**
     * I/O errors.
     */
    public data class Io(
        public val cause: Throwable,
    ) : ErrorKind()

    /**
     * A path does not exist.
     */
    public data object PathNotFound : ErrorKind()

    /**
     * Attempted to remove a watch that does not exist.
     */
    public data object WatchNotFound : ErrorKind()

    /**
     * An invalid value was passed as runtime configuration.
     */
    public data class InvalidConfig(
        public val config: Config,
    ) : ErrorKind()

    /**
     * Cannot watch more files; limit on total watches reached.
     */
    public data object MaxFilesWatch : ErrorKind()
}

/**
 * Notify error type.
 *
 * Errors are emitted either at creation time of a Watcher, or during the event stream. They
 * range from kernel errors to filesystem errors to argument errors.
 *
 * Errors can be general, or they can be about specific paths or subtrees. In that later case, the
 * error's paths field will be populated.
 */
public class Error(
    public val kind: ErrorKind,
    public val paths: List<String> = emptyList(),
) : Exception() {
    public constructor(kind: ErrorKind) : this(kind, emptyList())

    override val message: String
        get() {
            val description =
                when (val k = kind) {
                    is ErrorKind.PathNotFound -> "No path was found."
                    is ErrorKind.WatchNotFound -> "No watch was found."
                    is ErrorKind.InvalidConfig -> "Invalid configuration: ${k.config}"
                    is ErrorKind.Generic -> k.message
                    is ErrorKind.Io -> k.cause.message ?: k.cause.toString()
                    is ErrorKind.MaxFilesWatch -> "OS file watch limit reached."
                }
            return if (paths.isEmpty()) {
                description
            } else {
                "$description about $paths"
            }
        }

    override val cause: Throwable?
        get() =
            when (kind) {
                is ErrorKind.Io -> kind.cause
                else -> null
            }

    /**
     * Adds a path to the error.
     */
    public fun addPath(path: String): Error =
        Error(kind, paths + path)

    /**
     * Replaces the paths for the error.
     */
    public fun setPaths(paths: List<String>): Error =
        Error(kind, paths)

    public fun fmt(): String = message

    override fun toString(): String = message

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Error) return false
        return kind == other.kind && paths == other.paths
    }

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + paths.hashCode()
        return result
    }

    public companion object {
        /**
         * Creates a new Error with empty paths given its kind.
         */
        public fun new(kind: ErrorKind): Error = Error(kind)

        /**
         * Creates a new generic Error from a message.
         */
        public fun generic(msg: String): Error = Error.new(ErrorKind.Generic(msg))

        /**
         * Creates a new i/o Error from a Throwable.
         */
        public fun io(err: Throwable): Error = Error.new(ErrorKind.Io(err))

        /**
         * Similar to io, but specifically handles not found errors.
         */
        public fun ioWatch(err: Throwable): Error {
            val msg = err.message ?: ""
            return if (msg.contains("NotFound", ignoreCase = true) || msg.contains("No such file", ignoreCase = true)) {
                pathNotFound()
            } else {
                io(err)
            }
        }

        /**
         * Creates a new path not found error.
         */
        public fun pathNotFound(): Error = Error.new(ErrorKind.PathNotFound)

        /**
         * Creates a new watch not found error.
         */
        public fun watchNotFound(): Error = Error.new(ErrorKind.WatchNotFound)

        /**
         * Creates a new invalid config error from the given Config.
         */
        public fun invalidConfig(config: Config): Error = Error.new(ErrorKind.InvalidConfig(config))

        /**
         * Converts a Throwable into an Error.
         */
        public fun from(err: Throwable): Error = io(err)
    }
}
