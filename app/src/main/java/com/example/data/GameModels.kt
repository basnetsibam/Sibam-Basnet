package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.PlasmaViolet
import com.example.ui.theme.TacticalAmber

enum class GameMode(
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val maxPlayersLabel: String,
    val isRanked: Boolean
) {
    BATTLE_ROYALE_RANKED(
        title = "Battle Royale Ranked",
        shortTitle = "BR Ranked",
        subtitle = "Drop into Infine, loot weapons & vehicles, survive shrinking zones & use Revival Points.",
        maxPlayersLabel = "20 Competitors • Infine Map",
        isRanked = true
    ),
    CLASH_SQUAD_RANKED(
        title = "Clash Squad Ranked",
        shortTitle = "CS Ranked",
        subtitle = "4v4 tactical round-based elimination with Clash Cash buy phases across Infine zones.",
        maxPlayersLabel = "4v4 Rounds • First to 4",
        isRanked = true
    ),
    BATTLE_ROYALE_CASUAL(
        title = "Infine Casual (Unranked)",
        shortTitle = "Casual BR",
        subtitle = "Unranked Battle Royale on Infine. Finish matches to restore +8 Honor Score!",
        maxPlayersLabel = "20 Competitors • Restores +8 Honor",
        isRanked = false
    ),
    TRAINING_GROUND(
        title = "Training Ground",
        shortTitle = "Training",
        subtitle = "Free-roam Infine sandbox: test guns & vehicles, complete drills to restore +5 Honor!",
        maxPlayersLabel = "Sandbox • Restores +5 Honor",
        isRanked = false
    )
}

enum class SquadMode(val label: String) {
    SOLO("Solo"),
    SQUAD("4-Player Squad")
}

enum class RankTier(
    val label: String,
    val minRp: Int,
    val badgeColor: Color
) {
    BRONZE("Bronze", 0, Color(0xFFCD7F32)),
    SILVER("Silver", 1100, Color(0xFFC0C0C0)),
    GOLD("Gold", 1500, TacticalAmber),
    PLATINUM("Platinum", 2000, ElectricCyan),
    DIAMOND("Diamond", 2500, PlasmaViolet),
    HEROIC("Heroic", 3100, CombatCrimson),
    GRANDMASTER("Grandmaster", 3800, NanoEmerald);

    companion object {
        fun fromRp(rp: Int): RankTier {
            return entries.lastOrNull { rp >= it.minRp } ?: BRONZE
        }
    }
}

enum class WeaponCategory(val label: String) {
    AR("Assault Rifle"),
    SMG("SMG"),
    MARKSMAN("Marksman"),
    SNIPER("Sniper")
}

enum class WeaponId {
    AK47,
    CG15,
    MINI_UZI,
    MP5,
    UMP,
    WOODPECKER,
    M7,
    SNIPER
}

data class WeaponSpec(
    val id: WeaponId,
    val name: String,
    val category: WeaponCategory,
    val damage: Int,
    val fireRateMs: Long,
    val range: Float,
    val bulletSpeed: Float,
    val spreadRad: Float,
    val magSize: Int,
    val reloadMs: Long,
    val armorPen: Int,
    val clashCost: Int,
    val description: String,
    val accentColor: Color
)

