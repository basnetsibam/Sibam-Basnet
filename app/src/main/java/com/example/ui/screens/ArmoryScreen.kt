package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.OperatorCatalog
import com.example.data.SkillType
import com.example.data.VehicleCatalog
import com.example.data.WeaponCatalog
import com.example.data.WeaponId
import com.example.data.WeaponSkinCatalog
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.WeaponSkinStateEntity
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArmoryScreen(
    profile: PlayerProfileEntity,
    weaponSkins: Map<WeaponId, WeaponSkinStateEntity>,
    bannerMessage: String?,
    onEquipWeapon: (WeaponId) -> Unit,
    onEquipOperator: (String) -> Unit,
    onUnlockOrEquipSkin: (WeaponId, String) -> Unit,
    onDismissBanner: () -> Unit,
    onOpenCurrencyVault: () -> Unit,
    onOpenStore: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val equippedWeaponId = runCatching { WeaponId.valueOf(profile.equippedWeaponName) }
        .getOrDefault(WeaponId.AK47)

    Column(modifier = Modifier.fillMaxSize()) {
        // Armory Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_arsenal_banner),
                contentDescription = "Armory Weapon Vault",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0x550B101B), ObsidianVoid)))
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ObsidianSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenStore)
                        .testTag("armory_open_store_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = "Cosmetic Store", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Text("Store", style = MaterialTheme.typography.labelLarge, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = ObsidianSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .border(1.dp, TacticalAmber.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenCurrencyVault)
                        .testTag("armory_credits_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Stars, contentDescription = "Gold", tint = TacticalAmber, modifier = Modifier.size(16.dp))
                            Text("${profile.credits} G", style = MaterialTheme.typography.labelLarge, color = TacticalAmber, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Diamond, contentDescription = "Gems", tint = ElectricCyan, modifier = Modifier.size(15.dp))
                            Text("${profile.gems}", style = MaterialTheme.typography.labelLarge, color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("TACTICAL ARMORY", style = MaterialTheme.typography.headlineMedium, color = TacticalAmber)
                Text("8 Weapons & Skins • 3 Character Skills • 4 Infine Vehicles", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ObsidianSurface,
            contentColor = TacticalAmber
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Weapons (8)") },
                modifier = Modifier.testTag("armory_tab_weapons")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Skills (3)") },
                modifier = Modifier.testTag("armory_tab_skills")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Vehicles (4)") },
                modifier = Modifier.testTag("armory_tab_vehicles")
            )
        }

        if (bannerMessage != null) {
            Surface(
                color = CombatCrimson.copy(alpha = 0.16f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .border(1.dp, CombatCrimson, RoundedCornerShape(10.dp))
                    .testTag("armory_banner_message")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = bannerMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissBanner) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss alert", tint = CombatCrimson)
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(WeaponCatalog.all) { weapon ->
                        val isEquipped = weapon.id == equippedWeaponId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEquipped) ObsidianElevated else ObsidianCard
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isEquipped) 2.dp else 1.dp,
                                    color = if (isEquipped) TacticalAmber else TacticalBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("weapon_card_${weapon.id.name}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(weapon.accentColor.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = weapon.accentColor)
                                        }
                                        Column {
                                            Text(weapon.name, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                            Text("${weapon.category.label} • Mag ${weapon.magSize}", style = MaterialTheme.typography.labelSmall, color = weapon.accentColor)
                                        }
                                    }

                                    if (isEquipped) {
                                        Surface(color = TacticalAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Equipped", tint = TacticalAmber, modifier = Modifier.size(16.dp))
                                                Text("EQUIPPED", style = MaterialTheme.typography.labelSmall, color = TacticalAmber)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { onEquipWeapon(weapon.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
                                            modifier = Modifier.testTag("equip_weapon_${weapon.id.name}")
                                        ) {
                                            Text("Equip", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Text(weapon.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    WeaponStatBar("DMG", weapon.damage / 120f, "${weapon.damage}", weapon.accentColor, Modifier.weight(1f))
                                    WeaponStatBar("PEN", weapon.armorPen / 100f, "${weapon.armorPen}%", ElectricCyan, Modifier.weight(1f))
                                    WeaponStatBar("RNG", (weapon.range / 1000f).coerceIn(0f, 1f), "${weapon.range.toInt()}m", NanoEmerald, Modifier.weight(1f))
                                }

                                // Weapon Skins Section
                                val skinState = weaponSkins[weapon.id]
                                val equippedSkinId = skinState?.equippedSkinId ?: "standard"
                                val unlockedSkinIds = skinState?.unlockedSkinIdsCsv
                                    ?.split(",")
                                    ?.filter { it.isNotBlank() }
                                    ?.toSet()
                                    ?: setOf("standard")

                                Text(
                                    text = "WEAPON SKINS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    WeaponSkinCatalog.all.forEach { skin ->
                                        val isSkinEquipped = equippedSkinId == skin.id
                                        val isSkinUnlocked = skin.id in unlockedSkinIds || (skin.costCredits == 0 && skin.costGems == 0)
                                        val priceColor = if (skin.costGems > 0) ElectricCyan else TacticalAmber
                                        Surface(
                                            color = when {
                                                isSkinEquipped -> skin.primaryColor.copy(alpha = 0.22f)
                                                isSkinUnlocked -> ObsidianSurface
                                                else -> ObsidianVoid.copy(alpha = 0.7f)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .border(
                                                    width = if (isSkinEquipped) 1.5.dp else 1.dp,
                                                    color = if (isSkinEquipped) skin.primaryColor else TacticalBorder,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { onUnlockOrEquipSkin(weapon.id, skin.id) }
                                                .testTag("weapon_skin_${weapon.id.name}_${skin.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(skin.primaryColor)
                                                )
                                                Column {
                                                    Text(
                                                        text = skin.name,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = if (isSkinEquipped) skin.primaryColor else TextPrimary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        if (!isSkinUnlocked) {
                                                            Icon(
                                                                Icons.Default.Lock,
                                                                contentDescription = null,
                                                                tint = priceColor,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = when {
                                                                isSkinEquipped -> "EQUIPPED"
                                                                isSkinUnlocked -> "Tap to Equip"
                                                                skin.costGems > 0 -> "${skin.costGems} Gems"
                                                                else -> "${skin.costCredits} Gold"
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = when {
                                                                isSkinEquipped -> skin.primaryColor
                                                                isSkinUnlocked -> ElectricCyan
                                                                else -> priceColor
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(OperatorCatalog.all) { op ->
                        val isEquipped = op.id == profile.equippedOperatorId
                        val skillIcon = when (op.skillType) {
                            SkillType.HEAL -> Icons.Default.LocalHospital
                            SkillType.REVIVE -> Icons.Default.Favorite
                            SkillType.DAMAGE -> Icons.Default.Bolt
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isEquipped) ObsidianElevated else ObsidianCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isEquipped) 2.dp else 1.dp,
                                    color = if (isEquipped) op.accentColor else TacticalBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onEquipOperator(op.id) }
                                .testTag("operator_card_${op.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(op.accentColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(skillIcon, contentDescription = op.skillName, tint = op.accentColor, modifier = Modifier.size(30.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(op.name, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                        Surface(color = op.accentColor.copy(alpha = 0.18f), shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = "${op.skillType.shortLabel.uppercase()} SKILL",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = op.accentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("${op.skillName} • ${op.cooldownSec}s CD", style = MaterialTheme.typography.titleMedium, color = op.accentColor)
                                    Text(op.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                                if (isEquipped) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active Operator", tint = op.accentColor)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(VehicleCatalog.all) { veh ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(veh.accentColor.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (veh.badge == "🛺") {
                                                Text("🛺", style = MaterialTheme.typography.titleLarge)
                                            } else {
                                                Icon(Icons.Default.DirectionsCar, contentDescription = veh.name, tint = veh.accentColor)
                                            }
                                        }
                                        Column {
                                            Text(veh.name, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                            Text("${veh.seats} Seats • ${veh.maxHp} HP", style = MaterialTheme.typography.labelSmall, color = veh.accentColor)
                                        }
                                    }
                                    Surface(color = ObsidianElevated, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            text = "RAM ${veh.ramDamage} DMG",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CombatCrimson,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Text(veh.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                WeaponStatBar("TOP SPEED", (veh.maxSpeed / 360f).coerceIn(0f, 1f), "${veh.maxSpeed.toInt()} km/h", ElectricCyan, Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeaponStatBar(
    label: String,
    progress: Float,
    valueText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(valueText, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = color,
            trackColor = ObsidianSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
