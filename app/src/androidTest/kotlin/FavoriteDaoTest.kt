import android.content.Context
import androidx.datastore.core.IOException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flightsearch.data.AppDatabase
import com.example.flightsearch.data.dao.AirportDao
import com.example.flightsearch.data.dao.FavoriteDao
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.Favorite
import com.example.flightsearch.data.model.Route
import com.example.flightsearch.data.model.toFavorite
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith



@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var airportDao: AirportDao
    private lateinit var appDatabase: AppDatabase
    private var favorite1 = Favorite(1, "DUS", "SVO")
    private var favorite2 = Favorite(2, "BGU", "MUC")
    private var airport1 = Airport(1, "DUS", "Düsseldorf International Airport", passengers = 1)
    private var airport2 = Airport(2, "SVO", "Sheremetyevo Alexander S. Pushkin International Airport", passengers = 2)
    private var airport3 = Airport(3, "BGU", "Bangassou Airport", passengers = 3)
    private var airport4 = Airport(4, "MUC", "Munich Airport Franz Josef Strauss", passengers = 4)

    private suspend fun addOneFavoriteToDb() {
        favoriteDao.insert(favorite1)
        airportDao.apply {
            insert(airport1)
            insert(airport2)
        }
    }

    private suspend fun addTwoFavoritesToDb() {
        favoriteDao.apply {
            insert(favorite1)
            insert(favorite2)
        }
        airportDao.apply {
            insert(airport1)
            insert(airport2)
            insert(airport3)
            insert(airport4)
        }
    }

    @Before
    fun createDatabase() {
        val context: Context = ApplicationProvider.getApplicationContext()

        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        favoriteDao = appDatabase.favouriteDao()
        airportDao = appDatabase.airportDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        appDatabase.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsFavoriteIntoDb() = runBlocking {
        addOneFavoriteToDb()

        val allFavorites: List<Route> = favoriteDao.getAll().first()
        assertEquals(allFavorites[0].toFavorite(), favorite1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAll_returnsAllFavoritesFromDb() = runBlocking {
        addTwoFavoritesToDb()

        val allFavorites: List<Route> = favoriteDao.getAll().first()
        assertEquals(allFavorites[0].toFavorite(), favorite1)
        assertEquals(allFavorites[1].toFavorite(), favorite2)
    }

    @Test
    @Throws(Exception::class)
    fun daoDelete_deletesAllFavoritesFromDb() = runBlocking {
        addTwoFavoritesToDb()
        favoriteDao.delete(favorite1.departureCode, favorite1.destinationCode)
        favoriteDao.delete(favorite2.departureCode, favorite2.destinationCode)

        val allFavorites: List<Route> = favoriteDao.getAll().first()
        assertTrue(allFavorites.isEmpty())
    }
}