object WeaponCatalog {
    val all: List<WeaponSpec> = listOf(
        WeaponSpec(
            id = WeaponId.AK47,
            name = "AK47",
            category = WeaponCategory.AR,
            damage = 38,
            fireRateMs = 155L,
            range = 540f,
            bulletSpeed = 980f,
            spreadRad = 0.07f,
            magSize = 30,
            reloadMs = 1600L,
            armorPen = 72,
            clashCost = 1400,
            description = "Iconic 7.62mm rifle with devastating mid-range stopping power.",
            accentColor = TacticalAmber
        ),
        WeaponSpec(
            id = WeaponId.CG15,
            name = "CG15",
            category = WeaponCategory.SMG,
            damage = 31,
            fireRateMs = 135L,
            range = 580f,
            bulletSpeed = 1080f,
            spreadRad = 0.04f,
            magSize = 28,
            reloadMs = 1450L,
            armorPen = 78,
            clashCost = 1600,
            description = "Futuristic energy carbine; every 5th shot fires an overcharged piercing bolt.",
            accentColor = ElectricCyan
        ),
        WeaponSpec(
            id = WeaponId.MINI_UZI,
            name = "Mini Uzi",
            category = WeaponCategory.SMG,
            damage = 21,
            fireRateMs = 75L,
            range = 340f,
            bulletSpeed = 880f,
            spreadRad = 0.13f,
            magSize = 32,
            reloadMs = 1150L,
            armorPen = 45,
            clashCost = 850,
            description = "Ultra-high RPM pocket shredder built for close-quarters house clears.",
            accentColor = NanoEmerald
        ),
        WeaponSpec(
            id = WeaponId.MP5,
            name = "MP5",
            category = WeaponCategory.SMG,
            damage = 26,
            fireRateMs = 105L,
            range = 430f,
            bulletSpeed = 920f,
            spreadRad = 0.05f,
            magSize = 30,
            reloadMs = 1250L,
            armorPen = 58,
            clashCost = 1100,
            description = "Laser-stable tactical SMG favored for agile movement and tight recoil.",
            accentColor = ElectricCyan
        ),
        WeaponSpec(
            id = WeaponId.UMP,
            name = "UMP",
            category = WeaponCategory.SMG,
            damage = 30,
            fireRateMs = 120L,
            range = 460f,
            bulletSpeed = 940f,
            spreadRad = 0.06f,
            magSize = 25,
            reloadMs = 1300L,
            armorPen = 84,
            clashCost = 1250,
            description = "Heavy-caliber armor-piercing SMG that melts vests in Clash Squad.",
            accentColor = TacticalAmber
        ),
        WeaponSpec(
            id = WeaponId.WOODPECKER,
            name = "Woodpecker",
            category = WeaponCategory.MARKSMAN,
            damage = 64,
            fireRateMs = 340L,
            range = 760f,
            bulletSpeed = 1260f,
            spreadRad = 0.02f,
            magSize = 12,
            reloadMs = 1750L,
            armorPen = 94,
            clashCost = 1850,
            description = "Precision marksman rifle with lethal armor penetration over open streets.",
            accentColor = PlasmaViolet
        ),
        WeaponSpec(
            id = WeaponId.M7,
            name = "M7",
            category = WeaponCategory.AR,
            damage = 42,
            fireRateMs = 145L,
            range = 520f,
            bulletSpeed = 1000f,
            spreadRad = 0.065f,
            magSize = 30,
            reloadMs = 1550L,
            armorPen = 80,
            clashCost = 1700,
            description = "High-DPS tactical assault rifle engineered for aggressive squad pushes.",
            accentColor = CombatCrimson
        ),
        WeaponSpec(
            id = WeaponId.SNIPER,
            name = "Sniper",
            category = WeaponCategory.SNIPER,
            damage = 118,
            fireRateMs = 950L,
            range = 980f,
            bulletSpeed = 1620f,
            spreadRad = 0.005f,
            magSize = 5,
            reloadMs = 2200L,
            armorPen = 100,
            clashCost = 2300,
            description = "Long-range bolt-action sniper rifle capable of instant knockdowns.",
            accentColor = CombatCrimson
        )
    )

    fun byId(id: WeaponId): WeaponSpec = all.first { it.id == id }
}

enum class VehicleType {
    JEEP,
    TUK_TUK,
    SUPERCAR,
    BIKE
}

data class VehicleSpec(
    val type: VehicleType,
    val name: String,
    val badge: String,
    val maxHp: Int,
    val maxSpeed: Float,
    val ramDamage: Int,
    val seats: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val description: String
)

object VehicleCatalog {
    val all: List<VehicleSpec> = listOf(
        VehicleSpec(
            type = VehicleType.JEEP,
            name = "Jeep",
            badge = "4x4",
            maxHp = 340,
            maxSpeed = 235f,
            ramDamage = 48,
            seats = 4,
            primaryColor = Color(0xFF4D6B53),
            accentColor = TacticalAmber,
            description = "Armored 4-seat tactical off-roader built to carry the whole squad."
        ),
        VehicleSpec(
            type = VehicleType.TUK_TUK,
            name = "Auto-Rickshaw",
            badge = "🛺",
            maxHp = 210,
            maxSpeed = 215f,
            ramDamage = 32,
            seats = 3,
            primaryColor = Color(0xFF2E7D32),
            accentColor = Color(0xFFFFD54F),
            description = "Nimble 3-wheeler with ultra-tight cornering around Clock Tower houses."
        ),
        VehicleSpec(
            type = VehicleType.SUPERCAR,
            name = "Supercar",
            badge = "TURBO",
            maxHp = 250,
            maxSpeed = 355f,
            ramDamage = 65,
            seats = 2,
            primaryColor = CombatCrimson,
            accentColor = ElectricCyan,
            description = "Blazing twin-turbo hypercar that outruns the shrinking Infine zone."
        ),
        VehicleSpec(
            type = VehicleType.BIKE,
            name = "Bike",
            badge = "2W",
            maxHp = 165,
            maxSpeed = 315f,
            ramDamage = 36,
            seats = 2,
            primaryColor = Color(0xFF1E293B),
            accentColor = TacticalAmber,
            description = "High-acceleration street bike that threads narrow gaps between cover."
        )
    )

