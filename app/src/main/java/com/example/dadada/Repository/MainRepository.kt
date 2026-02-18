package com.example.dadada.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dadada.Domain.FilmItemModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainRepository {
    // Explicit URL avoids hangs when google-services.json doesn't contain firebase_database_url.
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://cs330-pz-6310-default-rtdb.europe-west1.firebasedatabase.app/"
    )

    fun loadUpcoming(): LiveData<MutableList<FilmItemModel>> {
        val listData = MutableLiveData<MutableList<FilmItemModel>>()
        // Try common node names and fallback to Items if upcoming list is missing.
        loadFirstNonEmpty(
            paths = listOf("Upcoming", "upcoming", "Upcomming", "Items")
        ) { movies ->
            listData.value = movies
        }
        return listData
    }

    fun loadItems(): LiveData<MutableList<FilmItemModel>> {
        val listData = MutableLiveData<MutableList<FilmItemModel>>()
        loadFirstNonEmpty(
            paths = listOf("Items", "items")
        ) { movies ->
            listData.value = movies
        }
        return listData
    }

    private fun loadFirstNonEmpty(
        paths: List<String>,
        index: Int = 0,
        onResult: (MutableList<FilmItemModel>) -> Unit
    ) {
        if (index >= paths.size) {
            onResult(mutableListOf())
            return
        }

        val path = paths[index]
        val ref = firebaseDatabase.getReference(path)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val movies = parseMovies(snapshot)
                Log.d("MainRepository", "Node '$path' returned ${movies.size} movies")
                if (movies.isNotEmpty() || index == paths.lastIndex) {
                    onResult(movies)
                } else {
                    loadFirstNonEmpty(paths, index + 1, onResult)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Node '$path' cancelled: ${error.message}")
                loadFirstNonEmpty(paths, index + 1, onResult)
            }
        })
    }

    private fun parseMovies(snapshot: DataSnapshot): MutableList<FilmItemModel> {
        val movies = mutableListOf<FilmItemModel>()
        for (childSnapshot in snapshot.children) {
            try {
                val item = childSnapshot.getValue(FilmItemModel::class.java)
                item?.let { movies.add(it) }
            } catch (e: Exception) {
                Log.e("MainRepository", "Parse failed for key '${childSnapshot.key}': ${e.message}")
            }
        }
        return movies
    }
}
