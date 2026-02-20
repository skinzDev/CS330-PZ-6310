package com.example.dadada.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.dadada.Domain.FilmItemModel
import com.example.dadada.Repository.MainRepository

class MainViewModel: ViewModel() {
    private val repository = MainRepository()

    val upcomingMovies: LiveData<MutableList<FilmItemModel>> = repository.loadUpcoming()

    val newMovies: LiveData<MutableList<FilmItemModel>> = repository.loadItems()
}
