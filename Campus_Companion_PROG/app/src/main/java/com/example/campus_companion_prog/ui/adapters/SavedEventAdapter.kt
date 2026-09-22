package com.example.campus_companion_prog.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.data.SavedEventItem
import com.example.campus_companion_prog.databinding.ItemSavedOpportunityBinding

class SavedEventAdapter(
    private val onOpen: (SavedEventItem) -> Unit,
    private val onRemove: (SavedEventItem) -> Unit
) : ListAdapter<SavedEventItem, SavedEventAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSavedOpportunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemSavedOpportunityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavedEventItem) {
            binding.tvOpportunityBadge.text = item.event.type.replace('_', ' ')
            binding.tvOpportunityTitle.text = item.event.title
            binding.tvOpportunityDeadline.text =
                binding.root.context.getString(R.string.starts_on, item.event.startDateTime.take(10))
            binding.root.setOnClickListener { onOpen(item) }
            binding.btnRemoveOpportunity.setOnClickListener { onRemove(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SavedEventItem>() {
            override fun areItemsTheSame(a: SavedEventItem, b: SavedEventItem) =
                a.favourite.id == b.favourite.id

            override fun areContentsTheSame(a: SavedEventItem, b: SavedEventItem) = a == b
        }
    }
}
