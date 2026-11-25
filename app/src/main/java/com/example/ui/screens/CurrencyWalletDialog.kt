package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.CurrencyTransactionEntity
import com.example.data.local.PlayerProfileEntity
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

@Composable
fun CurrencyWalletDialog(
    profile: PlayerProfileEntity,
    transactions: List<CurrencyTransactionEntity>,
    statusMessage: String?,
    onBuyMedkitSupply: () -> Unit,
    onBuyShieldSupply: () -> Unit,
    onExchangeGoldForGems: () -> Unit,
    onExchangeGemsForGold: () -> Unit,
    onOpenStore: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TacticalAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TacticalAmber)
                    }
                    Column {
                        Text("INFINE CURRENCY VAULT", style = MaterialTheme.typography.titleLarge, color = TacticalAmber)
                        Text("Earn Gold & Gems in matches & daily login", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Vault", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dual Balance Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WalletBalanceCard(
                        label = "INFINE GOLD",
                        amount = "${profile.credits}",
                        icon = Icons.Default.Stars,
                        accent = TacticalAmber,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_gold_balance")
                    )
                    WalletBalanceCard(
                        label = "INFINE GEMS",
                        amount = "${profile.gems}",
                        icon = Icons.Default.Diamond,
                        accent = ElectricCyan,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_gems_balance")
                    )
                }

                // Active Drop Boosters Indicator
                if (profile.bonusMedkitDrops > 0 || profile.bonusShieldDrops > 0) {
                    Surface(
                        color = NanoEmerald.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NanoEmerald, RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "Next Drop Boosters Active: +${profile.bonusMedkitDrops} Medkits • +${profile.bonusShieldDrops} Armor",
                            style = MaterialTheme.typography.labelMedium,
                            color = NanoEmerald,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (statusMessage != null) {
                    Surface(
                        color = ObsidianElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .testTag("wallet_status_message")
                    ) {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text("TACTICAL SUPPLIES & EXCHANGE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

                CurrencyActionRow(
                    title = "Field Medkit Pack (+2 Medkits)",
                    subtitle = "Drop with extra Medkits next ranked match",
                    costLabel = "250 Gold",
                    icon = Icons.Default.LocalHospital,
                    accent = NanoEmerald,
                    enabled = profile.credits >= 250 && profile.bonusMedkitDrops < 4,
                    testTag = "buy_medkit_supply_button",
                    onClick = onBuyMedkitSupply
                )

                CurrencyActionRow(
                    title = "Kevlar Armor Plate (+25 Shield)",
                    subtitle = "Drop with reinforced 75 Armor next match",
                    costLabel = "350 Gold",
                    icon = Icons.Default.Shield,
                    accent = ElectricCyan,
                    enabled = profile.credits >= 350 && profile.bonusShieldDrops < 25,
                    testTag = "buy_shield_supply_button",
                    onClick = onBuyShieldSupply
                )

                CurrencyActionRow(
                    title = "Mint +35 Infine Gems",
                    subtitle = "Convert 1,000 Gold into 35 Gems",
                    costLabel = "1,000 Gold",
                    icon = Icons.Default.SwapHoriz,
                    accent = TacticalAmber,
                    enabled = profile.credits >= 1000,
                    testTag = "exchange_gold_to_gems_button",
                    onClick = onExchangeGoldForGems
                )

                CurrencyActionRow(
                    title = "Convert +450 Infine Gold",
                    subtitle = "Exchange 25 Gems for 450 Gold",
                    costLabel = "25 Gems",
                    icon = Icons.Default.Diamond,
                    accent = ElectricCyan,
                    enabled = profile.gems >= 25,
                    testTag = "exchange_gems_to_gold_button",
                    onClick = onExchangeGemsForGold
                )

                Text("RECENT CURRENCY LEDGER", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

                if (transactions.isEmpty()) {
                    Text(
                        text = "Complete ranked matches or claim Daily Rewards to record Gold & Gem transactions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(transactions, key = { it.id }) { tx ->
                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (tx.goldDelta != 0) {
                                            Text(
                                                text = "${if (tx.goldDelta > 0) "+" else ""}${tx.goldDelta} G",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (tx.goldDelta > 0) TacticalAmber else CombatCrimson,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (tx.gemsDelta != 0) {
                                            Text(
                                                text = "${if (tx.gemsDelta > 0) "+" else ""}${tx.gemsDelta} Gem",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (tx.gemsDelta > 0) ElectricCyan else CombatCrimson,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = if (onOpenStore != null) {
            {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenStore()
                    },
                    modifier = Modifier.testTag("wallet_open_store_button")
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cosmetic Store", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            null
        }
    )
}

@Composable
private fun WalletBalanceCard(
    label: String,
    amount: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(22.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(amount, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CurrencyActionRow(
    title: String,
    subtitle: String,
    costLabel: String,
    icon: ImageVector,
    accent: Color,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.testTag(testTag)
            ) {
                Text(costLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
