package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CharacterSkinCatalog
import com.example.data.CharacterSkinSpec
import com.example.data.CosmeticCategory
import com.example.data.EmoteCatalog
import com.example.data.EmoteSpec
import com.example.data.WeaponCatalog
import com.example.data.WeaponId
import com.example.data.WeaponSkinCatalog
import com.example.data.WeaponSkinRarity
import com.example.data.WeaponSkinSpec
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.WeaponSkinStateEntity
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PlasmaViolet
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private sealed interface PendingPurchase {
    val itemName: String
    val costCredits: Int
    val costGems: Int

    data class CharacterSkin(val spec: CharacterSkinSpec) : PendingPurchase {
        override val itemName: String = spec.name
        override val costCredits: Int = spec.costCredits
        override val costGems: Int = spec.costGems
    }

    data class WeaponWrap(val weaponId: WeaponId, val weaponName: String, val spec: WeaponSkinSpec) : PendingPurchase {
        override val itemName: String = "$weaponName • ${spec.name}"
        override val costCredits: Int = spec.costCredits
        override val costGems: Int = spec.costGems
    }

    data class Emote(val spec: EmoteSpec) : PendingPurchase {
        override val itemName: String = spec.name
        override val costCredits: Int = spec.costCredits
        override val costGems: Int = spec.costGems
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    profile: PlayerProfileEntity,
    weaponSkins: Map<WeaponId, WeaponSkinStateEntity>,
    statusMessage: String?,
    onUnlockOrEquipCharacterSkin: (String) -> Unit,
    onUnlockOrEquipWeaponWrap: (WeaponId, String) -> Unit,
    onUnlockOrEquipEmote: (String) -> Unit,
    onClearStatusMessage: () -> Unit,
    onOpenCurrencyVault: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedCategory by rememberSaveable { mutableStateOf(CosmeticCategory.ALL) }
    val initialWeapon = remember(profile.equippedWeaponName) {
        runCatching { WeaponId.valueOf(profile.equippedWeaponName) }.getOrDefault(WeaponId.AK47)
    }
    var selectedWeaponId by rememberSaveable { mutableStateOf(initialWeapon) }
    var pendingPurchase by remember { mutableStateOf<PendingPurchase?>(null) }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            snackbarHostState.showSnackbar(statusMessage)
            onClearStatusMessage()
        }
    }

    val unlockedCharacterSkins = remember(profile.unlockedCharacterSkinsCsv) {
        profile.unlockedCharacterSkinsCsv.split(",").filter { it.isNotBlank() }.toSet() + "default_vanguard"
    }
    val unlockedEmotes = remember(profile.unlockedEmotesCsv) {
        profile.unlockedEmotesCsv.split(",").filter { it.isNotBlank() }.toSet() + "tactical_salute"
    }
    val selectedWeaponSpec = WeaponCatalog.byId(selectedWeaponId)
    val weaponSkinState = weaponSkins[selectedWeaponId]
    val equippedWrapId = weaponSkinState?.equippedSkinId ?: "standard"
    val unlockedWrapIds = remember(weaponSkinState?.unlockedSkinIdsCsv) {
        (weaponSkinState?.unlockedSkinIdsCsv?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()) + "standard"
    }

    Scaffold(
        containerColor = ObsidianVoid,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianSurface,
                    titleContentColor = TextPrimary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("store_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = TacticalAmber
                        )
                        Text(
                            text = "INFINE STORE",
                            style = MaterialTheme.typography.titleLarge,
                            color = TacticalAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(10.dp),
                        onClick = onOpenCurrencyVault,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .heightIn(min = 48.dp)
                            .border(1.dp, TacticalAmber.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .testTag("store_wallet_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = "Gold", tint = TacticalAmber, modifier = Modifier.size(16.dp))
                                Text("${profile.credits}", style = MaterialTheme.typography.labelLarge, color = TacticalAmber, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Diamond, contentDescription = "Gems", tint = ElectricCyan, modifier = Modifier.size(15.dp))
                                Text("${profile.gems}", style = MaterialTheme.typography.labelLarge, color = ElectricCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = CosmeticCategory.entries.indexOf(selectedCategory),
                containerColor = ObsidianSurface,
                contentColor = TacticalAmber
            ) {
                CosmeticCategory.entries.forEach { category ->
                    val selected = selectedCategory == category
                    Tab(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = category.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("store_tab_${category.name}")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Featured Cosmetic Bundle Banner
                item(key = "store_hero_banner") {
                    FeaturedStoreBanner(
                        equippedSkinName = CharacterSkinCatalog.byId(profile.equippedCharacterSkinId).name,
                        equippedEmoteName = EmoteCatalog.byId(profile.equippedEmoteId).name,
                        equippedWrapName = WeaponSkinCatalog.byId(equippedWrapId).name,
                        selectedWeaponName = selectedWeaponSpec.name
                    )
                }

                // Section 1: Character Skins
                if (selectedCategory == CosmeticCategory.ALL || selectedCategory == CosmeticCategory.CHARACTER_SKINS) {
                    item(key = "header_skins") {
                        StoreSectionHeader(
                            title = "CHARACTER SKINS",
                            subtitle = "Customize your operator exo-suit visible live in Infine matches",
                            accent = PlasmaViolet
                        )
                    }
                    items(CharacterSkinCatalog.all, key = { "char_skin_${it.id}" }) { skin ->
                        val isUnlocked = skin.id in unlockedCharacterSkins || (skin.costCredits == 0 && skin.costGems == 0)
                        val isEquipped = profile.equippedCharacterSkinId == skin.id
                        CharacterSkinStoreCard(
                            spec = skin,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            canAfford = profile.credits >= skin.costCredits && profile.gems >= skin.costGems,
                            onActionClick = {
                                if (isUnlocked) {
                                    onUnlockOrEquipCharacterSkin(skin.id)
                                } else {
                                    pendingPurchase = PendingPurchase.CharacterSkin(skin)
                                }
                            }
                        )
                    }
                }

                // Section 2: Weapon Wraps
                if (selectedCategory == CosmeticCategory.ALL || selectedCategory == CosmeticCategory.WEAPON_WRAPS) {
                    item(key = "header_wraps") {
                        StoreSectionHeader(
                            title = "WEAPON WRAPS",
                            subtitle = "Select a weapon below to unlock & equip tracer wraps",
                            accent = ElectricCyan
                        )
                    }
                    item(key = "weapon_selector_row") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(WeaponCatalog.all, key = { "wrap_weapon_${it.id.name}" }) { weapon ->
                                val isSel = weapon.id == selectedWeaponId
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedWeaponId = weapon.id },
                                    label = { Text(weapon.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TacticalAmber.copy(alpha = 0.24f),
                                        selectedLabelColor = TacticalAmber
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("store_wrap_weapon_${weapon.id.name}")
                                )
                            }
                        }
                    }
                    items(WeaponSkinCatalog.all, key = { "wrap_${selectedWeaponId.name}_${it.id}" }) { wrap ->
                        val isUnlocked = wrap.id in unlockedWrapIds || (wrap.costCredits == 0 && wrap.costGems == 0)
                        val isEquipped = equippedWrapId == wrap.id
                        WeaponWrapStoreCard(
                            weaponName = selectedWeaponSpec.name,
                            spec = wrap,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            canAfford = profile.credits >= wrap.costCredits && profile.gems >= wrap.costGems,
                            onActionClick = {
                                if (isUnlocked) {
                                    onUnlockOrEquipWeaponWrap(selectedWeaponId, wrap.id)
                                } else {
                                    pendingPurchase = PendingPurchase.WeaponWrap(selectedWeaponId, selectedWeaponSpec.name, wrap)
                                }
                            }
                        )
                    }
                }

                // Section 3: Emotes
                if (selectedCategory == CosmeticCategory.ALL || selectedCategory == CosmeticCategory.EMOTES) {
                    item(key = "header_emotes") {
                        StoreSectionHeader(
                            title = "COMBAT EMOTES",
                            subtitle = "Trigger glowing radial shockwaves & callout shouts inside matches",
                            accent = TacticalAmber
                        )
                    }
                    items(EmoteCatalog.all, key = { "emote_${it.id}" }) { emote ->
                        val isUnlocked = emote.id in unlockedEmotes || (emote.costCredits == 0 && emote.costGems == 0)
                        val isEquipped = profile.equippedEmoteId == emote.id
                        EmoteStoreCard(
                            spec = emote,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            canAfford = profile.credits >= emote.costCredits && profile.gems >= emote.costGems,
                            onActionClick = {
                                if (isUnlocked) {
                                    onUnlockOrEquipEmote(emote.id)
                                } else {
                                    pendingPurchase = PendingPurchase.Emote(emote)
                                }
                            }
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    pendingPurchase?.let { purchase ->
        val hasEnoughGold = profile.credits >= purchase.costCredits
        val hasEnoughGems = profile.gems >= purchase.costGems
        val canConfirm = hasEnoughGold && hasEnoughGems
        val priceText = formatStorePrice(purchase.costCredits, purchase.costGems)

        AlertDialog(
            onDismissRequest = { pendingPurchase = null },
            containerColor = ObsidianCard,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TacticalAmber)
                    Text(
                        text = "Confirm Cosmetic Purchase",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = purchase.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan
                    )
                    Text(
                        text = if (canConfirm) {
                            "Unlock and equip this cosmetic for $priceText?"
                        } else {
                            "You need $priceText to unlock this item. Earn more Gold & Gems in ranked matches or visit the Currency Vault."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (canConfirm) TextSecondary else CombatCrimson
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (purchase) {
                            is PendingPurchase.CharacterSkin -> onUnlockOrEquipCharacterSkin(purchase.spec.id)
                            is PendingPurchase.WeaponWrap -> onUnlockOrEquipWeaponWrap(purchase.weaponId, purchase.spec.id)
                            is PendingPurchase.Emote -> onUnlockOrEquipEmote(purchase.spec.id)
                        }
                        pendingPurchase = null
                    },
                    enabled = canConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalAmber,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("confirm_store_purchase_button")
                ) {
                    Text("Buy ($priceText)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingPurchase = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancel", color = ElectricCyan)
                }
            }
        )
    }
}

@Composable
private fun FeaturedStoreBanner(
    equippedSkinName: String,
    equippedEmoteName: String,
    equippedWrapName: String,
    selectedWeaponName: String
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = ObsidianCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TacticalAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PlasmaViolet.copy(alpha = 0.22f),
                            ObsidianCard,
                            ElectricCyan.copy(alpha = 0.16f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TacticalAmber)
                    Text(
                        text = "SEASON COSMETIC SHOWCASE",
                        style = MaterialTheme.typography.labelLarge,
                        color = TacticalAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Suit up in Void Eclipse, Clockwork Sovereign & Plasma Prism wraps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EquippedMiniPill("SKIN", equippedSkinName, PlasmaViolet, Modifier.weight(1f))
                    EquippedMiniPill(selectedWeaponName.uppercase(), equippedWrapName, ElectricCyan, Modifier.weight(1f))
                    EquippedMiniPill("EMOTE", equippedEmoteName, TacticalAmber, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EquippedMiniPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = accent, maxLines = 1)
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StoreSectionHeader(
    title: String,
    subtitle: String,
    accent: Color
) {
    Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun CharacterSkinStoreCard(
    spec: CharacterSkinSpec,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onActionClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isEquipped) ObsidianElevated else ObsidianCard
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) spec.suitColor else TacticalBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("store_skin_card_${spec.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Operator Exo-Suit Preview Swatch
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, spec.rimColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(38.dp)) {
                    drawCircle(
                        color = spec.rimColor,
                        radius = size.minDimension * 0.44f
                    )
                    drawCircle(
                        color = spec.suitColor,
                        radius = size.minDimension * 0.34f
                    )
                    drawLine(
                        color = Color.White,
                        start = center,
                        end = Offset(center.x + size.minDimension * 0.34f, center.y),
                        strokeWidth = 5f
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(spec.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    RarityBadge(spec.rarity)
                }
                Text(spec.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                StorePriceRow(
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    costCredits = spec.costCredits,
                    costGems = spec.costGems
                )
            }

            StoreActionButton(
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                canAfford = canAfford,
                accent = spec.suitColor,
                testTag = "store_skin_btn_${spec.id}",
                onClick = onActionClick
            )
        }
    }
}

@Composable
private fun WeaponWrapStoreCard(
    weaponName: String,
    spec: WeaponSkinSpec,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onActionClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isEquipped) ObsidianElevated else ObsidianCard
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) spec.primaryColor else TacticalBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("store_wrap_card_${spec.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, spec.primaryColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    drawRoundRect(
                        color = spec.primaryColor,
                        topLeft = Offset(size.width * 0.1f, size.height * 0.36f),
                        size = Size(size.width * 0.65f, size.height * 0.24f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawLine(
                        color = spec.tracerColor,
                        start = Offset(size.width * 0.76f, size.height * 0.48f),
                        end = Offset(size.width * 0.96f, size.height * 0.48f),
                        strokeWidth = 6f
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(spec.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    RarityBadge(spec.rarity)
                }
                Text(
                    text = "$weaponName finish with custom tracer rounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                StorePriceRow(
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    costCredits = spec.costCredits,
                    costGems = spec.costGems
                )
            }

            StoreActionButton(
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                canAfford = canAfford,
                accent = spec.primaryColor,
                testTag = "store_wrap_btn_${spec.id}",
                onClick = onActionClick
            )
        }
    }
}

@Composable
private fun EmoteStoreCard(
    spec: EmoteSpec,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onActionClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isEquipped) ObsidianElevated else ObsidianCard
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) spec.accentColor else TacticalBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("store_emote_card_${spec.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, spec.accentColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    drawCircle(
                        color = spec.accentColor.copy(alpha = 0.25f),
                        radius = size.minDimension * 0.45f
                    )
                    drawCircle(
                        color = spec.accentColor,
                        radius = size.minDimension * 0.34f,
                        style = Stroke(width = 3.5f)
                    )
                }
                Text(text = spec.emoji, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("${spec.emoji} ${spec.name}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    RarityBadge(spec.rarity)
                }
                Text("${spec.description} (Shout: \"${spec.shoutText}\")", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                StorePriceRow(
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    costCredits = spec.costCredits,
                    costGems = spec.costGems
                )
            }

            StoreActionButton(
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                canAfford = canAfford,
                accent = spec.accentColor,
                testTag = "store_emote_btn_${spec.id}",
                onClick = onActionClick
            )
        }
    }
}