    fun byType(type: VehicleType): VehicleSpec = all.first { it.type == type }
}

enum class SkillType(val shortLabel: String) {
    HEAL("Heal"),
    REVIVE("Revive"),
    DAMAGE("Damage")
}

data class CharacterOperator(
    val id: String,
    val name: String,
    val roleTitle: String,
    val skillName: String,
    val skillType: SkillType,
    val cooldownSec: Int,
    val accentColor: Color,
    val description: String
)

object OperatorCatalog {
    val all: List<CharacterOperator> = listOf(
        CharacterOperator(
            id = "vespera",
            name = "Vespera",
            roleTitle = "Field Medic",
            skillName = "Nano-Mend Pulse",
            skillType = SkillType.HEAL,
            cooldownSec = 15,
            accentColor = NanoEmerald,
            description = "Emits a 200m healing pulse restoring +50 HP over 4s to you and nearby squadmates."
        ),
        CharacterOperator(
            id = "kairo",
            name = "Kairo",
            roleTitle = "Phoenix Vanguard",
            skillName = "Second Wind",
            skillType = SkillType.REVIVE,
            cooldownSec = 20,
            accentColor = ElectricCyan,
            description = "Instantly self-revives from Knocked state with 65 HP or revives the nearest downed ally."
        ),
        CharacterOperator(
            id = "blaze",
            name = "Blaze",
            roleTitle = "Shock Striker",
            skillName = "Arc Strike",
            skillType = SkillType.DAMAGE,
            cooldownSec = 13,
            accentColor = CombatCrimson,
            description = "Detonates an electric plasma shockwave dealing 68 DMG and slowing nearby enemies."
        )
    )

    fun byId(id: String): CharacterOperator = all.firstOrNull { it.id == id } ?: all.first()
}

data class ArenaLevelSpec(
    val level: Int,
    val name: String,
    val sectorId: String,
    val difficultyLabel: String,
    val botHp: Int,
    val botShield: Int,
    val botDamageScale: Float,
    val botSpeedScale: Float,
    val xpMultiplier: Float,
    val accentColor: Color
)

object ArenaLevelCatalog {
    private val sectorCycle = listOf(
        "clock_tower",
        "factory",
        "association",
        "arena",
        "school",
        "hospital",
        "shop",
        "garden"
    )

    private val sectorTitleCycle = listOf(
        "Clock Tower Siege",
        "Factory Lockdown",
        "Association Elite",
        "Arena Crucible",
        "School Clash",
        "Hospital Breach",
        "Shop Ambush",
        "Garden Ambush"
    )

    private val accentCycle = listOf(
        NanoEmerald,
        ElectricCyan,
        TacticalAmber,
        PlasmaViolet,
        CombatCrimson
    )

    fun byLevel(rawLevel: Int): ArenaLevelSpec {
        val level = rawLevel.coerceAtLeast(1)
        val idx = (level - 1) % sectorCycle.size
        val difficultyLabel = when {
            level == 1 -> "Standard"
            level <= 3 -> "Skilled"
            level <= 6 -> "Tactical"
            level <= 12 -> "Hardcore"
            level <= 25 -> "Grandmaster"
            else -> "Infinite Tier ${(level - 1) / 10 + 1}"
        }
        return ArenaLevelSpec(
            level = level,
            name = "Level $level • ${sectorTitleCycle[idx]}",
            sectorId = sectorCycle[idx],
            difficultyLabel = difficultyLabel,
            botHp = 90 + (level - 1) * 10,
            botShield = (15 + (level - 1) * 8).coerceAtMost(150),
            botDamageScale = (0.35f + (level - 1) * 0.08f).coerceAtMost(1.8f),
            botSpeedScale = (0.88f + (level - 1) * 0.05f).coerceAtMost(1.75f),
            xpMultiplier = 1.0f + (level - 1) * 0.25f,
            accentColor = accentCycle[(level - 1) % accentCycle.size]
        )
    }
}

data class PlayerLevelInfo(
    val level: Int,
    val title: String,
    val currentXpInLevel: Int,
    val xpRequiredForNext: Int,
    val progress: Float
) {
    companion object {
        fun fromTotalXp(totalXp: Int): PlayerLevelInfo {
            var remaining = totalXp.coerceAtLeast(0)
            var level = 1
            var req = 250
            while (remaining >= req) {
                remaining -= req
                level++
                req = 250 + (level - 1) * 75
            }
            val title = when {
                level < 5 -> "Recruit"
                level < 12 -> "Vanguard"
                level < 20 -> "Sharpshooter"
                level < 35 -> "Infine Tactician"
                level < 50 -> "Arena Ace"
                level < 100 -> "Infine Legend"
                else -> "Infinite Mythic"
            }
            return PlayerLevelInfo(
                level = level,
                title = title,
                currentXpInLevel = remaining,
                xpRequiredForNext = req,
                progress = (remaining.toFloat() / req.toFloat()).coerceIn(0f, 1f)
            )
        }
    }
}

