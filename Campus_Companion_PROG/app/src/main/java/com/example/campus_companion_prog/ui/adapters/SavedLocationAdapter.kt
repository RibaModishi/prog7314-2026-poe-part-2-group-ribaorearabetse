package com.example.campus_companion_prog.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_companion_prog.data.SavedLocationItem
import com.example.campus_companion_prog.databinding.ItemSavedLocationBinding

class SavedLocationAdapter(
    private val onOpen: (SavedLocationItem) -> Unit,
    private val onRemove: (SavedLocationItem) -> Unit
) : ListAdapter<SavedLocationItem, SavedLocationAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSavedLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemSavedLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavedLocationItem) {
            binding.tvLocationBadge.text = item.location.category.replace('_', ' ')
            binding.tvLocationTitle.text = item.location.name
            binding.tvLocationHours.text = item.location.openingHours
            binding.root.setOnClickListener { onOpen(item) }
            binding.btnRemoveLocation.setOnClickListener { onRemove(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SavedLocationItem>() {
            override fun areItemsTheSame(a: SavedLocationItem, b: SavedLocationItem) =
                a.favourite.id == b.favourite.id

            override fun areContentsTheSame(a: SavedLocationItem, b: SavedLocationItem) = a == b
        }
    }
}
