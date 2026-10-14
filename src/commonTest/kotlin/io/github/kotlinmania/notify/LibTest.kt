// port-lint: tests lib.rs
package io.github.kotlinmania.notify

import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemTemporaryDirectory
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

        val path = Path(SystemTemporaryDirectory, "integration_dir").toString()
        val res = watcher.watch(path, RecursiveMode.Recursive)
        assertTrue(res.isSuccess)

        val unwatchRes = watcher.unwatch(path)
        assertTrue(unwatchRes.isSuccess)
    }

    @Test
    fun testWindowsTrashDir() {
        val baseDir = Path(SystemTemporaryDirectory, "windows_trash_dir")
        val childDir = Path(baseDir, "child").toString()
        val watcher = recommendedWatcher { }.getOrThrow()
        val res = watcher.watch(childDir, RecursiveMode.NonRecursive)
        assertTrue(res.isSuccess)
        val res2 = watcher.watch(baseDir.toString(), RecursiveMode.NonRecursive)
        assertTrue(res2.isSuccess)
    }

    @Test
    fun testPathsMut() {
        val dirA = Path(SystemTemporaryDirectory, "dirA").toString()
        val dirB = Path(SystemTemporaryDirectory, "dirB").toString()

        val watcher = RecommendedWatcher.new { }.getOrThrow()
        val paths = watcher.pathsMut()
        assertTrue(paths.add(dirA, RecursiveMode.Recursive).isSuccess)
        assertTrue(paths.add(dirB, RecursiveMode.Recursive).isSuccess)
        assertTrue(paths.commit().isSuccess)

        assertTrue(paths.remove(dirA).isSuccess)
        assertTrue(paths.commit().isSuccess)
    }
}
