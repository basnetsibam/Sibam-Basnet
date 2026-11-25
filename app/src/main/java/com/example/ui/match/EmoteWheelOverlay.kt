package com.example.ui.match

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmoteCatalog
import com.example.data.EmoteSpec
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun EmoteWheelOverlay(
    equippedEmoteId: String,
    unlockedEmoteIds: Set<String>,
    onEquipEmoteOnly: (String) -> Unit,
    onEquipAndTriggerEmote: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val emotes = remember { EmoteCatalog.all }
    val equippedEmote = EmoteCatalog.byId(equippedEmoteId)
    var performOnTap by remember { mutableStateOf(true) }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }

    val wheelScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "emoteWheelScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.78f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .testTag("emote_wheel_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header & Tap Mode Switch
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎭 TACTICAL EMOTE WHEEL",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Active: ${equippedEmote.emoji} ${equippedEmote.name} (\"${equippedEmote.shoutText}\")",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (performOnTap) NanoEmerald.copy(alpha = 0.22f) else TacticalAmber.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .border(
                                    1.dp,
                                    if (performOnTap) NanoEmerald else TacticalAmber,
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    performOnTap = !performOnTap
                                    feedbackBanner = if (performOnTap) {
                                        "⚡ Tap any unlocked emote to Equip & Perform!"
                                    } else {
                                        "📌 Equip-Only Mode: tap slices to switch active emote"
                                    }
                                }
                                .testTag("emote_wheel_mode_toggle")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (performOnTap) "⚡ Equip + Go" else "📌 Equip Only",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (performOnTap) NanoEmerald else TacticalAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = ObsidianElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onDismiss)
                                .testTag("emote_wheel_close_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✖️", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Adaptive Radial Wheel Container
            BoxWithConstraints(
                modifier = Modifier
                    .size(336.dp)
                    .scale(wheelScale),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val boxPx = with(density) { maxWidth.toPx() }
                val orbitRadiusPx = boxPx * 0.34f
                val nodeSizeDp = 88.dp
                val nodeHalfPx = with(density) { (nodeSizeDp / 2).toPx() }

                val activeIndex = emotes.indexOfFirst { it.id == equippedEmote.id }.coerceAtLeast(0)
                val sectorSweep = 360f / emotes.size.coerceAtLeast(1)

                // Flat non-gradient radial geometry & highlighted active sector
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension * 0.48f
                    val innerRadius = size.minDimension * 0.19f

                    // Outer dark ring base
                    drawCircle(
                        color = ObsidianSurface.copy(alpha = 0.92f),
                        radius = outerRadius,
                        center = center
                    )

                    // Highlight active emote wedge (flat alpha fill, no gradient)
                    val startAngle = -90f + activeIndex * sectorSweep - (sectorSweep / 2f)
                    drawArc(
                        color = equippedEmote.accentColor.copy(alpha = 0.18f),
                        startAngle = startAngle,
                        sweepAngle = sectorSweep,
                        useCenter = true,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = androidx.compose.ui.geometry.Size(outerRadius * 2f, outerRadius * 2f)
                    )

                    // Concentric tactical rings
                    drawCircle(
                        color = TacticalBorder,
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.35f),
                        radius = orbitRadiusPx,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        color = ObsidianVoid,
                        radius = innerRadius,
                        center = center
                    )

                    // Radial sector dividers
                    emotes.indices.forEach { i ->
                        val dividerDeg = -90f + (i - 0.5f) * sectorSweep
                        val rad = dividerDeg * (PI / 180.0)
                        val sx = center.x + (innerRadius * cos(rad)).toFloat()
                        val sy = center.y + (innerRadius * sin(rad)).toFloat()
                        val ex = center.x + (outerRadius * cos(rad)).toFloat()
                        val ey = center.y + (outerRadius * sin(rad)).toFloat()
                        drawLine(
                            color = TacticalBorder.copy(alpha = 0.85f),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 2f
                        )
                    }
                }

                // Radial Emote Nodes around Center Hub
                emotes.forEachIndexed { index, emote ->
                    val isUnlocked = emote.id in unlockedEmoteIds || (emote.costCredits == 0 && emote.costGems == 0)
                    val isEquipped = emote.id == equippedEmote.id
                    val angleRad = (-90.0 + index * sectorSweep) * (PI / 180.0)
                    val offsetX = (orbitRadiusPx * cos(angleRad)).roundToInt()
                    val offsetY = (orbitRadiusPx * sin(angleRad)).roundToInt()

                    EmoteWheelSlotNode(
                        emote = emote,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = offsetX,
                                    y = offsetY
                                )
                            }
                            .size(nodeSizeDp),
                        onClick = {
                            if (!isUnlocked) {
                                val price = if (emote.costGems > 0) "${emote.costGems} 💎" else "${emote.costCredits} 🪙"
                                feedbackBanner = "🔒 ${emote.name} locked ($price) — unlock in Armory/Store!"
                            } else if (performOnTap) {
                                onEquipAndTriggerEmote(emote.id)
                                onDismiss()
                            } else {
                                onEquipEmoteOnly(emote.id)
                                feedbackBanner = "✅ Equipped ${emote.emoji} ${emote.name} to quick slot!"
                            }
                        }
                    )
                }

                // Center Hub: Instant Perform Active Emote
                Surface(
                    color = ObsidianCard,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(98.dp)
                        .border(2.5.dp, equippedEmote.accentColor, CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            onEquipAndTriggerEmote(equippedEmote.id)
                            onDismiss()
                        }
                        .testTag("emote_wheel_center_trigger")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = equippedEmote.emoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PERFORM",
                            style = MaterialTheme.typography.labelSmall,
                            color = equippedEmote.accentColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Inline Feedback / Hint Footer
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = feedbackBanner
                        ?: "💡 Tap any unlocked emote to equip & perform in-arena, or tap the center hub to repeat ${equippedEmote.emoji} ${equippedEmote.name}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (feedbackBanner?.startsWith("🔒") == true) TacticalAmber else TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmoteWheelSlotNode(
    emote: EmoteSpec,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = when {
        isEquipped -> emote.accentColor
        isUnlocked -> emote.accentColor.copy(alpha = 0.55f)
        else -> TacticalBorder
    }
    val containerColor = when {
        isEquipped -> ObsidianElevated
        isUnlocked -> ObsidianCard
        else -> ObsidianVoid.copy(alpha = 0.88f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .border(
                width = if (isEquipped) 2.5.dp else 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("emote_wheel_slot_${emote.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isUnlocked) emote.emoji else "🔒",
                fontSize = 24.sp
            )
            Text(
                text = emote.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) TextPrimary else TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isEquipped) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = when {
                    isEquipped -> "EQUIPPED"
                    isUnlocked -> emote.shoutText
                    emote.costGems > 0 -> "${emote.costGems} 💎"
                    else -> "${emote.costCredits} 🪙"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isEquipped -> emote.accentColor
                    isUnlocked -> TextSecondary
                    else -> TacticalAmber
                },
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
