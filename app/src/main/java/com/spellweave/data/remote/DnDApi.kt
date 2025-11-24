package com.spellweave.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface DndApi {

    // Get data for ONE specific level of ONE class
    @GET("api/2014/classes/{class}/levels/{class_level}")
    suspend fun getClassLevel(
        @Path("class") className: String,
        @Path("class_level") classLevel: Int
    ): ClassLevelResponse
}

data class ClassLevelResponse(
    val level: Int,
    val spellcasting: SpellcastingInfo?
)

data class SpellcastingInfo(
    val spell_slots_level_1: Int,
    val spell_slots_level_2: Int,
    val spell_slots_level_3: Int,
    val spell_slots_level_4: Int,
    val spell_slots_level_5: Int,
    val spell_slots_level_6: Int,
    val spell_slots_level_7: Int,
    val spell_slots_level_8: Int,
    val spell_slots_level_9: Int
)
