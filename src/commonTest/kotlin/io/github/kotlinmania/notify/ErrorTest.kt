// port-lint: tests error.rs
package io.github.kotlinmania.notify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ErrorTest {
    @Test
    fun displayFormattedErrors() {
        val expected = "Some error"

        assertEquals(expected, Error.generic(expected).toString())
        assertEquals(expected, Error.io(RuntimeException(expected)).toString())
    }

    @Test
    fun errorPathsAndKinds() {
        val err = Error.pathNotFound().addPath("/tmp/foo")
        assertEquals("No path was found. about [/tmp/foo]", err.toString())

        val setPathsErr = Error.watchNotFound().setPaths(listOf("/a", "/b"))
        assertEquals("No watch was found. about [/a, /b]", setPathsErr.toString())
        assertNull(setPathsErr.cause)

        val invalidConfigErr = Error.invalidConfig(Config.default())
        assertEquals(ErrorKind.InvalidConfig(Config.default()), invalidConfigErr.kind)
    }
}
