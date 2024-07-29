package info.rashadtanjim.interactivephotogallery.ui

import android.animation.Animator
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import dagger.hilt.android.AndroidEntryPoint
import info.rashadtanjim.interactivephotogallery.R
import info.rashadtanjim.interactivephotogallery.StorageUtil
import info.rashadtanjim.interactivephotogallery.data.UserPreferences
import info.rashadtanjim.interactivephotogallery.databinding.ActivityMainBinding
import info.rashadtanjim.interactivephotogallery.ui.base.BaseActivity
import info.rashadtanjim.interactivephotogallery.ui.gallery.GalleryActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarColor =
            ResourcesCompat.getColor(resources, R.color.background, null)

        binding.lottie.setAnimation(R.raw.gallary_lottie)
        binding.lottie.playAnimation()
        binding.lottie.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                startActivity(Intent(this@MainActivity, GalleryActivity::class.java))
                finishAffinity()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        binding.captureImageButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("MainActivity", "Error occurred while creating the file", ex)
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "info.rashadtanjim.interactivephotogallery.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val photoId = getNextPhotoId()
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "Photo_$photoId",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun getNextPhotoId(): Int {
        // Implement logic to get the next available PhotoId
        // For simplicity, let's assume it returns a unique integer
        return (System.currentTimeMillis() / 1000).toInt()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.i("MainActivity", "Image saved at: $currentPhotoPath")
            saveImageToGallery(currentPhotoPath)
            setPic()
        } else {
            Log.e("MainActivity", "Failed to capture image")
        }
    }

    private fun saveImageToGallery(imagePath: String) {
        // Implement logic to save the image to the interactive gallery
        // This could involve copying the file to a specific directory and updating the database
        StorageUtil.saveImageToGallery(this, imagePath)
    }

    private fun setPic() {
        val targetW: Int = binding.capturedImageView.width
        val targetH: Int = binding.capturedImageView.height

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(currentPhotoPath, this)
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            binding.capturedImageView.visibility = View.VISIBLE
            binding.capturedImageView.setImageBitmap(bitmap)
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}