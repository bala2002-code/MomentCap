package info.rashadtanjim.interactivephotogallery.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import info.rashadtanjim.interactivephotogallery.databinding.ListItemPhotosBinding
import info.rashadtanjim.interactivephotogallery.domain.model.PicsumPhotosItem

class GalleryAdapter(
    private val onClick: (PicsumPhotosItem) -> Unit,
    private val onDelete: (PicsumPhotosItem) -> Unit
) : ListAdapter<PicsumPhotosItem, GalleryAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    class PhotoViewHolder(
        private val itemBinding: ListItemPhotosBinding,
        private val onClick: (PicsumPhotosItem) -> Unit,
        private val onDelete: (PicsumPhotosItem) -> Unit
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        private var selectedPhoto: PicsumPhotosItem? = null

        init {
            itemBinding.root.setOnClickListener {
                selectedPhoto?.let { photo ->
                    onClick(photo)
                }
            }
            itemBinding.deleteButton.setOnClickListener {
                selectedPhoto?.let { photo ->
                    onDelete(photo)
                }
            }
        }

        fun bind(photoItem: PicsumPhotosItem) {
            selectedPhoto = photoItem

            itemBinding.textViewAuthorName.text = "Author: ${photoItem.author}"
            itemBinding.textViewPhotoNumber.text = "Photo ID: ${photoItem.id}"

            Glide.with(itemView).load(photoItem.download_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // caching image file using Glide
                .into(itemBinding.imageViewItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val itemBinding =
            ListItemPhotosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(itemBinding, onClick, onDelete)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoItem = getItem(position)
        holder.bind(photoItem)
    }
}

class PhotoDiffCallback : DiffUtil.ItemCallback<PicsumPhotosItem>() {
    override fun areItemsTheSame(oldItem: PicsumPhotosItem, newItem: PicsumPhotosItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PicsumPhotosItem, newItem: PicsumPhotosItem): Boolean {
        return oldItem == newItem
    }
}