data class DailyRewardDay(
    val day: Int,
    val title: String,
    val creditsReward: Int,
    val gemsReward: Int,
    val xpReward: Int,
    val accentColor: Color
)

object DailyRewardCatalog {
    val cycle: List<DailyRewardDay> = listOf(
        DailyRewardDay(1, "Day 1 • Welcome Supply", creditsReward = 500, gemsReward = 15, xpReward = 150, accentColor = NanoEmerald),
        DailyRewardDay(2, "Day 2 • Combat Training", creditsReward = 250, gemsReward = 10, xpReward = 350, accentColor = ElectricCyan),
        DailyRewardDay(3, "Day 3 • Armory Stipend", creditsReward = 800, gemsReward = 25, xpReward = 200, accentColor = TacticalAmber),
        DailyRewardDay(4, "Day 4 • Tactical Surge", creditsReward = 350, gemsReward = 20, xpReward = 550, accentColor = ElectricCyan),
        DailyRewardDay(5, "Day 5 • Clock Tower Cache", creditsReward = 1200, gemsReward = 40, xpReward = 300, accentColor = TacticalAmber),
        DailyRewardDay(6, "Day 6 • Veteran Boost", creditsReward = 500, gemsReward = 35, xpReward = 850, accentColor = PlasmaViolet),
        DailyRewardDay(7, "Day 7 • Infine Jackpot", creditsReward = 2000, gemsReward = 100, xpReward = 1200, accentColor = CombatCrimson)
    )

    fun forStreakDay(streakDayOneBased: Int): DailyRewardDay {
        val idx = ((streakDayOneBased - 1).coerceAtLeast(0)) % cycle.size
        return cycle[idx]
    }
}

enum class WeaponSkinRarity(val label: String, val badgeColor: Color) {
    STANDARD("Standard", Color(0xFF94A3B8)),
    RARE("Rare", TacticalAmber),
    EPIC("Epic", ElectricCyan),
    LEGENDARY("Legendary", CombatCrimson)
}

data class WeaponSkinSpec(
    val id: String,
    val name: String,
    val rarity: WeaponSkinRarity,
    val costCredits: Int,
    val costGems: Int = 0,
    val primaryColor: Color,
    val tracerColor: Color
)

object WeaponSkinCatalog {
    val all: List<WeaponSkinSpec> = listOf(
        WeaponSkinSpec(
            id = "standard",
            name = "Standard",
            rarity = WeaponSkinRarity.STANDARD,
            costCredits = 0,
            primaryColor = Color(0xFF94A3B8),
            tracerColor = TacticalAmber
        ),
        WeaponSkinSpec(
            id = "clock_gold",
            name = "Clock Tower Gold",
            rarity = WeaponSkinRarity.RARE,
            costCredits = 600,
            primaryColor = TacticalAmber,
            tracerColor = Color(0xFFFFD54F)
        ),
        WeaponSkinSpec(
            id = "cyber_cyan",
            name = "Cyber Infine",
            rarity = WeaponSkinRarity.EPIC,
            costCredits = 1100,
            primaryColor = ElectricCyan,
            tracerColor = ElectricCyan
        ),
        WeaponSkinSpec(
            id = "plasma_prism",
            name = "Plasma Prism",
            rarity = WeaponSkinRarity.EPIC,
            costCredits = 0,
            costGems = 45,
            primaryColor = PlasmaViolet,
            tracerColor = Color(0xFFD500F9)
        ),
        WeaponSkinSpec(
            id = "crimson_apex",
            name = "Crimson Apex",
            rarity = WeaponSkinRarity.LEGENDARY,
            costCredits = 1800,
            primaryColor = CombatCrimson,
            tracerColor = Color(0xFFFF5252)
        )
    )

    fun byId(id: String): WeaponSkinSpec = all.firstOrNull { it.id == id } ?: all.first()
}

enum class CosmeticCategory(val label: String) {
    ALL("All"),
    CHARACTER_SKINS("Skins"),
    WEAPON_WRAPS("Wraps"),
    EMOTES("Emotes")
}

data class CharacterSkinSpec(
    val id: String,
    val name: String,
    val rarity: WeaponSkinRarity,
    val costCredits: Int,
    val costGems: Int,
    val suitColor: Color,
    val rimColor: Color,
    val description: String
)

