package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.GameMode
import com.example.data.SquadMode
import com.example.data.WeaponId

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val callsign: String = "APEX-01",
    val brRp: Int = 1540,
    val csRp: Int = 1480,
    val totalXp: Int = 0,
    val credits: Int = 1000,
    val gems: Int = 120,
    val bonusMedkitDrops: Int = 0,
    val bonusShieldDrops: Int = 0,
    val loginStreak: Int = 0,
    val lastDailyClaimDay: Long = -1L,
    val selectedArenaLevel: Int = 1,
    val totalKills: Int = 0,
    val totalWins: Int = 0,
    val totalMatches: Int = 0,
    val totalRevives: Int = 0,
    val equippedWeaponName: String = WeaponId.AK47.name,
    val equippedOperatorId: String = "vespera",
    val equippedCharacterSkinId: String = "default_vanguard",
    val unlockedCharacterSkinsCsv: String = "default_vanguard",
    val equippedEmoteId: String = "tactical_salute",
    val unlockedEmotesCsv: String = "tactical_salute",
    val preferredModeName: String = GameMode.BATTLE_ROYALE_RANKED.name,
    val preferredSquadName: String = SquadMode.SQUAD.name,
    val honorScore: Int = 100,
    val lastHonorNotice: String = "Honor 100/100 • All Ranked Modes Cleared"
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modeName: String,
    val squadName: String,
    val placementText: String,
    val kills: Int,
    val revives: Int,
    val damageDealt: Int,
    val rpDelta: Int,
    val xpEarned: Int = 0,
    val arenaLevel: Int = 1,
    val mapLocation: String,
    val weaponUsed: String,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "social_friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val callsign: String,
    val rankLabel: String,
    val isInSquad: Boolean = false,
    val isOnline: Boolean = true,
    val addedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "weapon_skins")
data class WeaponSkinStateEntity(
    @PrimaryKey val weaponName: String,
    val equippedSkinId: String = "standard",
    val unlockedSkinIdsCsv: String = "standard"
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey val callsign: String,
    val regionTag: String,
    val rp: Int,
    val totalWins: Int,
    val totalKills: Int,
    val totalMatches: Int,
    val isFriend: Boolean = false,
    val lastDeltaNote: String = "",
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "currency_transactions")
data class CurrencyTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val goldDelta: Int,
    val gemsDelta: Int,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "incident_reports")
data class IncidentReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reportedCallsign: String,
    val reasonLabel: String,
    val details: String,
    val modeName: String,
    val arenaLevel: Int,
    val mapLocation: String,
    val phaseLabel: String,
    val statusLabel: String = "Logged to DB",
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "clans")
data class ClanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val tag: String,
    val motto: String,
    val leaderCallsign: String,
    val isJoined: Boolean = false,
    val isPlayerCreated: Boolean = false,
    val memberCount: Int = 1,
    val clanXp: Int = 0,
    val equippedBannerId: String = "vanguard_crest",
    val membersCsv: String = ""
)

@Entity(tableName = "clan_missions")
data class ClanMissionProgressEntity(
    @PrimaryKey val missionId: String,
    val clanId: Int,
    val progressCount: Int = 0,
    val isClaimed: Boolean = false
)

@Entity(tableName = "match_replays")
data class MatchReplayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modeName: String,
    val squadName: String,
    val arenaLevel: Int,
    val won: Boolean,
    val placementRank: Int,
    val kills: Int,
    val durationSeconds: Int,
    val mapLocation: String,
    val weaponUsed: String,
    val framesJson: String,
    val timestampMs: Long = System.currentTimeMillis()
)