@Composable
private fun RarityBadge(rarity: WeaponSkinRarity) {
    Surface(
        color = rarity.badgeColor.copy(alpha = 0.18f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = rarity.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = rarity.badgeColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StorePriceRow(
    isUnlocked: Boolean,
    isEquipped: Boolean,
    costCredits: Int,
    costGems: Int
) {
    when {
        isEquipped -> {
            Text("Equipped in Loadout", style = MaterialTheme.typography.labelMedium, color = NanoEmerald, fontWeight = FontWeight.SemiBold)
        }
        isUnlocked -> {
            Text("Owned", style = MaterialTheme.typography.labelMedium, color = ElectricCyan, fontWeight = FontWeight.SemiBold)
        }
        else -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (costCredits > 0) {
                    Icon(Icons.Default.Stars, contentDescription = "Gold", tint = TacticalAmber, modifier = Modifier.size(14.dp))
                    Text("$costCredits Gold", style = MaterialTheme.typography.labelMedium, color = TacticalAmber, fontWeight = FontWeight.Bold)
                }
                if (costGems > 0) {
                    Icon(Icons.Default.Diamond, contentDescription = "Gems", tint = ElectricCyan, modifier = Modifier.size(14.dp))
                    Text("$costGems Gems", style = MaterialTheme.typography.labelMedium, color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StoreActionButton(
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    when {
        isEquipped -> {
            Surface(
                color = accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .border(1.dp, accent, RoundedCornerShape(10.dp))
                    .testTag(testTag)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Equipped", tint = accent, modifier = Modifier.size(16.dp))
                    Text("ACTIVE", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold)
                }
            }
        }
        isUnlocked -> {
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(testTag)
            ) {
                Text("Equip", color = ElectricCyan, fontWeight = FontWeight.Bold)
            }
        }
        else -> {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) TacticalAmber else ObsidianElevated,
                    contentColor = if (canAfford) ObsidianVoid else TextSecondary
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(testTag)
            ) {
                Text("Unlock", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatStorePrice(costCredits: Int, costGems: Int): String {
    val parts = buildList {
        if (costCredits > 0) add("$costCredits Gold")
        if (costGems > 0) add("$costGems Gems")
    }
    return if (parts.isEmpty()) "Free" else parts.joinToString(" + ")
}