object CharacterSkinCatalog {
    val all: List<CharacterSkinSpec> = listOf(
        CharacterSkinSpec(
            id = "default_vanguard",
            name = "Vanguard Issue",
            rarity = WeaponSkinRarity.STANDARD,
            costCredits = 0,
            costGems = 0,
            suitColor = ElectricCyan,
            rimColor = Color.White,
            description = "Standard-issue Infine Royale combat armor."
        ),
        CharacterSkinSpec(
            id = "neon_phantom",
            name = "Neon Phantom",
            rarity = WeaponSkinRarity.RARE,
            costCredits = 750,
            costGems = 0,
            suitColor = NanoEmerald,
            rimColor = ElectricCyan,
            description = "Stealth bio-weave suit with emerald visor trim."
        ),
        CharacterSkinSpec(
            id = "clockwork_sovereign",
            name = "Clockwork Sovereign",
            rarity = WeaponSkinRarity.EPIC,
            costCredits = 0,
            costGems = 45,
            suitColor = TacticalAmber,
            rimColor = Color(0xFFFFD54F),
            description = "Gilded Clock Tower ceremonial battle armor."
        ),
        CharacterSkinSpec(
            id = "crimson_valkyrie",
            name = "Crimson Valkyrie",
            rarity = WeaponSkinRarity.LEGENDARY,
            costCredits = 1600,
            costGems = 0,
            suitColor = CombatCrimson,
            rimColor = TacticalAmber,
            description = "High-contrast scarlet tactical exo-frame."
        ),
        CharacterSkinSpec(
            id = "void_eclipse",
            name = "Void Eclipse",
            rarity = WeaponSkinRarity.LEGENDARY,
            costCredits = 0,
            costGems = 80,
            suitColor = PlasmaViolet,
            rimColor = ElectricCyan,
            description = "Quantum violet stealth rig forged for Grandmasters."
        )
    )

    fun byId(id: String): CharacterSkinSpec = all.firstOrNull { it.id == id } ?: all.first()
}

data class EmoteSpec(
    val id: String,
    val name: String,
    val emoji: String,
    val shoutText: String,
    val rarity: WeaponSkinRarity,
    val costCredits: Int,
    val costGems: Int,
    val accentColor: Color,
    val description: String
)

object EmoteCatalog {
    val all: List<EmoteSpec> = listOf(
        EmoteSpec(
            id = "tactical_salute",
            name = "Tactical Salute",
            emoji = "🫡",
            shoutText = "READY!",
            rarity = WeaponSkinRarity.STANDARD,
            costCredits = 0,
            costGems = 0,
            accentColor = ElectricCyan,
            description = "Crisp squad salute signaling battle readiness."
        ),
        EmoteSpec(
            id = "squad_gg",
            name = "Victory Peace",
            emoji = "✌️",
            shoutText = "GG SQUAD!",
            rarity = WeaponSkinRarity.STANDARD,
            costCredits = 0,
            costGems = 0,
            accentColor = NanoEmerald,
            description = "Friendly squad peace flash for clean takedowns."
        ),
        EmoteSpec(
            id = "booyah_flare",
            name = "Booyah Flare",
            emoji = "🔥",
            shoutText = "BOOYAH!",
            rarity = WeaponSkinRarity.RARE,
            costCredits = 450,
            costGems = 0,
            accentColor = TacticalAmber,
            description = "Ignites a golden victory shockwave above your operator."
        ),
        EmoteSpec(
            id = "clockwork_flex",
            name = "Clockwork Flex",
            emoji = "⏱️",
            shoutText = "TICK-TOCK!",
            rarity = WeaponSkinRarity.EPIC,
            costCredits = 850,
            costGems = 0,
            accentColor = NanoEmerald,
            description = "Projects a glowing Clock Tower chime pulse in arena."
        ),
        EmoteSpec(
            id = "cyber_holo",
            name = "Cyber Hologram",
            emoji = "💠",
            shoutText = "GG EZ!",
            rarity = WeaponSkinRarity.EPIC,
            costCredits = 0,
            costGems = 30,
            accentColor = ElectricCyan,
            description = "Deploys a neon cyan holographic victory ring."
        ),
        EmoteSpec(
            id = "apex_crown",
            name = "Apex Crown",
            emoji = "👑",
            shoutText = "ROYALTY!",
            rarity = WeaponSkinRarity.LEGENDARY,
            costCredits = 0,
            costGems = 55,
            accentColor = CombatCrimson,
            description = "Radiates a crimson sovereign crown aura."
        )
    )

    fun byId(id: String): EmoteSpec = all.firstOrNull { it.id == id } ?: all.first()
}

