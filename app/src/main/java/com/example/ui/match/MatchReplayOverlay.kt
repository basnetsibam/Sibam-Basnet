package com.example.ui.match

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.InfineMapBlueprint
import com.example.data.ObstacleKind
import com.example.data.ReplayFrameSnapshot
import com.example.ui.theme.CombatCrimson
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
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun MatchReplayOverlay(
    title: String,
    subtitle: String,
    frames: List<ReplayFrameSnapshot>,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val safeFrames = remember(frames) {
        if (frames.isNotEmpty()) frames else listOf(
            ReplayFrameSnapshot(
                timeSec = 0,
                playerX = 1200f,
                playerY = 1200f,
                playerAimRad = 0f,
                playerHp = 100,
                isKnocked = false,
                safeZoneX = 1200f,
                safeZoneY = 1200f,
                safeZoneRadius = 1180f,
                aliveCount = 1,
                kills = 0,
                eventText = "No telemetry frames captured"
            )
        )
    }

    var currentIndex by remember(safeFrames) { mutableIntStateOf(0) }
    var isPlaying by remember(safeFrames) { mutableStateOf(true) }
    var speedMultiplier by remember { mutableFloatStateOf(1f) }
    var followPlayerCam by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying, speedMultiplier, safeFrames) {
        while (isPlaying && safeFrames.size > 1) {
            val stepMs = (650f / speedMultiplier).toLong().coerceAtLeast(120L)
            delay(stepMs)
            if (currentIndex < safeFrames.lastIndex) {
                currentIndex++
            } else {
                isPlaying = false
            }
        }
    }

    val currentFrame = safeFrames[currentIndex.coerceIn(0, safeFrames.lastIndex)]
    val eventMilestones = remember(safeFrames) {
        safeFrames.mapIndexedNotNull { idx, f ->
            if (!f.eventText.isNullOrBlank()) idx to f else null
        }.take(8)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("match_replay_overlay")
    ) {
        // Replay Tactical Arena Canvas
        ReplayArenaCanvas(
            frame = currentFrame,
            followPlayer = followPlayerCam,
            modifier = Modifier.fillMaxSize()
        )

        // Top Telemetry Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = ObsidianSurface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) CombatCrimson else TacticalAmber)
                            )
                            Text(
                                text = "MATCH REPLAY • $title",
                                style = MaterialTheme.typography.titleMedium,
                                color = TacticalAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReplayMiniStat("HP", "${currentFrame.playerHp}", if (currentFrame.isKnocked) CombatCrimson else NanoEmerald)
                        ReplayMiniStat("ALIVE", "${currentFrame.aliveCount}", ElectricCyan)
                        ReplayMiniStat("KILLS", "${currentFrame.kills}", TacticalAmber)
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("replay_close_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Replay", tint = TextPrimary)
                        }
                    }
                }
            }

            // Event Callout Pill if current frame has combat/zone highlight
            if (!currentFrame.eventText.isNullOrBlank()) {
                Surface(
                    color = ObsidianCard.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .border(1.dp, TacticalAmber, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "${formatReplaySec(currentFrame.timeSec)} • ${currentFrame.eventText}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TacticalAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Playback Controls Deck
        Surface(
            color = ObsidianSurface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .border(1.dp, TacticalBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (eventMilestones.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        eventMilestones.forEach { (frameIdx, frameSnap) ->
                            val selected = frameIdx == currentIndex
                            Surface(
                                color = if (selected) TacticalAmber.copy(alpha = 0.24f) else ObsidianCard,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .border(1.dp, if (selected) TacticalAmber else TacticalBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        currentIndex = frameIdx
                                        isPlaying = false
                                    }
                            ) {
                                Text(
                                    text = "${formatReplaySec(frameSnap.timeSec)} ${frameSnap.eventText?.take(22) ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) TacticalAmber else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Scrubber Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatReplaySec(currentFrame.timeSec),
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = currentIndex.toFloat(),
                        onValueChange = {
                            currentIndex = it.roundToInt().coerceIn(0, safeFrames.lastIndex)
                            isPlaying = false
                        },
                        valueRange = 0f..safeFrames.lastIndex.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = TacticalAmber,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = ObsidianElevated
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("replay_timeline_slider")
                    )
                    Text(
                        text = formatReplaySec(safeFrames.last().timeSec),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }

                // Playback Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { followPlayerCam = !followPlayerCam },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("replay_camera_toggle")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (followPlayerCam) "Player Cam" else "Full Map", color = ElectricCyan)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentIndex = (currentIndex - 5).coerceAtLeast(0) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("replay_rewind_button")
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Rewind 5s", tint = TextPrimary)
                        }

                        Button(
                            onClick = {
                                if (!isPlaying && currentIndex >= safeFrames.lastIndex) {
                                    currentIndex = 0
                                }
                                isPlaying = !isPlaying
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalAmber,
                                contentColor = ObsidianVoid
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("replay_play_pause_button")
                        ) {
                            Icon(
                                imageVector = when {
                                    isPlaying -> Icons.Default.Pause
                                    currentIndex >= safeFrames.lastIndex -> Icons.Default.Replay
                                    else -> Icons.Default.PlayArrow
                                },
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPlaying) "Pause" else "Play", fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { currentIndex = (currentIndex + 5).coerceAtMost(safeFrames.lastIndex) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("replay_forward_button")
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Forward 5s", tint = TextPrimary)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            speedMultiplier = when (speedMultiplier) {
                                1f -> 2f
                                2f -> 4f
                                else -> 1f
                            }
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("replay_speed_button")
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = TacticalAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${speedMultiplier.toInt()}x", color = TacticalAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplayArenaCanvas(
    frame: ReplayFrameSnapshot,
    followPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val worldSize = InfineMapBlueprint.WORLD_SIZE
        val span = if (followPlayer) 1050f else worldSize
        val halfSpan = span / 2f
        val originX = if (followPlayer) (frame.playerX - halfSpan).coerceIn(0f, worldSize - span) else 0f
        val originY = if (followPlayer) (frame.playerY - halfSpan).coerceIn(0f, worldSize - span) else 0f

        val minDim = minOf(size.width, size.height)
        val scale = if (followPlayer) maxOf(size.width, size.height) / span else minDim / worldSize
        val offsetX = if (followPlayer) 0f else (size.width - worldSize * scale) / 2f
        val offsetY = if (followPlayer) 0f else (size.height - worldSize * scale) / 2f

        fun sx(wx: Float): Float = offsetX + (wx - originX) * scale
        fun sy(wy: Float): Float = offsetY + (wy - originY) * scale

        clipRect {
            drawRect(color = Color(0xFF0C1422), size = size)

            // Botanical Garden patch
            drawCircle(
                color = Color(0xFF122C24),
                radius = 250f * scale,
                center = Offset(sx(460f), sy(1200f))
            )

            // Roads
            InfineMapBlueprint.roads.forEach { road ->
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(sx(road.x1), sy(road.y1)),
                    end = Offset(sx(road.x2), sy(road.y2)),
                    strokeWidth = (road.width * scale).coerceAtLeast(3f),
                    cap = StrokeCap.Round
                )
            }

            // Buildings & Obstacles
            InfineMapBlueprint.obstacles.forEach { rect ->
                val fill = when (rect.kind) {
                    ObstacleKind.CLOCK_TOWER -> Color(0xFF312747)
                    ObstacleKind.HOSPITAL_WING -> Color(0xFF33242E)
                    ObstacleKind.GARDEN_HEDGE -> Color(0xFF19422E)
                    else -> Color(0xFF28354E)
                }
                val stroke = when (rect.kind) {
                    ObstacleKind.CLOCK_TOWER -> TacticalAmber
                    ObstacleKind.HOSPITAL_WING -> Color(0xFFFF5252)
                    ObstacleKind.GARDEN_HEDGE -> NanoEmerald
                    else -> Color(0xFF64748B)
                }
                drawRoundRect(
                    color = fill,
                    topLeft = Offset(sx(rect.left), sy(rect.top)),
                    size = Size(rect.width * scale, rect.height * scale),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = stroke,
                    topLeft = Offset(sx(rect.left), sy(rect.top)),
                    size = Size(rect.width * scale, rect.height * scale),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 1.5f)
                )
            }

            // Revival Points
            InfineMapBlueprint.revivalPoints.forEach { rp ->
                drawCircle(
                    color = NanoEmerald.copy(alpha = 0.25f),
                    radius = 60f * scale,
                    center = Offset(sx(rp.x), sy(rp.y))
                )
                drawCircle(
                    color = NanoEmerald,
                    radius = 12f * scale.coerceAtLeast(0.35f),
                    center = Offset(sx(rp.x), sy(rp.y))
                )
            }

            // Safe Zone Circle
            drawCircle(
                color = ElectricCyan,
                radius = frame.safeZoneRadius * scale,
                center = Offset(sx(frame.safeZoneX), sy(frame.safeZoneY)),
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
                )
            )

            // Actors (Allies & Enemies)
            frame.actors.forEach { actor ->
                val ax = sx(actor.x)
                val ay = sy(actor.y)
                if (actor.isEliminated) {
                    val xSize = if (followPlayer) 8f else 5f
                    drawLine(Color(0x66FF3D57), Offset(ax - xSize, ay - xSize), Offset(ax + xSize, ay + xSize), strokeWidth = 2f)
                    drawLine(Color(0x66FF3D57), Offset(ax + xSize, ay - xSize), Offset(ax - xSize, ay + xSize), strokeWidth = 2f)
                } else {
                    val col = when {
                        actor.isKnocked -> TacticalAmber
                        actor.isAlly -> ElectricCyan
                        else -> CombatCrimson
                    }
                    val r = if (followPlayer) 11f else 6f
                    drawCircle(color = ObsidianVoid, radius = r + 2f, center = Offset(ax, ay))
                    drawCircle(color = col, radius = r, center = Offset(ax, ay))
                    val dirLen = r + 8f
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(ax, ay),
                        end = Offset(ax + cos(actor.aimRad) * dirLen, ay + sin(actor.aimRad) * dirLen),
                        strokeWidth = 2.5f
                    )
                }
            }

            // Local Player FOV Cone + Marker
            val px = sx(frame.playerX)
            val py = sy(frame.playerY)
            val fovLen = if (followPlayer) 48f else 28f
            val conePath = Path().apply {
                moveTo(px, py)
                lineTo(px + cos(frame.playerAimRad - 0.48f) * fovLen, py + sin(frame.playerAimRad - 0.48f) * fovLen)
                lineTo(px + cos(frame.playerAimRad + 0.48f) * fovLen, py + sin(frame.playerAimRad + 0.48f) * fovLen)
                close()
            }
            drawPath(path = conePath, color = Color(0x45FFFFFF))
            drawCircle(
                color = TacticalAmber,
                radius = if (followPlayer) 15f else 8f,
                center = Offset(px, py)
            )
            drawCircle(
                color = if (frame.isKnocked) CombatCrimson else Color.White,
                radius = if (followPlayer) 11f else 5.5f,
                center = Offset(px, py)
            )
        }
    }
}

@Composable
private fun ReplayMiniStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

private fun formatReplaySec(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format(java.util.Locale.US, "%02d:%02d", m, s)
}
