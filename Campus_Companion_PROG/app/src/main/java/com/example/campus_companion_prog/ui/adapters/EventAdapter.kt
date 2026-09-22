package com.example.campus_companion_prog.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_companion_prog.api.EventOrOpportunity
import com.example.campus_companion_prog.databinding.ItemEventBinding

class EventAdapter(
    private val onClick: (EventOrOpportunity) -> Unit
) : ListAdapter<EventAdapter.Row, EventAdapter.VH>(DIFF) {

    data class Row(
        val event: EventOrOpportunity,
        val weatherIndicator: String? = null
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: Row) {
            val item = row.event
            binding.tvEventBadge.text = item.type.replace('_', ' ')
            binding.tvEventTitle.text = item.title
            val date = item.startDateTime.take(10)
            binding.tvEventMeta.text = "${item.organiser} • $date"
            binding.tvEventDescription.text = item.description

            val warning = row.weatherIndicator
            if (warning.isNullOrBlank()) {
                binding.tvWeatherIndicator.visibility = View.GONE
            } else {
                binding.tvWeatherIndicator.visibility = View.VISIBLE
                binding.tvWeatherIndicator.text = warning
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Row>() {
            override fun areItemsTheSame(old: Row, new: Row) = old.event.id == new.event.id
            override fun areContentsTheSame(old: Row, new: Row) = old == new
        }
    }
}
