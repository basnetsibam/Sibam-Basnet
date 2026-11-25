package com.example.ui.match

import androidx.compose.ui.graphics.Color
import com.example.data.GameMode
import com.example.data.ReplayActorSnapshot
import com.example.data.ReplayFrameSnapshot
import com.example.data.SquadMode
import com.example.data.VehicleType
import com.example.data.WeaponId
import com.example.data.WeatherCondition
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.TacticalAmber

enum class LootKind {
    WEAPON,
    MEDKIT,
    INHALER,
    ARMOR
}

data class LootDrop(
    val id: String,
    val x: Float,
    val y: Float,
    val kind: LootKind,
    val weaponId: WeaponId? = null,
    val label: String
)

data class WorldVehicle(
    val id: String,
    val type: VehicleType,
    var x: Float,
    var y: Float,
    var headingRad: Float,
    var hp: Int,
    val maxHp: Int,
    var driverId: String? = null
)

enum class CombatantTeam {
    PLAYER_SQUAD,
    ENEMY
}

data class Combatant(
    val id: String,
    val name: String,
    val team: CombatantTeam,
    var x: Float,
    var y: Float,
    var aimRad: Float,
    var hp: Int,
    val maxHp: Int = 100,
    var shield: Int = 25,
    var weaponId: WeaponId,
    var isKnocked: Boolean = false,
    var knockedTimerSec: Float = 0f,
    var isEliminated: Boolean = false,
    var isTargetDummy: Boolean = false,
    var lastFireMs: Long = 0L,
    var slowTimerSec: Float = 0f,
    var aiTargetX: Float = x,
    var aiTargetY: Float = y,
    var aiRetargetCooldown: Float = 0f,
    var kills: Int = 0,
    val operatorId: String = "vespera",
    var skillCooldownRemaining: Float = 0f,
    val skillCooldownTotal: Float = 15f,
    var skillActiveRemaining: Float = 0f
)

data class Projectile(
    val id: Long,
    val ownerId: String,
    val team: CombatantTeam,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val damage: Int,
    val armorPen: Int,
    var remainingDist: Float,
    val isOvercharged: Boolean = false,
    val isSniper: Boolean = false,
    val weaponLabel: String = ""
)

data class VisualEffect(
    val id: Long,
    val x: Float,
    val y: Float,
    val maxRadius: Float,
    val colorHex: Long,
    val ttlSec: Float,
    val maxTtlSec: Float,
    val label: String? = null
)

enum class KillFeedKind {
    ELIMINATION,
    KNOCKDOWN,
    REVIVE,
    ZONE,
    SYSTEM
}

data class KillFeedEntry(
    val id: Long,
    val text: String,
    val isPlayerHighlight: Boolean,
    val killerName: String? = null,
    val victimName: String? = null,
    val methodLabel: String = "",
    val kind: KillFeedKind = KillFeedKind.SYSTEM,
    val ttlSec: Float = 6.5f,
    val maxTtlSec: Float = 6.5f
)

data class SquadMemberResult(
    val name: String,
    val isLocalPlayer: Boolean,
    val kills: Int,
    val isEliminated: Boolean
)

data class DeathCamBullet(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val isFromKiller: Boolean
)

data class DeathCamFrame(
    val timeOffsetSec: Float,
    val killerX: Float,
    val killerY: Float,
    val killerAimRad: Float,
    val killerHp: Int,
    val killerMaxHp: Int,
    val victimX: Float,
    val victimY: Float,
    val victimHp: Int,
    val victimKnocked: Boolean,
    val safeZoneX: Float,
    val safeZoneY: Float,
    val safeZoneRadius: Float,
    val actors: List<ReplayActorSnapshot> = emptyList(),
    val bullets: List<DeathCamBullet> = emptyList(),
    val impactBanner: String? = null
)

enum class AntiCheatStatus(
    val emoji: String,
    val title: String,
    val badgeText: String,
    val accentColor: Color
) {
    VERIFIED("🛡️", "Infine Sentinel Active", "AC SHIELD • CLEAN", NanoEmerald),
    GUARDED("👁️", "Honor Watch Active", "AC WATCH • REVIEW", TacticalAmber),
    FLAGGED("🚨", "Integrity Alert", "AC ALERT • FLAGGED", CombatCrimson)
}

data class AntiCheatSnapshot(
    val status: AntiCheatStatus = AntiCheatStatus.VERIFIED,
    val honorScore: Int = 100,
    val checksPassed: Int = 1,
    val speedBoundsOk: Boolean = true,
    val fireCadenceOk: Boolean = true,
    val hitValidationOk: Boolean = true,
    val activeScanNote: String = "Speed, Ballistics & Cadence Verified"
)

