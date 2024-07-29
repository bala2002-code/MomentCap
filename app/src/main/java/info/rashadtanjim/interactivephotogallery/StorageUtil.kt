package info.rashadtanjim.interactivephotogallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import info.rashadtanjim.interactivephotogallery.data.source.local.AppDatabase
import info.rashadtanjim.interactivephotogallery.domain.model.PicsumPhotosItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object StorageUtil {

    fun saveImageToGallery(context: Context, imagePath: String) {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val galleryDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "InteractiveGallery")
        if (!galleryDir.exists()) {
            galleryDir.mkdirs()
        }
        val photoId = getNextPhotoId()
        val imageFile = File(galleryDir, "Photo_$photoId.jpg")
        try {
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            Log.i("StorageUtil", "Image saved to gallery: ${imageFile.absolutePath}")
            // Update the database with the new image details
            CoroutineScope(Dispatchers.IO).launch {
                updateDatabase(context, imageFile.absolutePath, photoId)
            }
        } catch (e: IOException) {
            Log.e("StorageUtil", "Error saving image to gallery", e)
        }
    }

    private fun getNextPhotoId(): Int {
        // Implement logic to get the next available PhotoId
        // For simplicity, let's assume it returns a unique integer
        return (System.currentTimeMillis() / 1000).toInt()
    }

    private suspend fun updateDatabase(context: Context, imagePath: String, photoId: Int) {
        // Implement logic to update the database with the new image details
        val db = AppDatabase.getDatabase(context)
        val photoItem = PicsumPhotosItem(
            id = photoId,
            imagePath = imagePath,
            width = 0, // Provide appropriate values
            height = 0, // Provide appropriate values
            url = "", // Provide appropriate values
            author = "Unknown", // Provide appropriate values
            download_url = "" // Provide appropriate values
        )
        db.PicsumDao().insert(photoItem)
    }
}