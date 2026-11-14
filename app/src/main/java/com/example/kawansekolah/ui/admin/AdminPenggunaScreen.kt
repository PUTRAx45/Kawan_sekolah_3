package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.Pengguna
import com.example.kawansekolah.ui.components.EmptyStateView
import com.example.kawansekolah.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPenggunaScreen(
    userList: List<Pengguna> = emptyList(),
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToDaftarAccount: () -> Unit = {},
    onNavigateToPenugasanGuru: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) }

    val filterOptions = listOf("Semua", "Guru", "Wali kelas", "Siswa", "Orang tua", "Admin")

    val filteredList = remember(userList, searchQuery, selectedFilter) {
        userList.filter { user ->
            val matchesSearch = searchQuery.isBlank() || user.nama.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                1 -> user.role.equals("guru", ignoreCase = true) || user.role.equals("TEACHER", ignoreCase = true)
                2 -> user.role.equals("wali_kelas", ignoreCase = true) || user.role.equals("WALI_KELAS", ignoreCase = true)
                3 -> user.role.equals("siswa", ignoreCase = true) || user.role.equals("STUDENT", ignoreCase = true)
                4 -> user.role.equals("orang_tua", ignoreCase = true) || user.role.equals("PARENT", ignoreCase = true)
                5 -> user.role.equals("admin", ignoreCase = true) || user.role.equals("ADMIN", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Pengguna", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama atau username") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Horizontal (Semua, Guru, Wali kelas, Siswa, Orang tua, Admin)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterOptions.take(4).forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedFilter == idx,
                        onClick = { selectedFilter = idx },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Akun terdaftar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("${filteredList.size} akun", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "Belum ada akun pengguna terdaftar",
                        description = "Daftar akun pengguna sekolah akan tampil secara otomatis di sini setelah ditambahkan.",
                        icon = Icons.Default.Group
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList.size) { idx ->
                        val user = filteredList[idx]
                        val initials = if (user.nama.isNotBlank()) user.nama.split(" ").map { it.take(1) }.take(2).joinToString("") else "US"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetail(user.nama) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${user.role} • ${user.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Badge(containerColor = if (user.aktif) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)) {
                                    Text(if (user.aktif) "Aktif" else "Nonaktif", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (user.aktif) Color(0xFF166534) else Color(0xFF64748B), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToPenugasanGuru,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Penugasan Guru", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onNavigateToDaftarAccount,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Daftarkan Akun", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
