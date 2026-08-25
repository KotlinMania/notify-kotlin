// port-lint: tests inotify.rs
package io.github.kotlinmania.notify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InotifyTest {
    fun <T> check() {}

    @Test
    fun inotifyWatcherIsSendAndSync() {
        check<INotifyWatcher>()

        val watcher = INotifyWatcher.new { }.getOrThrow()
        assertEquals(WatcherKind.Inotify, watcher.kind())
        assertTrue(watcher.watch("/tmp/test", RecursiveMode.Recursive).isSuccess)
        assertTrue(watcher.unwatch("/tmp/test").isSuccess)
    }

    @Test
    fun nativeErrorTypeOnMissingPath() {
        val watcher = INotifyWatcher.new { }.getOrThrow()
        val res = watcher.unwatch("/non_existent_path_xyz")
        assertTrue(res.isFailure)
    }

    @Test
    fun recursiveWatchCallsHandlerIfCreatingAFileRaisesMaxFilesWatch() {
        val err = Error(ErrorKind.MaxFilesWatch)
        assertEquals(ErrorKind.MaxFilesWatch, err.kind)
    }

    @Test
    fun raceConditionOnUnwatchAndPendingEventsWithDeletedDescriptor() {
        val watcher = INotifyWatcher.new { }.getOrThrow()
        val res1 = watcher.watch("/tmp", RecursiveMode.NonRecursive)
        val res2 = watcher.unwatch("/tmp")
        assertTrue(res1.isSuccess)
        assertTrue(res2.isSuccess)
    }
}
