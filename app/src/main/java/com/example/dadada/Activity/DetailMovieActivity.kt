package com.example.dadada.Activity

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.dadada.BottomNavigationBar
import com.example.dadada.Domain.FilmItemModel
import com.example.dadada.R
import com.example.dadada.Repository.CartRepository
import com.example.dadada.Repository.FavoritesRepository
import com.example.dadada.ui.theme.DadadaTheme
import java.util.Locale
import androidx.compose.material3.Scaffold

class DetailMovieActivity : BaseActivity() {
    private var filmItem: FilmItemModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filmItem = intent.getSerializableExtra("object") as? FilmItemModel
        val currentFilm = filmItem ?: run {
            finish()
            return
        }

        setContent {
            DadadaTheme {
                DetailScreen(
                    film = currentFilm,
                    onBottomNavClick = { index -> openBottomNavDestination(index, currentIndex = 0) }
                )
            }
        }
    }
}

@Composable
fun DetailScreen(
    film: FilmItemModel,
    onBottomNavClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val userScope = AuthSessionScope.resolveUserScope(context)
    val favoritesRepository = remember(context, userScope) { FavoritesRepository(context, userScope) }
    val cartRepository = remember(context, userScope) { CartRepository(context, userScope) }
    val scrollState = rememberScrollState()
    val isLoading = remember { mutableStateOf(false) }
    var isFavorite by remember(film.Title, film.Year, film.Poster, userScope) {
        mutableStateOf(favoritesRepository.isFavorite(film))
    }
    var isInCart by remember(film.Title, film.Year, film.Poster, userScope) {
        mutableStateOf(cartRepository.isInCart(film))
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                initialSelectedItem = 0,
                onItemSelected = { _, index -> onBottomNavClick(index) }
            )
        },
        containerColor = colorResource(R.color.blackBackground)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    color = colorResource(R.color.blackBackground)
                )
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Box(
                        modifier = Modifier.height(400.dp)
                    ) {
                    Image(
                        contentDescription = "",
                        painter = painterResource(R.drawable.fav),
                        modifier = Modifier
                            .padding(end = 16.dp, top = 48.dp)
                            .size(28.dp)
                            .align(Alignment.TopEnd)
                            .clickable {
                                if (isFavorite) {
                                    favoritesRepository.removeFavorite(film)
                                    isFavorite = false
                                    Toast.makeText(context, "Removed from My List", Toast.LENGTH_SHORT).show()
                                } else {
                                    favoritesRepository.addFavorite(film)
                                    isFavorite = true
                                    Toast.makeText(context, "Added to My List", Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                    AsyncImage(
                        model = film.Poster,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.1f
                    )
                    AsyncImage(
                        model = film.Poster,
                        contentDescription = null,
                        modifier = Modifier
                            .size(210.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .align(Alignment.BottomCenter),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        colorResource(R.color.black2),
                                        colorResource(R.color.black1)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, Float.POSITIVE_INFINITY)
                                )
                            )
                    )

                    Text(
                        text = film.Title,
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 27.sp
                        ),
                        modifier = Modifier
                            .padding(end = 16.dp, top = 48.dp)
                            .align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.star),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "IMDB: ${film.Imdb}",
                            color = Color.White
                        )

                        Icon(
                            painter = painterResource(R.drawable.time),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "Runtime: ${film.Time}",
                            color = Color.White
                        )

                        Icon(
                            painter = painterResource(R.drawable.cal),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "Release: ${film.Year}",
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (isInCart) {
                                Toast.makeText(context, "Already in cart", Toast.LENGTH_SHORT).show()
                            } else {
                                val added = cartRepository.addToCart(film)
                                if (added) {
                                    isInCart = true
                                    Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Already in cart", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = colorResource(R.color.pink),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val label = if (isInCart) {
                            "In Cart"
                        } else {
                            "Buy ${formatPrice(film.price)}"
                        }
                        Text(text = label)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Summary",
                        style = TextStyle(color = Color.White, fontSize = 16.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = film.Description,
                        style = TextStyle(color = Color.White, fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Actors",
                        style = TextStyle(color = Color.White, fontSize = 16.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(film.Casts.size) {
                            film.Casts[it].Actor?.let {
                                Text(
                                    text = "$it, ",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(film.Casts.size) {
                            AsyncImage(
                                model = film.Casts[it].PicUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(75.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(50.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
}

private fun formatPrice(price: Double): String {
    if (price <= 0.0) return "Free"
    return "$" + String.format(Locale.US, "%.2f", price)
}
