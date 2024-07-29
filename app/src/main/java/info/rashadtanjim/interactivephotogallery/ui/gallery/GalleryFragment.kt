package info.rashadtanjim.interactivephotogallery.ui.gallery

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.scopes.FragmentScoped
import info.rashadtanjim.core.utlis.snakbar
import info.rashadtanjim.interactivephotogallery.App
import info.rashadtanjim.interactivephotogallery.R
import info.rashadtanjim.interactivephotogallery.data.repository.UserRepository
import info.rashadtanjim.interactivephotogallery.data.source.remote.PicsumApi
import info.rashadtanjim.interactivephotogallery.data.util.DataState
import info.rashadtanjim.interactivephotogallery.databinding.FragmentGalleryBinding
import info.rashadtanjim.interactivephotogallery.domain.model.PicsumPhotosItem
import info.rashadtanjim.interactivephotogallery.ui.adapter.GalleryAdapter
import info.rashadtanjim.interactivephotogallery.ui.base.BaseFragment
import java.io.File
import java.io.FileOutputStream

@FragmentScoped
class GalleryFragment :
    BaseFragment<SharedViewModel, FragmentGalleryBinding, UserRepository>() {

    private lateinit var galleryAdapter: GalleryAdapter
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PERMISSIONS = 100

    private var photoListItem: List<PicsumPhotosItem>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryAdapter = GalleryAdapter(
            onClick = { photoItem ->
                val action = GalleryFragmentDirections.actionGalleryFragmentToPhotoViewFragment(photoItem.download_url)
                findNavController().navigate(action)
            },
            onDelete = { photoItem ->
                // Handle delete action
            }
        )

        binding.recycleViewPhotoList.adapter = galleryAdapter
        binding.recycleViewPhotoList.setHasFixedSize(true)

        viewModel.photoList.observe(viewLifecycleOwner, {
            when (it) {
                is DataState.Success -> {
                    updateUI(it.value)  //success update result
                    photoListItem = it.value
                    binding.progressBar.isVisible = false
                }
                is DataState.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is DataState.Failure -> {
                    binding.progressBar.isVisible = false
                    binding.root.snakbar(getString(R.string.no_internet_connection))
                }
            }
        })

        viewModel.photoData()

        binding.imageButtonSettings.setOnClickListener {
            findNavController().navigate(R.id.action_galleryFragment_to_settingsFragment)
        }

        binding.captureImageButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        checkPermissions()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                saveImage(imageBitmap)
            } else {
                Log.e("GalleryFragment", "Camera returned null data")
            }
        } else {
            Log.e("GalleryFragment", "Failed to capture image")
        }
    }

    private fun saveImage(imageBitmap: Bitmap) {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir?.exists() == true || storageDir?.mkdirs() == true) {
            val imageFile = File(storageDir, "IMG_${System.currentTimeMillis()}.jpg")
            try {
                FileOutputStream(imageFile).use { out ->
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                // Update the database with the new image details
                val photoId = (System.currentTimeMillis() / 1000).toInt()
                val photoItem = PicsumPhotosItem(
                    id = photoId,
                    imagePath = imageFile.absolutePath,
                    width = imageBitmap.width,
                    height = imageBitmap.height,
                    url = "", // Provide appropriate values
                    author = "Unknown", // Provide appropriate values
                    download_url = "" // Provide appropriate values
                )
                viewModel.insertPhoto(photoItem)
                viewModel.photoData()  // Trigger data reload
            } catch (e: Exception) {
                Log.e("GalleryFragment", "Error saving image", e)
            }
        } else {
            Log.e("GalleryFragment", "Failed to create directory")
        }
    }

    private fun updateUI(photoListItem: List<PicsumPhotosItem>?) {
        galleryAdapter.submitList(photoListItem?.toMutableList())
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
            } else {
                // Permissions denied
                Log.e("GalleryFragment", "Permissions denied")
            }
        }
    }

    override fun getViewModel() = SharedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGalleryBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): UserRepository {
        val api = remoteDataSource.buildApi(PicsumApi::class.java)
        val picsumDao = (requireActivity().applicationContext as App).database.PicsumDao()
        return UserRepository(api, picsumDao)
    }
}