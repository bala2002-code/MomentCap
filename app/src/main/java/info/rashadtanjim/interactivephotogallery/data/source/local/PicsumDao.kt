package info.rashadtanjim.interactivephotogallery.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.rashadtanjim.interactivephotogallery.domain.model.PicsumPhotosItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PicsumDao {

    @Query("SELECT * FROM picsum_photos")
    fun getAllPhotos(): Flow<List<PicsumPhotosItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photo: List<PicsumPhotosItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PicsumPhotosItem)

    @Query("DELETE FROM picsum_photos")
    suspend fun deleteAll()

    @Query("DELETE FROM picsum_photos WHERE id = :photoId")
    suspend fun deleteById(photoId: Int)
}