// port-lint: tests kqueue.rs
package io.github.kotlinmania.notify

import io.github.kotlinmania.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KqueueTest {
    @Test
    fun testRemoveRecursive() {
        val watcher = KqueueWatcher.new { }.getOrThrow()
        assertEquals(WatcherKind.Kqueue, watcher.kind())
        val path = Path("src").toString()
        val res = watcher.watch(path, RecursiveMode.Recursive)
        assertTrue(res.isSuccess)
        val unwatchRes = watcher.unwatch(path)
        assertTrue(unwatchRes.isSuccess)
    }
}