data class MatchEndSummary(
    val mode: GameMode,
    val squadMode: SquadMode,
    val arenaLevel: Int,
    val won: Boolean,
    val placementRank: Int,
    val kills: Int,
    val revives: Int,
    val damageDealt: Int,
    val rpDelta: Int,
    val xpEarned: Int,
    val goldEarned: Int,
    val gemsEarned: Int,
    val durationSeconds: Int,
    val squadResults: List<SquadMemberResult>,
    val participantNames: List<String> = emptyList(),
    val replayFrames: List<ReplayFrameSnapshot> = emptyList(),
    val lastLocation: String,
    val weaponUsed: WeaponId,
    val abandonedEarly: Boolean = false,
    val eliminatedByName: String? = null,
    val eliminatedByWeapon: String = "",
    val deathCamFrames: List<DeathCamFrame> = emptyList()
)

data class ArenaHudState(
    val mode: GameMode = GameMode.BATTLE_ROYALE_RANKED,
    val squadMode: SquadMode = SquadMode.SQUAD,
    val arenaLevel: Int = 1,
    val playerX: Float = 1200f,
    val playerY: Float = 1200f,
    val playerAimRad: Float = 0f,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val shield: Int = 50,
    val isKnocked: Boolean = false,
    val knockedTimerSec: Float = 0f,
    val primaryWeapon: WeaponId = WeaponId.AK47,
    val secondaryWeapon: WeaponId = WeaponId.MINI_UZI,
    val equippedWeaponSkinId: String = "standard",
    val equippedCharacterSkinId: String = "default_vanguard",
    val equippedEmoteId: String = "tactical_salute",
    val matchElapsedSeconds: Int = 0,
    val activeSlot: Int = 0,
    val magAmmo: Int = 30,
    val maxMagAmmo: Int = 30,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val medkitCount: Int = 2,
    val inhalerCount: Int = 2,
    val isHealing: Boolean = false,
    val healProgress: Float = 0f,
    val operatorId: String = "vespera",
    val skillCooldownRemaining: Float = 0f,
    val skillCooldownTotal: Float = 15f,
    val healAuraRemaining: Float = 0f,
    val drivingVehicle: WorldVehicle? = null,
    val nearbyVehicle: WorldVehicle? = null,
    val nearbyLoot: LootDrop? = null,
    val nearbyRevivalPointName: String? = null,
    val revivalProgress: Float = 0f,
    val canReviveNearby: Boolean = false,
    val currentLocationName: String = "Clock Tower",
    val aliveCount: Int = 16,
    val totalCount: Int = 16,
    val kills: Int = 0,
    val revives: Int = 0,
    val damageDealt: Int = 0,
    val safeZoneX: Float = 1200f,
    val safeZoneY: Float = 1200f,
    val safeZoneRadius: Float = 1180f,
    val nextSafeZoneX: Float = 1200f,
    val nextSafeZoneY: Float = 1200f,
    val nextSafeZoneRadius: Float = 820f,
    val stormPhase: Int = 1,
    val isStormClosing: Boolean = false,
    val stormPhaseSecondsLeft: Int = 12,
    val stormDamagePerSec: Int = 4,
    val stormTickDamageFlashAlpha: Float = 0f,
    val isOutsideZone: Boolean = false,
    // Clash Squad fields
    val csRound: Int = 1,
    val csPlayerTeamWins: Int = 0,
    val csEnemyTeamWins: Int = 0,
    val csClashCash: Int = 1200,
    val isBuyPhase: Boolean = false,
    val buyPhaseSecondsLeft: Int = 0,
    // World render snapshots
    val combatants: List<Combatant> = emptyList(),
    val vehicles: List<WorldVehicle> = emptyList(),
    val lootDrops: List<LootDrop> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val effects: List<VisualEffect> = emptyList(),
    val killFeed: List<KillFeedEntry> = emptyList(),
    val weather: WeatherCondition = WeatherCondition.CLEAR,
    val weatherSecondsLeft: Int = 22,
    val weatherIntensity: Float = 0f,
    val lightningFlashAlpha: Float = 0f,
    val weatherTickPhase: Float = 0f,
    val activeEmoteText: String? = null,
    val activeEmoteColorHex: Long = 0xFF00E5FF,
    val activeEmoteTtl: Float = 0f,
    val hitMarkerTtl: Float = 0f,
    val hitMarkerMaxTtl: Float = 0.25f,
    val hitMarkerWorldX: Float = 0f,
    val hitMarkerWorldY: Float = 0f,
    val hitMarkerDamage: Int = 0,
    val hitMarkerIsKill: Boolean = false,
    val hitMarkerIsCrit: Boolean = false,
    val hitMarkerComboCount: Int = 0,
    val antiCheat: AntiCheatSnapshot = AntiCheatSnapshot(),
    val showFullMapOverlay: Boolean = false,
    val matchSummary: MatchEndSummary? = null
)
