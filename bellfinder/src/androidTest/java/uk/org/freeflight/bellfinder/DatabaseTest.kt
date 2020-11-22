package uk.org.freeflight.bellfinder

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.org.freeflight.bellfinder.db.*
import java.io.IOException
import java.util.*

val TOWER1 = Tower(1, "Lockerley", "", "Hants", "S John", 6, 1000, false, "Thu", "", 51.0, -1.0)
val TOWER2 = Tower(2, "East Tytherley", "", "Hants", "S Peter", 8, 1200, false, "Thu", "", 51.1, -1.0)

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var bellFinderDao: BellFinderDao
    private lateinit var db: BellFinderDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, BellFinderDatabase::class.java).build()
        bellFinderDao = db.bellFinderDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeAndRead() = runBlocking {
        bellFinderDao.insertTower(TOWER1)
        val result = bellFinderDao.liveTowers().waitForValue()
        assertThat(result[0], equalTo(TOWER1))
    }
    
    @Test
    @Throws(Exception::class)
    fun readById() = runBlocking {
        val towerInfo = listOf(TOWER1, TOWER2)
        bellFinderDao.insertTowers(towerInfo)

        val result = bellFinderDao.getTower(1)
        assertThat(result, equalTo(TOWER1))
    }

    @Test
    @Throws(Exception::class)
    fun deleteAll() = runBlocking {
        bellFinderDao.insertTower(TOWER1)
        bellFinderDao.deleteTowers()

        val result = bellFinderDao.liveTowers().waitForValue()
        assertThat(result.size, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun visitIds() = runBlocking {
        val c1 = GregorianCalendar(2020, 10, 7)
        val c2 = GregorianCalendar(2020, 10, 8)

        val v1 = Visit(null, 1, c1,"Plain Bob", peal = false,  quarter = false)
        bellFinderDao.insertVisit(v1)

        val v2 = Visit(null, 1, c2,"Plain Bob", peal = false,  quarter = false)
        bellFinderDao.insertVisit(v2)

        var ids = bellFinderDao.liveVisitedTowerIds().waitForValue()

        assertThat(ids.size, equalTo(1))

        val v3 = Visit(null, 2, c2,"Plain Bob", peal = false,  quarter = false)
        bellFinderDao.insertVisit(v3)

        ids = bellFinderDao.liveVisitedTowerIds().waitForValue()

        assertThat(ids.size, equalTo(2))
    }

    @Test
    @Throws(Exception::class)
    fun visitViews() = runBlocking {
        val c1 = GregorianCalendar(2020, 10, 7)
        val c2 = GregorianCalendar(2020, 10, 8)

        val v1 = Visit(null, 1, c1,"Plain Bob", peal = false,  quarter = false)
        val v2 = Visit(null, 1, c2,"Plain Bob", peal = false,  quarter = false)

        bellFinderDao.insertVisit(v1)
        bellFinderDao.insertVisit(v2)

        bellFinderDao.insertTower(TOWER1)

        val visits = bellFinderDao.liveVisitViews().waitForValue()

        val view = VisitView(2, 1, c2, "Plain Bob", peal=false, quarter=false,"Lockerley", "", "S John", 6)
        assertThat(visits[0], equalTo(view))
    }

    @Test
    @Throws(Exception::class)
    fun insertVisit() = runBlocking {
        bellFinderDao.insertTower(TOWER1)

        val c = GregorianCalendar(2020, 10, 31)
        bellFinderDao.insertVisit(1, c, "Plain Bob",  peal=true, quarter=false)

        val visit = bellFinderDao.getVisit(1)

        val refVisit = Visit(1, 1, c, "Plain Bob", peal=true, quarter=false)
        assertThat(visit, equalTo(refVisit))
    }
}
