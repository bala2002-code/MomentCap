package info.rashadtanjim.interactivephotogallery.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picsum_photos")
data class PicsumPhotosItem(
    @PrimaryKey val id: Int,
    val imagePath: String,
    val width: Int,
    val height: Int,
    val url: String,
    val author: String, // Add this property
    val download_url: String // Add this property
)