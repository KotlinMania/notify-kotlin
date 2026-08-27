// port-lint: tests lib.rs
package io.github.kotlinmania.notify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibTest {
    @Test
    fun testObjectSafe() {
        val watcher: Watcher = NullWatcher.new { }.getOrThrow()
        assertEquals(WatcherKind.NullWatcher, watcher.kind())
    }

    @Test
    fun testDebugImpl() {
        val config = Config.default()
        val error = Error.pathNotFound()
        val errorKind = ErrorKind.PathNotFound
        val nullWatcher = NullWatcher.new { }.getOrThrow()
        val pollWatcher = PollWatcher.new { }.getOrThrow()
        val recommended = recommendedWatcher { }.getOrThrow()
        val recursiveMode = RecursiveMode.Recursive
        val kind = WatcherKind.PollWatcher

        assertTrue(config.toString().isNotEmpty())
        assertTrue(error.toString().isNotEmpty())
        assertTrue(errorKind.toString().isNotEmpty())
        assertTrue(nullWatcher.toString().isNotEmpty())
        assertTrue(pollWatcher.toString().isNotEmpty())
        assertTrue(recommended.toString().isNotEmpty())
        assertTrue(recursiveMode.toString().isNotEmpty())
        assertTrue(kind.toString().isNotEmpty())
    }

    private fun iterWithTimeout(events: List<Event>): List<Event> = events

    @Test
    fun integration() {
        val received = mutableListOf<Event>()
        val watcher =
            recommendedWatcher { res ->
                res.getOrNull()?.let { received.add(it) }
            }.getOrThrow()

        val path = "/tmp/integration_dir"
        val res = watcher.watch(path, RecursiveMode.Recursive)
        assertTrue(res.isSuccess)

        val unwatchRes = watcher.unwatch(path)
        assertTrue(unwatchRes.isSuccess)
    }

    @Test
    fun testWindowsTrashDir() {
        val childDir = "/tmp/child"
        val watcher = recommendedWatcher { }.getOrThrow()
        val res = watcher.watch(childDir, RecursiveMode.NonRecursive)
        assertTrue(res.isSuccess)
        val res2 = watcher.watch("/tmp", RecursiveMode.NonRecursive)
        assertTrue(res2.isSuccess)
    }

    @Test
    fun testPathsMut() {
        val dirA = "/tmp/dirA"
        val dirB = "/tmp/dirB"

        val watcher = RecommendedWatcher.new { }.getOrThrow()
        val paths = watcher.pathsMut()
        assertTrue(paths.add(dirA, RecursiveMode.Recursive).isSuccess)
        assertTrue(paths.add(dirB, RecursiveMode.Recursive).isSuccess)
        assertTrue(paths.commit().isSuccess)

        assertTrue(paths.remove(dirA).isSuccess)
        assertTrue(paths.commit().isSuccess)
    }
}
