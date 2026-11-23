package com.example.dream_sentinel.domain.radio

import com.example.dream_sentinel.data.radio.RadioStation
import kotlinx.coroutines.flow.Flow

interface RadioRepository {

    val stations: Flow<List<RadioStation>>

    val currentStationId: Flow<String?>

    /**
     * Dodaje stację do końca listy.
     * Walidacja (nazwa, URL, duplikat) odbywa się tutaj, UI tylko prezentuje wynik.
     */
    suspend fun addStation(
        name: String,
        streamUrl: String,
        genre: String? = null,
    ): AddStationResult

    /**
     * Usuwa stację. Jeśli była aktualna, repozytorium wskazuje następną
     * (a gdy jej nie ma to poprzednią); przy pustej liście czyści wybór.
     */
    suspend fun removeStation(id: String)

    /** Ustawia aktualną stację tylko wtedy, gdy istnieje na liście. */
    suspend fun setCurrentStation(id: String)
}

sealed interface AddStationResult {
    data class Added(val station: RadioStation) : AddStationResult
    data object BlankName : AddStationResult
    data object InvalidUrl : AddStationResult
    data object DuplicateUrl : AddStationResult
}
