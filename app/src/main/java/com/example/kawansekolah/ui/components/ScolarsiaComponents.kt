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

// 9. Labeled TextField
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

// 10. White Rounded Card
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

// 11. Colored Progress Bar (Green / Yellow / Red)
@Composable
fun ColoredProgressBar(
    score: Double,
    modifier: Modifier = Modifier
) {
    val progress = (score / 100.0).coerceIn(0.0, 1.0).toFloat()
    val barColor = when {
        score >= 80 -> SuccessGreen
        score >= 70 -> WarningYellow
        else -> DangerRed
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "${score.toInt()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = barColor
        )
    }
}

// 12. Info Card (Light Blue Card)
@Composable
fun InfoCard(
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

// 13. Warning Card (Cream Card)
@Composable
fun WarningCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = WarningYellowBg,
        border = BorderStroke(1.dp, WarningYellowStroke)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarningYellow)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, fontSize = 11.sp, color = WarningYellow)
            }
            StatusBadge(type = StatusBadgeType.PERLU_DICEK)
        }
    }
}

// 14. Dark Profile Header Card
@Composable
fun DarkProfileHeaderCard(
    title: String,
    subtitle: String,
    email: String,
    initials: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScolarsiaAvatar(
                initials = initials,
                size = 56.dp,
                variant = AvatarVariant.SOLID_BLUE
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = ColorWhite.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = email,
                    fontSize = 11.sp,
                    color = ColorWhite.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// 15. Empty State View
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            fontSize = 12.sp,
            color = TextMuted,
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
