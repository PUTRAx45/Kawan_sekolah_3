package com.example.kawansekolah.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kawansekolah.data.model.UserRole

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    userRole: UserRole = UserRole.SISWA
) {
    val items = when (userRole) {
        UserRole.ADMIN -> listOf(
            Triple("Beranda", Icons.Default.Home, "admin_beranda"),
            Triple("Pengguna", Icons.Default.People, "admin_pengguna"),
            Triple("Akademik", Icons.Default.School, "admin_akademik"),
            Triple("Profil", Icons.Default.Person, "admin_profil")
        )
        UserRole.GURU -> listOf(
            Triple("Beranda", Icons.Default.Home, "beranda"),
            Triple("Nilai", Icons.Default.Edit, "guru_input"),
            Triple("Insight", Icons.Default.AutoAwesome, "insight_page"),
            Triple("Profil", Icons.Default.Person, "profil")
        )
        UserRole.WALI_KELAS -> listOf(
            Triple("Beranda", Icons.Default.Home, "beranda"),
            Triple("Kelas", Icons.Default.Class, "rekap_wali"),
            Triple("Insight", Icons.Default.AutoAwesome, "insight_page"),
            Triple("Profil", Icons.Default.Person, "profil")
        )
        UserRole.SISWA -> listOf(
            Triple("Beranda", Icons.Default.Home, "beranda"),
            Triple("Nilai", Icons.Default.Assessment, "nilai"),
            Triple("AI", Icons.Default.AutoAwesome, "ai"),
            Triple("Profil", Icons.Default.Person, "profil")
        )
        UserRole.ORANG_TUA -> listOf(
            Triple("Beranda", Icons.Default.Home, "beranda"),
            Triple("Nilai Anak", Icons.Default.Assessment, "nilai"),
            Triple("AI", Icons.Default.AutoAwesome, "ai"),
            Triple("Profil", Icons.Default.Person, "profil")
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.90f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            tonalElevation = 0.dp
        ) {
            items.forEach { (title, icon, route) ->
                val isSelected = currentRoute == route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    },
                    selected = isSelected,
                    alwaysShowLabel = false,
                    onClick = {
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
