// port-lint: tests poll.rs
package io.github.kotlinmania.notify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PollTest {
    @Test
    fun pollWatcherIsSendAndSync() {
        fun check() {}
        check()

        val watcher = PollWatcher.new { }.getOrThrow()
        assertEquals(WatcherKind.PollWatcher, watcher.kind())
        assertTrue(watcher.watch("/test", RecursiveMode.Recursive).isSuccess)
        assertTrue(watcher.poll().isSuccess)
        assertTrue(watcher.unwatch("/test").isSuccess)
    }
}
