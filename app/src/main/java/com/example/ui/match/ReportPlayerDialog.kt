package com.example.ui.match

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ReportReason
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ReportPlayerDialog(
    phaseLabel: String,
    candidateCallsigns: List<String>,
    modeName: String,
    arenaLevel: Int,
    mapLocation: String,
    onSubmit: (callsign: String, reason: ReportReason, details: String) -> Unit,
    onDismiss: () -> Unit
) {
    val cleanCandidates = remember(candidateCallsigns) {
        candidateCallsigns.filter { it.isNotBlank() }.distinct().take(12)
    }
    var selectedCallsign by remember(cleanCandidates) {
        mutableStateOf(cleanCandidates.firstOrNull() ?: "")
    }
    var customCallsignMode by remember { mutableStateOf(cleanCandidates.isEmpty()) }
    var selectedReason by remember { mutableStateOf(ReportReason.AIMBOT) }
    var detailsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = CombatCrimson,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "REPORT SUSPICIOUS PLAYER",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$phaseLabel • LVL $arenaLevel $modeName • $mapLocation",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = ObsidianElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Reports are saved directly to the local Infine Anti-Cheat database.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "SELECT PLAYER / CALLSIGN",
                    style = MaterialTheme.typography.labelLarge,
                    color = TacticalAmber
                )

                if (cleanCandidates.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cleanCandidates.forEach { callsign ->
                            val isSelected = !customCallsignMode && selectedCallsign == callsign
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    customCallsignMode = false
                                    selectedCallsign = callsign
                                },
                                label = { Text(callsign) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CombatCrimson.copy(alpha = 0.25f),
                                    selectedLabelColor = TextPrimary
                                ),
                                modifier = Modifier.testTag("report_candidate_$callsign")
                            )
                        }
                        FilterChip(
                            selected = customCallsignMode,
                            onClick = {
                                customCallsignMode = true
                                if (selectedCallsign in cleanCandidates) {
                                    selectedCallsign = ""
                                }
                            },
                            label = { Text("Custom") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.22f),
                                selectedLabelColor = ElectricCyan
                            ),
                            modifier = Modifier.testTag("report_candidate_custom")
                        )
                    }
                }

                OutlinedTextField(
                    value = selectedCallsign,
                    onValueChange = {
                        customCallsignMode = true
                        selectedCallsign = it.take(24)
                    },
                    label = { Text("Suspect Callsign") },
                    placeholder = { Text("Enter player callsign") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_callsign_input")
                )

                Text(
                    text = "VIOLATION CATEGORY",
                    style = MaterialTheme.typography.labelLarge,
                    color = TacticalAmber
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReportReason.entries.forEach { reason ->
                        val active = selectedReason == reason
                        Surface(
                            color = if (active) CombatCrimson.copy(alpha = 0.16f) else ObsidianSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .border(
                                    width = if (active) 1.5.dp else 1.dp,
                                    color = if (active) CombatCrimson else TacticalBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedReason = reason }
                                .testTag("report_reason_${reason.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = active,
                                    onClick = { selectedReason = reason },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CombatCrimson,
                                        unselectedColor = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reason.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (active) TextPrimary else TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = reason.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it.take(180) },
                    label = { Text("Incident Details (Optional)") },
                    placeholder = { Text("e.g., Snapped through Clock Tower cover") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_details_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        selectedCallsign.trim().ifBlank { "Unknown Player" },
                        selectedReason,
                        detailsText.trim()
                    )
                },
                enabled = selectedCallsign.isNotBlank() || !customCallsignMode,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CombatCrimson,
                    contentColor = ObsidianVoid
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("report_submit_button")
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Submit Report", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("report_cancel_button")
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
