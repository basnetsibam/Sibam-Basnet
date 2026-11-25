package com.example.ui.match

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun DeathCamOverlay(
    killerName: String,
    killerWeapon: String,
    victimCallsign: String,
    frames: List<DeathCamFrame>,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val safeFrames = remember(frames) {
        if (frames.isNotEmpty()) frames else listOf(
            DeathCamFrame(
                timeOffsetSec = 0f,
                killerX = 1200f,
                killerY = 1200f,
                killerAimRad = 0f,
                killerHp = 100,
                killerMaxHp = 100,
                victimX = 1320f,
                victimY = 1200f,
                victimHp = 0,
                victimKnocked = false,
                safeZoneX = 1200f,
                safeZoneY = 1200f,
                safeZoneRadius = 1000f,
                impactBanner = "ELIMINATED BY $killerName ($killerWeapon)"
            )
        )
    }

    var frameIndex by remember(safeFrames) { mutableIntStateOf(0) }
    var isPlaying by remember(safeFrames) { mutableStateOf(true) }
    var slowMoRate by remember { mutableFloatStateOf(0.35f) } // Slow-motion default 0.35x

    val currentFrame = safeFrames[frameIndex.coerceIn(0, safeFrames.lastIndex)]

    LaunchedEffect(isPlaying, slowMoRate, safeFrames) {
        while (isPlaying && safeFrames.size > 1) {
            val baseIntervalMs = 150f
            val delayMs = (baseIntervalMs / slowMoRate).toLong().coerceIn(80L, 650L)
            delay(delayMs)
            if (frameIndex < safeFrames.lastIndex) {
                frameIndex += 1
            } else {
                // Hold briefly on final kill shot frame then pause
                delay(700L)
                isPlaying = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("death_cam_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Killer POV Header Bar
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CombatCrimson, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = CombatCrimson.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🎥 DEATH CAM • ${slowMoRate}x SLOW-MO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CombatCrimson,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "%.1fs", currentFrame.timeOffsetSec),
                                style = MaterialTheme.typography.labelMedium,
                                color = TacticalAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "💀 Killer POV: $killerName • $killerWeapon",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Killer HP ${currentFrame.killerHp}/${currentFrame.killerMaxHp} vs $victimCallsign HP ${currentFrame.victimHp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = onClose,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("death_cam_skip_button")
                    ) {
                        Text("⏭️ Skip", color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Killer Perspective Canvas Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, CombatCrimson.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .background(Color(0xFF0A111D))
            ) {
                DeathCamCanvas(
                    frame = currentFrame,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left Killer POV badge
                Surface(
                    color = ObsidianVoid.copy(alpha = 0.84f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .border(1.dp, CombatCrimson.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "🎯 CAMERA: $killerName (KILLER)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CombatCrimson,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                // Final Impact Banner
                currentFrame.impactBanner?.let { banner ->
                    Surface(
                        color = CombatCrimson.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💥 $banner",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Slow-Mo Timeline & Playback Controls
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ Final Moments Playback",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Frame ${frameIndex + 1} / ${safeFrames.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }

                    Slider(
                        value = frameIndex.toFloat(),
                        onValueChange = {
                            isPlaying = false
                            frameIndex = it.roundToInt().coerceIn(0, safeFrames.lastIndex)
                        },
                        valueRange = 0f..safeFrames.lastIndex.coerceAtLeast(1).toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = CombatCrimson,
                            activeTrackColor = CombatCrimson,
                            inactiveTrackColor = ObsidianElevated
                        ),
                        modifier = Modifier.testTag("death_cam_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (!isPlaying && frameIndex >= safeFrames.lastIndex) {
                                    frameIndex = 0
                                }
                                isPlaying = !isPlaying
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalAmber,
                                contentColor = ObsidianVoid
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("death_cam_play_pause_button")
                        ) {
                            Text(
                                text = if (isPlaying) "⏸️ Pause" else "▶️ Replay",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Slow-motion speed pills
                        listOf(0.25f to "🐢 0.25x", 0.35f to "🐢 0.35x", 0.5f to "🎬 0.5x", 1.0f to "⏩ 1x").forEach { (rate, label) ->
                            val active = slowMoRate == rate
                            OutlinedButton(
                                onClick = { slowMoRate = rate },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .border(
                                        width = if (active) 1.5.dp else 1.dp,
                                        color = if (active) CombatCrimson else TacticalBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Text(
                                    text = label,
                                    color = if (active) CombatCrimson else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianVoid
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("death_cam_continue_results_button")
                    ) {
                        Text("📊 View Match Summary", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeathCamCanvas(
    frame: DeathCamFrame,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val zoom = 1.28f
        val camX = frame.killerX - (size.width / 2f) / zoom
        val camY = frame.killerY - (size.height / 2f) / zoom

        fun sx(wx: Float): Float = (wx - camX) * zoom
        fun sy(wy: Float): Float = (wy - camY) * zoom

        clipRect {
            drawRect(color = Color(0xFF0C1524), size = size)

            // Roads
            InfineMapBlueprint.roads.forEach { road ->
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(sx(road.x1), sy(road.y1)),
                    end = Offset(sx(road.x2), sy(road.y2)),
                    strokeWidth = road.width * zoom,
                    cap = StrokeCap.Round
                )
            }

            // Obstacles
            InfineMapBlueprint.obstacles.forEach { rect ->
                val oColor = when (rect.kind) {
                    ObstacleKind.CLOCK_TOWER -> Color(0xFF28203B)
                    ObstacleKind.HOSPITAL_WING -> Color(0xFF283747)
                    ObstacleKind.GARDEN_HEDGE -> Color(0xFF19422E)
                    else -> Color(0xFF243045)
                }
                drawRoundRect(
                    color = oColor,
                    topLeft = Offset(sx(rect.left), sy(rect.top)),
                    size = Size(rect.width * zoom, rect.height * zoom),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            // Safe Zone boundary
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.5f),
                radius = frame.safeZoneRadius * zoom,
                center = Offset(sx(frame.safeZoneX), sy(frame.safeZoneY)),
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f)))
            )

            // Other combatants
            frame.actors.filter { !it.isEliminated }.forEach { actor ->
                val ax = sx(actor.x)
                val ay = sy(actor.y)
                val color = if (actor.isAlly) ElectricCyan else Color(0xFFF97316)
                drawCircle(color = color.copy(alpha = 0.75f), radius = 12f, center = Offset(ax, ay))
            }

            // Projectiles / bullet tracers
            frame.bullets.forEach { b ->
                val bx = sx(b.x)
                val by = sy(b.y)
                val tailX = bx - b.vx * 0.025f
                val tailY = by - b.vy * 0.025f
                val bColor = if (b.isFromKiller) TacticalAmber else CombatCrimson
                drawLine(
                    color = bColor,
                    start = Offset(tailX, tailY),
                    end = Offset(bx, by),
                    strokeWidth = if (b.isFromKiller) 5f else 3f,
                    cap = StrokeCap.Round
                )
            }

            // Target line & reticle around victim (YOU)
            val vx = sx(frame.victimX)
            val vy = sy(frame.victimY)
            val kx = sx(frame.killerX)
            val ky = sy(frame.killerY)

            // Killer POV sight cone
            val coneLen = 290f
            val spread = 0.32f
            val conePath = Path().apply {
                moveTo(kx, ky)
                lineTo(kx + cos(frame.killerAimRad - spread) * coneLen, ky + sin(frame.killerAimRad - spread) * coneLen)
                lineTo(kx + cos(frame.killerAimRad + spread) * coneLen, ky + sin(frame.killerAimRad + spread) * coneLen)
                close()
            }
            drawPath(path = conePath, color = CombatCrimson.copy(alpha = 0.14f))

            // Killer aim tracer line
            drawLine(
                color = CombatCrimson.copy(alpha = 0.55f),
                start = Offset(kx, ky),
                end = Offset(vx, vy),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )

            // Victim (YOU) marker + target reticle
            drawCircle(
                color = if (frame.victimHp <= 0) CombatCrimson else ElectricCyan,
                radius = 15f,
                center = Offset(vx, vy)
            )
            drawCircle(
                color = TacticalAmber,
                radius = 24f,
                center = Offset(vx, vy),
                style = Stroke(width = 2.5f)
            )

            // Killer Marker (Centered POV)
            drawLine(
                color = TacticalAmber,
                start = Offset(kx, ky),
                end = Offset(kx + cos(frame.killerAimRad) * 32f, ky + sin(frame.killerAimRad) * 32f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawCircle(color = ObsidianVoid, radius = 21f, center = Offset(kx, ky))
            drawCircle(color = CombatCrimson, radius = 17f, center = Offset(kx, ky))
            drawCircle(color = Color.White, radius = 25f, center = Offset(kx, ky), style = Stroke(width = 2f))
        }
    }
}
