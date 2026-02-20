package com.example.dadada.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.dadada.BottomNavigationBar
import com.example.dadada.Domain.FilmItemModel
import com.example.dadada.R
import com.example.dadada.Repository.FavoritesRepository
import com.example.dadada.ui.theme.DadadaTheme

class MyListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DadadaTheme {
                MyListScreen(
                    onMovieClick = { movie ->
                        val intent = Intent(this, DetailMovieActivity::class.java)
                        intent.putExtra("object", movie)
                        startActivity(intent)
                    },
                    onBottomNavClick = { index -> openBottomNavDestination(index, currentIndex = 1) }
                )
            }
        }
    }
}

@Composable
private fun MyListScreen(
    onMovieClick: (FilmItemModel) -> Unit,
    onBottomNavClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val userScope = AuthSessionScope.resolveUserScope(context)
    val favoritesRepository = remember(context, userScope) { FavoritesRepository(context, userScope) }
    val favorites = remember { mutableStateListOf<FilmItemModel>() }

    LaunchedEffect(userScope) {
        favorites.clear()
        favorites.addAll(favoritesRepository.getFavorites())
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                initialSelectedItem = 1,
                onItemSelected = { _, index -> onBottomNavClick(index) }
            )
        },
        containerColor = colorResource(R.color.blackBackground)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.blackBackground))
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                alpha = 0.2f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 90.dp)
            ) {
                Text(
                    text = "My List",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (favorites.isEmpty()) {
                    Text(
                        text = "Your list is empty. Tap hearts on movies to save them.",
                        color = Color.LightGray
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(
                            items = favorites,
                            key = { "${it.Title}-${it.Year}-${it.Poster}" }
                        ) { movie ->
                            FavoriteMovieRow(
                                movie = movie,
                                onMovieClick = onMovieClick,
                                onRemoveClick = {
                                    if (favoritesRepository.removeFavorite(movie)) {
                                        favorites.remove(movie)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteMovieRow(
    movie: FilmItemModel,
    onMovieClick: (FilmItemModel) -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x33282F32), RoundedCornerShape(14.dp))
            .clickable { onMovieClick(movie) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = movie.Poster,
            contentDescription = null,
            modifier = Modifier
                .size(width = 80.dp, height = 118.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = movie.Title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Text(
                text = "Year: ${movie.Year}",
                color = Color.LightGray,
                fontSize = 13.sp
            )
            Text(
                text = "IMDB: ${movie.Imdb}",
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }

        Image(
            painter = painterResource(R.drawable.fav),
            contentDescription = "Remove from list",
            colorFilter = ColorFilter.tint(colorResource(R.color.pink)),
            modifier = Modifier
                .size(26.dp)
                .clickable { onRemoveClick() }
        )
    }
}
