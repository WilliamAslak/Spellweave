package com.spellweave.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    val api: DndApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.dnd5eapi.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DndApi::class.java)
    }
}
