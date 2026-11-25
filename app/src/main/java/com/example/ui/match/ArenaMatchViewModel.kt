package com.example.ui.match

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.audio.SoundSettingsState
import com.example.data.ArenaLevelCatalog
import com.example.data.EmoteCatalog
import com.example.data.EmoteSpec
import com.example.data.GameMode
import com.example.data.InfineMapBlueprint
import com.example.data.MapRect
import com.example.data.OperatorCatalog
import com.example.data.ReplayActorSnapshot
import com.example.data.ReplayFrameSnapshot
import com.example.data.RoboHonorPolicy
import com.example.data.SkillType
import com.example.data.SquadMode
import com.example.data.VehicleCatalog
import com.example.data.VehicleType
import com.example.data.WeaponCatalog
import com.example.data.WeaponId
import com.example.data.WeatherCondition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class ArenaMatchViewModel(application: Application) : AndroidViewModel(application) {

    private val soundManager = SoundManager.getInstance(application)
    val soundSettings: StateFlow<SoundSettingsState> = soundManager.settings

    private val _hudState = MutableStateFlow(ArenaHudState())
    val hudState: StateFlow<ArenaHudState> = _hudState.asStateFlow()

    private var simJob: Job? = null
    private var initialized = false

    // Input controls
    @Volatile var moveInputX: Float = 0f
    @Volatile var moveInputY: Float = 0f
    @Volatile var aimInputX: Float = 0f
    @Volatile var aimInputY: Float = 0f
    @Volatile var isFiring: Boolean = false

    // Mutable simulation state
    private var mode: GameMode = GameMode.BATTLE_ROYALE_RANKED
    private var squadMode: SquadMode = SquadMode.SQUAD
    private var arenaLevel: Int = 1
    private var playerCallsign: String = "APEX-01"
    private var operatorId: String = "vespera"

    private var playerX = 1200f
    private var playerY = 1200f
    private var playerAimRad = 0f
    private var playerHp = 100
    private var playerMaxHp = 100
    private var playerShield = 50
    private var isPlayerKnocked = false
    private var playerKnockedTimer = 0f
    private var soloSelfReviveTokens = 1

    private var primaryWeapon = WeaponId.AK47
    private var secondaryWeapon = WeaponId.MINI_UZI
    private var equippedWeaponSkinId = "standard"
    private var equippedCharacterSkinId = "default_vanguard"
    private var equippedEmoteId = "tactical_salute"
    private var matchElapsedSec = 0f
    private var squadFriendNames: List<String> = emptyList()
    private var activeSlot = 0
    private var magAmmo = 30
    private var shotCounter = 0
    private var lastFireTimeMs = 0L
    private var isReloading = false
    private var reloadElapsedMs = 0L

    private var medkitCount = 2
    private var inhalerCount = 2
    private var isHealing = false
    private var healElapsedSec = 0f

    private var skillCooldownRemaining = 0f
    private var healAuraRemaining = 0f

    private var drivingVehicleId: String? = null
    private var revivalChannelSec = 0f

    private var kills = 0
    private var revives = 0
    private var damageDealt = 0

    private var safeZoneX = 1200f
    private var safeZoneY = 1200f
    private var safeZoneRadius = 1180f
    private var safeZoneTargetX = 1200f
    private var safeZoneTargetY = 1200f
    private var nextSafeZoneX = 1200f
    private var nextSafeZoneY = 1200f
    private var nextSafeZoneRadius = 800f
    private var stormStartX = 1200f
    private var stormStartY = 1200f
    private var stormStartRadius = 1180f
    private var stormPhase = 1
    private var isStormClosing = false
    private var stormStageTimerSec = 10f
    private var stormStageDurationSec = 10f
    private var stormDamagePerSec = 5
    private var stormTickDamageFlashAlpha = 0f
    private var zoneDamageTickSec = 0f

    // Dynamic Weather System
    private var currentWeather = WeatherCondition.CLEAR
    private var weatherRemainingSec = 18f
    private var weatherIntensity = 0f
    private var weatherTickPhase = 0f
    private var lightningFlashAlpha = 0f
    private var nextLightningSec = 6f

    // Expressive Emote Banner & Anti-Cheat Telemetry Monitor
    private var activeEmoteText: String? = null
    private var activeEmoteColorHex = 0xFF00E5FFL
    private var activeEmoteTtl = 0f
    private var playerHonorScore = 100
    private var antiCheatChecksPassed = 1
    private var antiCheatPulseSec = 0f
    private var prevCheckedX = 1200f
    private var prevCheckedY = 1200f
    private var antiCheatSnapshot = AntiCheatSnapshot()

    // Visual Hit Marker Telemetry
    private var hitMarkerTtl = 0f
    private var hitMarkerMaxTtl = 0.25f
    private var hitMarkerWorldX = 0f
    private var hitMarkerWorldY = 0f
    private var hitMarkerDamage = 0
    private var hitMarkerIsKill = false
    private var hitMarkerIsCrit = false
    private var hitMarkerComboCount = 0
    private var hitMarkerComboWindowSec = 0f

    // Clash Squad
    private var csRound = 1
    private var csPlayerWins = 0
    private var csEnemyWins = 0
    private var csClashCash = 1200
    private var isBuyPhase = false
    private var buyPhaseTimerSec = 0f
    private var csArenaCenterX = 1200f
    private var csArenaCenterY = 1200f

    private val combatants = mutableListOf<Combatant>()
    private val vehicles = mutableListOf<WorldVehicle>()
    private val lootDrops = mutableListOf<LootDrop>()
    private val projectiles = mutableListOf<Projectile>()
    private val effects = mutableListOf<VisualEffect>()
    private val killFeed = mutableListOf<KillFeedEntry>()
    private val recordedReplayFrames = mutableListOf<ReplayFrameSnapshot>()
    private var lastReplaySampleSec = -1
    private val deathCamBuffer = ArrayDeque<BufferedDeathSnapshot>(44)
    private var lastDeathCamSampleTime = -1f
    private var lastAttackerId: String? = null
    private var lastAttackerName: String? = null
    private var lastAttackerWeapon: String = ""
    private var nextId = 1L
    private var matchFinished = false

    private data class BufferedDeathSnapshot(
        val elapsedSec: Float,
        val victimX: Float,
        val victimY: Float,
        val victimHp: Int,
        val victimKnocked: Boolean,
        val safeZoneX: Float,
        val safeZoneY: Float,
        val safeZoneRadius: Float,
        val killerStates: Map<String, KillerSnapshot>,
        val actors: List<ReplayActorSnapshot>,
        val bullets: List<DeathCamBullet>
    )

    private data class KillerSnapshot(
        val x: Float,
        val y: Float,
        val aimRad: Float,
        val hp: Int,
        val maxHp: Int
    )

    private var bonusStartMedkits = 0
    private var bonusStartShield = 0

    fun startMatchIfNeeded(
        gameMode: GameMode,
        squad: SquadMode,
        level: Int,
        equippedWeapon: WeaponId,
        weaponSkinId: String,
        characterSkinId: String = "default_vanguard",
        emoteId: String = "tactical_salute",
        squadFriends: List<String>,
        equippedOperatorId: String,
        callsign: String,
        bonusMedkits: Int = 0,
        bonusShield: Int = 0,
        honorScore: Int = 100
    ) {
        if (initialized) return
        initialized = true
        this.mode = gameMode
        this.squadMode = squad
        this.arenaLevel = level.coerceAtLeast(1)
        this.primaryWeapon = equippedWeapon
        this.secondaryWeapon = if (equippedWeapon == WeaponId.MINI_UZI) WeaponId.AK47 else WeaponId.MINI_UZI
        this.equippedWeaponSkinId = weaponSkinId
        this.equippedCharacterSkinId = characterSkinId
        this.equippedEmoteId = emoteId
        this.squadFriendNames = squadFriends
        this.operatorId = equippedOperatorId
        this.playerCallsign = callsign
        this.bonusStartMedkits = if (gameMode.isRanked) bonusMedkits.coerceAtLeast(0) else 0
        this.bonusStartShield = if (gameMode.isRanked) bonusShield.coerceAtLeast(0) else 0
        this.playerHonorScore = honorScore.coerceIn(0, RoboHonorPolicy.MAX_HONOR)

        setupWorld()
        startSimulationLoop()
    }

    private fun setupWorld() {
        combatants.clear()
        vehicles.clear()
        lootDrops.clear()
        projectiles.clear()
        effects.clear()
        killFeed.clear()
        recordedReplayFrames.clear()
        lastReplaySampleSec = -1
        deathCamBuffer.clear()
        lastDeathCamSampleTime = -1f
        lastAttackerId = null
        lastAttackerName = null
        lastAttackerWeapon = ""
        activeEmoteText = null
        activeEmoteTtl = 0f
        antiCheatChecksPassed = 1
        antiCheatPulseSec = 0f
        prevCheckedX = 1200f
        prevCheckedY = 1200f
        antiCheatSnapshot = computeAntiCheatSnapshot(speedOk = true, cadenceOk = true, hitOk = true)
        hitMarkerTtl = 0f
        hitMarkerComboCount = 0
        hitMarkerComboWindowSec = 0f
        matchFinished = false

        kills = 0
        revives = 0
        damageDealt = 0
        matchElapsedSec = 0f
        isPlayerKnocked = false
        playerKnockedTimer = 0f
        playerHp = 100
        playerMaxHp = 100
        playerShield = (50 + bonusStartShield).coerceAtMost(75)
        medkitCount = 2 + bonusStartMedkits
        inhalerCount = 2
        skillCooldownRemaining = 0f
        healAuraRemaining = 0f
        drivingVehicleId = null
        revivalChannelSec = 0f
        activeSlot = 0
        val wSpec = WeaponCatalog.byId(primaryWeapon)
        magAmmo = wSpec.magSize
        isReloading = false
        isHealing = false

        // Initialize dynamic weather cycle (Clear skies briefly at drop, then shifts through Fog, Rain, Storm)
        currentWeather = WeatherCondition.CLEAR
        weatherRemainingSec = if (mode == GameMode.TRAINING_GROUND) 26f else 18f
        weatherIntensity = 0f
        weatherTickPhase = 0f
        lightningFlashAlpha = 0f
        nextLightningSec = 6f

        // Spawn vehicles across Infine (all 4 types!)
        InfineMapBlueprint.vehicleSpawns.forEachIndexed { idx, spawn ->
            val spec = VehicleCatalog.byType(spawn.type)
            vehicles += WorldVehicle(
                id = "veh_$idx",
                type = spawn.type,
                x = spawn.x,
                y = spawn.y,
                headingRad = spawn.headingRad,
                hp = spec.maxHp,
                maxHp = spec.maxHp
            )
        }

        when (mode) {
            GameMode.BATTLE_ROYALE_RANKED, GameMode.BATTLE_ROYALE_CASUAL -> setupBattleRoyale()
            GameMode.CLASH_SQUAD_RANKED -> setupClashSquad()
            GameMode.TRAINING_GROUND -> setupTrainingGround()
        }
        captureReplayFrame(forceEvent = "Deployed to ${InfineMapBlueprint.nearestLocationName(playerX, playerY)}")
        publishState()
    }

    private fun setupBattleRoyale() {
        val lvlSpec = ArenaLevelCatalog.byLevel(arenaLevel)
        val startLoc = InfineMapBlueprint.locations.firstOrNull { it.id == lvlSpec.sectorId }
            ?: InfineMapBlueprint.locations.random()
        playerX = (startLoc.x + 85f).coerceIn(180f, 2220f)
        playerY = (startLoc.y + 85f).coerceIn(180f, 2220f)
        safeZoneX = 1200f
        safeZoneY = 1200f
        safeZoneRadius = 1180f
        stormPhase = 1
        isStormClosing = false
        stormStageDurationSec = 10f
        stormStageTimerSec = 10f
        stormDamagePerSec = 5
        stormTickDamageFlashAlpha = 0f
        zoneDamageTickSec = 0f
        pickNextSafeZone(targetRadius = 800f)
        soloSelfReviveTokens = if (squadMode == SquadMode.SOLO) 1 else 0

        if (squadMode == SquadMode.SQUAD) {
            val defaultAllies = listOf("Kira_INF", "AuraMend", "ZedRush")
            val squadOps = listOf("kairo", "vespera", "blaze")
            val allyNames = List(3) { idx -> squadFriendNames.getOrNull(idx) ?: defaultAllies[idx] }
            allyNames.forEachIndexed { i, name ->
                val op = OperatorCatalog.byId(squadOps[i % squadOps.size])
                combatants += Combatant(
                    id = "ally_$i",
                    name = name,
                    team = CombatantTeam.PLAYER_SQUAD,
                    x = playerX + ((i - 1) * 48f),
                    y = playerY + 55f,
                    aimRad = 0f,
                    hp = 100,
                    shield = 40,
                    weaponId = listOf(WeaponId.MP5, WeaponId.AK47, WeaponId.UMP)[i],
                    operatorId = op.id,
                    skillCooldownRemaining = if (i == 2) 4f else 0f,
                    skillCooldownTotal = op.cooldownSec.toFloat()
                )
            }
        }

        val enemyNames = listOf(
            "VortexX", "ShadowAK", "NovaSniper", "Reaper77", "DriftKing",
            "TitanMP5", "PyroM7", "Falcon_CS", "PulseCG", "BlazeOps",
            "RogueUzi", "IronWood", "ViperINF", "GhostM7", "ApexBot", "StormCG"
        )
        enemyNames.forEachIndexed { idx, name ->
            val poi = InfineMapBlueprint.locations[idx % InfineMapBlueprint.locations.size]
            val sx = (poi.x + Random.nextFloat() * 240f - 120f).coerceIn(160f, 2240f)
            val sy = (poi.y + Random.nextFloat() * 240f - 120f).coerceIn(160f, 2240f)
            // Keep initial spawn away from immediate player point-blank
            val finalX = if (hypot(sx - playerX, sy - playerY) < 320f) (sx + 420f).coerceAtMost(2200f) else sx
            combatants += Combatant(
                id = "enemy_$idx",
                name = name,
                team = CombatantTeam.ENEMY,
                x = finalX,
                y = sy,
                aimRad = Random.nextFloat() * 6.28f,
                hp = lvlSpec.botHp,
                maxHp = lvlSpec.botHp,
                shield = lvlSpec.botShield,
                weaponId = WeaponCatalog.all[idx % WeaponCatalog.all.size].id
            )
        }

        spawnBrLoot()
        pushKillFeed("LVL $arenaLevel • Dropped at ${startLoc.name}!", true)
    }

    private fun spawnBrLoot() {
        var idx = 0
        InfineMapBlueprint.locations.forEach { poi ->
            // Every POI spawns guns & medical supplies
            val weaponA = WeaponCatalog.all[idx % WeaponCatalog.all.size]
            val weaponB = WeaponCatalog.all[(idx + 3) % WeaponCatalog.all.size]
            lootDrops += LootDrop(
                id = "loot_${idx++}",
                x = poi.x - 55f,
                y = poi.y + 42f,
                kind = LootKind.WEAPON,
                weaponId = weaponA.id,
                label = weaponA.name
            )
            lootDrops += LootDrop(
                id = "loot_${idx++}",
                x = poi.x + 65f,
                y = poi.y - 38f,
                kind = LootKind.WEAPON,
                weaponId = weaponB.id,
                label = weaponB.name
            )
            lootDrops += LootDrop(
                id = "loot_${idx++}",
                x = poi.x + 15f,
                y = poi.y + 72f,
                kind = if (poi.id == "hospital" || idx % 2 == 0) LootKind.MEDKIT else LootKind.INHALER,
                label = if (poi.id == "hospital" || idx % 2 == 0) "Medkit" else "Inhaler"
            )
            if (idx % 3 == 0) {
                lootDrops += LootDrop(
                    id = "loot_${idx++}",
                    x = poi.x - 75f,
                    y = poi.y - 50f,
                    kind = LootKind.ARMOR,
                    label = "Tactical Vest"
                )
            }
        }
    }

    private fun setupClashSquad() {
        csRound = 1
        csPlayerWins = 0
        csEnemyWins = 0
        csClashCash = 1300
        startClashSquadRound(withBuyPhase = true)
    }

    private fun startClashSquadRound(withBuyPhase: Boolean) {
        combatants.clear()
        projectiles.clear()
        lootDrops.clear()
        drivingVehicleId = null
        isPlayerKnocked = false
        playerHp = 100
        playerShield = 50
        medkitCount = 2
        inhalerCount = 2
        val wSpec = WeaponCatalog.byId(currentWeaponId())
        magAmmo = wSpec.magSize
        isReloading = false
        isHealing = false

        val csZones = listOf("clock_tower", "arena", "factory", "school", "hospital", "shop", "association")
        val zoneId = csZones[(csRound - 1) % csZones.size]
        val zone = InfineMapBlueprint.locations.first { it.id == zoneId }
        csArenaCenterX = zone.x
        csArenaCenterY = zone.y
        safeZoneX = zone.x
        safeZoneY = zone.y
        safeZoneRadius = 380f
        stormPhase = 1
        isStormClosing = false
        stormStageDurationSec = 14f
        stormStageTimerSec = 14f
        stormDamagePerSec = 7
        stormTickDamageFlashAlpha = 0f
        zoneDamageTickSec = 0f
        nextSafeZoneX = zone.x
        nextSafeZoneY = zone.y
        nextSafeZoneRadius = 210f
        safeZoneTargetX = zone.x
        safeZoneTargetY = zone.y

        playerX = (zone.x - 210f).coerceIn(140f, 2260f)
        playerY = zone.y

        val defaultCsAllies = listOf("Kira_INF", "AuraMend", "ZedRush")
        val squadOps = listOf("kairo", "vespera", "blaze")
        val csAllies = List(3) { idx -> squadFriendNames.getOrNull(idx) ?: defaultCsAllies[idx] }
        csAllies.forEachIndexed { idx, name ->
            val op = OperatorCatalog.byId(squadOps[idx % squadOps.size])
            combatants += Combatant(
                id = "cs_ally_$idx",
                name = name,
                team = CombatantTeam.PLAYER_SQUAD,
                x = playerX + 35f,
                y = playerY + (idx - 1) * 55f,
                aimRad = 0f,
                hp = 100,
                shield = 50,
                weaponId = listOf(WeaponId.MP5, WeaponId.AK47, WeaponId.UMP)[idx],
                operatorId = op.id,
                skillCooldownRemaining = if (idx == 2) 3f else 0f,
                skillCooldownTotal = op.cooldownSec.toFloat()
            )
        }

        val lvlSpec = ArenaLevelCatalog.byLevel(arenaLevel)
        listOf("VortexX", "ShadowAK", "PulseCG", "Reaper77").forEachIndexed { idx, name ->
            combatants += Combatant(
                id = "cs_enemy_$idx",
                name = name,
                team = CombatantTeam.ENEMY,
                x = (zone.x + 210f).coerceIn(140f, 2260f),
                y = zone.y + (idx - 1.5f) * 55f,
                aimRad = 3.14f,
                hp = lvlSpec.botHp,
                maxHp = lvlSpec.botHp,
                shield = lvlSpec.botShield,
                weaponId = WeaponCatalog.all[(csRound + idx) % WeaponCatalog.all.size].id
            )
        }

        isBuyPhase = withBuyPhase
        buyPhaseTimerSec = if (withBuyPhase) 8f else 0f
        pushKillFeed("LVL $arenaLevel • CS Round $csRound @ ${zone.name}", true)
    }

    private fun setupTrainingGround() {
        playerX = 1200f
        playerY = 1320f
        safeZoneX = 1200f
        safeZoneY = 1200f
        safeZoneRadius = 1500f
        medkitCount = 9
        inhalerCount = 9

        // Place all 8 guns in a weapon rack row right below Clock Tower
        WeaponCatalog.all.forEachIndexed { i, spec ->
            lootDrops += LootDrop(
                id = "train_gun_$i",
                x = 980f + i * 64f,
                y = 1365f,
                kind = LootKind.WEAPON,
                weaponId = spec.id,
                label = spec.name
            )
        }
        // Place healing pickups
        lootDrops += LootDrop("train_med", 1140f, 1280f, LootKind.MEDKIT, label = "Medkit")
        lootDrops += LootDrop("train_inh", 1260f, 1280f, LootKind.INHALER, label = "Inhaler")

        // Place all 4 vehicles right beside Clock Tower plaza for instant test driving
        vehicles.clear()
        VehicleType.entries.forEachIndexed { i, vType ->
            val spec = VehicleCatalog.byType(vType)
            vehicles += WorldVehicle(
                id = "train_veh_$i",
                type = vType,
                x = 980f + i * 145f,
                y = 1510f,
                headingRad = 0f,
                hp = spec.maxHp,
                maxHp = spec.maxHp
            )
        }

        // Spawn target dummies + moving strafe bots + 1 knocked friendly dummy to test Revival Point!
        for (i in 0..3) {
            combatants += Combatant(
                id = "dummy_$i",
                name = "Target ${i + 1}",
                team = CombatantTeam.ENEMY,
                x = 1060f + i * 95f,
                y = 950f,
                aimRad = 1.57f,
                hp = 120,
                maxHp = 120,
                shield = 0,
                weaponId = WeaponId.MP5,
                isTargetDummy = true
            )
        }
        combatants += Combatant(
            id = "train_ally_knocked",
            name = "Training Ally",
            team = CombatantTeam.PLAYER_SQUAD,
            x = 1200f,
            y = 1090f,
            aimRad = 0f,
            hp = 0,
            shield = 0,
            weaponId = WeaponId.AK47,
            isKnocked = true,
            knockedTimerSec = 999f,
            operatorId = "kairo",
            skillCooldownRemaining = 6f,
            skillCooldownTotal = 20f
        )
        pushKillFeed("Training Ground Ready • Test guns, vehicles & Revival Point", true)
    }

    private fun startSimulationLoop() {
        simJob?.cancel()
        simJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                val dt = ((now - lastTime).coerceIn(10L, 50L)) / 1000f
                lastTime = now
                if (!matchFinished) {
                    stepSimulation(dt, now)
                }
                publishState()
                delay(16L)
            }
        }
    }

    private fun stepSimulation(dt: Float, nowMs: Long) {
        if (mode == GameMode.CLASH_SQUAD_RANKED && isBuyPhase) {
            buyPhaseTimerSec -= dt
            if (buyPhaseTimerSec <= 0f) {
                isBuyPhase = false
            }
            return
        }

        matchElapsedSec += dt
        stepWeather(dt)

        // Cooldowns & timers
        if (skillCooldownRemaining > 0f) {
            skillCooldownRemaining = (skillCooldownRemaining - dt).coerceAtLeast(0f)
        }
        if (healAuraRemaining > 0f) {
            healAuraRemaining = (healAuraRemaining - dt).coerceAtLeast(0f)
            // Heal player & nearby squadmates continuously
            if (!isPlayerKnocked) {
                playerHp = (playerHp + (13f * dt).toInt().coerceAtLeast(1)).coerceAtMost(playerMaxHp)
            }
            combatants.filter { it.team == CombatantTeam.PLAYER_SQUAD && !it.isEliminated && !it.isKnocked }
                .forEach { ally ->
                    if (hypot(ally.x - playerX, ally.y - playerY) < 220f) {
                        ally.hp = (ally.hp + 1).coerceAtMost(ally.maxHp)
                    }
                }
        }

        if (isReloading) {
            reloadElapsedMs += (dt * 1000f).toLong()
            val spec = WeaponCatalog.byId(currentWeaponId())
            if (reloadElapsedMs >= spec.reloadMs) {
                magAmmo = spec.magSize
                isReloading = false
                reloadElapsedMs = 0L
            }
        }

        if (isHealing) {
            healElapsedSec += dt
            if (healElapsedSec >= 1.6f) {
                playerHp = (playerHp + 75).coerceAtMost(playerMaxHp)
                medkitCount = (medkitCount - 1).coerceAtLeast(0)
                isHealing = false
                healElapsedSec = 0f
                soundManager.playMedkit()
                spawnEffect(playerX, playerY, 85f, 0xFF00E676, 0.5f, "+75 HP")
            }
        }

        // Handle Knocked state
        if (isPlayerKnocked) {
            playerKnockedTimer -= dt
            if (playerKnockedTimer <= 0f) {
                finishMatch(won = false)
                return
            }
        }

        // Player Movement / Driving
        val activeVehicle = vehicles.firstOrNull { it.id == drivingVehicleId }
        val moveMag = hypot(moveInputX, moveInputY)
        if (moveMag > 0.05f) {
            val nx = moveInputX / moveMag
            val ny = moveInputY / moveMag
            val speed = when {
                isPlayerKnocked -> 65f
                activeVehicle != null -> VehicleCatalog.byType(activeVehicle.type).maxSpeed * currentWeather.speedMultiplier
                isHealing -> 95f * currentWeather.speedMultiplier
                else -> 195f * currentWeather.speedMultiplier
            }
            val nextX = (playerX + nx * speed * dt).coerceIn(60f, InfineMapBlueprint.WORLD_SIZE - 60f)
            val nextY = (playerY + ny * speed * dt).coerceIn(60f, InfineMapBlueprint.WORLD_SIZE - 60f)
            val radius = if (activeVehicle != null) 26f else 16f
            val resolved = resolveObstacleCollision(playerX, playerY, nextX, nextY, radius)
            playerX = resolved.first
            playerY = resolved.second

            if (activeVehicle != null) {
                activeVehicle.x = playerX
                activeVehicle.y = playerY
                activeVehicle.headingRad = atan2(ny, nx)
                playerAimRad = activeVehicle.headingRad
                // Ramming enemies with vehicle!
                val vehSpec = VehicleCatalog.byType(activeVehicle.type)
                val ramDmg = vehSpec.ramDamage
                combatants.filter { it.team == CombatantTeam.ENEMY && !it.isEliminated && !it.isKnocked }
                    .forEach { enemy ->
                        if (hypot(enemy.x - playerX, enemy.y - playerY) < 40f) {
                            applyDamageToCombatant(
                                target = enemy,
                                rawDamage = ramDmg,
                                isFromPlayer = true,
                                weaponLabel = vehSpec.name
                            )
                            enemy.x = (enemy.x + nx * 42f).coerceIn(80f, 2320f)
                            enemy.y = (enemy.y + ny * 42f).coerceAtMost(2320f)
                        }
                    }
            } else if (hypot(aimInputX, aimInputY) < 0.1f) {
                playerAimRad = atan2(ny, nx)
            }
            if (activeVehicle == null && !isPlayerKnocked) {
                soundManager.playFootstep(currentWeather)
            }
        }
        soundManager.updateVehicleEngine(activeVehicle?.type, isMoving = moveMag > 0.05f)

        // Aim direction (reduced auto-aim visibility range in Fog/Storm)
        if (hypot(aimInputX, aimInputY) > 0.1f) {
            playerAimRad = atan2(aimInputY, aimInputX)
        } else if (isFiring && activeVehicle == null) {
            // Subtle tactical auto-aim assist toward closest visible enemy in range when pressing fire button
            val spec = WeaponCatalog.byId(currentWeaponId())
            val weatherAimScale = if (currentWeather == WeatherCondition.FOG) 0.72f else if (currentWeather == WeatherCondition.STORM) 0.82f else 1.0f
            val effectiveAimRange = spec.range * 1.1f * weatherAimScale
            val nearestEnemy = combatants
                .filter { it.team == CombatantTeam.ENEMY && !it.isEliminated }
                .minByOrNull { hypot(it.x - playerX, it.y - playerY) }
            if (nearestEnemy != null && hypot(nearestEnemy.x - playerX, nearestEnemy.y - playerY) <= effectiveAimRange) {
                playerAimRad = atan2(nearestEnemy.y - playerY, nearestEnemy.x - playerX)
            }
        }

        // Player Firing
        if (isFiring && !isPlayerKnocked && !isReloading && activeVehicle == null) {
            tryFirePlayerWeapon(nowMs)
        }

        // Revival Point channeling
        val nearestRp = InfineMapBlueprint.revivalPoints.minByOrNull { hypot(it.x - playerX, it.y - playerY) }
        val inRevivalPoint = nearestRp != null && hypot(nearestRp.x - playerX, nearestRp.y - playerY) < 88f
        val hasFallenOrKnockedAlly = combatants.any {
            it.team == CombatantTeam.PLAYER_SQUAD && (it.isKnocked || it.isEliminated)
        }
        val knockedAllyNearby = combatants.firstOrNull {
            it.team == CombatantTeam.PLAYER_SQUAD && it.isKnocked && hypot(it.x - playerX, it.y - playerY) < 88f
        }

        if (!isPlayerKnocked && ((inRevivalPoint && hasFallenOrKnockedAlly) || knockedAllyNearby != null || (inRevivalPoint && isPlayerKnocked))) {
            revivalChannelSec += dt
            if (revivalChannelSec >= 2.8f) {
                revivalChannelSec = 0f
                reviveAlliesOrSelf(fromRevivalPoint = inRevivalPoint)
            }
        } else if (isPlayerKnocked && inRevivalPoint) {
            revivalChannelSec += dt
            if (revivalChannelSec >= 2.5f) {
                revivalChannelSec = 0f
                revivePlayer(70)
            }
        } else {
            revivalChannelSec = (revivalChannelSec - dt * 1.5f).coerceAtLeast(0f)
        }

        // Shrinking Storm Circle & Safe Zone DOT (BR & Clash Squad)
        if (stormTickDamageFlashAlpha > 0f) {
            stormTickDamageFlashAlpha = (stormTickDamageFlashAlpha - dt * 1.15f).coerceAtLeast(0f)
        }
        if (mode == GameMode.BATTLE_ROYALE_RANKED || mode == GameMode.BATTLE_ROYALE_CASUAL || mode == GameMode.CLASH_SQUAD_RANKED) {
            stepStormCircle(dt)
        }

        // Update AI Combatants
        stepCombatants(dt, nowMs)

        // Update Projectiles
        stepProjectiles(dt)

        // Update Visual Effects
        val effIter = effects.iterator()
        while (effIter.hasNext()) {
            val eff = effIter.next()
            val updated = eff.copy(ttlSec = eff.ttlSec - dt)
            if (updated.ttlSec <= 0f) {
                effIter.remove()
            } else {
                effects[effects.indexOf(eff)] = updated
            }
        }

        // Decay Real-Time Kill Feed Entries
        val kfIter = killFeed.listIterator()
        while (kfIter.hasNext()) {
            val entry = kfIter.next()
            val remaining = entry.ttlSec - dt
            if (remaining <= 0f) {
                kfIter.remove()
            } else {
                kfIter.set(entry.copy(ttlSec = remaining))
            }
        }

        // Sample replay telemetry once per second (capped to keep DB compact)
        val currentSec = matchElapsedSec.toInt()
        if (currentSec > lastReplaySampleSec && recordedReplayFrames.size < 220) {
            captureReplayFrame()
        }

        // High-resolution rolling Death Cam telemetry buffer (every 0.15s, last ~6.0s)
        if (matchElapsedSec - lastDeathCamSampleTime >= 0.15f) {
            lastDeathCamSampleTime = matchElapsedSec
            captureDeathCamSample()
        }

        if (activeEmoteTtl > 0f) {
            activeEmoteTtl = (activeEmoteTtl - dt).coerceAtLeast(0f)
            if (activeEmoteTtl <= 0f) {
                activeEmoteText = null
            }
        }

        if (hitMarkerTtl > 0f) {
            hitMarkerTtl = (hitMarkerTtl - dt).coerceAtLeast(0f)
        }
        if (hitMarkerComboWindowSec > 0f) {
            hitMarkerComboWindowSec = (hitMarkerComboWindowSec - dt).coerceAtLeast(0f)
            if (hitMarkerComboWindowSec <= 0f) {
                hitMarkerComboCount = 0
            }
        }

        stepAntiCheatMonitor(dt)

        // Check match end conditions
        checkMatchWinOrLoss()
    }

    private fun tryFirePlayerWeapon(nowMs: Long) {
        val spec = WeaponCatalog.byId(currentWeaponId())
        if (nowMs - lastFireTimeMs < spec.fireRateMs) return
        if (magAmmo <= 0) {
            startReload()
            return
        }
        lastFireTimeMs = nowMs
        if (mode != GameMode.TRAINING_GROUND) {
            magAmmo--
        }
        if (isHealing) {
            isHealing = false
            healElapsedSec = 0f
        }
        shotCounter++
        soundManager.playWeaponFire(spec.id)
        val isOvercharged = spec.id == WeaponId.CG15 && shotCounter % 5 == 0
        val dmg = if (isOvercharged) (spec.damage * 1.9f).toInt() else spec.damage
        val angle = playerAimRad + (Random.nextFloat() - 0.5f) * spec.spreadRad * currentWeather.spreadMultiplier
        val muzzleX = playerX + cos(playerAimRad) * 22f
        val muzzleY = playerY + sin(playerAimRad) * 22f
        projectiles += Projectile(
            id = nextId++,
            ownerId = "player",
            team = CombatantTeam.PLAYER_SQUAD,
            x = muzzleX,
            y = muzzleY,
            vx = cos(angle) * spec.bulletSpeed,
            vy = sin(angle) * spec.bulletSpeed,
            damage = dmg,
            armorPen = spec.armorPen,
            remainingDist = spec.range,
            isOvercharged = isOvercharged,
            isSniper = spec.id == WeaponId.SNIPER || spec.id == WeaponId.WOODPECKER,
            weaponLabel = if (isOvercharged) "${spec.name}+" else spec.name
        )
        if (magAmmo == 0 && mode != GameMode.TRAINING_GROUND) {
            startReload()
        }
    }

    private fun stepCombatants(dt: Float, nowMs: Long) {
        for (bot in combatants) {
            if (bot.isEliminated) continue
            if (bot.slowTimerSec > 0f) {
                bot.slowTimerSec = (bot.slowTimerSec - dt).coerceAtLeast(0f)
            }
            if (bot.team == CombatantTeam.PLAYER_SQUAD) {
                if (bot.skillCooldownRemaining > 0f) {
                    bot.skillCooldownRemaining = (bot.skillCooldownRemaining - dt).coerceAtLeast(0f)
                }
                if (bot.skillActiveRemaining > 0f) {
                    bot.skillActiveRemaining = (bot.skillActiveRemaining - dt).coerceAtLeast(0f)
                }
            }
            if (bot.isKnocked) {
                if (!bot.isTargetDummy && bot.knockedTimerSec < 500f) {
                    bot.knockedTimerSec -= dt
                    if (bot.knockedTimerSec <= 0f) {
                        bot.isEliminated = true
                        pushStructuredKillFeed(
                            text = "${bot.name} bled out",
                            killerName = null,
                            victimName = bot.name,
                            methodLabel = "Bleedout",
                            kind = KillFeedKind.ELIMINATION,
                            highlight = false
                        )
                    }
                }
                continue
            }
            if (bot.isTargetDummy) {
                // Training dummies oscillate gently side-to-side
                if (bot.id == "dummy_2" || bot.id == "dummy_3") {
                    bot.x += sin(nowMs / 700.0).toFloat() * 55f * dt
                }
                continue
            }

            // Pick nearest hostile
            val hostileTargets = mutableListOf<Triple<String, Float, Float>>()
            if (bot.team == CombatantTeam.ENEMY) {
                if (!isPlayerKnocked) {
                    hostileTargets += Triple("player", playerX, playerY)
                }
                combatants.filter { it.team == CombatantTeam.PLAYER_SQUAD && !it.isEliminated && !it.isKnocked }
                    .forEach { hostileTargets += Triple(it.id, it.x, it.y) }
            } else {
                combatants.filter { it.team == CombatantTeam.ENEMY && !it.isEliminated && !it.isKnocked }
                    .forEach { hostileTargets += Triple(it.id, it.x, it.y) }
            }

            val nearest = hostileTargets.minByOrNull { hypot(it.second - bot.x, it.third - bot.y) }
            val distToTarget = nearest?.let { hypot(it.second - bot.x, it.third - bot.y) } ?: 9999f
            val spec = WeaponCatalog.byId(bot.weaponId)

            val lvlSpec = ArenaLevelCatalog.byLevel(arenaLevel)
            if (nearest != null && distToTarget < currentWeather.botSightRange) {
                bot.aimRad = atan2(nearest.third - bot.y, nearest.second - bot.x)
                val moveDir = if (distToTarget > spec.range * 0.7f) 1f else -0.3f
                val baseSpeed = (if (bot.team == CombatantTeam.ENEMY) 135f * lvlSpec.botSpeedScale else 135f) * currentWeather.speedMultiplier
                val spd = (if (bot.slowTimerSec > 0f) 65f else baseSpeed) * moveDir
                val nx = bot.x + cos(bot.aimRad) * spd * dt
                val ny = bot.y + sin(bot.aimRad) * spd * dt
                val resolved = resolveObstacleCollision(bot.x, bot.y, nx, ny, 16f)
                bot.x = resolved.first.coerceIn(70f, 2330f)
                bot.y = resolved.second.coerceIn(70f, 2330f)

                if (distToTarget <= spec.range && nowMs - bot.lastFireMs > (spec.fireRateMs * 2.2f).toLong()) {
                    bot.lastFireMs = nowMs
                    val spread = (Random.nextFloat() - 0.5f) * 0.22f * currentWeather.spreadMultiplier
                    val ang = bot.aimRad + spread
                    val dmgScale = if (bot.team == CombatantTeam.ENEMY) lvlSpec.botDamageScale else 0.5f
                    projectiles += Projectile(
                        id = nextId++,
                        ownerId = bot.id,
                        team = bot.team,
                        x = bot.x + cos(ang) * 20f,
                        y = bot.y + sin(ang) * 20f,
                        vx = cos(ang) * (spec.bulletSpeed * 0.78f),
                        vy = sin(ang) * (spec.bulletSpeed * 0.78f),
                        damage = (spec.damage * dmgScale).toInt().coerceAtLeast(8),
                        armorPen = spec.armorPen,
                        remainingDist = spec.range * 0.9f,
                        weaponLabel = spec.name
                    )
                }
            } else {
                // Roam toward safe zone or ally
                bot.aiRetargetCooldown -= dt
                if (bot.aiRetargetCooldown <= 0f) {
                    bot.aiRetargetCooldown = 2.5f + Random.nextFloat() * 2f
                    val distFromCenter = hypot(bot.x - safeZoneX, bot.y - safeZoneY)
                    if (bot.team == CombatantTeam.PLAYER_SQUAD && distFromCenter <= safeZoneRadius * 0.92f) {
                        bot.aiTargetX = (playerX + Random.nextFloat() * 160f - 80f).coerceIn(120f, 2280f)
                        bot.aiTargetY = (playerY + Random.nextFloat() * 160f - 80f).coerceIn(120f, 2280f)
                    } else {
                        val pullRadius = (nextSafeZoneRadius * 0.72f).coerceAtLeast(45f)
                        bot.aiTargetX = (nextSafeZoneX + (Random.nextFloat() * 2f - 1f) * pullRadius).coerceIn(140f, 2260f)
                        bot.aiTargetY = (nextSafeZoneY + (Random.nextFloat() * 2f - 1f) * pullRadius).coerceIn(140f, 2260f)
                    }
                }
                val dx = bot.aiTargetX - bot.x
                val dy = bot.aiTargetY - bot.y
                val d = hypot(dx, dy)
                if (d > 24f) {
                    bot.aimRad = atan2(dy, dx)
                    val spd = if (bot.slowTimerSec > 0f) 60f else 120f * lvlSpec.botSpeedScale
                    val resolved = resolveObstacleCollision(
                        bot.x,
                        bot.y,
                        bot.x + (dx / d) * spd * dt,
                        bot.y + (dy / d) * spd * dt,
                        16f
                    )
                    bot.x = resolved.first.coerceIn(70f, 2330f)
                    bot.y = resolved.second.coerceIn(70f, 2330f)
                }
            }

            if (bot.team == CombatantTeam.PLAYER_SQUAD && !bot.isKnocked && bot.skillCooldownRemaining <= 0f) {
                tryTriggerSquadmateSkill(bot)
            }
        }
    }

    private fun tryTriggerSquadmateSkill(bot: Combatant) {
        val op = OperatorCatalog.byId(bot.operatorId)
        when (op.skillType) {
            SkillType.HEAL -> {
                val playerNeedsHeal = !isPlayerKnocked && playerHp < 80 && hypot(playerX - bot.x, playerY - bot.y) <= 260f
                val selfOrAllyNeedsHeal = bot.hp < 75 || combatants.any {
                    it.team == CombatantTeam.PLAYER_SQUAD && !it.isEliminated && !it.isKnocked && it.hp < 75 && hypot(it.x - bot.x, it.y - bot.y) <= 260f
                }
                if (playerNeedsHeal || selfOrAllyNeedsHeal) {
                    bot.hp = (bot.hp + 30).coerceAtMost(bot.maxHp)
                    if (!isPlayerKnocked && hypot(playerX - bot.x, playerY - bot.y) <= 260f) {
                        playerHp = (playerHp + 25).coerceAtMost(playerMaxHp)
                    }
                    combatants.filter {
                        it.team == CombatantTeam.PLAYER_SQUAD && !it.isEliminated && !it.isKnocked && hypot(it.x - bot.x, it.y - bot.y) <= 260f
                    }.forEach { ally ->
                        ally.hp = (ally.hp + 25).coerceAtMost(ally.maxHp)
                    }
                    bot.skillActiveRemaining = 3.5f
                    bot.skillCooldownRemaining = op.cooldownSec.toFloat()
                    spawnEffect(bot.x, bot.y, 175f, 0xFF00E676, 0.8f, "${bot.name}: MEND")
                }
            }
            SkillType.REVIVE -> {
                if (isPlayerKnocked && hypot(playerX - bot.x, playerY - bot.y) <= 420f) {
                    revivePlayer(65)
                    bot.skillActiveRemaining = 2.5f
                    bot.skillCooldownRemaining = op.cooldownSec.toFloat()
                } else {
                    val downedAlly = combatants.firstOrNull {
                        it.id != bot.id && it.team == CombatantTeam.PLAYER_SQUAD && it.isKnocked && hypot(it.x - bot.x, it.y - bot.y) <= 360f
                    }
                    if (downedAlly != null) {
                        downedAlly.isKnocked = false
                        downedAlly.hp = 75
                        bot.skillActiveRemaining = 2.5f
                        bot.skillCooldownRemaining = op.cooldownSec.toFloat()
                        spawnEffect(downedAlly.x, downedAlly.y, 110f, 0xFF00E5FF, 0.8f, "SQUAD REVIVE")
                        pushStructuredKillFeed(
                            text = "${bot.name} revived ${downedAlly.name}!",
                            killerName = bot.name,
                            victimName = downedAlly.name,
                            methodLabel = op.skillName,
                            kind = KillFeedKind.REVIVE,
                            highlight = true
                        )
                    }
                }
            }
            SkillType.DAMAGE -> {
                val closeEnemies = combatants.filter {
                    it.team == CombatantTeam.ENEMY && !it.isEliminated && !it.isTargetDummy && hypot(it.x - bot.x, it.y - bot.y) <= 200f
                }
                if (closeEnemies.isNotEmpty()) {
                    closeEnemies.forEach { enemy ->
                        enemy.slowTimerSec = 2.5f
                        applyDamageToCombatant(
                            target = enemy,
                            rawDamage = 42,
                            isFromPlayer = false,
                            attackerId = bot.id,
                            weaponLabel = op.skillName
                        )
                    }
                    bot.skillActiveRemaining = 2.0f
                    bot.skillCooldownRemaining = op.cooldownSec.toFloat()
                    spawnEffect(bot.x, bot.y, 185f, 0xFFFF3D57, 0.7f, "${bot.name}: ARC")
                }
            }
        }
    }

    private fun stepProjectiles(dt: Float) {
        val iter = projectiles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            val stepX = p.vx * dt
            val stepY = p.vy * dt
            p.x += stepX
            p.y += stepY
            p.remainingDist -= hypot(stepX, stepY)

            if (p.remainingDist <= 0f || p.x < 0f || p.x > InfineMapBlueprint.WORLD_SIZE || p.y < 0f || p.y > InfineMapBlueprint.WORLD_SIZE) {
                iter.remove()
                continue
            }

            if (pointHitsObstacle(p.x, p.y)) {
                iter.remove()
                continue
            }

            var hit = false
            if (p.team == CombatantTeam.ENEMY && !isPlayerKnocked) {
                if (hypot(p.x - playerX, p.y - playerY) < 18f) {
                    val attacker = combatants.firstOrNull { it.id == p.ownerId }
                    applyDamageToPlayer(
                        rawDamage = p.damage,
                        attackerId = p.ownerId,
                        attackerName = attacker?.name,
                        weaponLabel = p.weaponLabel
                    )
                    hit = true
                }
            }

            if (!hit) {
                for (c in combatants) {
                    if (c.isEliminated || c.team == p.team) continue
                    if (hypot(p.x - c.x, p.y - c.y) < 18f) {
                        applyDamageToCombatant(
                            target = c,
                            rawDamage = p.damage,
                            isFromPlayer = (p.ownerId == "player"),
                            attackerId = p.ownerId,
                            weaponLabel = p.weaponLabel
                        )
                        hit = true
                        break
                    }
                }
            }

            if (hit) iter.remove()
        }
    }

    private fun stepStormCircle(dt: Float) {
        val isCs = mode == GameMode.CLASH_SQUAD_RANKED
        val maxPhases = if (isCs) 3 else 5

        if (stormStageTimerSec > 0f) {
            stormStageTimerSec = (stormStageTimerSec - dt).coerceAtLeast(0f)
        }

        if (isStormClosing) {
            val dur = stormStageDurationSec.coerceAtLeast(1f)
            val progress = (1f - stormStageTimerSec / dur).coerceIn(0f, 1f)
            safeZoneX = stormStartX + (nextSafeZoneX - stormStartX) * progress
            safeZoneY = stormStartY + (nextSafeZoneY - stormStartY) * progress
            safeZoneRadius = stormStartRadius + (nextSafeZoneRadius - stormStartRadius) * progress

            if (stormStageTimerSec <= 0f) {
                safeZoneX = nextSafeZoneX
                safeZoneY = nextSafeZoneY
                safeZoneRadius = nextSafeZoneRadius
                isStormClosing = false

                if (stormPhase < maxPhases) {
                    stormPhase++
                    if (isCs) {
                        val csRadii = listOf(210f, 110f, 45f)
                        val csDps = listOf(7, 12, 18)
                        nextSafeZoneX = safeZoneX
                        nextSafeZoneY = safeZoneY
                        nextSafeZoneRadius = csRadii[(stormPhase - 1).coerceIn(0, csRadii.lastIndex)]
                        stormDamagePerSec = csDps[(stormPhase - 1).coerceIn(0, csDps.lastIndex)]
                        stormStageDurationSec = 10f
                        stormStageTimerSec = 10f
                    } else {
                        val brRadii = listOf(800f, 540f, 340f, 195f, 95f)
                        val brDps = listOf(5, 8, 12, 18, 26)
                        pickNextSafeZone(targetRadius = brRadii[(stormPhase - 1).coerceIn(0, brRadii.lastIndex)])
                        stormDamagePerSec = brDps[(stormPhase - 1).coerceIn(0, brDps.lastIndex)]
                        stormStageDurationSec = 9f
                        stormStageTimerSec = 9f
                    }
                    pushKillFeed("🌀 Storm Phase $stormPhase • Next Safe Zone on mini-map!", true)
                } else {
                    stormStageTimerSec = 0f
                }
            }
        } else if (stormStageTimerSec <= 0f && safeZoneRadius > nextSafeZoneRadius + 2f) {
            isStormClosing = true
            stormStartX = safeZoneX
            stormStartY = safeZoneY
            stormStartRadius = safeZoneRadius
            stormStageDurationSec = if (isCs) 16f else (16f - (stormPhase - 1) * 1.2f).coerceAtLeast(10f)
            stormStageTimerSec = stormStageDurationSec
            pushKillFeed("🌀 STORM PHASE $stormPhase SHRINKING! Move inside Safe Zone!", true)
        }

        zoneDamageTickSec += dt
        if (zoneDamageTickSec >= 1.0f) {
            zoneDamageTickSec = 0f
            if (hypot(playerX - safeZoneX, playerY - safeZoneY) > safeZoneRadius) {
                stormTickDamageFlashAlpha = 0.55f
                spawnEffect(playerX, playerY - 18f, 46f, 0xFFE879F9, 0.55f, "-$stormDamagePerSec STORM")
                applyDamageToPlayer(
                    rawDamage = stormDamagePerSec,
                    attackerName = "Storm Zone",
                    weaponLabel = "Storm Phase $stormPhase",
                    bypassVehicle = true,
                    bypassShield = true
                )
            }
            combatants.filter { !it.isEliminated }.forEach { bot ->
                if (hypot(bot.x - safeZoneX, bot.y - safeZoneY) > safeZoneRadius) {
                    applyDamageToCombatant(
                        target = bot,
                        rawDamage = stormDamagePerSec + 1,
                        isFromPlayer = false,
                        weaponLabel = "Storm Phase $stormPhase",
                        isZoneDamage = true
                    )
                }
            }
        }
    }

    private fun pickNextSafeZone(targetRadius: Float) {
        val maxShift = (safeZoneRadius - targetRadius).coerceAtLeast(0f)
        val angle = Random.nextFloat() * (2f * kotlin.math.PI.toFloat())
        val dist = maxShift * (0.25f + Random.nextFloat() * 0.65f)
        nextSafeZoneX = (safeZoneX + cos(angle) * dist).coerceIn(targetRadius + 80f, InfineMapBlueprint.WORLD_SIZE - targetRadius - 80f)
        nextSafeZoneY = (safeZoneY + sin(angle) * dist).coerceIn(targetRadius + 80f, InfineMapBlueprint.WORLD_SIZE - targetRadius - 80f)
        // Guarantee next circle stays inside current safe zone
        val dx = nextSafeZoneX - safeZoneX
        val dy = nextSafeZoneY - safeZoneY
        val d = hypot(dx, dy)
        if (d > maxShift && d > 0.001f) {
            nextSafeZoneX = safeZoneX + (dx / d) * maxShift
            nextSafeZoneY = safeZoneY + (dy / d) * maxShift
        }
        nextSafeZoneRadius = targetRadius
        safeZoneTargetX = nextSafeZoneX
        safeZoneTargetY = nextSafeZoneY
    }

    private fun applyDamageToPlayer(
        rawDamage: Int,
        attackerId: String? = null,
        attackerName: String? = null,
        weaponLabel: String = "",
        bypassVehicle: Boolean = false,
        bypassShield: Boolean = false
    ) {
        if (mode == GameMode.TRAINING_GROUND) return
        if (attackerId != null) {
            lastAttackerId = attackerId
        }
        if (!attackerName.isNullOrBlank()) {
            lastAttackerName = attackerName
        }
        if (weaponLabel.isNotBlank()) {
            lastAttackerWeapon = weaponLabel
        }
        val activeVeh = if (bypassVehicle) null else vehicles.firstOrNull { it.id == drivingVehicleId }
        if (activeVeh != null) {
            activeVeh.hp = (activeVeh.hp - rawDamage).coerceAtLeast(0)
            if (activeVeh.hp == 0) {
                drivingVehicleId = null
                spawnEffect(playerX, playerY, 90f, 0xFFFF3D57, 0.5f, "VEHICLE DOWN")
            }
            return
        }
        var dmg = rawDamage
        if (playerShield > 0 && !bypassShield) {
            val absorbed = minOf(playerShield, (dmg * 0.45f).toInt().coerceAtLeast(1))
            playerShield -= absorbed
            dmg -= absorbed
        }
        playerHp = (playerHp - dmg).coerceAtLeast(0)
        if (playerHp == 0 && !isPlayerKnocked) {
            val canKnock = (squadMode == SquadMode.SQUAD && combatants.any { it.team == CombatantTeam.PLAYER_SQUAD && !it.isEliminated && !it.isKnocked }) ||
                (soloSelfReviveTokens > 0) ||
                (OperatorCatalog.byId(operatorId).skillType == SkillType.REVIVE && skillCooldownRemaining <= 0f)
            if (canKnock) {
                isPlayerKnocked = true
                playerKnockedTimer = 14f
                isHealing = false
                isReloading = false
                pushKillFeed("$playerCallsign KNOCKED! Use Revival Skill or Revival Point!", true)
            } else {
                finishMatch(won = false)
            }
        }
    }

    private fun applyDamageToCombatant(
        target: Combatant,
        rawDamage: Int,
        isFromPlayer: Boolean,
        attackerId: String? = null,
        weaponLabel: String = "",
        isZoneDamage: Boolean = false
    ) {
        var dmg = rawDamage
        if (target.shield > 0) {
            val absorbed = minOf(target.shield, (dmg * 0.35f).toInt())
            target.shield -= absorbed
            dmg -= absorbed
        }
        target.hp = (target.hp - dmg).coerceAtLeast(0)
        if (isFromPlayer) {
            damageDealt += dmg
            val isLethal = target.hp == 0
            val isCrit = dmg >= 42 ||
                weaponLabel.contains("+") ||
                weaponLabel.contains("Sniper", ignoreCase = true) ||
                weaponLabel.contains("Woodpecker", ignoreCase = true)
            hitMarkerMaxTtl = if (isLethal) 0.42f else 0.24f
            hitMarkerTtl = hitMarkerMaxTtl
            hitMarkerWorldX = target.x
            hitMarkerWorldY = target.y
            hitMarkerDamage = dmg
            hitMarkerIsKill = isLethal
            hitMarkerIsCrit = isCrit
            hitMarkerComboCount = if (hitMarkerComboWindowSec > 0f) hitMarkerComboCount + 1 else 1
            hitMarkerComboWindowSec = 1.6f
            soundManager.playHitMarker(isKill = isLethal)
            spawnEffect(target.x, target.y - 16f, 26f, 0xFFFFB300, 0.35f, "-$dmg")
        }

        if (target.hp == 0) {
            val resolvedMethod = weaponLabel.ifBlank {
                if (isFromPlayer) WeaponCatalog.byId(currentWeaponId()).name else "Elim"
            }
            if (target.isTargetDummy) {
                target.hp = target.maxHp
                kills++
                spawnEffect(target.x, target.y, 60f, 0xFF00E5FF, 0.45f, "TARGET HIT")
                pushStructuredKillFeed(
                    text = "$playerCallsign hit ${target.name} [$resolvedMethod]",
                    killerName = playerCallsign,
                    victimName = target.name,
                    methodLabel = resolvedMethod,
                    kind = KillFeedKind.ELIMINATION,
                    highlight = true
                )
                return
            }
            if (!target.isKnocked && squadMode == SquadMode.SQUAD) {
                target.isKnocked = true
                target.knockedTimerSec = 12f
                val attacker = attackerId?.let { id -> combatants.firstOrNull { it.id == id } }
                val knocker = when {
                    isFromPlayer -> playerCallsign
                    attacker != null -> attacker.name
                    isZoneDamage -> "Safe Zone"
                    else -> null
                }
                pushStructuredKillFeed(
                    text = if (knocker != null) "$knocker knocked ${target.name}" else "${target.name} knocked",
                    killerName = knocker,
                    victimName = target.name,
                    methodLabel = if (isZoneDamage) "Zone" else resolvedMethod,
                    kind = if (isZoneDamage) KillFeedKind.ZONE else KillFeedKind.KNOCKDOWN,
                    highlight = isFromPlayer || (attacker?.team == CombatantTeam.PLAYER_SQUAD)
                )
            } else {
                target.isEliminated = true
                if (isFromPlayer) {
                    kills++
                    csClashCash += 300
                    pushStructuredKillFeed(
                        text = "$playerCallsign eliminated ${target.name} [$resolvedMethod]",
                        killerName = playerCallsign,
                        victimName = target.name,
                        methodLabel = resolvedMethod,
                        kind = KillFeedKind.ELIMINATION,
                        highlight = true
                    )
                    // Drop loot crate on BR map
                    if (mode == GameMode.BATTLE_ROYALE_RANKED || mode == GameMode.BATTLE_ROYALE_CASUAL) {
                        lootDrops += LootDrop(
                            id = "drop_${nextId++}",
                            x = target.x,
                            y = target.y,
                            kind = LootKind.MEDKIT,
                            label = "Medkit"
                        )
                    }
                } else if (attackerId != null) {
                    val attacker = combatants.firstOrNull { it.id == attackerId }
                    attacker?.let {
                        it.kills++
                        val aiWeapon = weaponLabel.ifBlank { WeaponCatalog.byId(it.weaponId).name }
                        pushStructuredKillFeed(
                            text = "${it.name} eliminated ${target.name} [$aiWeapon]",
                            killerName = it.name,
                            victimName = target.name,
                            methodLabel = aiWeapon,
                            kind = KillFeedKind.ELIMINATION,
                            highlight = it.team == CombatantTeam.PLAYER_SQUAD
                        )
                    }
                } else if (isZoneDamage) {
                    pushStructuredKillFeed(
                        text = "${target.name} eliminated by Safe Zone",
                        killerName = "Safe Zone",
                        victimName = target.name,
                        methodLabel = "Zone",
                        kind = KillFeedKind.ZONE,
                        highlight = false
                    )
                } else {
                    pushStructuredKillFeed(
                        text = "${target.name} eliminated",
                        killerName = null,
                        victimName = target.name,
                        methodLabel = resolvedMethod,
                        kind = KillFeedKind.ELIMINATION,
                        highlight = false
                    )
                }
            }
        }
    }

    private fun checkMatchWinOrLoss() {
        if (mode == GameMode.TRAINING_GROUND) return

        val aliveEnemies = combatants.count { it.team == CombatantTeam.ENEMY && !it.isEliminated }
        val aliveAllies = combatants.count { it.team == CombatantTeam.PLAYER_SQUAD && !it.isEliminated }

        if (mode == GameMode.CLASH_SQUAD_RANKED) {
            if (aliveEnemies == 0) {
                csPlayerWins++
                csClashCash += 1400
                if (csPlayerWins >= 4) {
                    finishMatch(won = true)
                } else {
                    csRound++
                    startClashSquadRound(withBuyPhase = true)
                }
            } else if (isPlayerKnocked && aliveAllies == 0) {
                csEnemyWins++
                csClashCash += 950
                if (csEnemyWins >= 4) {
                    finishMatch(won = false)
                } else {
                    csRound++
                    startClashSquadRound(withBuyPhase = true)
                }
            }
        } else if (mode == GameMode.BATTLE_ROYALE_RANKED || mode == GameMode.BATTLE_ROYALE_CASUAL) {
            if (aliveEnemies == 0) {
                finishMatch(won = true)
            }
        }
    }

    private fun finishMatch(won: Boolean, abandonedEarly: Boolean = false) {
        if (matchFinished) return
        matchFinished = true
        soundManager.stopVehicleEngine()
        if (won) soundManager.playUiConfirm()
        val lvlSpec = ArenaLevelCatalog.byLevel(arenaLevel)
        val aliveEnemies = combatants.count { it.team == CombatantTeam.ENEMY && !it.isEliminated }
        val placement = if (won) 1 else (aliveEnemies + 1).coerceAtLeast(2)
        val rpDelta = when (mode) {
            GameMode.BATTLE_ROYALE_RANKED -> {
                val placementBonus = if (won) 42 else ((16 - placement) * 3 - 10)
                ((placementBonus + kills * 7 + revives * 6) * (0.9f + arenaLevel * 0.1f)).toInt()
            }
            GameMode.CLASH_SQUAD_RANKED -> {
                val base = if (won) 28 + kills * 3 else -14 + kills * 2
                (base * (0.9f + arenaLevel * 0.1f)).toInt()
            }
            GameMode.BATTLE_ROYALE_CASUAL, GameMode.TRAINING_GROUND -> 0
        }
        val baseXp = when (mode) {
            GameMode.TRAINING_GROUND -> (kills * 12 + damageDealt / 20).coerceAtLeast(15)
            GameMode.BATTLE_ROYALE_CASUAL -> if (abandonedEarly) 20 else ((if (won) 105 else 45) + kills * 30 + revives * 35 + (damageDealt / 9))
            GameMode.BATTLE_ROYALE_RANKED, GameMode.CLASH_SQUAD_RANKED -> (if (won) 120 else 45) + kills * 35 + revives * 45 + (damageDealt / 8)
        }
        val xpEarned = (baseXp * lvlSpec.xpMultiplier).toInt().coerceAtLeast(10)
        val goldEarned = when (mode) {
            GameMode.TRAINING_GROUND -> 50 + kills * 5
            GameMode.BATTLE_ROYALE_CASUAL -> if (abandonedEarly) 40 else ((if (won) 240 else 100) + kills * 28 + revives * 20)
            GameMode.BATTLE_ROYALE_RANKED, GameMode.CLASH_SQUAD_RANKED -> ((if (won) 320 else 120) + kills * 35 + revives * 25 + (arenaLevel - 1) * 15)
        }
        val gemsEarned = when {
            mode == GameMode.TRAINING_GROUND -> 0
            abandonedEarly -> 0
            mode == GameMode.BATTLE_ROYALE_CASUAL -> if (won) 6 else 1
            won -> 12 + (kills / 2)
            kills >= 3 -> 4
            else -> 2
        }
        val squadList = buildList {
            add(SquadMemberResult(name = playerCallsign, isLocalPlayer = true, kills = kills, isEliminated = !won && isPlayerKnocked))
            combatants.filter { it.team == CombatantTeam.PLAYER_SQUAD }.forEach { ally ->
                add(SquadMemberResult(name = ally.name, isLocalPlayer = false, kills = ally.kills, isEliminated = ally.isEliminated))
            }
        }
        val locName = InfineMapBlueprint.nearestLocationName(playerX, playerY)
        val participants = combatants
            .sortedBy { if (it.team == CombatantTeam.ENEMY) 0 else 1 }
            .map { it.name }
            .distinct()
        captureReplayFrame(
            forceEvent = if (abandonedEarly) "Abandoned match early at $locName" else if (won) "BOOYAH! Victory at $locName" else "Match ended (#$placement) at $locName",
            allowDuplicateSecond = true
        )
        val deathCamResult = if (!won && !abandonedEarly && mode != GameMode.TRAINING_GROUND) {
            buildDeathCamPackage()
        } else {
            Triple(null, "", emptyList<DeathCamFrame>())
        }
        _hudState.value = _hudState.value.copy(
            matchSummary = MatchEndSummary(
                mode = mode,
                squadMode = squadMode,
                arenaLevel = arenaLevel,
                won = won,
                placementRank = placement,
                kills = kills,
                revives = revives,
                damageDealt = damageDealt,
                rpDelta = rpDelta,
                xpEarned = xpEarned,
                goldEarned = goldEarned,
                gemsEarned = gemsEarned,
                durationSeconds = matchElapsedSec.toInt().coerceAtLeast(1),
                squadResults = squadList,
                participantNames = participants,
                replayFrames = recordedReplayFrames.toList(),
                lastLocation = locName,
                weaponUsed = currentWeaponId(),
                abandonedEarly = abandonedEarly,
                eliminatedByName = deathCamResult.first,
                eliminatedByWeapon = deathCamResult.second,
                deathCamFrames = deathCamResult.third
            )
        )
    }

    private fun captureDeathCamSample() {
        val killerStates = combatants.associate { c ->
            c.id to KillerSnapshot(
                x = c.x,
                y = c.y,
                aimRad = c.aimRad,
                hp = c.hp,
                maxHp = c.maxHp
            )
        }
        val actors = combatants.map { c ->
            ReplayActorSnapshot(
                name = c.name,
                isAlly = c.team == CombatantTeam.PLAYER_SQUAD,
                x = c.x,
                y = c.y,
                aimRad = c.aimRad,
                isKnocked = c.isKnocked,
                isEliminated = c.isEliminated
            )
        }
        val bullets = projectiles.map { p ->
            DeathCamBullet(
                x = p.x,
                y = p.y,
                vx = p.vx,
                vy = p.vy,
                isFromKiller = (p.ownerId == lastAttackerId)
            )
        }
        if (deathCamBuffer.size >= 40) {
            deathCamBuffer.removeFirst()
        }
        deathCamBuffer.addLast(
            BufferedDeathSnapshot(
                elapsedSec = matchElapsedSec,
                victimX = playerX,
                victimY = playerY,
                victimHp = playerHp,
                victimKnocked = isPlayerKnocked,
                safeZoneX = safeZoneX,
                safeZoneY = safeZoneY,
                safeZoneRadius = safeZoneRadius,
                killerStates = killerStates,
                actors = actors,
                bullets = bullets
            )
        )
    }

    private fun buildDeathCamPackage(): Triple<String?, String, List<DeathCamFrame>> {
        captureDeathCamSample()
        val resolvedKiller = combatants.firstOrNull { it.id == lastAttackerId }
            ?: combatants.filter { it.team == CombatantTeam.ENEMY && !it.isEliminated }
                .minByOrNull { hypot(it.x - playerX, it.y - playerY) }
        val killerId = resolvedKiller?.id
        val killerName = lastAttackerName ?: resolvedKiller?.name ?: "Hostile Squad"
        val killerWeapon = lastAttackerWeapon.ifBlank {
            resolvedKiller?.let { WeaponCatalog.byId(it.weaponId).name } ?: "Tactical Fire"
        }

        val samples = deathCamBuffer.toList()
        if (samples.isEmpty()) return Triple(killerName, killerWeapon, emptyList())
        val endTime = samples.last().elapsedSec

        val frames = samples.mapIndexed { idx, s ->
            val ks = killerId?.let { s.killerStates[it] }
            val kx = ks?.x ?: resolvedKiller?.x ?: (s.victimX + 160f)
            val ky = ks?.y ?: resolvedKiller?.y ?: (s.victimY - 90f)
            val aim = ks?.aimRad ?: atan2(s.victimY - ky, s.victimX - kx)
            val isFinal = idx == samples.lastIndex
            val banner = when {
                isFinal -> "ELIMINATED BY $killerName ($killerWeapon)"
                idx >= samples.lastIndex - 2 -> "FINAL SHOT LOCKED"
                else -> null
            }
            DeathCamFrame(
                timeOffsetSec = (s.elapsedSec - endTime).coerceAtMost(0f),
                killerX = kx,
                killerY = ky,
                killerAimRad = aim,
                killerHp = ks?.hp ?: resolvedKiller?.hp ?: 100,
                killerMaxHp = ks?.maxHp ?: resolvedKiller?.maxHp ?: 100,
                victimX = s.victimX,
                victimY = s.victimY,
                victimHp = if (isFinal) 0 else s.victimHp,
                victimKnocked = s.victimKnocked,
                safeZoneX = s.safeZoneX,
                safeZoneY = s.safeZoneY,
                safeZoneRadius = s.safeZoneRadius,
                actors = s.actors,
                bullets = s.bullets,
                impactBanner = banner
            )
        }
        return Triple(killerName, killerWeapon, frames)
    }

    private fun captureReplayFrame(
        forceEvent: String? = null,
        allowDuplicateSecond: Boolean = false
    ) {
        val tSec = matchElapsedSec.toInt().coerceAtLeast(0)
        if (!allowDuplicateSecond && tSec == lastReplaySampleSec) return
        lastReplaySampleSec = tSec
        val latestEvent = forceEvent ?: killFeed.firstOrNull { it.ttlSec >= 5.2f }?.text
        val aliveCount = combatants.count { !it.isEliminated } + if (!matchFinished) 1 else 0
        recordedReplayFrames += ReplayFrameSnapshot(
            timeSec = tSec,
            playerX = playerX,
            playerY = playerY,
            playerAimRad = playerAimRad,
            playerHp = playerHp,
            isKnocked = isPlayerKnocked,
            safeZoneX = safeZoneX,
            safeZoneY = safeZoneY,
            safeZoneRadius = safeZoneRadius,
            aliveCount = aliveCount,
            kills = kills,
            eventText = latestEvent,
            actors = combatants.map { c ->
                ReplayActorSnapshot(
                    name = c.name,
                    isAlly = c.team == CombatantTeam.PLAYER_SQUAD,
                    x = c.x,
                    y = c.y,
                    aimRad = c.aimRad,
                    isKnocked = c.isKnocked,
                    isEliminated = c.isEliminated
                )
            }
        )
    }

    // === PLAYER ACTIONS ===

    fun toggleSfx() {
        soundManager.toggleSfx()
    }

    fun playUiClick() {
        soundManager.playUiClick()
    }

    fun equipEmoteOnly(emoteId: String) {
        val emote = EmoteCatalog.byId(emoteId)
        equippedEmoteId = emote.id
        soundManager.playUiConfirm()
    }

    fun equipAndTriggerEmote(emoteId: String) {
        equippedEmoteId = EmoteCatalog.byId(emoteId).id
        triggerEmote()
    }

    fun triggerEmote() {
        val emote = EmoteCatalog.byId(equippedEmoteId)
        val colorHex = emoteAccentHex(emote)
        activeEmoteText = "${emote.emoji} ${emote.name}: \"${emote.shoutText}\""
        activeEmoteColorHex = colorHex
        activeEmoteTtl = 2.6f
        soundManager.playEmoteChime()
        spawnEffect(playerX, playerY, 128f, colorHex, 1.8f, "${emote.emoji} ${emote.shoutText}")
        pushStructuredKillFeed(
            text = "${emote.emoji} $playerCallsign: ${emote.name} (${emote.shoutText})",
            killerName = playerCallsign,
            victimName = null,
            methodLabel = "Emote",
            kind = KillFeedKind.SYSTEM,
            highlight = true
        )
        // Expressive squadmate social reaction when nearby allies witness the emote
        val nearbyAlly = combatants.firstOrNull {
            it.team == CombatantTeam.PLAYER_SQUAD &&
                !it.isEliminated &&
                !it.isKnocked &&
                hypot(it.x - playerX, it.y - playerY) < 520f
        }
        if (nearbyAlly != null) {
            val replyEmoji = if (emote.id == "booyah_flare") "🔥" else "🫡"
            spawnEffect(nearbyAlly.x, nearbyAlly.y, 82f, 0xFF00E5FFL, 1.35f, "$replyEmoji GG!")
        }
    }

    private fun emoteAccentHex(emote: EmoteSpec): Long = when (emote.id) {
        "squad_gg" -> 0xFF00E676L
        "booyah_flare" -> 0xFFFFB300L
        "clockwork_flex" -> 0xFF00E676L
        "apex_crown" -> 0xFFFF3D57L
        else -> 0xFF00E5FFL
    }

    fun triggerFirePress(firing: Boolean) {
        isFiring = firing
    }

    fun startReload() {
        if (isReloading || isPlayerKnocked) return
        val spec = WeaponCatalog.byId(currentWeaponId())
        if (magAmmo >= spec.magSize) return
        isReloading = true
        reloadElapsedMs = 0L
        soundManager.playReload()
    }

    fun switchWeaponSlot(slot: Int) {
        if (slot == activeSlot || isPlayerKnocked) return
        activeSlot = slot
        isReloading = false
        reloadElapsedMs = 0L
        magAmmo = WeaponCatalog.byId(currentWeaponId()).magSize
        soundManager.playUiClick()
    }

    fun useMedkit() {
        if (isPlayerKnocked || isHealing || medkitCount <= 0 || playerHp >= playerMaxHp) return
        isHealing = true
        healElapsedSec = 0f
        soundManager.playUiClick()
    }

    fun useInhaler() {
        if (isPlayerKnocked || inhalerCount <= 0 || playerHp >= playerMaxHp) return
        inhalerCount--
        playerHp = (playerHp + 32).coerceAtMost(playerMaxHp)
        soundManager.playMedkit()
        spawnEffect(playerX, playerY, 65f, 0xFF00E676, 0.45f, "+32 HP")
    }

    fun activateCharacterSkill() {
        if (skillCooldownRemaining > 0f) return
        val op = OperatorCatalog.byId(operatorId)

        when (op.skillType) {
            SkillType.HEAL -> {
                if (isPlayerKnocked) return
                healAuraRemaining = 4.0f
                playerHp = (playerHp + 20).coerceAtMost(playerMaxHp)
                spawnEffect(playerX, playerY, 210f, 0xFF00E676, 0.9f, "NANO-MEND")
                pushKillFeed("${op.name} activated ${op.skillName}!", true)
            }
            SkillType.REVIVE -> {
                if (isPlayerKnocked) {
                    revivePlayer(65)
                    playerShield = (playerShield + 25).coerceAtMost(75)
                } else {
                    val downedAlly = combatants.firstOrNull {
                        it.team == CombatantTeam.PLAYER_SQUAD && (it.isKnocked || it.isEliminated)
                    }
                    if (downedAlly != null) {
                        downedAlly.isKnocked = false
                        downedAlly.isEliminated = false
                        downedAlly.hp = 80
                        revives++
                        spawnEffect(downedAlly.x, downedAlly.y, 120f, 0xFF00E5FF, 0.8f, "REVIVED!")
                        pushStructuredKillFeed(
                            text = "${op.name} revived ${downedAlly.name}!",
                            killerName = playerCallsign,
                            victimName = downedAlly.name,
                            methodLabel = op.skillName,
                            kind = KillFeedKind.REVIVE,
                            highlight = true
                        )
                    } else {
                        playerShield = (playerShield + 35).coerceAtMost(75)
                        spawnEffect(playerX, playerY, 95f, 0xFF00E5FF, 0.6f, "+35 SHIELD")
                    }
                }
            }
            SkillType.DAMAGE -> {
                if (isPlayerKnocked) return
                spawnEffect(playerX, playerY, 240f, 0xFFFF3D57, 0.7f, "ARC STRIKE!")
                var hitCount = 0
                combatants.filter { it.team == CombatantTeam.ENEMY && !it.isEliminated }
                    .forEach { enemy ->
                        if (hypot(enemy.x - playerX, enemy.y - playerY) <= 240f) {
                            enemy.slowTimerSec = 3.0f
                            applyDamageToCombatant(
                                target = enemy,
                                rawDamage = 68,
                                isFromPlayer = true,
                                weaponLabel = "Arc Strike"
                            )
                            hitCount++
                        }
                    }
                pushKillFeed("Arc Strike hit $hitCount enemy targets!", true)
            }
        }
        skillCooldownRemaining = if (mode == GameMode.TRAINING_GROUND) 2f else op.cooldownSec.toFloat()
    }

    private fun revivePlayer(hpAmount: Int) {
        isPlayerKnocked = false
        playerKnockedTimer = 0f
        playerHp = hpAmount
        if (soloSelfReviveTokens > 0) soloSelfReviveTokens--
        revives++
        spawnEffect(playerX, playerY, 120f, 0xFF00E5FF, 0.8f, "REVIVED!")
        pushStructuredKillFeed(
            text = "$playerCallsign revived!",
            killerName = playerCallsign,
            victimName = null,
            methodLabel = "Revive",
            kind = KillFeedKind.REVIVE,
            highlight = true
        )
    }

    private fun reviveAlliesOrSelf(fromRevivalPoint: Boolean) {
        if (isPlayerKnocked) {
            revivePlayer(70)
            return
        }
        var revivedAny = false
        combatants.filter {
            it.team == CombatantTeam.PLAYER_SQUAD && (it.isKnocked || (fromRevivalPoint && it.isEliminated))
        }.forEach { ally ->
            ally.isKnocked = false
            ally.isEliminated = false
            ally.hp = 85
            ally.x = playerX + Random.nextFloat() * 60f - 30f
            ally.y = playerY + Random.nextFloat() * 60f - 30f
            revivedAny = true
            revives++
            spawnEffect(ally.x, ally.y, 110f, 0xFF00E5FF, 0.8f, "${ally.name} REVIVED")
        }
        if (revivedAny) {
            pushStructuredKillFeed(
                text = "Squadmate revived at Revival Point!",
                killerName = playerCallsign,
                victimName = "Squadmate",
                methodLabel = "Revival Pt",
                kind = KillFeedKind.REVIVE,
                highlight = true
            )
        }
    }

    fun interactNearby() {
        if (isPlayerKnocked) return
        if (drivingVehicleId != null) {
            // Exit vehicle safely
            drivingVehicleId = null
            playerX = (playerX + 34f).coerceIn(70f, 2330f)
            soundManager.playUiClick()
            return
        }

        val nearVeh = vehicles.firstOrNull { it.hp > 0 && hypot(it.x - playerX, it.y - playerY) < 92f }
        if (nearVeh != null) {
            drivingVehicleId = nearVeh.id
            isHealing = false
            isReloading = false
            soundManager.playUiConfirm()
            return
        }

        val nearLoot = lootDrops.firstOrNull { hypot(it.x - playerX, it.y - playerY) < 75f }
        if (nearLoot != null) {
            when (nearLoot.kind) {
                LootKind.WEAPON -> {
                    val wId = nearLoot.weaponId ?: return
                    if (activeSlot == 0) primaryWeapon = wId else secondaryWeapon = wId
                    magAmmo = WeaponCatalog.byId(wId).magSize
                    isReloading = false
                    spawnEffect(playerX, playerY, 65f, 0xFFFFB300, 0.45f, "EQUIPPED ${nearLoot.label}")
                    if (mode != GameMode.TRAINING_GROUND) lootDrops.remove(nearLoot)
                }
                LootKind.MEDKIT -> {
                    medkitCount++
                    spawnEffect(playerX, playerY, 55f, 0xFF00E676, 0.4f, "+1 MEDKIT")
                    if (mode != GameMode.TRAINING_GROUND) lootDrops.remove(nearLoot)
                }
                LootKind.INHALER -> {
                    inhalerCount++
                    spawnEffect(playerX, playerY, 55f, 0xFF00E676, 0.4f, "+1 INHALER")
                    if (mode != GameMode.TRAINING_GROUND) lootDrops.remove(nearLoot)
                }
                LootKind.ARMOR -> {
                    playerShield = 75
                    spawnEffect(playerX, playerY, 65f, 0xFF00E5FF, 0.45f, "ARMOR +75")
                    if (mode != GameMode.TRAINING_GROUND) lootDrops.remove(nearLoot)
                }
            }
        }
    }

    fun buyClashSquadWeapon(weaponId: WeaponId) {
        val spec = WeaponCatalog.byId(weaponId)
        if (csClashCash < spec.clashCost) return
        csClashCash -= spec.clashCost
        if (activeSlot == 0) primaryWeapon = weaponId else secondaryWeapon = weaponId
        magAmmo = spec.magSize
        publishState()
    }

    fun buyClashSquadMedkit() {
        if (csClashCash < 300) return
        csClashCash -= 300
        medkitCount++
        publishState()
    }

    fun buyClashSquadArmor() {
        if (csClashCash < 450 || playerShield >= 75) return
        csClashCash -= 450
        playerShield = 75
        publishState()
    }

    fun skipBuyPhase() {
        isBuyPhase = false
        publishState()
    }

    fun selectTrainingWeapon(weaponId: WeaponId) {
        if (mode != GameMode.TRAINING_GROUND) return
        if (activeSlot == 0) primaryWeapon = weaponId else secondaryWeapon = weaponId
        magAmmo = WeaponCatalog.byId(weaponId).magSize
        isReloading = false
        spawnEffect(playerX, playerY, 60f, 0xFFFFB300, 0.45f, WeaponCatalog.byId(weaponId).name)
        publishState()
    }

    fun resetTrainingArena() {
        if (mode != GameMode.TRAINING_GROUND) return
        playerHp = playerMaxHp
        playerShield = 75
        skillCooldownRemaining = 0f
        combatants.filter { it.team == CombatantTeam.PLAYER_SQUAD }.forEach {
            it.isKnocked = true
            it.isEliminated = false
            it.hp = 0
        }
        spawnEffect(playerX, playerY, 90f, 0xFF00E676, 0.5f, "ARENA RESET")
        publishState()
    }

    fun pingSquadmate(allyId: String) {
        val ally = combatants.firstOrNull { it.id == allyId && it.team == CombatantTeam.PLAYER_SQUAD } ?: return
        soundManager.playUiClick()
        spawnEffect(ally.x, ally.y, 90f, 0xFF00E5FF, 0.7f, "📍 RALLY")
        if (!ally.isEliminated && !ally.isKnocked) {
            ally.aiTargetX = playerX
            ally.aiTargetY = playerY
            ally.aiRetargetCooldown = 4.0f
            pushKillFeed("Rally ping sent to ${ally.name}", true)
        } else if (ally.isKnocked) {
            pushKillFeed("En route to revive ${ally.name}!", true)
        }
        publishState()
    }

    fun toggleFullMapOverlay() {
        _hudState.value = _hudState.value.copy(showFullMapOverlay = !_hudState.value.showFullMapOverlay)
    }

    fun cycleWeatherManually() {
        applyWeatherShift(currentWeather.nextCycle())
    }

    private fun stepWeather(dt: Float) {
        weatherTickPhase = (weatherTickPhase + dt) % 1000f
        weatherRemainingSec -= dt
        if (weatherRemainingSec <= 0f) {
            applyWeatherShift(currentWeather.nextCycle())
        }

        val targetIntensity = if (currentWeather == WeatherCondition.CLEAR) 0f else 1f
        weatherIntensity = if (weatherIntensity < targetIntensity) {
            (weatherIntensity + dt * 0.55f).coerceAtMost(1f)
        } else {
            (weatherIntensity - dt * 0.55f).coerceAtLeast(0f)
        }

        if (lightningFlashAlpha > 0f) {
            lightningFlashAlpha = (lightningFlashAlpha - dt * 2.2f).coerceAtLeast(0f)
        }

        if (currentWeather == WeatherCondition.STORM) {
            nextLightningSec -= dt
            if (nextLightningSec <= 0f) {
                nextLightningSec = 5.5f + Random.nextFloat() * 4.5f
                lightningFlashAlpha = 0.72f
                soundManager.playThunder()
                val strikeX = (playerX + (Random.nextFloat() - 0.5f) * 320f).coerceIn(120f, 2280f)
                val strikeY = (playerY + (Random.nextFloat() - 0.5f) * 320f).coerceIn(120f, 2280f)
                spawnEffect(strikeX, strikeY, 105f, 0xFFD500F9, 0.7f, "⚡ STORM STRIKE")
                if (mode != GameMode.TRAINING_GROUND && hypot(strikeX - playerX, strikeY - playerY) < 70f) {
                    applyDamageToPlayer(4, attackerName = "Lightning Storm", weaponLabel = "Thunderbolt")
                }
            }
        }
    }

    private fun applyWeatherShift(next: WeatherCondition) {
        currentWeather = next
        weatherRemainingSec = if (next == WeatherCondition.CLEAR) 20f else 24f
        if (next == WeatherCondition.STORM) {
            nextLightningSec = 2.0f
            lightningFlashAlpha = 0.5f
        }
        val msg = "${next.emoji} Weather: ${next.label} (${next.effectSummary})"
        pushStructuredKillFeed(
            text = msg,
            killerName = null,
            victimName = null,
            methodLabel = "Weather",
            kind = KillFeedKind.SYSTEM,
            highlight = next != WeatherCondition.CLEAR
        )
    }

    private fun stepAntiCheatMonitor(dt: Float) {
        antiCheatPulseSec += dt
        if (antiCheatPulseSec < 1.8f) return
        val interval = antiCheatPulseSec.coerceAtLeast(0.5f)
        antiCheatPulseSec = 0f

        val distMoved = hypot(playerX - prevCheckedX, playerY - prevCheckedY)
        prevCheckedX = playerX
        prevCheckedY = playerY
        val maxReasonableSpeed = if (drivingVehicleId != null) 720f else 360f
        val speedOk = (distMoved / interval) <= maxReasonableSpeed
        val cadenceOk = WeaponCatalog.byId(currentWeaponId()).fireRateMs >= 50L
        val hitOk = playerHp in 0..playerMaxHp && playerShield in 0..75
        if (speedOk && cadenceOk && hitOk) {
            antiCheatChecksPassed += 1
        }
        antiCheatSnapshot = computeAntiCheatSnapshot(speedOk, cadenceOk, hitOk)
    }

    private fun computeAntiCheatSnapshot(
        speedOk: Boolean,
        cadenceOk: Boolean,
        hitOk: Boolean
    ): AntiCheatSnapshot {
        val status = when {
            !speedOk || !cadenceOk || !hitOk || playerHonorScore < 70 -> AntiCheatStatus.FLAGGED
            playerHonorScore < RoboHonorPolicy.MIN_RANKED_HONOR -> AntiCheatStatus.GUARDED
            else -> AntiCheatStatus.VERIFIED
        }
        val note = when (status) {
            AntiCheatStatus.VERIFIED -> "Speed, Ballistics & Cadence Verified"
            AntiCheatStatus.GUARDED -> "Honor ${playerHonorScore}/100 • Finish Matches Cleanly"
            AntiCheatStatus.FLAGGED -> "Telemetry Anomaly or Low Honor Standing"
        }
        return AntiCheatSnapshot(
            status = status,
            honorScore = playerHonorScore,
            checksPassed = antiCheatChecksPassed,
            speedBoundsOk = speedOk,
            fireCadenceOk = cadenceOk,
            hitValidationOk = hitOk,
            activeScanNote = note
        )
    }

    fun forfeitMatchEarly(): MatchEndSummary {
        finishMatch(won = false, abandonedEarly = true)
        return _hudState.value.matchSummary!!
    }

    private fun currentWeaponId(): WeaponId = if (activeSlot == 0) primaryWeapon else secondaryWeapon

    private fun spawnEffect(x: Float, y: Float, maxR: Float, colorHex: Long, ttl: Float, label: String? = null) {
        effects += VisualEffect(nextId++, x, y, maxR, colorHex, ttl, ttl, label)
    }

    private fun pushKillFeed(text: String, highlight: Boolean) {
        pushStructuredKillFeed(
            text = text,
            killerName = null,
            victimName = null,
            methodLabel = "",
            kind = KillFeedKind.SYSTEM,
            highlight = highlight
        )
    }

    private fun pushStructuredKillFeed(
        text: String,
        killerName: String?,
        victimName: String?,
        methodLabel: String,
        kind: KillFeedKind,
        highlight: Boolean
    ) {
        killFeed.add(
            0,
            KillFeedEntry(
                id = nextId++,
                text = text,
                isPlayerHighlight = highlight,
                killerName = killerName,
                victimName = victimName,
                methodLabel = methodLabel,
                kind = kind,
                ttlSec = 6.5f,
                maxTtlSec = 6.5f
            )
        )
        if (killFeed.size > 5) killFeed.removeAt(killFeed.lastIndex)
    }

    private fun resolveObstacleCollision(oldX: Float, oldY: Float, newX: Float, newY: Float, radius: Float): Pair<Float, Float> {
        var rx = newX
        var ry = newY
        for (rect in InfineMapBlueprint.obstacles) {
            if (circleIntersectsRect(rx, ry, radius, rect)) {
                if (!circleIntersectsRect(oldX, ry, radius, rect)) {
                    rx = oldX
                } else if (!circleIntersectsRect(rx, oldY, radius, rect)) {
                    ry = oldY
                } else {
                    rx = oldX
                    ry = oldY
                }
            }
        }
        return rx to ry
    }

    private fun circleIntersectsRect(cx: Float, cy: Float, r: Float, rect: MapRect): Boolean {
        val closestX = cx.coerceIn(rect.left, rect.right)
        val closestY = cy.coerceIn(rect.top, rect.bottom)
        val dx = cx - closestX
        val dy = cy - closestY
        return (dx * dx + dy * dy) < (r * r)
    }

    private fun pointHitsObstacle(px: Float, py: Float): Boolean {
        return InfineMapBlueprint.obstacles.any { px >= it.left && px <= it.right && py >= it.top && py <= it.bottom }
    }

    private fun publishState() {
        val activeSpec = WeaponCatalog.byId(currentWeaponId())
        val activeVeh = vehicles.firstOrNull { it.id == drivingVehicleId }?.copy()
        val nearVeh = if (activeVeh == null) {
            vehicles.firstOrNull { it.hp > 0 && hypot(it.x - playerX, it.y - playerY) < 92f }?.copy()
        } else null
        val nearLoot = lootDrops.firstOrNull { hypot(it.x - playerX, it.y - playerY) < 75f }
        val nearRp = InfineMapBlueprint.revivalPoints.firstOrNull { hypot(it.x - playerX, it.y - playerY) < 88f }
        val canRevive = combatants.any {
            it.team == CombatantTeam.PLAYER_SQUAD && (it.isKnocked || (nearRp != null && it.isEliminated))
        } || isPlayerKnocked
        val aliveCount = combatants.count { !it.isEliminated } + if (!matchFinished) 1 else 0
        val op = OperatorCatalog.byId(operatorId)

        _hudState.value = _hudState.value.copy(
            mode = mode,
            squadMode = squadMode,
            arenaLevel = arenaLevel,
            playerX = playerX,
            playerY = playerY,
            playerAimRad = playerAimRad,
            hp = playerHp,
            maxHp = playerMaxHp,
            shield = playerShield,
            isKnocked = isPlayerKnocked,
            knockedTimerSec = playerKnockedTimer,
            primaryWeapon = primaryWeapon,
            secondaryWeapon = secondaryWeapon,
            equippedWeaponSkinId = equippedWeaponSkinId,
            equippedCharacterSkinId = equippedCharacterSkinId,
            equippedEmoteId = equippedEmoteId,
            matchElapsedSeconds = matchElapsedSec.toInt(),
            activeSlot = activeSlot,
            magAmmo = magAmmo,
            maxMagAmmo = activeSpec.magSize,
            isReloading = isReloading,
            reloadProgress = if (isReloading) (reloadElapsedMs.toFloat() / activeSpec.reloadMs).coerceIn(0f, 1f) else 0f,
            medkitCount = medkitCount,
            inhalerCount = inhalerCount,
            isHealing = isHealing,
            healProgress = if (isHealing) (healElapsedSec / 1.6f).coerceIn(0f, 1f) else 0f,
            operatorId = operatorId,
            skillCooldownRemaining = skillCooldownRemaining,
            skillCooldownTotal = op.cooldownSec.toFloat(),
            healAuraRemaining = healAuraRemaining,
            drivingVehicle = activeVeh,
            nearbyVehicle = nearVeh,
            nearbyLoot = nearLoot,
            nearbyRevivalPointName = nearRp?.name,
            revivalProgress = (revivalChannelSec / 2.8f).coerceIn(0f, 1f),
            canReviveNearby = canRevive,
            currentLocationName = InfineMapBlueprint.nearestLocationName(playerX, playerY),
            aliveCount = aliveCount,
            totalCount = combatants.size + 1,
            kills = kills,
            revives = revives,
            damageDealt = damageDealt,
            safeZoneX = safeZoneX,
            safeZoneY = safeZoneY,
            safeZoneRadius = safeZoneRadius,
            nextSafeZoneX = nextSafeZoneX,
            nextSafeZoneY = nextSafeZoneY,
            nextSafeZoneRadius = nextSafeZoneRadius,
            stormPhase = stormPhase,
            isStormClosing = isStormClosing,
            stormPhaseSecondsLeft = stormStageTimerSec.toInt().coerceAtLeast(0),
            stormDamagePerSec = stormDamagePerSec,
            stormTickDamageFlashAlpha = stormTickDamageFlashAlpha,
            isOutsideZone = hypot(playerX - safeZoneX, playerY - safeZoneY) > safeZoneRadius,
            csRound = csRound,
            csPlayerTeamWins = csPlayerWins,
            csEnemyTeamWins = csEnemyWins,
            csClashCash = csClashCash,
            isBuyPhase = isBuyPhase,
            buyPhaseSecondsLeft = buyPhaseTimerSec.toInt().coerceAtLeast(0),
            combatants = combatants.map { it.copy() },
            vehicles = vehicles.map { it.copy() },
            lootDrops = lootDrops.toList(),
            projectiles = projectiles.toList(),
            effects = effects.toList(),
            killFeed = killFeed.toList(),
            weather = currentWeather,
            weatherSecondsLeft = weatherRemainingSec.toInt().coerceAtLeast(0),
            weatherIntensity = weatherIntensity,
            lightningFlashAlpha = lightningFlashAlpha,
            weatherTickPhase = weatherTickPhase,
            activeEmoteText = activeEmoteText,
            activeEmoteColorHex = activeEmoteColorHex,
            activeEmoteTtl = activeEmoteTtl,
            hitMarkerTtl = hitMarkerTtl,
            hitMarkerMaxTtl = hitMarkerMaxTtl,
            hitMarkerWorldX = hitMarkerWorldX,
            hitMarkerWorldY = hitMarkerWorldY,
            hitMarkerDamage = hitMarkerDamage,
            hitMarkerIsKill = hitMarkerIsKill,
            hitMarkerIsCrit = hitMarkerIsCrit,
            hitMarkerComboCount = hitMarkerComboCount,
            antiCheat = antiCheatSnapshot
        )
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stopVehicleEngine()
        simJob?.cancel()
    }
}