enum class LeaderboardSortBy(val label: String) {
    TOTAL_WINS("Total Wins"),
    KD_RATIO("K/D Ratio")
}

data class GlobalLeaderboardRow(
    val rank: Int,
    val callsign: String,
    val regionTag: String,
    val rp: Int,
    val rankTier: RankTier,
    val totalWins: Int,
    val totalKills: Int,
    val totalMatches: Int,
    val deaths: Int,
    val kdRatio: Float,
    val winRatePercent: Int,
    val isLocalPlayer: Boolean,
    val isFriend: Boolean,
    val lastDeltaNote: String
)

enum class ReportReason(val label: String, val description: String) {
    AIMBOT("Aimbot / Auto-Lock", "Unnatural instant snapping or auto-aim tracking"),
    WALLHACK("Wallhack / ESP", "Tracking or pre-firing enemies cleanly through cover"),
    SPEED_HACK("Speed / Movement Hack", "Abnormal sprint, teleport, or vehicle speed bursts"),
    TEAMING_GRIEFING("Teaming / Griefing", "Collaborating with rivals or sabotaging squadmates"),
    TOXIC_VOICE_CHAT("Toxic Voice / Comms", "Abusive voice chat, spam, or harassment")
}

// === CLAN SYSTEM CATALOGS ===

data class ClanBannerSpec(
    val id: String,
    val name: String,
    val unlockLevel: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val emblemTag: String,
    val description: String
)

object ClanBannerCatalog {
    val all: List<ClanBannerSpec> = listOf(
        ClanBannerSpec(
            id = "vanguard_crest",
            name = "Vanguard Crest",
            unlockLevel = 1,
            primaryColor = ElectricCyan,
            accentColor = Color(0xFF0E7490),
            emblemTag = "VGD",
            description = "Standard Infine Clan standard unlocked at Clan Lv.1."
        ),
        ClanBannerSpec(
            id = "clockwork_aegis",
            name = "Clockwork Aegis",
            unlockLevel = 2,
            primaryColor = TacticalAmber,
            accentColor = Color(0xFFD97706),
            emblemTag = "CLK",
            description = "Gilded Clock Tower insignia unlocked at Clan Lv.2."
        ),
        ClanBannerSpec(
            id = "emerald_warden",
            name = "Emerald Warden",
            unlockLevel = 3,
            primaryColor = NanoEmerald,
            accentColor = ElectricCyan,
            emblemTag = "WRD",
            description = "Bio-shielded Botanical Sector crest unlocked at Clan Lv.3."
        ),
        ClanBannerSpec(
            id = "crimson_dominion",
            name = "Crimson Dominion",
            unlockLevel = 4,
            primaryColor = CombatCrimson,
            accentColor = TacticalAmber,
            emblemTag = "DOM",
            description = "Scarlet Arena Warlord banner unlocked at Clan Lv.4."
        ),
        ClanBannerSpec(
            id = "apex_sovereign",
            name = "Apex Sovereign",
            unlockLevel = 5,
            primaryColor = PlasmaViolet,
            accentColor = TacticalAmber,
            emblemTag = "APX",
            description = "Mythic Grandmaster Clan banner unlocked at Clan Lv.5."
        )
    )

    fun byId(id: String): ClanBannerSpec = all.firstOrNull { it.id == id } ?: all.first()
}

data class ClanLevelInfo(
    val level: Int,
    val title: String,
    val currentXpInLevel: Int,
    val xpRequiredForNext: Int,
    val progress: Float
) {
    companion object {
        fun fromClanXp(totalXp: Int): ClanLevelInfo {
            var remaining = totalXp.coerceAtLeast(0)
            var level = 1
            var req = 400
            while (remaining >= req) {
                remaining -= req
                level++
                req = 400 + (level - 1) * 250
            }
            val title = when {
                level == 1 -> "Outpost Squad"
                level == 2 -> "Sector Raiders"
                level == 3 -> "Infine Vanguard"
                level == 4 -> "Warlord Syndicate"
                else -> "Apex Sovereign"
            }
            return ClanLevelInfo(
                level = level,
                title = title,
                currentXpInLevel = remaining,
                xpRequiredForNext = req,
                progress = (remaining.toFloat() / req.toFloat()).coerceIn(0f, 1f)
            )
        }
    }
}

enum class ClanMissionKind {
    ELIMINATIONS,
    MATCHES_PLAYED,
    REVIVES,
    WIN_MATCHES,
    DAMAGE_DEALT
}

data class ClanMissionSpec(
    val id: String,
    val title: String,
    val description: String,
    val kind: ClanMissionKind,
    val targetCount: Int,
    val clanXpReward: Int,
    val goldReward: Int,
    val badgeLabel: String
)

