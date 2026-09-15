package com.example.kalasetu.features.opportunity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object OpportunityStore {
    private val _opportunities = MutableStateFlow<List<Opportunity>>(emptyList())
    val opportunities: StateFlow<List<Opportunity>> = _opportunities.asStateFlow()

    fun addOpportunity(opportunity: Opportunity) {
        _opportunities.update { it + opportunity }
    }

    fun opportunitiesForEvent(eventId: String): List<Opportunity> =
        _opportunities.value.filter { it.eventId == eventId }

    fun deleteOpportunity(id: String) {
        _opportunities.update { it.filterNot { opp -> opp.id == id } }
    }

    fun updateOpportunity(opportunity: Opportunity) {
        _opportunities.update { list ->
            list.map { if (it.id == opportunity.id) opportunity else it }
        }
    }
}
