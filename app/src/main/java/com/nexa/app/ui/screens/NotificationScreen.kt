package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.NotificationItem
import com.nexa.app.data.Repository
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(nav: NavController, repo: Repository) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun reload() {
        loading = true
        error = null
        scope.launch {
            repo.getNotifications()
                .onSuccess {
                    notifications = it.notifications
                    loading = false
                }
                .onFailure {
                    error = it.message
                    loading = false
                }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
    ) {
        // ═══ Header ═══
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(NexaSurface2)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(13.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { nav.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, null, tint = NexaText, modifier = Modifier.size(18.dp))
            }
            Text(
                "Notifications",
                color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            val unread = notifications.count { !it.read }
            if (unread > 0) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexaGreen.copy(alpha = 0.15f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch {
                                repo.markAllNotificationsRead()
                                reload()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text("Read all", color = NexaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(Modifier.width(60.dp))
            }
        }

        // ═══ Content ═══
        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Loading…", color = NexaMuted, fontSize = 13.sp)
            }

            error != null -> Box(Modifier.fillMaxSize().padding(28.dp), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = NexaRed, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Tap to retry",
                        color = NexaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { reload() }
                    )
                }
            }

            notifications.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)
                ) {
                    Box(
                        Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(NexaGreen.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            null, tint = NexaGreen,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "No notifications yet",
                        color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "KYC updates, deposits, withdrawals —\nall alerts will appear here.",
                        color = NexaMuted, fontSize = 13.sp,
                        textAlign = TextAlign.Center, lineHeight = 19.sp
                    )
                }
            }

            else -> Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                notifications.forEach { n ->
                    NotificationCard(
                        n = n,
                        onMarkRead = {
                            scope.launch {
                                repo.markNotificationRead(n.id ?: "")
                                notifications = notifications.map {
                                    if (it.id == n.id) it.copy(read = true) else it
                                }
                            }
                        },
                        onDelete = {
                            scope.launch {
                                repo.deleteNotification(n.id ?: "")
                                notifications = notifications.filter { it.id != n.id }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    n: NotificationItem,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, tint) = when (n.type) {
        "kyc" -> Icons.Filled.Shield to NexaGreen
        "deposit" -> Icons.Filled.TrendingDown to NexaGreen
        "cashout" -> Icons.Filled.TrendingUp to NexaTeal
        "transfer" -> Icons.Filled.SwapHoriz to NexaTeal
        else -> Icons.Filled.Info to NexaMuted
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (n.read) NexaSurface.copy(alpha = 0.6f) else NexaSurface)
            .border(
                1.dp,
                if (n.read) NexaBorder.copy(alpha = 0.06f) else NexaBorder.copy(alpha = 0.18f),
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (!n.read) onMarkRead() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        n.title,
                        color = NexaText,
                        fontWeight = if (n.read) FontWeight.SemiBold else FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (!n.read) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NexaGreen)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    n.body,
                    color = NexaMuted,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    formatDate(n.createdAt),
                    color = NexaDim,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Delete,
                    null,
                    tint = NexaDim,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val d = parser.parse(iso) ?: return iso.take(10)
        val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
        fmt.format(d)
    } catch (_: Exception) { iso.take(10) }
}
