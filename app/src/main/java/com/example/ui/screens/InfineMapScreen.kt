package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InfineMapBlueprint
import com.example.data.ObstacleKind
import com.example.data.VehicleCatalog
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfineMapScreen() {
    var selectedLocation by remember { mutableStateOf(InfineMapBlueprint.locations.first()) }
    val textMeasurer = rememberTextMeasurer()
    val mapLabelStyle = remember {
        TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("INFINE TACTICAL ATLAS", style = MaterialTheme.typography.headlineMedium, color = TacticalAmber)
            Text(
                text = "8 Named Districts • Clock Tower & Small Houses • 4 Revival Points • 8 Vehicle Spawns",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Interactive Blueprint Canvas of Infine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0E1726))
                .border(1.5.dp, TacticalBorder, RoundedCornerShape(18.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = size.width / InfineMapBlueprint.WORLD_SIZE

                // Roads
                InfineMapBlueprint.roads.forEach { road ->
                    drawLine(
                        color = Color(0xFF233048),
                        start = Offset(road.x1 * scale, road.y1 * scale),
                        end = Offset(road.x2 * scale, road.y2 * scale),
                        strokeWidth = road.width * scale
                    )
                }

                // Buildings / Clock Tower / Small Houses
                InfineMapBlueprint.obstacles.forEach { rect ->
                    val color = when (rect.kind) {
                        ObstacleKind.CLOCK_TOWER -> TacticalAmber
                        ObstacleKind.SMALL_HOUSE -> Color(0xFF64748B)
                        ObstacleKind.GARDEN_HEDGE -> NanoEmerald
                        ObstacleKind.HOSPITAL_WING -> CombatCrimson
                        else -> Color(0xFF475569)
                    }
                    drawRoundRect(
                        color = color.copy(alpha = 0.72f),
                        topLeft = Offset(rect.left * scale, rect.top * scale),
                        size = Size(rect.width * scale, rect.height * scale),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }

                // Vehicle spawn markers
                InfineMapBlueprint.vehicleSpawns.forEach { veh ->
                    drawCircle(
                        color = Color(0xFFFFEA00),
                        radius = 4.5f,
                        center = Offset(veh.x * scale, veh.y * scale)
                    )
                }

                // Revival Point beacons
                InfineMapBlueprint.revivalPoints.forEach { rp ->
                    drawCircle(
                        color = NanoEmerald.copy(alpha = 0.3f),
                        radius = 16f,
                        center = Offset(rp.x * scale, rp.y * scale)
                    )
                    drawCircle(
                        color = NanoEmerald,
                        radius = 6f,
                        center = Offset(rp.x * scale, rp.y * scale)
                    )
                }

                // POI markers & highlight ring
                InfineMapBlueprint.locations.forEach { loc ->
                    val cx = loc.x * scale
                    val cy = loc.y * scale
                    val isSel = loc.id == selectedLocation.id
                    if (isSel) {
                        drawCircle(
                            color = ElectricCyan,
                            radius = 24f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3f)
                        )
                    }
                    val labelResult = textMeasurer.measure(
                        text = loc.name.uppercase(),
                        style = mapLabelStyle,
                        softWrap = false,
                        maxLines = 1
                    )
                    val maxX = (size.width - labelResult.size.width - 6f).coerceAtLeast(6f)
                    val maxY = (size.height - labelResult.size.height - 6f).coerceAtLeast(6f)
                    if (size.width > 0f && size.height > 0f) {
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(
                                x = (cx - 32f).coerceIn(6f, maxX),
                                y = (cy - 22f).coerceIn(6f, maxY)
                            )
                        )
                    }
                }
            }
        }

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(TacticalAmber, "Clock Tower")
            LegendItem(NanoEmerald, "Revival Point")
            LegendItem(Color(0xFFFFEA00), "Vehicle Spawn")
            LegendItem(ElectricCyan, "Selected POI")
        }

        // POI Selector Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfineMapBlueprint.locations.forEach { loc ->
                val selected = loc.id == selectedLocation.id
                FilterChip(
                    selected = selected,
                    onClick = { selectedLocation = loc },
                    label = { Text(loc.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalAmber.copy(alpha = 0.22f),
                        selectedLabelColor = TacticalAmber
                    ),
                    modifier = Modifier.testTag("map_poi_${loc.id}")
                )
            }
        }

        // Selected POI Briefing Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TacticalAmber)
                        Text(selectedLocation.name.uppercase(), style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    }
                    Surface(color = ObsidianElevated, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = selectedLocation.lootTier.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(selectedLocation.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                if (selectedLocation.hasRevivalPoint) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = NanoEmerald, modifier = Modifier.size(16.dp))
                        Text("Revival Point Active in this Zone", style = MaterialTheme.typography.labelSmall, color = NanoEmerald)
                    }
                }
            }
        }

        // Vehicle Spawns overview
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("INFINE VEHICLE FLEET", style = MaterialTheme.typography.titleMedium, color = TacticalAmber)
                Text(
                    text = VehicleCatalog.all.joinToString(" • ") { "${it.badge} ${it.name}" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