object ClanMissionCatalog {
    val all: List<ClanMissionSpec> = listOf(
        ClanMissionSpec(
            id = "cm_eliminations",
            title = "Clock Tower Sweep",
            description = "Score 6 eliminations in Infine matches while representing your clan.",
            kind = ClanMissionKind.ELIMINATIONS,
            targetCount = 6,
            clanXpReward = 260,
            goldReward = 200,
            badgeLabel = "COMBAT"
        ),
        ClanMissionSpec(
            id = "cm_deployments",
            title = "Clan Frontline",
            description = "Complete 2 ranked deployments (BR Ranked or Clash Squad).",
            kind = ClanMissionKind.MATCHES_PLAYED,
            targetCount = 2,
            clanXpReward = 220,
            goldReward = 160,
            badgeLabel = "DEPLOY"
        ),
        ClanMissionSpec(
            id = "cm_revives",
            title = "Field Rescue Protocol",
            description = "Complete 2 squadmate or self revives during combat.",
            kind = ClanMissionKind.REVIVES,
            targetCount = 2,
            clanXpReward = 240,
            goldReward = 180,
            badgeLabel = "SUPPORT"
        ),
        ClanMissionSpec(
            id = "cm_victory",
            title = "Booyah Banner Strike",
            description = "Win 1 ranked match to raise your clan crest over Infine.",
            kind = ClanMissionKind.WIN_MATCHES,
            targetCount = 1,
            clanXpReward = 360,
            goldReward = 320,
            badgeLabel = "VICTORY"
        ),
        ClanMissionSpec(
            id = "cm_damage",
            title = "Heavy Suppression",
            description = "Deal 450 total damage across Infine sectors.",
            kind = ClanMissionKind.DAMAGE_DEALT,
            targetCount = 450,
            clanXpReward = 280,
            goldReward = 220,
            badgeLabel = "DAMAGE"
        )
    )

    fun byId(id: String): ClanMissionSpec = all.firstOrNull { it.id == id } ?: all.first()
}

// === MATCH REPLAY TELEMETRY ===

data class ReplayActorSnapshot(
    val name: String,
    val isAlly: Boolean,
    val x: Float,
    val y: Float,
    val aimRad: Float,
    val isKnocked: Boolean,
    val isEliminated: Boolean
)

data class ReplayFrameSnapshot(
    val timeSec: Int,
    val playerX: Float,
    val playerY: Float,
    val playerAimRad: Float,
    val playerHp: Int,
    val isKnocked: Boolean,
    val safeZoneX: Float,
    val safeZoneY: Float,
    val safeZoneRadius: Float,
    val aliveCount: Int,
    val kills: Int,
    val eventText: String? = null,
    val actors: List<ReplayActorSnapshot> = emptyList()
)

