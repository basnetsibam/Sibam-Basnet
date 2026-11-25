package com.example.ui.match

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KillFeedOverlay(
    entries: List<KillFeedEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return

    Column(
        modifier = modifier.testTag("kill_feed_overlay"),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        entries.take(4).forEach { entry ->
            val fadeAlpha = (entry.ttlSec / 1.2f).coerceIn(0.25f, 1f)
            val borderColor = when {
                entry.kind == KillFeedKind.REVIVE -> NanoEmerald
                entry.kind == KillFeedKind.ZONE -> CombatCrimson
                entry.isPlayerHighlight -> TacticalAmber
                entry.kind == KillFeedKind.KNOCKDOWN -> ElectricCyan.copy(alpha = 0.7f)
                else -> TacticalBorder
            }
            val bgColor = if (entry.isPlayerHighlight) {
                ObsidianElevated.copy(alpha = 0.90f)
            } else {
                ObsidianSurface.copy(alpha = 0.80f)
            }

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .alpha(fadeAlpha)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .testTag("kill_feed_entry_${entry.id}")
            ) {
                if (entry.killerName != null || entry.victimName != null) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        entry.killerName?.let { killer ->
                            Text(
                                text = killer,
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    entry.kind == KillFeedKind.ZONE -> CombatCrimson
                                    entry.isPlayerHighlight -> TacticalAmber
                                    else -> ElectricCyan
                                },
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        KillFeedMethodChip(entry)

                        entry.victimName?.let { victim ->
                            Text(
                                text = victim,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (entry.kind == KillFeedKind.REVIVE) NanoEmerald else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (entry.isPlayerHighlight) TacticalAmber else TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = entry.text,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.isPlayerHighlight) TacticalAmber else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KillFeedMethodChip(entry: KillFeedEntry) {
    val (icon, tint, tagText) = when (entry.kind) {
        KillFeedKind.REVIVE -> Triple(Icons.Default.Favorite, NanoEmerald, entry.methodLabel.ifBlank { "REVIVE" })
        KillFeedKind.ZONE -> Triple(Icons.Default.Warning, CombatCrimson, "ZONE")
        KillFeedKind.KNOCKDOWN -> Triple(Icons.Default.GpsFixed, ElectricCyan, "${entry.methodLabel} KNOCK")
        KillFeedKind.ELIMINATION -> {
            val method = entry.methodLabel.ifBlank { "ELIM" }
            val ic = when {
                method in listOf("Jeep", "Auto-Rickshaw", "Supercar", "Bike") -> Icons.Default.DirectionsCar
                method == "Arc Strike" -> Icons.Default.Bolt
                else -> Icons.Default.GpsFixed
            }
            Triple(ic, if (entry.isPlayerHighlight) TacticalAmber else CombatCrimson, method)
        }
        KillFeedKind.SYSTEM -> Triple(Icons.Default.Notifications, TextSecondary, entry.methodLabel)
    }

    Surface(
        color = tint.copy(alpha = 0.18f),
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tagText,
                tint = tint,
                modifier = Modifier.size(11.dp)
            )
            if (tagText.isNotBlank()) {
                Text(
                    text = tagText.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
