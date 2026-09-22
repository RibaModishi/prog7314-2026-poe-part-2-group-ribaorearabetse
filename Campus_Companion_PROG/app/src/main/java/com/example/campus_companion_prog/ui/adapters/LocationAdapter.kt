package com.example.campus_companion_prog.ui.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.databinding.ItemLocationBinding
import com.example.campus_companion_prog.domain.WeatherRules

class LocationAdapter(
    private val onClick: (CampusLocation) -> Unit
) : ListAdapter<LocationAdapter.Row, LocationAdapter.VH>(DIFF) {

    data class Row(
        val location: CampusLocation,
        val weatherStatus: String? = null
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: Row) {
            val item = row.location
            binding.tvLocationBadge.text = item.category.replace('_', ' ')
            binding.tvLocationTitle.text = item.name
            binding.tvLocationMeta.text =
                "${item.openingHours} • Quiet ${item.quietLevel}/5"
            binding.tvLocationDescription.text = item.description

            val status = row.weatherStatus
            if (status != null) {
                val color = WeatherRules.markerColor(status)
                val drawable = binding.viewWeatherMarker.background?.mutate()
                    ?: GradientDrawable().apply { shape = GradientDrawable.OVAL }
                DrawableCompat.setTint(drawable, color)
                binding.viewWeatherMarker.background = drawable
                binding.tvWeatherStatus.text = WeatherRules.localizedMarkerLabel(binding.root.context, status)
                binding.tvWeatherStatus.setTextColor(color)
            } else {
                binding.tvWeatherStatus.text = ""
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Row>() {
            override fun areItemsTheSame(old: Row, new: Row) = old.location.id == new.location.id
            override fun areContentsTheSame(old: Row, new: Row) = old == new
        }
    }
}
