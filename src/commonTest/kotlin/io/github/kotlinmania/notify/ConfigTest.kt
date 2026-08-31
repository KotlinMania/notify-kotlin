// port-lint: tests notify/src/config.rs
package io.github.kotlinmania.notify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ConfigTest {
    @Test
    fun testDefaultConfig() {
        val config = Config.default()
        assertEquals(30.seconds, config.pollInterval())
        assertFalse(config.compareContents())
        assertTrue(config.followSymlinks())
    }

    @Test
    fun testConfigBuilders() {
        val config =
            Config
                .default()
                .withPollInterval(5.seconds)
                .withCompareContents(true)
                .withFollowSymlinks(false)

        assertEquals(5.seconds, config.pollInterval())
        assertTrue(config.compareContents())
        assertFalse(config.followSymlinks())

        val manualConfig = config.withManualPolling()
        assertNull(manualConfig.pollInterval())
    }

    @Test
    fun testRecursiveMode() {
        assertTrue(RecursiveMode.Recursive.isRecursive())
        assertFalse(RecursiveMode.NonRecursive.isRecursive())
    }
}
