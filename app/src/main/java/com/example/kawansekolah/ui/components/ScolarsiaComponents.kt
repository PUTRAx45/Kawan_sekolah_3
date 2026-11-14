package com.example.kawansekolah.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.ui.theme.*

// 1. Primary Button (#1C64F2, 12.dp, White Bold)
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue,
            disabledContainerColor = DisabledGray,
            disabledContentColor = Color.White
        ),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// 2. Secondary Button (Border #1C64F2, 12.dp, #1C64F2 SemiBold)
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        border = BorderStroke(1.dp, PrimaryBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = PrimaryBlue
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
        }
    }
}

// Backward-compatible alias for ScolarsiaOutlineButton
@Composable
fun ScolarsiaOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    SecondaryButton(text = text, onClick = onClick, modifier = modifier, enabled = enabled, icon = icon)
}

// 3. Danger Button (White, Border #FCA5A5, #E02424 SemiBold)
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = DangerRed
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = DangerRed)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DangerRed)
        }
    }
}

// 4. Disabled Button (#CAD5E2 / Muted Blue, Disabled state, White Text)
@Composable
fun DisabledButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = {},
        enabled = false,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = DisabledGray,
            disabledContentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// 5. Filter Chip (Active: #EBF2FE / #1C64F2, Inactive: #F3F4F6 / #6B7280, 20.dp Capsule)
@Composable
fun ScolarsiaFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) PrimarySoftBlue else BackgroundMuted,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) PrimaryBlue else TextMuted
            )
        }
    }
}

// 6. Status Badges (Pill Capsule, Padding 10.dp horizontal, 4.dp vertical)
enum class StatusBadgeType(val label: String, val containerColor: Color, val textColor: Color) {
    LENGKAP("Lengkap", SuccessGreenBg, SuccessGreen),
    PERLU_LATIHAN("Perlu latihan", WarningYellowBg, WarningYellow),
    PERLU_PERHATIAN("Perlu perhatian", DangerRedBg, DangerRed),
    INFO("Info", PrimarySoftBlue, PrimaryBlue),
    BELUM_LENGKAP("Belum lengkap", BackgroundMuted, TextMuted),
    AKTIF("Aktif", SuccessGreenBg, SuccessGreen),
    PERLU_DICEK("PERLU DICEK", WarningYellowBg, WarningYellow),
    KUAT("KUAT", SuccessGreenBg, SuccessGreen),
    LATIHAN("LATIHAN", WarningYellowBg, WarningYellow),
    RESIKO("RESIKO", DangerRedBg, DangerRed)
}

@Composable
fun StatusBadge(
    type: StatusBadgeType,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    Surface(
        shape = CircleShape,
        color = type.containerColor,
        modifier = modifier
    ) {
        Text(
            text = customText ?: type.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = type.textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// 7. Avatar (Circle with Initials, 3 Variant Colors)
enum class AvatarVariant(val containerColor: Color, val textColor: Color) {
    SOLID_BLUE(PrimaryBlue, ColorWhite),
    SOFT_BLUE(PrimarySoftBlue, PrimaryBlue),
    DARK_NAVY(NavyContainer, ColorWhite)
}

@Composable
fun ScolarsiaAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    variant: AvatarVariant = AvatarVariant.SOLID_BLUE
) {
    val cleanInitials = if (initials.isBlank()) "S" else initials.trim().take(2).uppercase()

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(variant.containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cleanInitials,
            fontSize = (size.value * 0.38).sp,
            fontWeight = FontWeight.Bold,
            color = variant.textColor
        )
    }
}

// 8. Chat Input Field (Pill shape, #E5E7EB border, #1C64F2 circle button with ArrowUpward)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Tulis pertanyaan tentang nilai...",
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, fontSize = 13.sp, color = TextMuted) },
                singleLine = true,
                enabled = enabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (value.isNotBlank() && enabled) PrimaryBlue else DisabledGray)
                    .clickable(enabled = value.isNotBlank() && enabled) { onSendClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Kirim",
                    tint = ColorWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 9. Kotak Saran / Callout Cards (Info, Fokus, Berhasil)
@Composable
fun CalloutInfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PrimarySoftBlue,
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, color = PrimaryBlue)
        }
    }
}

