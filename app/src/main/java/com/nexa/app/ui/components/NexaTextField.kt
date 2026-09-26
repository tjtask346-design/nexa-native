package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.ui.theme.*

@Composable
fun NexaTextField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    error: Boolean = false
) {
    val focused = remember { mutableStateOf(false) }
    val borderColor = when {
        error -> NexaRed
        focused.value -> NexaGreen.copy(alpha = 0.55f)
        else -> NexaBorder.copy(alpha = 0.09f)
    }
    val bgColor = if (focused.value) NexaSurface2 else NexaSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(17.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        if (leadingIcon != null) {
            Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                leadingIcon()
            }
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(
                color = NexaText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = SolidColor(NexaGreen),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focused.value = it.isFocused }
        ) { innerTextField ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 17.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = NexaDim,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                innerTextField()
            }
        }
    }
}

/**
 * Amount input — bigger, with $ prefix
 */
@Composable
fun NexaAmountField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00"
) {
    val focused = remember { mutableStateOf(false) }
    val borderColor = if (focused.value) NexaGreen.copy(alpha = 0.5f) else NexaBorder.copy(alpha = 0.09f)
    val bgColor = if (focused.value) NexaSurface2 else NexaSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$", color = NexaGreen, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(6.dp))
        BasicTextField(
            value = value,
            onValueChange = { new ->
                if (new.isEmpty() || new.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onChange(new)
            },
            singleLine = true,
            textStyle = TextStyle(
                color = NexaText,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            cursorBrush = SolidColor(NexaGreen),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focused.value = it.isFocused }
        ) { innerTextField ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = androidx.compose.ui.graphics.Color(0xFF2C4438),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                innerTextField()
            }
        }
    }
}

/**
 * Simple plain input — used for TrxID, phone numbers etc.
 */
@Composable
fun NexaPlainField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val focused = remember { mutableStateOf(false) }
    val borderColor = if (focused.value) NexaGreen.copy(alpha = 0.5f) else NexaBorder.copy(alpha = 0.09f)
    val bgColor = if (focused.value) NexaSurface2 else NexaSurface

    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(
            color = NexaText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        ),
        cursorBrush = SolidColor(NexaGreen),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .onFocusChanged { focused.value = it.isFocused }
            .padding(horizontal = 16.dp, vertical = 15.dp)
    ) { innerTextField ->
        Box(Modifier.fillMaxWidth()) {
            if (value.isEmpty()) {
                Text(placeholder, color = NexaDim, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            innerTextField()
        }
    }
}
