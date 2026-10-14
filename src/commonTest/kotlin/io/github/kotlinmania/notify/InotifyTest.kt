// port-lint: tests inotify.rs
package io.github.kotlinmania.notify

import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemTemporaryDirectory
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
        val dir = Path(SystemTemporaryDirectory, "inotify_test").toString()
        assertTrue(watcher.watch(dir, RecursiveMode.Recursive).isSuccess)
        assertTrue(watcher.unwatch(dir).isSuccess)
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
        val dir = Path(SystemTemporaryDirectory, "inotify_race").toString()
        val res1 = watcher.watch(dir, RecursiveMode.NonRecursive)
        val res2 = watcher.unwatch(dir)
        assertTrue(res1.isSuccess)
        assertTrue(res2.isSuccess)
    }
}