@Composable
fun FocusCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, WarningYellowStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FOKUS BELAJAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WarningYellow
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SuccessCard(
    title: String = "Semua nilai lengkap",
    subtitle: String = "Siap dianalisis.",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SuccessGreenBg,
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SuccessGreen)
                Text(subtitle, fontSize = 11.sp, color = SuccessGreen.copy(alpha = 0.8f))
            }
        }
    }
}

// 10. Hero Card (Rata-rata Semester)
@Composable
fun HeroScoreCard(
    title: String = "RATA-RATA SEMESTER",
    scoreText: String = "82,87",
    trendText: String? = "Stabil",
    subtitleText: String? = "4 mata pelajaran • Semester Ganjil",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorWhite.copy(alpha = 0.85f)
                )

                if (trendText != null) {
                    Surface(
                        shape = CircleShape,
                        color = ColorWhite.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = trendText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = scoreText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ColorWhite
            )

            if (subtitleText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    fontSize = 11.sp,
                    color = ColorWhite.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// 11. Navy Card (Periode Penilaian)
@Composable
fun NavyPeriodeCard(
    periodeName: String = "Semester Ganjil 2026/2027",
    badgeText: String = "AKTIF",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PERIODE PENILAIAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorWhite.copy(alpha = 0.7f)
                )

                Surface(
                    shape = CircleShape,
                    color = ColorWhite.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = periodeName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorWhite
            )
        }
    }
}

// 12. Score Grid Item & Bar/Topik
@Composable
fun ScoreGridItem(
    label: String,
    percentageLabel: String,
    scoreValue: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$label • $percentageLabel",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (scoreValue > 0) "${scoreValue.toInt()}" else "-",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TopicProgressBar(
    topicName: String,
    score: Double,
    modifier: Modifier = Modifier
) {
    val progress = (score / 100.0).coerceIn(0.0, 1.0).toFloat()
    val barColor = when {
        score >= 80 -> SuccessGreen
        score >= 70 -> WarningYellow
        else -> DangerRed
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(topicName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(if (score > 0) "${score.toInt()}" else "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = barColor)
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f)
        )
    }
}

// 13. Link Row Item
@Composable
fun LinkRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimarySoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 14. User Row Item
@Composable
fun UserRowItem(
    name: String,
    roleAndSubject: String,
    statusText: String = "Aktif",
    avatarVariant: AvatarVariant = AvatarVariant.SOFT_BLUE,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScolarsiaAvatar(initials = name, size = 42.dp, variant = avatarVariant)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = roleAndSubject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusBadge(type = StatusBadgeType.AKTIF, customText = statusText)
        }
    }
}

// 15. Student Row Item
@Composable
fun StudentRowItem(
    name: String,
    nis: String,
    score: Double? = null,
    isComplete: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScolarsiaAvatar(initials = name, size = 42.dp, variant = AvatarVariant.SOFT_BLUE)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "NIS $nis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(
                    type = if (isComplete) StatusBadgeType.LENGKAP else StatusBadgeType.BELUM_LENGKAP
                )

                if (score != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (score > 0) String.format(java.util.Locale.getDefault(), "%.1f", score) else "-",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// 16. Setting Row Item
@Composable
fun SettingRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimarySoftBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (badgeText != null) {
            StatusBadge(type = StatusBadgeType.INFO, customText = badgeText)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

// 17. Labeled TextField
@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = singleLine,
        enabled = enabled,
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            }
        } else null,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

// 18. White Rounded Card
@Composable
fun ScolarsiaCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = CardBorderStroke,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

// 19. Empty State View
@Composable
fun EmptyStateView(
    title: String = "Belum ada data",
    description: String = "Data akan muncul secara otomatis di sini setelah diisi.",
    icon: ImageVector = Icons.Default.Inbox,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(BackgroundMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(actionButtonText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorWhite)
            }
        }
    }
}
