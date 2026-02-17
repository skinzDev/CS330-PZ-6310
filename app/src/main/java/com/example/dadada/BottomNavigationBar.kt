package com.example.dadada

import android.widget.Toast
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavigationBar() {
    val bottomMenuItemsList = prepareBottomMenu()
    val contextForToast = LocalContext.current
    var selectedItem by remember {
        mutableStateOf(0)
    }

    NavigationBar(
        contentColor = colorResource(id = R.color.white),
        containerColor = colorResource(id = R.color.black3),
        tonalElevation = 0.dp
    ) {
        bottomMenuItemsList.forEachIndexed { index, bottomMenuItem ->
            if (index == 2) {
                Spacer(modifier = Modifier.width(64.dp))
            }

            NavigationBarItem(
                selected = (selectedItem == index),
                onClick = {
                    selectedItem = index
                    Toast.makeText(contextForToast, bottomMenuItem.label, Toast.LENGTH_SHORT).show()
                },
                icon = {
                    Icon(
                        painter = bottomMenuItem.icon,
                        contentDescription = bottomMenuItem.label,
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp)
                    )
                },
                label = {
                    Text(
                        text = bottomMenuItem.label,
                        style = TextStyle(
                            color = colorResource(id = R.color.white),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(top = 14.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(id = R.color.white),
                    unselectedIconColor = colorResource(id = R.color.white),
                    selectedTextColor = colorResource(id = R.color.white),
                    unselectedTextColor = colorResource(id = R.color.white),
                    indicatorColor = Color.Transparent
                ),
                alwaysShowLabel = true,
                enabled = true
            )
        }
    }
}

data class BottomMenuItems(
    val label:String,
    val icon: Painter
)

@Composable
fun prepareBottomMenu():List<BottomMenuItems> {
    return listOf(
        BottomMenuItems(
            label = "Home",
            icon = painterResource(id=R.drawable.btn_1)
        ),
        BottomMenuItems(
            label = "List",
            icon = painterResource(id=R.drawable.btn_2)
        ),
        BottomMenuItems(
            label = "Cart",
            icon = painterResource(id=R.drawable.btn_3)
        ),
        BottomMenuItems(
            label = "Profile",
            icon = painterResource(id=R.drawable.btn_4)
        )
    )
}
