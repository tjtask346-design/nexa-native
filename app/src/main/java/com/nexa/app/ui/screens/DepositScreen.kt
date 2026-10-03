package com.nexa.app.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.nexa.app.R
import com.nexa.app.data.Repository
import com.nexa.app.data.User
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.theme.*

data class CryptoOption(
    val key: String,
    val name: String,
    val network: String,
    val color: Color,
    val imageRes: Int
)

private val OPTIONS = listOf(
    CryptoOption("usdt", "USDT", "BEP20 · BSC", Color(0xFF26A17B), R.drawable.usdt),
    CryptoOption("ltc",  "Litecoin", "LTC", Color(0xFF345D9D), R.drawable.ltc),
    CryptoOption("nexa", "Nexa User", "Instant · 0% fee", NexaGreen, R.drawable.nexa_logo)
)

@Composable
fun DepositScreen(
    nav: NavController,
    repo: Repository,
    initialCrypto: String = ""
) {
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selected by remember {
        mutableStateOf(initialCrypto.takeIf { it.isNotBlank() })
    }

    LaunchedEffect(Unit) {
        repo.me().onSuccess { user = it.user }
        loading = false
    }

    fun addressFor(key: String): String? = when (key) {
        "usdt" -> user?.wallets?.bscAddress?.takeIf { it.isNotBlank() }
        "ltc"  -> user?.ltcAddress?.takeIf { it.isNotBlank() }
        "nexa" -> user?.accountNumber?.takeIf { it.isNotBlank() }
        else   -> null
    }

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexaIconButton(onClick = { nav.popBackStack() }) {
                Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "Deposit",
                color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            when {
                loading -> Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    Text("Loading…", color = NexaMuted, fontSize = 14.sp)
                }

                user == null -> KycRequiredCard(nav)

                user?.wallets?.bscAddress.isNullOrBlank() &&
                user?.ltcAddress.isNullOrBlank() -> KycRequiredCard(nav)

                selected == null -> CryptoPicker(OPTIONS) { key -> selected = key }

                else -> {
                    val opt = OPTIONS.first { it.key == selected }
                    val address = addressFor(selected!!)

                    if (address.isNullOrBlank()) {
                        MissingAddressView(opt) { selected = null }
                    } else {
                        AddressView(
                            option = opt,
                            address = address,
                            onBack = { selected = null },
                            onCopy = {
                                clipboard.setText(AnnotatedString(address))
                                Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun KycRequiredCard(nav: NavController) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha=0.15f), NexaTeal.copy(alpha=0.12f))))
                .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Shield, null, tint = NexaGreen, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text("KYC Required", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Complete KYC verification to get your\ndeposit address and unlock crypto deposits.",
            color = NexaMuted, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 19.sp
        )
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(NexaGreen, NexaTeal)))
                .clickable { nav.navigate(com.nexa.app.nav.Routes.KYC) }
                .padding(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text("Go to KYC", color = Color(0xFF04140D), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CryptoPicker(options: List<CryptoOption>, onPick: (String) -> Unit) {
    Column {
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose cryptocurrency",
            color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
            letterSpacing = (-0.3).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Send crypto from any external wallet or exchange.",
            color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(20.dp))

        options.forEach { opt ->
            Box(
                Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(18.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPick(opt.key) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(15.dp))
                            .background(opt.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(opt.imageRes),
                            contentDescription = opt.name,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(opt.name, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(opt.network, color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("›", color = NexaDim, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun MissingAddressView(option: CryptoOption, onBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "← Back",
                color = NexaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBack() }
            )
        }

        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(18.dp))
                .background(option.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(option.imageRes),
                contentDescription = option.name,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text("${option.name} address", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Spacer(Modifier.height(14.dp))

        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(NexaSurface)
                .border(1.dp, NexaRed.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Lock, null, tint = NexaRed, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    "Address not generated yet",
                    color = NexaText, fontWeight = FontWeight.Bold, fontSize = 14.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your ${option.name} address will be generated after your KYC is approved by an admin.",
                    color = NexaMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun AddressView(
    option: CryptoOption,
    address: String,
    onBack: () -> Unit,
    onCopy: () -> Unit
) {
    val qrBitmap = remember(address) { generateQr(address) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "← Back",
                color = NexaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBack() }
            )
        }

        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(18.dp))
                .background(option.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(option.imageRes),
                contentDescription = option.name,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "Deposit ${option.name}",
            color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            option.network,
            color = option.color, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(option.color.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(22.dp))

        if (option.key == "nexa") {
            // ═══ NEXA — Account Number (not username) ═══
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "YOUR ACCOUNT NUMBER",
                        color = NexaMuted, fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        address,
                        color = NexaGreen,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            CopyButton(onCopy)
        } else {
            Box(
                Modifier.size(200.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).padding(14.dp)
            ) {
                if (qrBitmap != null) {
                    Image(qrBitmap.asImageBitmap(), "QR", modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(Modifier.height(18.dp))

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Address", color = NexaMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        address,
                        color = NexaText, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace, lineHeight = 18.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            CopyButton(onCopy)
        }

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(NexaTeal.copy(alpha = 0.07f))
                .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Info, null, tint = NexaTeal, modifier = Modifier.size(16.dp))
            Text(
                if (option.key == "nexa")
                    "Share this account number with another Nexa user. They can send you money instantly with 0% fee."
                else
                    "Send only ${option.name} on ${option.network} to this address. Wrong network may cause permanent loss.",
                color = Color(0xFF7DD3C8), fontSize = 11.5.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CopyButton(onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(NexaGreen.copy(alpha = 0.13f))
            .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.ContentCopy, null, tint = NexaGreen, modifier = Modifier.size(16.dp))
            Text("Copy", color = NexaGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

private fun generateQr(content: String): Bitmap? {
    if (content.isBlank()) return null
    return try {
        val size = 512
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) for (y in 0 until size)
            b.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        b
    } catch (_: Exception) { null }
}
