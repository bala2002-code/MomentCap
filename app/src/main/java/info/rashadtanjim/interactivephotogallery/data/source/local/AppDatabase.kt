package info.rashadtanjim.interactivephotogallery.data.source.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import info.rashadtanjim.interactivephotogallery.domain.model.PicsumPhotosItem

@Database(entities = [PicsumPhotosItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun PicsumDao(): PicsumDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "interactive_photo_gallery_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}