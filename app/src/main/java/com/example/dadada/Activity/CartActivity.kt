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
import com.example.dadada.Repository.CartRepository
import com.example.dadada.ui.theme.DadadaTheme
import java.util.Locale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

class CartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DadadaTheme {
                CartScreen(
                    onMovieClick = { movie ->
                        val intent = Intent(this, DetailMovieActivity::class.java)
                        intent.putExtra("object", movie)
                        startActivity(intent)
                    },
                    onCheckoutClick = {
                        startActivity(Intent(this, CheckoutActivity::class.java))
                    },
                    onBottomNavClick = { index -> openBottomNavDestination(index, currentIndex = 2) }
                )
            }
        }
    }
}

@Composable
private fun CartScreen(
    onMovieClick: (FilmItemModel) -> Unit,
    onCheckoutClick: () -> Unit,
    onBottomNavClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val userScope = AuthSessionScope.resolveUserScope(context)
    val cartRepository = remember(context, userScope) { CartRepository(context, userScope) }
    val cartItems = remember { mutableStateListOf<FilmItemModel>() }

    LaunchedEffect(userScope) {
        cartItems.clear()
        cartItems.addAll(cartRepository.getCartItems())
    }

    val totalPrice = cartItems.sumOf { it.price }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                initialSelectedItem = 2,
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
                    text = "Cart",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total: ${formatPrice(totalPrice)}",
                    color = Color(0xFF8CFF98),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (cartItems.isEmpty()) {
                    Text(
                        text = "Your cart is empty. Tap Buy on movie details.",
                        color = Color.LightGray
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(
                            items = cartItems,
                            key = { "${it.Title}-${it.Year}-${it.Poster}" }
                        ) { movie ->
                            CartMovieRow(
                                movie = movie,
                                onMovieClick = onMovieClick,
                                onRemoveClick = {
                                    if (cartRepository.removeFromCart(movie)) {
                                        cartItems.remove(movie)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onCheckoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.pink),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Checkout")
                    }
                }
            }
        }
    }
}

@Composable
private fun CartMovieRow(
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
                text = "Price: ${formatPrice(movie.price)}",
                color = Color(0xFF8CFF98),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Image(
            painter = painterResource(R.drawable.ic_bin),
            contentDescription = "Remove from cart",
            colorFilter = ColorFilter.tint(colorResource(R.color.pink)),
            modifier = Modifier
                .size(24.dp)
                .clickable { onRemoveClick() }
        )
    }
}

private fun formatPrice(price: Double): String {
    if (price <= 0.0) return "Free"
    return "$" + String.format(Locale.US, "%.2f", price)
}
