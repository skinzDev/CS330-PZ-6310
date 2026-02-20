package com.example.dadada.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dadada.BottomNavigationBar
import com.example.dadada.Domain.FilmItemModel
import com.example.dadada.FilmItem
import com.example.dadada.R
import com.example.dadada.SearchBar
import com.example.dadada.ViewModel.MainViewModel
import com.example.dadada.ui.theme.DadadaTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DadadaTheme {
                MainScreen(
                    onItemClick = { item ->
                        val intent = Intent(this, DetailMovieActivity::class.java)
                        intent.putExtra("object", item)
                        startActivity(intent)
                    },
                    onBottomNavClick = { index -> openBottomNavDestination(index, currentIndex = 0) }
                )
            }
        }
    }
}

@Preview
@Composable
fun MainScreen(
    onItemClick: (FilmItemModel) -> Unit = {},
    onBottomNavClick: (Int) -> Unit = {}
) {
    var randomMoviePool by remember { mutableStateOf<List<FilmItemModel>>(emptyList()) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onItemSelected = { _, index -> onBottomNavClick(index) }
            )
        }, floatingActionButton = {
        Box(
            modifier = Modifier
                .offset(y = 58.dp)
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.pink),
                            colorResource(R.color.green)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    randomMoviePool.randomOrNull()?.let { randomMovie ->
                        onItemClick(randomMovie)
                    }
                },
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                containerColor = colorResource(R.color.black3),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.float_icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            )
        }
    },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = colorResource(R.color.blackBackground)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .background(color = colorResource(R.color.blackBackground))
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            MainContent(
                onItemClick = onItemClick,
                onMoviesAvailableChanged = { movies ->
                    randomMoviePool = movies
                }
            )
        }
    }
}

@Composable
fun MainContent(
    onItemClick: (FilmItemModel) -> Unit,
    onMoviesAvailableChanged: (List<FilmItemModel>) -> Unit = {}
) {
    val viewModel: MainViewModel = viewModel()
    val upcomingData by viewModel.upcomingMovies.observeAsState()
    val newMoviesData by viewModel.newMovies.observeAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val upcoming = upcomingData ?: emptyList()
    val newMovies = newMoviesData ?: emptyList()
    val showUpcomingLoad = upcomingData == null
    val showNewMoviesLoading = newMoviesData == null

    val displayedNewMovies = if (searchQuery.isBlank()) {
        newMovies
    } else {
        newMovies.filter { it.matchesQuery(searchQuery) }
    }
    val displayedUpcoming = if (searchQuery.isBlank()) {
        upcoming
    } else {
        upcoming.filter { it.matchesQuery(searchQuery) }
    }

    LaunchedEffect(newMovies, upcoming) {
        onMoviesAvailableChanged(
            (newMovies + upcoming).distinctBy { "${it.Title}_${it.Year}_${it.Poster}" }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 60.dp, bottom = 100.dp)
    ) {
        Text(
            text = "What would you like to watch?",
            style = TextStyle(color = Color.White, fontSize = 25.sp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        SearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            hint = "Search Movies..."
        )

        SectionTitle("New Movies")

        if (showNewMoviesLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (newMovies.isEmpty()) {
                Text(
                    text = "No movies loaded. Check Firebase Database URL/rules.",
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else if (displayedNewMovies.isEmpty()) {
                Text(
                    text = "No New Movies match \"$searchQuery\"",
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(displayedNewMovies) { item ->
                        FilmItem(item, onItemClick)
                    }
                }
            }
        }

        SectionTitle("Upcoming Movies")

        if (showUpcomingLoad) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (upcoming.isEmpty()) {
                Text(
                    text = "No upcoming movies loaded. Check Firebase Database URL/rules.",
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else if (displayedUpcoming.isEmpty()) {
                Text(
                    text = "No Upcoming Movies match \"$searchQuery\"",
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(displayedUpcoming) { item ->
                        FilmItem(item, onItemClick)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(color = Color(0xFFFFC107), fontSize = 18.sp),
        modifier = Modifier
            .padding(start = 16.dp, top = 32.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

private fun FilmItemModel.matchesQuery(query: String): Boolean {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return true

    val queryLower = normalizedQuery.lowercase()
    val genreMatch = Genre.any { it.lowercase().contains(queryLower) }
    val castMatch = Casts.any { it.Actor.lowercase().contains(queryLower) }

    return Title.lowercase().contains(queryLower) ||
        Description.lowercase().contains(queryLower) ||
        genreMatch ||
        castMatch
}
