package com.nexa.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nexa.app.R

val PlusJakarta = FontFamily(
    Font(R.font.plus_jakarta_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_extrabold, FontWeight.ExtraBold)
)

val NexaTypography = Typography(
    displayLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, letterSpacing = (-1.6).sp),
    headlineLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, letterSpacing = (-0.7).sp),
    headlineMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Medium, fontSize = 13.5.sp),
    bodySmall = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp, letterSpacing = 0.5.sp)
)
