package com.example.campus_companion_prog.data

import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.api.EventOrOpportunity
import com.example.campus_companion_prog.api.Favourite

/** A favourite joined with the full location/event details for the Saved Items UI. */
data class SavedLocationItem(
    val favourite: Favourite,
    val location: CampusLocation
)

data class SavedEventItem(
    val favourite: Favourite,
    val event: EventOrOpportunity
)
