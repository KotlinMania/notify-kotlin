// port-lint: tests fsevent.rs
package io.github.kotlinmania.notify

import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemTemporaryDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FseventTest {
    @Test
    fun testFseventWatcherDrop() {
        val watcher = FsEventWatcher.new { }.getOrThrow()
        assertEquals(WatcherKind.Fsevent, watcher.kind())
        val dir = Path(SystemTemporaryDirectory, "fsevent_watcher_drop").toString()
        assertTrue(watcher.watch(dir, RecursiveMode.Recursive).isSuccess)
        assertTrue(watcher.unwatch(dir).isSuccess)
        watcher.drop()
    }

    @Test
    fun testSteamContextInfoSendAndSync() {
        val info = StreamContextInfo(EventHandler { })
        info.releaseContext()
    }
}
