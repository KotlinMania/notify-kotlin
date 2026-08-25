// port-lint: tests fsevent.rs
package io.github.kotlinmania.notify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FseventTest {
    @Test
    fun testFseventWatcherDrop() {
        val watcher = FsEventWatcher.new { }.getOrThrow()
        assertEquals(WatcherKind.Fsevent, watcher.kind())
        assertTrue(watcher.watch("/tmp", RecursiveMode.Recursive).isSuccess)
        assertTrue(watcher.unwatch("/tmp").isSuccess)
        watcher.drop()
    }

    @Test
    fun testSteamContextInfoSendAndSync() {
        val info = StreamContextInfo(EventHandler { })
        info.releaseContext()
    }
}
