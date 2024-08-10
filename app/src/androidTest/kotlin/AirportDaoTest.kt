import android.content.Context
import androidx.datastore.core.IOException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flightsearch.data.AppDatabase
import com.example.flightsearch.data.dao.AirportDao
import com.example.flightsearch.data.model.Airport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.jvm.Throws


@RunWith(AndroidJUnit4::class)
class AirportDaoTest {
    private lateinit var airportDao: AirportDao
    private lateinit var database: AppDatabase
    private val airport1 = Airport(1, "DUS", "Düsseldorf International Airport", passengers = 3)
    private val airport2 = Airport(2, "SVO", "Sheremetyevo Alexander S. Pushkin International Airport", passengers = 2)
    private val airport3 = Airport(3, "BGU", "Bangassou Airport", passengers = 1)

    private suspend fun addOneAirportToDb() {
        airportDao.insert(airport1)
    }

    private suspend fun addThreeAirportsToDb() {
        airportDao.apply {
            insert(airport1)
            insert(airport2)
            insert(airport3)
        }
    }

    @Before
    @Throws(IOException::class)
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()

        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        airportDao = database.airportDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() = database.close()


    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsAirportToDb() = runBlocking {
        addOneAirportToDb()

        val airport = airportDao.getByIataCode(airport1.iataCode).first()
        assertEquals(airport, airport1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetByIataCode_returnsAirportByIataCodeFromDb() = runBlocking {
        addThreeAirportsToDb()

        val airport = airportDao.getByIataCode(airport1.iataCode).first()
        assertEquals(airport, airport1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllExcluding_returnsAllAirportDescendingExcludingGivenOneFromDb() = runBlocking {
        addThreeAirportsToDb()

        val airports = airportDao.getAllExcluding(airport1.iataCode).first()
        assertEquals(airports[0], airport2)
        assertEquals(airports[1], airport3)
        assertTrue(airports[1].passengers <= airports[0].passengers)
        assertTrue(airports.size == 2)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetByPrompt_returnsAllAirportDescendingMatchingPromptFromDb() = runBlocking {
        addThreeAirportsToDb()

        val prompt = "international"
        val airports = airportDao.getByPrompt(prompt).first()
        assertEquals(airports[0], airport1)
        assertEquals(airports[1], airport2)
        assertTrue(airports[1].passengers <= airports[0].passengers)
        assertTrue(airports.size == 2)
    }
}
