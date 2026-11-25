package com.example.ui.match

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.data.CharacterSkinCatalog
import com.example.data.GameMode
import com.example.data.InfineMapBlueprint
import com.example.data.ObstacleKind
import com.example.data.VehicleCatalog
import com.example.data.VehicleType
import com.example.data.WeaponSkinCatalog
import com.example.data.WeatherCondition
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.TacticalAmber
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun ArenaWorldCanvas(
    hudState: ArenaHudState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val poiStyle = remember {
        TextStyle(
            color = Color(0xCCFFFFFF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
    val tinyStyle = remember {
        TextStyle(
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val camX = hudState.playerX - size.width / 2f
        val camY = hudState.playerY - size.height / 2f

        // Base terrain
        drawRect(color = Color(0xFF0E1726), size = size)

        // Tactical grid lines (visible in viewport only)
        val gridStep = 120f
        val startX = (camX / gridStep).toInt() * gridStep
        val startY = (camY / gridStep).toInt() * gridStep
        var gx = startX
        while (gx < camX + size.width + gridStep) {
            val sx = gx - camX
            drawLine(
                color = Color(0xFF162238),
                start = Offset(sx, 0f),
                end = Offset(sx, size.height),
                strokeWidth = 1f
            )
            gx += gridStep
        }
        var gy = startY
        while (gy < camY + size.height + gridStep) {
            val sy = gy - camY
            drawLine(
                color = Color(0xFF162238),
                start = Offset(0f, sy),
                end = Offset(size.width, sy),
                strokeWidth = 1f
            )
            gy += gridStep
        }

        // Botanical Garden grass patch
        drawCircle(
            color = Color(0xFF122C24),
            radius = 250f,
            center = Offset(460f - camX, 1200f - camY)
        )

        // Roads connecting Infine locations
        InfineMapBlueprint.roads.forEach { road ->
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(road.x1 - camX, road.y1 - camY),
                end = Offset(road.x2 - camX, road.y2 - camY),
                strokeWidth = road.width,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0x44FFB300),
                start = Offset(road.x1 - camX, road.y1 - camY),
                end = Offset(road.x2 - camX, road.y2 - camY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f))
            )
        }

        // Map borders
        drawRect(
            color = CombatCrimson,
            topLeft = Offset(-camX, -camY),
            size = Size(InfineMapBlueprint.WORLD_SIZE, InfineMapBlueprint.WORLD_SIZE),
            style = Stroke(width = 6f)
        )

        // Revival Points (glowing beacons)
        InfineMapBlueprint.revivalPoints.forEach { rp ->
            val rx = rp.x - camX
            val ry = rp.y - camY
            if (rx in -160f..(size.width + 160f) && ry in -160f..(size.height + 160f)) {
                drawCircle(
                    color = Color(0x2800E5FF),
                    radius = 86f,
                    center = Offset(rx, ry)
                )
                drawCircle(
                    color = ElectricCyan,
                    radius = 86f,
                    center = Offset(rx, ry),
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                    )
                )
                drawCircle(
                    color = NanoEmerald,
                    radius = 18f,
                    center = Offset(rx, ry)
                )
                drawLabel(textMeasurer, "REVIVAL POINT", Offset(rx - 44f, ry + 26f), tinyStyle)
            }
        }

        // Obstacles / Buildings / Clock Tower + Small Houses
        InfineMapBlueprint.obstacles.forEach { rect ->
            val left = rect.left - camX
            val top = rect.top - camY
            if (left + rect.width < -80f || left > size.width + 80f ||
                top + rect.height < -80f || top > size.height + 80f
            ) return@forEach

            val (fillColor, strokeColor) = when (rect.kind) {
                ObstacleKind.CLOCK_TOWER -> Color(0xFF28203B) to TacticalAmber
                ObstacleKind.SMALL_HOUSE -> Color(0xFF28354E) to Color(0xFF64748B)
                ObstacleKind.FACTORY_HALL -> Color(0xFF332724) to Color(0xFFFF7043)
                ObstacleKind.ARENA_WALL -> Color(0xFF2D1E32) to CombatCrimson
                ObstacleKind.SHOP_STALL -> Color(0xFF203445) to TacticalAmber
                ObstacleKind.SCHOOL_BLOCK -> Color(0xFF25324C) to ElectricCyan
                ObstacleKind.ASSOCIATION_HALL -> Color(0xFF222B45) to Color(0xFFB388FF)
                ObstacleKind.GARDEN_HEDGE -> Color(0xFF19422E) to NanoEmerald
                ObstacleKind.HOSPITAL_WING -> Color(0xFF283747) to Color(0xFFFF5252)
            }

            // Drop shadow
            drawRoundRect(
                color = Color(0x55000000),
                topLeft = Offset(left + 6f, top + 8f),
                size = Size(rect.width, rect.height),
                cornerRadius = CornerRadius(10f, 10f)
            )
            // Structure body
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(left, top),
                size = Size(rect.width, rect.height),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(left, top),
                size = Size(rect.width, rect.height),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 2.5f)
            )

            // Special big Clock Face on the Clock Tower!
            if (rect.kind == ObstacleKind.CLOCK_TOWER) {
                val cx = left + rect.width / 2f
                val cy = top + rect.height / 2f
                drawCircle(color = ObsidianVoid, radius = 38f, center = Offset(cx, cy))
                drawCircle(color = TacticalAmber, radius = 38f, center = Offset(cx, cy), style = Stroke(4f))
                drawCircle(color = ElectricCyan, radius = 29f, center = Offset(cx, cy), style = Stroke(1.5f))
                // Clock hands
                drawLine(TacticalAmber, Offset(cx, cy), Offset(cx, cy - 22f), strokeWidth = 4f, cap = StrokeCap.Round)
                drawLine(ElectricCyan, Offset(cx, cy), Offset(cx + 18f, cy + 8f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawCircle(Color.White, radius = 4f, center = Offset(cx, cy))
            } else if (rect.kind == ObstacleKind.HOSPITAL_WING) {
                val cx = left + rect.width / 2f
                val cy = top + rect.height / 2f
                drawLine(Color(0xFFFF5252), Offset(cx - 12f, cy), Offset(cx + 12f, cy), strokeWidth = 5f)
                drawLine(Color(0xFFFF5252), Offset(cx, cy - 12f), Offset(cx, cy + 12f), strokeWidth = 5f)
            } else if (rect.kind == ObstacleKind.SMALL_HOUSE) {
                // Pitched roof ridge
                drawLine(
                    color = Color(0x55FFFFFF),
                    start = Offset(left + 10f, top + rect.height / 2f),
                    end = Offset(left + rect.width - 10f, top + rect.height / 2f),
                    strokeWidth = 2f
                )
            }

            rect.label?.let { label ->
                drawLabel(textMeasurer, label, Offset(left + 6f, top + 6f), tinyStyle)
            }
        }

        // POI District Name banners
        InfineMapBlueprint.locations.forEach { poi ->
            val sx = poi.x - camX
            val sy = poi.y - camY - 120f
            if (sx in -140f..(size.width + 140f) && sy in -80f..(size.height + 80f)) {
                drawLabel(textMeasurer, poi.name.uppercase(), Offset(sx - 40f, sy), poiStyle)
            }
        }

        // Loot Drops
        hudState.lootDrops.forEach { loot ->
            val lx = loot.x - camX
            val ly = loot.y - camY
            if (lx in -40f..(size.width + 40f) && ly in -40f..(size.height + 40f)) {
                val lootColor = when (loot.kind) {
                    LootKind.WEAPON -> TacticalAmber
                    LootKind.MEDKIT -> NanoEmerald
                    LootKind.INHALER -> ElectricCyan
                    LootKind.ARMOR -> Color(0xFFB388FF)
                }
                drawCircle(color = lootColor.copy(alpha = 0.25f), radius = 18f, center = Offset(lx, ly))
                drawRoundRect(
                    color = lootColor,
                    topLeft = Offset(lx - 9f, ly - 9f),
                    size = Size(18f, 18f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawLabel(textMeasurer, loot.label, Offset(lx - 22f, ly + 13f), tinyStyle)
            }
        }

        // Vehicles (Jeep, Auto-Rickshaw 🛺, Supercar, Bike)
        hudState.vehicles.forEach { veh ->
            if (veh.hp <= 0) return@forEach
            val vx = veh.x - camX
            val vy = veh.y - camY
            if (vx !in -80f..(size.width + 80f) || vy !in -80f..(size.height + 80f)) return@forEach

            val spec = VehicleCatalog.byType(veh.type)
            val deg = Math.toDegrees(veh.headingRad.toDouble()).toFloat()
            rotate(degrees = deg, pivot = Offset(vx, vy)) {
                when (veh.type) {
                    VehicleType.JEEP -> {
                        drawRoundRect(spec.primaryColor, Offset(vx - 26f, vy - 17f), Size(52f, 34f), CornerRadius(6f, 6f))
                        drawRoundRect(spec.accentColor, Offset(vx - 26f, vy - 17f), Size(52f, 34f), CornerRadius(6f, 6f), style = Stroke(2f))
                        drawRect(Color(0xFF0F172A), Offset(vx + 6f, vy - 12f), Size(10f, 24f))
                    }
                    VehicleType.TUK_TUK -> {
                        drawRoundRect(spec.primaryColor, Offset(vx - 21f, vy - 14f), Size(42f, 28f), CornerRadius(9f, 9f))
                        drawRoundRect(spec.accentColor, Offset(vx - 19f, vy - 12f), Size(24f, 24f), CornerRadius(6f, 6f))
                        drawCircle(Color.White, radius = 4f, center = Offset(vx + 18f, vy))
                    }
                    VehicleType.SUPERCAR -> {
                        drawRoundRect(spec.primaryColor, Offset(vx - 28f, vy - 15f), Size(56f, 30f), CornerRadius(12f, 12f))
                        drawLine(spec.accentColor, Offset(vx - 20f, vy), Offset(vx + 22f, vy), strokeWidth = 3f)
                    }
                    VehicleType.BIKE -> {
                        drawRoundRect(spec.primaryColor, Offset(vx - 20f, vy - 7f), Size(40f, 14f), CornerRadius(7f, 7f))
                        drawRoundRect(spec.accentColor, Offset(vx - 10f, vy - 5f), Size(20f, 10f), CornerRadius(4f, 4f))
                    }
                }
            }
            drawLabel(textMeasurer, "${spec.badge} ${spec.name}", Offset(vx - 28f, vy + 22f), tinyStyle)
        }

        // Heal Aura ring around player when Vespera's Nano-Mend Pulse is active
        if (hudState.healAuraRemaining > 0f) {
            val px = hudState.playerX - camX
            val py = hudState.playerY - camY
            drawCircle(color = NanoEmerald.copy(alpha = 0.16f), radius = 200f, center = Offset(px, py))
            drawCircle(color = NanoEmerald, radius = 200f, center = Offset(px, py), style = Stroke(3f))
        }

        // AI Combatants (Allies & Enemies)
        hudState.combatants.forEach { bot ->
            if (bot.isEliminated) return@forEach
            val bx = bot.x - camX
            val by = bot.y - camY
            if (bx !in -60f..(size.width + 60f) || by !in -60f..(size.height + 60f)) return@forEach

            val bodyColor = when {
                bot.isKnocked -> TacticalAmber
                bot.team == CombatantTeam.PLAYER_SQUAD -> ElectricCyan
                bot.isTargetDummy -> Color(0xFFFB7185)
                else -> CombatCrimson
            }

            // Weapon barrel
            val gunEndX = bx + cos(bot.aimRad) * 24f
            val gunEndY = by + sin(bot.aimRad) * 24f
            drawLine(Color.LightGray, Offset(bx, by), Offset(gunEndX, gunEndY), strokeWidth = 4f, cap = StrokeCap.Round)

            drawCircle(color = ObsidianVoid, radius = 17f, center = Offset(bx, by))
            drawCircle(color = bodyColor, radius = 14f, center = Offset(bx, by))

            // HP Bar
            val hpRatio = (bot.hp.toFloat() / bot.maxHp).coerceIn(0f, 1f)
            drawRect(Color(0xAA000000), Offset(bx - 18f, by - 26f), Size(36f, 5f))
            drawRect(bodyColor, Offset(bx - 18f, by - 26f), Size(36f * hpRatio, 5f))
            drawLabel(
                textMeasurer,
                if (bot.isKnocked) "KNOCKED" else bot.name,
                Offset(bx - 22f, by + 18f),
                tinyStyle
            )
        }

        // Local Player (unless inside vehicle, still show driver indicator)
        val px = hudState.playerX - camX
        val py = hudState.playerY - camY
        val activeSkin = WeaponSkinCatalog.byId(hudState.equippedWeaponSkinId)
        val charSkin = CharacterSkinCatalog.byId(hudState.equippedCharacterSkinId)
        if (hudState.drivingVehicle == null) {
            val gunEndX = px + cos(hudState.playerAimRad) * 26f
            val gunEndY = py + sin(hudState.playerAimRad) * 26f
            drawLine(activeSkin.primaryColor, Offset(px, py), Offset(gunEndX, gunEndY), strokeWidth = 5.5f, cap = StrokeCap.Round)
            drawCircle(color = charSkin.rimColor, radius = 18f, center = Offset(px, py))
            drawCircle(
                color = if (hudState.isKnocked) CombatCrimson else charSkin.suitColor,
                radius = 14f,
                center = Offset(px, py)
            )
        } else {
            drawCircle(color = charSkin.suitColor, radius = 24f, center = Offset(px, py), style = Stroke(3f))
        }

        if (hudState.activeEmoteTtl > 0f) {
            val emoteColor = Color(hudState.activeEmoteColorHex)
            drawCircle(
                color = emoteColor.copy(alpha = 0.28f),
                radius = 30f,
                center = Offset(px, py)
            )
            drawCircle(
                color = emoteColor,
                radius = 32f,
                center = Offset(px, py),
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
            )
            hudState.activeEmoteText?.let { banner ->
                drawLabel(textMeasurer, banner, Offset(px - 44f, py - 42f), tinyStyle)
            }
        }

        // Projectiles / Bullets
        hudState.projectiles.forEach { p ->
            val sx = p.x - camX
            val sy = p.y - camY
            val color = when {
                p.isOvercharged -> ElectricCyan
                p.isSniper -> Color(0xFFFFEA00)
                p.ownerId == "player" -> activeSkin.tracerColor
                p.team == CombatantTeam.PLAYER_SQUAD -> TacticalAmber
                else -> CombatCrimson
            }
            drawCircle(color = color, radius = if (p.isOvercharged || p.isSniper) 5.5f else 3.8f, center = Offset(sx, sy))
        }

        // Visual Effects (Arc Strike, Heal ticks, Damage popups)
        hudState.effects.forEach { eff ->
            val ex = eff.x - camX
            val ey = eff.y - camY
            val progress = 1f - (eff.ttlSec / eff.maxTtlSec).coerceIn(0f, 1f)
            val color = Color(eff.colorHex)
            drawCircle(
                color = color.copy(alpha = (1f - progress) * 0.55f),
                radius = eff.maxRadius * (0.25f + progress * 0.75f),
                center = Offset(ex, ey),
                style = Stroke(width = 4f)
            )
            eff.label?.let { txt ->
                drawLabel(textMeasurer, txt, Offset(ex - 20f, ey - 18f - progress * 20f), tinyStyle)
            }
        }

        // Shrinking Storm Circle & Next Safe Zone (BR & CS)
        if (hudState.mode != GameMode.TRAINING_GROUND) {
            val szScreenX = hudState.safeZoneX - camX
            val szScreenY = hudState.safeZoneY - camY
            val stormOverlayPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addOval(
                    Rect(
                        szScreenX - hudState.safeZoneRadius,
                        szScreenY - hudState.safeZoneRadius,
                        szScreenX + hudState.safeZoneRadius,
                        szScreenY + hudState.safeZoneRadius
                    )
                )
            }
            drawPath(
                path = stormOverlayPath,
                color = Color(0x369333EA)
            )
            // Next Safe Zone Target Ring (dashed white preview)
            if (hudState.nextSafeZoneRadius < hudState.safeZoneRadius - 4f) {
                drawCircle(
                    color = Color(0xAAFFFFFF),
                    radius = hudState.nextSafeZoneRadius,
                    center = Offset(hudState.nextSafeZoneX - camX, hudState.nextSafeZoneY - camY),
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 14f))
                    )
                )
            }
            // Active Storm Wall Boundary
            drawCircle(
                color = if (hudState.isStormClosing) Color(0xFFE879F9) else ElectricCyan,
                radius = hudState.safeZoneRadius,
                center = Offset(szScreenX, szScreenY),
                style = Stroke(
                    width = if (hudState.isStormClosing) 5.5f else 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 14f))
                )
            )
        }

        // Dynamic Weather Visual & Visibility Overlay (non-gradient)
        if (hudState.weatherIntensity > 0.02f) {
            val intensity = hudState.weatherIntensity.coerceIn(0f, 1f)
            val phase = hudState.weatherTickPhase
            val pxScreen = hudState.playerX - camX
            val pyScreen = hudState.playerY - camY

            when (hudState.weather) {
                WeatherCondition.FOG -> {
                    drawRect(
                        color = Color(0xFF94A3B8).copy(alpha = 0.16f * intensity),
                        size = size
                    )
                    // Drifting fog banks across Infine
                    for (i in 0 until 7) {
                        val driftX = ((i * 290f + phase * (18f + (i % 3) * 7f)) % (size.width + 360f)) - 180f
                        val driftY = ((i * 175f + phase * 6f) % (size.height + 240f)) - 120f
                        drawOval(
                            color = Color(0xFFCBD5E1).copy(alpha = 0.14f * intensity),
                            topLeft = Offset(driftX, driftY),
                            size = Size(320f, 130f)
                        )
                    }
                }
                WeatherCondition.RAIN -> {
                    drawRect(
                        color = Color(0xFF082F49).copy(alpha = 0.22f * intensity),
                        size = size
                    )
                    // Diagonal monsoon rain streaks
                    for (i in 0 until 42) {
                        val rx = ((i * 83f + phase * 220f) % (size.width + 120f)) - 60f
                        val ry = ((i * 119f + phase * 640f) % (size.height + 120f)) - 60f
                        drawLine(
                            color = ElectricCyan.copy(alpha = 0.34f * intensity),
                            start = Offset(rx, ry),
                            end = Offset(rx - 14f, ry + 34f),
                            strokeWidth = 2f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                WeatherCondition.STORM -> {
                    drawRect(
                        color = Color(0xFF090D1A).copy(alpha = 0.38f * intensity),
                        size = size
                    )
                    // Heavy squall rain streaks
                    for (i in 0 until 54) {
                        val rx = ((i * 71f + phase * 390f) % (size.width + 160f)) - 80f
                        val ry = ((i * 103f + phase * 820f) % (size.height + 140f)) - 70f
                        drawLine(
                            color = Color(0xFF93C5FD).copy(alpha = 0.42f * intensity),
                            start = Offset(rx, ry),
                            end = Offset(rx - 26f, ry + 42f),
                            strokeWidth = 2.4f,
                            cap = StrokeCap.Round
                        )
                    }
                    // Lightning bolt flash silhouette
                    if (hudState.lightningFlashAlpha > 0.04f) {
                        drawRect(
                            color = Color(0xFFE0E7FF).copy(alpha = hudState.lightningFlashAlpha * 0.42f),
                            size = size
                        )
                        val boltX = size.width * 0.72f
                        val boltPath = Path().apply {
                            moveTo(boltX, 0f)
                            lineTo(boltX - 36f, size.height * 0.22f)
                            lineTo(boltX + 12f, size.height * 0.24f)
                            lineTo(boltX - 48f, size.height * 0.52f)
                        }
                        drawPath(
                            path = boltPath,
                            color = Color.White.copy(alpha = hudState.lightningFlashAlpha),
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                    }
                }
                WeatherCondition.CLEAR -> Unit
            }

            // Non-gradient visibility shroud rings outside sight radius
            val visRadius = hudState.weather.visibilityRadiusPx
            if (visRadius > 0f) {
                val veilColor = if (hudState.weather == WeatherCondition.FOG) {
                    Color(0xFFCBD5E1)
                } else {
                    Color(0xFF060A14)
                }
                drawCircle(
                    color = veilColor.copy(alpha = 0.16f * intensity),
                    radius = visRadius + 75f,
                    center = Offset(pxScreen, pyScreen),
                    style = Stroke(width = 140f)
                )
                drawCircle(
                    color = veilColor.copy(alpha = 0.34f * intensity),
                    radius = visRadius + 240f,
                    center = Offset(pxScreen, pyScreen),
                    style = Stroke(width = 240f)
                )
            }
        }

        // Visual Hit Marker Flash (Screen-Center Reticle + Impact Burst, zero gradients)
        if (hudState.hitMarkerTtl > 0f) {
            val maxTtl = hudState.hitMarkerMaxTtl.coerceAtLeast(0.05f)
            val fade = (hudState.hitMarkerTtl / maxTtl).coerceIn(0f, 1f)
            val expand = 1f - fade
            val markerColor = when {
                hudState.hitMarkerIsKill -> CombatCrimson
                hudState.hitMarkerIsCrit -> TacticalAmber
                else -> Color.White
            }
            val accentRingColor = if (hudState.hitMarkerIsKill) CombatCrimson else TacticalAmber

            // Subtle flat screen-edge hit flash strips
            val edgeAlpha = fade * (if (hudState.hitMarkerIsKill) 0.24f else 0.12f)
            val edgeThickness = if (hudState.hitMarkerIsKill) 8f else 5f
            drawRect(accentRingColor.copy(alpha = edgeAlpha), Offset.Zero, Size(size.width, edgeThickness))
            drawRect(accentRingColor.copy(alpha = edgeAlpha), Offset(0f, size.height - edgeThickness), Size(size.width, edgeThickness))
            drawRect(accentRingColor.copy(alpha = edgeAlpha), Offset.Zero, Size(edgeThickness, size.height))
            drawRect(accentRingColor.copy(alpha = edgeAlpha), Offset(size.width - edgeThickness, 0f), Size(edgeThickness, size.height))

            // 1) World impact point X-prongs
            val hx = hudState.hitMarkerWorldX - camX
            val hy = hudState.hitMarkerWorldY - camY
            if (hx in -80f..(size.width + 80f) && hy in -80f..(size.height + 80f)) {
                drawCrosshairHitProngs(
                    center = Offset(hx, hy),
                    innerGap = 7f + expand * 8f,
                    prongLen = if (hudState.hitMarkerIsKill) 16f else 12f,
                    color = markerColor.copy(alpha = fade),
                    strokeWidth = if (hudState.hitMarkerIsKill) 4.5f else 3.2f
                )
                if (hudState.hitMarkerIsKill) {
                    drawCircle(
                        color = CombatCrimson.copy(alpha = fade * 0.65f),
                        radius = 18f + expand * 34f,
                        center = Offset(hx, hy),
                        style = Stroke(width = 3f)
                    )
                }
            }

            // 2) Screen-center X hit marker reticle flash (always visible on screen)
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCrosshairHitProngs(
                center = Offset(cx, cy),
                innerGap = 12f + expand * 11f,
                prongLen = if (hudState.hitMarkerIsKill) 22f else 16f,
                color = ObsidianVoid.copy(alpha = fade * 0.7f),
                strokeWidth = if (hudState.hitMarkerIsKill) 7f else 5.5f
            )
            drawCrosshairHitProngs(
                center = Offset(cx, cy),
                innerGap = 12f + expand * 11f,
                prongLen = if (hudState.hitMarkerIsKill) 22f else 16f,
                color = markerColor.copy(alpha = fade),
                strokeWidth = if (hudState.hitMarkerIsKill) 4.2f else 3.2f
            )
        }

        // Storm Outside-Zone Flat Edge Flash & Warning Border
        if (hudState.mode != GameMode.TRAINING_GROUND && (hudState.isOutsideZone || hudState.stormTickDamageFlashAlpha > 0.01f)) {
            val stormAlpha = (if (hudState.isOutsideZone) 0.14f else 0f) + hudState.stormTickDamageFlashAlpha * 0.55f
            val borderThick = if (hudState.stormTickDamageFlashAlpha > 0.05f) 14f else 8f
            val stormEdgeColor = Color(0xFFE879F9).copy(alpha = stormAlpha.coerceIn(0f, 0.58f))
            drawRect(stormEdgeColor, Offset.Zero, Size(size.width, borderThick))
            drawRect(stormEdgeColor, Offset(0f, size.height - borderThick), Size(size.width, borderThick))
            drawRect(stormEdgeColor, Offset.Zero, Size(borderThick, size.height))
            drawRect(stormEdgeColor, Offset(size.width - borderThick, 0f), Size(borderThick, size.height))
        }
    }
}

private fun DrawScope.drawCrosshairHitProngs(
    center: Offset,
    innerGap: Float,
    prongLen: Float,
    color: Color,
    strokeWidth: Float
) {
    val diag = 0.7071f
    val dirs = listOf(
        Offset(-diag, -diag),
        Offset(diag, -diag),
        Offset(-diag, diag),
        Offset(diag, diag)
    )
    dirs.forEach { d ->
        drawLine(
            color = color,
            start = Offset(center.x + d.x * innerGap, center.y + d.y * innerGap),
            end = Offset(center.x + d.x * (innerGap + prongLen), center.y + d.y * (innerGap + prongLen)),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawLabel(
    measurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    style: TextStyle
) {
    if (text.isEmpty() || size.width <= 0f || size.height <= 0f) return
    if (topLeft.x >= size.width || topLeft.y >= size.height) return
    val layoutResult = measurer.measure(
        text = text,
        style = style,
        softWrap = false,
        maxLines = 1
    )
    if (topLeft.x + layoutResult.size.width <= 0f || topLeft.y + layoutResult.size.height <= 0f) {
        return
    }
    clipRect {
        drawText(
            textLayoutResult = layoutResult,
            topLeft = topLeft
        )
    }
}

@Composable
fun InfineMiniMap(
    hudState: ArenaHudState,
    zoomedRadar: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val worldSize = InfineMapBlueprint.WORLD_SIZE
        val viewSpan = if (zoomedRadar) 1050f else worldSize
        val halfSpan = viewSpan / 2f
        val viewOriginX = if (zoomedRadar) {
            (hudState.playerX - halfSpan).coerceIn(0f, worldSize - viewSpan)
        } else {
            0f
        }
        val viewOriginY = if (zoomedRadar) {
            (hudState.playerY - halfSpan).coerceIn(0f, worldSize - viewSpan)
        } else {
            0f
        }
        val scale = size.width / viewSpan

        fun toMapX(wx: Float): Float = (wx - viewOriginX) * scale
        fun toMapY(wy: Float): Float = (wy - viewOriginY) * scale

        clipRect {
            drawRect(color = Color(0xE0090F1A), size = size)

            // Tactical grid lines
            val gridWorldStep = if (zoomedRadar) 250f else 400f
            var gx = (viewOriginX / gridWorldStep).toInt() * gridWorldStep
            while (gx <= viewOriginX + viewSpan) {
                val mx = toMapX(gx)
                drawLine(Color(0xFF152236), Offset(mx, 0f), Offset(mx, size.height), strokeWidth = 1f)
                gx += gridWorldStep
            }
            var gy = (viewOriginY / gridWorldStep).toInt() * gridWorldStep
            while (gy <= viewOriginY + viewSpan) {
                val my = toMapY(gy)
                drawLine(Color(0xFF152236), Offset(0f, my), Offset(size.width, my), strokeWidth = 1f)
                gy += gridWorldStep
            }

            // Botanical Garden zone tint
            drawCircle(
                color = Color(0xFF133326),
                radius = 250f * scale,
                center = Offset(toMapX(460f), toMapY(1200f))
            )

            // Roads
            InfineMapBlueprint.roads.forEach { r ->
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(toMapX(r.x1), toMapY(r.y1)),
                    end = Offset(toMapX(r.x2), toMapY(r.y2)),
                    strokeWidth = (r.width * scale).coerceAtLeast(2.2f),
                    cap = StrokeCap.Round
                )
            }

            // Buildings & Obstacles
            InfineMapBlueprint.obstacles.forEach { rect ->
                val oColor = when (rect.kind) {
                    ObstacleKind.CLOCK_TOWER -> TacticalAmber.copy(alpha = 0.78f)
                    ObstacleKind.HOSPITAL_WING -> Color(0xCCEF5350)
                    ObstacleKind.GARDEN_HEDGE -> NanoEmerald.copy(alpha = 0.6f)
                    ObstacleKind.ARENA_WALL -> CombatCrimson.copy(alpha = 0.65f)
                    else -> Color(0xCC475569)
                }
                drawRect(
                    color = oColor,
                    topLeft = Offset(toMapX(rect.left), toMapY(rect.top)),
                    size = Size(
                        (rect.width * scale).coerceAtLeast(2f),
                        (rect.height * scale).coerceAtLeast(2f)
                    )
                )
            }

            // POIs
            InfineMapBlueprint.locations.forEach { poi ->
                drawCircle(
                    color = TacticalAmber.copy(alpha = 0.75f),
                    radius = if (zoomedRadar) 4.2f else 3.5f,
                    center = Offset(toMapX(poi.x), toMapY(poi.y))
                )
            }

            // Revival Points (highlight nearest to player)
            val nearestRp = InfineMapBlueprint.revivalPoints.minByOrNull { rp ->
                val dx = rp.x - hudState.playerX
                val dy = rp.y - hudState.playerY
                dx * dx + dy * dy
            }
            if (nearestRp != null) {
                drawLine(
                    color = NanoEmerald.copy(alpha = 0.6f),
                    start = Offset(toMapX(hudState.playerX), toMapY(hudState.playerY)),
                    end = Offset(toMapX(nearestRp.x), toMapY(nearestRp.y)),
                    strokeWidth = 1.6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
                )
            }
            InfineMapBlueprint.revivalPoints.forEach { rp ->
                val rx = toMapX(rp.x)
                val ry = toMapY(rp.y)
                val isNearest = rp.id == nearestRp?.id
                drawCircle(
                    color = NanoEmerald.copy(alpha = if (isNearest) 0.34f else 0.22f),
                    radius = if (isNearest) (if (zoomedRadar) 10.5f else 8f) else (if (zoomedRadar) 7.5f else 5.5f),
                    center = Offset(rx, ry)
                )
                if (isNearest) {
                    drawCircle(
                        color = NanoEmerald,
                        radius = if (zoomedRadar) 10.5f else 8f,
                        center = Offset(rx, ry),
                        style = Stroke(width = 1.4f)
                    )
                }
                drawCircle(
                    color = NanoEmerald,
                    radius = if (zoomedRadar) 4.2f else 3.2f,
                    center = Offset(rx, ry)
                )
            }

            // Live Vehicles
            hudState.vehicles.filter { it.hp > 0 }.forEach { veh ->
                val vx = toMapX(veh.x)
                val vy = toMapY(veh.y)
                val vSize = if (zoomedRadar) 5.5f else 4f
                drawRect(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(vx - vSize / 2f, vy - vSize / 2f),
                    size = Size(vSize, vSize)
                )
            }

            // Radar coordinates
            val px = toMapX(hudState.playerX)
            val py = toMapY(hudState.playerY)

            // Shrinking Storm Circle + Hazard Overlay + Next Safe Zone on Mini-Map
            if (hudState.mode != GameMode.TRAINING_GROUND) {
                val szX = toMapX(hudState.safeZoneX)
                val szY = toMapY(hudState.safeZoneY)
                val szR = hudState.safeZoneRadius * scale
                val stormMask = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addOval(Rect(szX - szR, szY - szR, szX + szR, szY + szR))
                }
                drawPath(
                    path = stormMask,
                    color = Color(0x4E9333EA)
                )

                // Next Safe Zone preview circle
                if (hudState.nextSafeZoneRadius < hudState.safeZoneRadius - 4f) {
                    drawCircle(
                        color = Color(0xDDFFFFFF),
                        radius = hudState.nextSafeZoneRadius * scale,
                        center = Offset(toMapX(hudState.nextSafeZoneX), toMapY(hudState.nextSafeZoneY)),
                        style = Stroke(
                            width = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
                        )
                    )
                }

                // Active shrinking storm boundary
                drawCircle(
                    color = if (hudState.isStormClosing) Color(0xFFE879F9) else ElectricCyan,
                    radius = szR,
                    center = Offset(szX, szY),
                    style = Stroke(width = if (hudState.isStormClosing) 2.2f else 1.7f)
                )

                // Escape guide vector from player into Next Safe Zone when outside safe zone
                if (hudState.isOutsideZone) {
                    drawLine(
                        color = Color(0xEEFFFFFF),
                        start = Offset(px, py),
                        end = Offset(toMapX(hudState.nextSafeZoneX), toMapY(hudState.nextSafeZoneY)),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }
            }

            // Radar range rings when zoomed
            if (zoomedRadar) {
                drawCircle(
                    color = Color(0x2800E5FF),
                    radius = 220f * scale,
                    center = Offset(px, py),
                    style = Stroke(1f)
                )
                drawCircle(
                    color = Color(0x1E00E5FF),
                    radius = 420f * scale,
                    center = Offset(px, py),
                    style = Stroke(1f)
                )
            }

            // Combatants (Allies & Enemies — Fog/Storm obscures far enemies beyond radarClarityRange)
            hudState.combatants.filter { !it.isEliminated }.forEach { bot ->
                if (bot.team == CombatantTeam.ENEMY && hudState.weather != WeatherCondition.CLEAR) {
                    val distToPlayer = hypot(bot.x - hudState.playerX, bot.y - hudState.playerY)
                    if (distToPlayer > hudState.weather.radarClarityRange) return@forEach
                }
                val color = when {
                    bot.isKnocked -> TacticalAmber
                    bot.team == CombatantTeam.PLAYER_SQUAD -> ElectricCyan
                    else -> CombatCrimson
                }
                val bx = toMapX(bot.x)
                val by = toMapY(bot.y)
                if (bot.team == CombatantTeam.ENEMY) {
                    drawCircle(color = color.copy(alpha = 0.28f), radius = if (zoomedRadar) 6f else 4.5f, center = Offset(bx, by))
                }
                drawCircle(color = color, radius = if (zoomedRadar) 3.4f else 2.6f, center = Offset(bx, by))
            }

            // Player FOV / Aim Cone
            val fovLength = if (zoomedRadar) 22f else 18f
            val fovSpread = 0.52f
            val aim = hudState.playerAimRad
            val conePath = Path().apply {
                moveTo(px, py)
                lineTo(px + cos(aim - fovSpread) * fovLength, py + sin(aim - fovSpread) * fovLength)
                lineTo(px + cos(aim + fovSpread) * fovLength, py + sin(aim + fovSpread) * fovLength)
                close()
            }
            drawPath(path = conePath, color = Color(0x45FFFFFF))

            // Player Directional Arrow Marker
            val tipLen = if (zoomedRadar) 7.5f else 6f
            val wingLen = if (zoomedRadar) 5.2f else 4.2f
            val arrowPath = Path().apply {
                moveTo(px + cos(aim) * tipLen, py + sin(aim) * tipLen)
                lineTo(px + cos(aim + 2.35f) * wingLen, py + sin(aim + 2.35f) * wingLen)
                lineTo(px - cos(aim) * (wingLen * 0.45f), py - sin(aim) * (wingLen * 0.45f))
                lineTo(px + cos(aim - 2.35f) * wingLen, py + sin(aim - 2.35f) * wingLen)
                close()
            }
            drawPath(path = arrowPath, color = Color.White)
            drawPath(path = arrowPath, color = ElectricCyan, style = Stroke(width = 1.2f))
        }

        drawRect(color = Color(0xFF3B4F7A), size = size, style = Stroke(2f))
    }
}