object MatchReplayCodec {
    fun encode(frames: List<ReplayFrameSnapshot>): String {
        val arr = org.json.JSONArray()
        frames.forEach { f ->
            val obj = org.json.JSONObject().apply {
                put("t", f.timeSec)
                put("px", f.playerX.toInt())
                put("py", f.playerY.toInt())
                put("pa", Math.round(f.playerAimRad * 100.0) / 100.0)
                put("hp", f.playerHp)
                put("kn", f.isKnocked)
                put("zx", f.safeZoneX.toInt())
                put("zy", f.safeZoneY.toInt())
                put("zr", f.safeZoneRadius.toInt())
                put("al", f.aliveCount)
                put("kl", f.kills)
                if (!f.eventText.isNullOrBlank()) put("ev", f.eventText)
                val actArr = org.json.JSONArray()
                f.actors.forEach { a ->
                    actArr.put(
                        org.json.JSONObject().apply {
                            put("n", a.name)
                            put("all", a.isAlly)
                            put("x", a.x.toInt())
                            put("y", a.y.toInt())
                            put("r", Math.round(a.aimRad * 100.0) / 100.0)
                            put("k", a.isKnocked)
                            put("e", a.isEliminated)
                        }
                    )
                }
                put("ac", actArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decode(json: String): List<ReplayFrameSnapshot> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = org.json.JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val actArr = o.optJSONArray("ac") ?: org.json.JSONArray()
                    val actors = buildList {
                        for (j in 0 until actArr.length()) {
                            val ao = actArr.getJSONObject(j)
                            add(
                                ReplayActorSnapshot(
                                    name = ao.optString("n", "Bot"),
                                    isAlly = ao.optBoolean("all", false),
                                    x = ao.optDouble("x", 1200.0).toFloat(),
                                    y = ao.optDouble("y", 1200.0).toFloat(),
                                    aimRad = ao.optDouble("r", 0.0).toFloat(),
                                    isKnocked = ao.optBoolean("k", false),
                                    isEliminated = ao.optBoolean("e", false)
                                )
                            )
                        }
                    }
                    add(
                        ReplayFrameSnapshot(
                            timeSec = o.optInt("t", i),
                            playerX = o.optDouble("px", 1200.0).toFloat(),
                            playerY = o.optDouble("py", 1200.0).toFloat(),
                            playerAimRad = o.optDouble("pa", 0.0).toFloat(),
                            playerHp = o.optInt("hp", 100),
                            isKnocked = o.optBoolean("kn", false),
                            safeZoneX = o.optDouble("zx", 1200.0).toFloat(),
                            safeZoneY = o.optDouble("zy", 1200.0).toFloat(),
                            safeZoneRadius = o.optDouble("zr", 1180.0).toFloat(),
                            aliveCount = o.optInt("al", 1),
                            kills = o.optInt("kl", 0),
                            eventText = o.optString("ev").takeIf { it.isNotBlank() },
                            actors = actors
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}

// === ROBO HONOR SENTINEL POLICY ===

enum class HonorTier(val label: String, val badgeColor: Color) {
    EXEMPLARY("CLEARED FOR RANKED", NanoEmerald),
    CAUTION("RANKED LOCKED", TacticalAmber),
    RESTRICTED("FAIR-PLAY ALERT", CombatCrimson)
}

object RoboHonorPolicy {
    const val MAX_HONOR = 100
    const val MIN_RANKED_HONOR = 90
    const val PENALTY_RANKED_QUIT = 12
    const val PENALTY_CASUAL_QUIT = 6
    const val GAIN_CASUAL_FINISH = 8
    const val GAIN_TRAINING_DRILL = 5
    const val GAIN_RANKED_CLEAN = 3

    fun canPlayRanked(honorScore: Int): Boolean = honorScore >= MIN_RANKED_HONOR

    fun tierFor(honorScore: Int): HonorTier = when {
        honorScore >= MIN_RANKED_HONOR -> HonorTier.EXEMPLARY
        honorScore >= 70 -> HonorTier.CAUTION
        else -> HonorTier.RESTRICTED
    }

    fun statusAdvice(honorScore: Int): String = if (honorScore >= MIN_RANKED_HONOR) {
        "Robo-Sentinel verified your fair play. Ranked BR & Clash Squad are unlocked."
    } else {
        val needed = MIN_RANKED_HONOR - honorScore
        "Honor is $honorScore/100 ($needed pts below Ranked minimum 90). Play Infine Casual (+8) or Training Drill (+5) to restore score!"
    }
}

// === DYNAMIC WEATHER SYSTEM ===

enum class WeatherCondition(
    val emoji: String,
    val label: String,
    val effectSummary: String,
    val accentColor: Color,
    val visibilityRadiusPx: Float,
    val botSightRange: Float,
    val spreadMultiplier: Float,
    val speedMultiplier: Float,
    val radarClarityRange: Float
) {
    CLEAR(
        emoji = "☀️",
        label = "Clear Skies",
        effectSummary = "100% Visibility",
        accentColor = NanoEmerald,
        visibilityRadiusPx = 0f,
        botSightRange = 480f,
        spreadMultiplier = 1.0f,
        speedMultiplier = 1.0f,
        radarClarityRange = 3000f
    ),
    FOG(
        emoji = "🌫️",
        label = "Dense Fog",
        effectSummary = "Low Sight • Stealth",
        accentColor = Color(0xFFCBD5E1),
        visibilityRadiusPx = 280f,
        botSightRange = 290f,
        spreadMultiplier = 1.08f,
        speedMultiplier = 0.96f,
        radarClarityRange = 460f
    ),
    RAIN(
        emoji = "🌧️",
        label = "Monsoon Rain",
        effectSummary = "Slick +5% Spd • +15% Drift",
        accentColor = ElectricCyan,
        visibilityRadiusPx = 420f,
        botSightRange = 400f,
        spreadMultiplier = 1.15f,
        speedMultiplier = 1.05f,
        radarClarityRange = 760f
    ),
    STORM(
        emoji = "⛈️",
        label = "Thunderstorm",
        effectSummary = "Wind -10% Spd • Lightning",
        accentColor = PlasmaViolet,
        visibilityRadiusPx = 330f,
        botSightRange = 325f,
        spreadMultiplier = 1.24f,
        speedMultiplier = 0.90f,
        radarClarityRange = 540f
    );

    fun nextCycle(): WeatherCondition = when (this) {
        CLEAR -> FOG
        FOG -> RAIN
        RAIN -> STORM
        STORM -> CLEAR
    }
}





