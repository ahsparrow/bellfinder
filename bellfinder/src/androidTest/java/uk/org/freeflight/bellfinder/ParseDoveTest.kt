package uk.org.freeflight.bellfinder

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

const val HEADER = """TowerID\DoveID\County\Place\Place2\Dedicn\Bells\Wt\PracN\PrXF\Lat\Long"""
const val BAD_HEADER = """TowerID\XDoveID\County\Place\Place2\Dedicn\Bells\Wt\PracN\PrXF\Lat\Long"""
const val DATA = """1\LOCKERLEY\Hants\Lockerley\\S John\6\1387\Thu\\51.03862\-1.57589"""
const val BAD_DATA = """LOCKERLEY\Hants\Lockerley\\S John\6\1387\Thu\\51.03862\-1.57589"""
const val BAD_DATA_VALUE = """"BAD,LOCKERLEY\Hants\Lockerley\\S John\6\1387\Thu\\51.03862\-1.57589"""

@RunWith(AndroidJUnit4::class)
class ParseDoveTest {
    @Test
    @Throws(Exception::class)
    fun goodDove() {
        val data = parseDove(listOf(HEADER, DATA))
        assertThat(data.size, equalTo(1))
    }

    @Test
    @Throws(Exception::class)
    fun badHeader() {
        val data = parseDove(listOf(BAD_HEADER, DATA))
        assertThat(data.size, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun badDataLine() {
        val data = parseDove(listOf(HEADER, BAD_DATA))
        assertThat(data.size, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun badDataValue() {
        val data = parseDove(listOf(HEADER, BAD_DATA_VALUE))
        assertThat(data.size, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun doveFile() {
        val ctx = getInstrumentation().targetContext
        val data = ctx.assets.open("dove.txt").bufferedReader().use {it.readLines()}

        val towers = parseDove(data)
        assertThat(towers.size, not(equalTo(0)))
    }
}