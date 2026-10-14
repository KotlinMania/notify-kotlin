// port-lint: tests error.rs
package io.github.kotlinmania.notify

import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemTemporaryDirectory
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
        val fooPath = Path(SystemTemporaryDirectory, "foo").toString()
        val err = Error.pathNotFound().addPath(fooPath)
        assertEquals("No path was found. about [$fooPath]", err.toString())

        val setPathsErr = Error.watchNotFound().setPaths(listOf("/a", "/b"))
        assertEquals("No watch was found. about [/a, /b]", setPathsErr.toString())
        assertNull(setPathsErr.cause)

        val invalidConfigErr = Error.invalidConfig(Config.default())
        assertEquals(ErrorKind.InvalidConfig(Config.default()), invalidConfigErr.kind)
    }
}
