package com.example.data

import com.example.data.local.ClanEntity
import com.example.data.local.ClanMissionProgressEntity
import com.example.data.local.CurrencyTransactionEntity
import com.example.data.local.FriendEntity
import com.example.data.local.GameDao
import com.example.data.local.IncidentReportEntity
import com.example.data.local.LeaderboardEntryEntity
import com.example.data.local.MatchHistoryEntity
import com.example.data.local.MatchReplayEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.WeaponSkinStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.random.Random

class GameRepository(private val dao: GameDao) {

    val profileFlow: Flow<PlayerProfileEntity> = dao.observeProfile().map { entity ->
        entity ?: PlayerProfileEntity()
    }

    val matchHistoryFlow: Flow<List<MatchHistoryEntity>> = dao.observeMatchHistory()

    val friendsFlow: Flow<List<FriendEntity>> = dao.observeFriends()

    val leaderboardEntriesFlow: Flow<List<LeaderboardEntryEntity>> = dao.observeLeaderboardEntries()

    val currencyTransactionsFlow: Flow<List<CurrencyTransactionEntity>> = dao.observeCurrencyTransactions()

    val incidentReportsFlow: Flow<List<IncidentReportEntity>> = dao.observeIncidentReports()

    val clansFlow: Flow<List<ClanEntity>> = dao.observeClans()

    val clanMissionsFlow: Flow<List<ClanMissionProgressEntity>> = dao.observeClanMissions()

    val matchReplaysFlow: Flow<List<MatchReplayEntity>> = dao.observeMatchReplays()

    val weaponSkinsFlow: Flow<Map<WeaponId, WeaponSkinStateEntity>> = dao.observeWeaponSkins().map { list ->
        val byName = list.associateBy { it.weaponName }
        WeaponId.entries.associateWith { id ->
            byName[id.name] ?: WeaponSkinStateEntity(weaponName = id.name)
        }
    }

    suspend fun ensureInitialized() {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(PlayerProfileEntity())
        }
        WeaponId.entries.forEach { weaponId ->
            if (dao.getWeaponSkinOnce(weaponId.name) == null) {
                dao.upsertWeaponSkin(WeaponSkinStateEntity(weaponName = weaponId.name))
            }
        }
        if (dao.getLeaderboardCount() == 0) {
            dao.upsertLeaderboardEntries(defaultGlobalCompetitors())
        }
        if (dao.getClansOnce().isEmpty()) {
            dao.upsertClans(defaultPresetClans())
        }
    }

    private fun defaultPresetClans(): List<ClanEntity> = listOf(
        ClanEntity(
            name = "Infine Phantoms",
            tag = "PHNT",
            motto = "Own the Clock Tower. Leave no squadmate behind.",
            leaderCallsign = "Kira_INF",
            isJoined = false,
            isPlayerCreated = false,
            memberCount = 4,
            clanXp = 480,
            equippedBannerId = "clockwork_aegis",
            membersCsv = "Kira_INF,AuraMend,ZedRush,NovaSniper"
        ),
        ClanEntity(
            name = "Apex Vanguard",
            tag = "APEX",
            motto = "Precision rotations and Booyah supremacy.",
            leaderCallsign = "VortexX",
            isJoined = false,
            isPlayerCreated = false,
            memberCount = 3,
            clanXp = 1250,
            equippedBannerId = "emerald_warden",
            membersCsv = "VortexX,ShadowAK,TitanMP5"
        ),
        ClanEntity(
            name = "Crimson Wolves",
            tag = "WOLF",
            motto = "Aggressive Clash Squad pushers across all sectors.",
            leaderCallsign = "Reaper77",
            isJoined = false,
            isPlayerCreated = false,
            memberCount = 3,
            clanXp = 220,
            equippedBannerId = "vanguard_crest",
            membersCsv = "Reaper77,PyroM7,BlazeOps"
        ),
        ClanEntity(
            name = "Botanical Wardens",
            tag = "WRDN",
            motto = "Revival Point specialists & endgame survivors.",
            leaderCallsign = "Falcon_CS",
            isJoined = false,
            isPlayerCreated = false,
            memberCount = 2,
            clanXp = 860,
            equippedBannerId = "clockwork_aegis",
            membersCsv = "Falcon_CS,PulseCG"
        )
    )

    private fun defaultGlobalCompetitors(): List<LeaderboardEntryEntity> = listOf(
        LeaderboardEntryEntity("Kira_INF", "APAC", rp = 4120, totalWins = 34, totalKills = 182, totalMatches = 72),
        LeaderboardEntryEntity("VortexX", "NA", rp = 3910, totalWins = 31, totalKills = 155, totalMatches = 68),
        LeaderboardEntryEntity("NovaSniper", "EU", rp = 3680, totalWins = 26, totalKills = 168, totalMatches = 70),
        LeaderboardEntryEntity("ShadowAK", "APAC", rp = 3340, totalWins = 22, totalKills = 129, totalMatches = 64),
        LeaderboardEntryEntity("Reaper77", "SA", rp = 3120, totalWins = 19, totalKills = 118, totalMatches = 61),
        LeaderboardEntryEntity("BlazeOps", "EU", rp = 2890, totalWins = 15, totalKills = 96, totalMatches = 52),
        LeaderboardEntryEntity("TitanMP5", "NA", rp = 2640, totalWins = 12, totalKills = 84, totalMatches = 48),
        LeaderboardEntryEntity("Falcon_CS", "MENA", rp = 2410, totalWins = 9, totalKills = 69, totalMatches = 42),
        LeaderboardEntryEntity("AuraMend", "APAC", rp = 2180, totalWins = 7, totalKills = 52, totalMatches = 38),
        LeaderboardEntryEntity("PulseCG", "EU", rp = 2020, totalWins = 5, totalKills = 44, totalMatches = 34),
        LeaderboardEntryEntity("ZedRush", "NA", rp = 1810, totalWins = 3, totalKills = 31, totalMatches = 26),
        LeaderboardEntryEntity("DriftKing", "SA", rp = 1640, totalWins = 2, totalKills = 24, totalMatches = 24),
        LeaderboardEntryEntity("PyroM7", "MENA", rp = 1490, totalWins = 1, totalKills = 16, totalMatches = 19),
        LeaderboardEntryEntity("EchoUzi", "NA", rp = 1320, totalWins = 1, totalKills = 11, totalMatches = 16),
        LeaderboardEntryEntity("Rook_09", "EU", rp = 1180, totalWins = 0, totalKills = 7, totalMatches = 12)
    )

    fun currentLocalDayIndex(): Long {
        val cal = Calendar.getInstance()
        return (cal.get(Calendar.YEAR) * 366L) + cal.get(Calendar.DAY_OF_YEAR)
    }

    suspend fun canClaimDailyReward(): Boolean {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        return current.lastDailyClaimDay != currentLocalDayIndex()
    }

    suspend fun claimDailyReward(): DailyRewardDay? {
        val today = currentLocalDayIndex()
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (current.lastDailyClaimDay == today) return null

        val nextStreak = if (current.lastDailyClaimDay == today - 1L) {
            current.loginStreak + 1
        } else if (current.loginStreak > 0 && current.lastDailyClaimDay >= 0L) {
            // Keep progression moving forward cleanly in 7-day cycle
            current.loginStreak + 1
        } else {
            1
        }

        val reward = DailyRewardCatalog.forStreakDay(nextStreak)
        dao.upsertProfile(
            current.copy(
                credits = current.credits + reward.creditsReward,
                gems = current.gems + reward.gemsReward,
                totalXp = current.totalXp + reward.xpReward,
                loginStreak = nextStreak,
                lastDailyClaimDay = today
            )
        )
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(
                title = "Daily Reward • Day ${reward.day}",
                goldDelta = reward.creditsReward,
                gemsDelta = reward.gemsReward
            )
        )
        return reward
    }

    suspend fun addFriend(callsign: String, rankLabel: String): Boolean {
        val cleaned = callsign.trim().take(16)
        if (cleaned.isBlank()) return false
        val existing = dao.getFriendByCallsign(cleaned)
        if (existing != null) return false
        dao.upsertFriend(
            FriendEntity(
                callsign = cleaned,
                rankLabel = rankLabel,
                isInSquad = false,
                isOnline = true
            )
        )
        val existingBoard = dao.getLeaderboardEntryByCallsign(cleaned)
        if (existingBoard != null) {
            dao.upsertLeaderboardEntry(existingBoard.copy(isFriend = true))
        } else {
            val tier = RankTier.entries.firstOrNull { it.label.equals(rankLabel, ignoreCase = true) } ?: RankTier.GOLD
            val seedMatches = when (tier) {
                RankTier.SILVER -> 18
                RankTier.GOLD -> 28
                RankTier.PLATINUM -> 39
                RankTier.DIAMOND -> 50
                RankTier.HEROIC -> 62
                RankTier.GRANDMASTER -> 74
                RankTier.BRONZE -> 10
            }
            val seedWins = (seedMatches * 0.22f).toInt().coerceAtLeast(1)
            val seedKills = (seedMatches * 1.45f).toInt()
            dao.upsertLeaderboardEntry(
                LeaderboardEntryEntity(
                    callsign = cleaned,
                    regionTag = "SQD",
                    rp = tier.minRp + 120,
                    totalWins = seedWins,
                    totalKills = seedKills,
                    totalMatches = seedMatches,
                    isFriend = true,
                    lastDeltaNote = "Added Friend"
                )
            )
        }
        return true
    }

    suspend fun toggleFriendSquad(friend: FriendEntity) {
        dao.setFriendInSquad(friend.id, !friend.isInSquad)
    }

    suspend fun removeFriend(friendId: Int) {
        dao.deleteFriendById(friendId)
    }

    suspend fun unlockOrEquipSkin(weaponId: WeaponId, skinId: String): Pair<Boolean, String> {
        val skinSpec = WeaponSkinCatalog.byId(skinId)
        val state = dao.getWeaponSkinOnce(weaponId.name) ?: WeaponSkinStateEntity(weaponId.name)
        val unlocked = state.unlockedSkinIdsCsv.split(",").filter { it.isNotBlank() }.toMutableSet()

        if (skinId in unlocked || (skinSpec.costCredits == 0 && skinSpec.costGems == 0)) {
            dao.upsertWeaponSkin(state.copy(equippedSkinId = skinId))
            return true to "Equipped ${skinSpec.name} on ${WeaponCatalog.byId(weaponId).name}!"
        }

        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (skinSpec.costGems > 0) {
            if (profile.gems < skinSpec.costGems) {
                return false to "Need ${skinSpec.costGems} Gems to unlock ${skinSpec.name}."
            }
            dao.upsertProfile(profile.copy(gems = profile.gems - skinSpec.costGems))
        } else {
            if (profile.credits < skinSpec.costCredits) {
                return false to "Need ${skinSpec.costCredits} Gold to unlock ${skinSpec.name}."
            }
            dao.upsertProfile(profile.copy(credits = profile.credits - skinSpec.costCredits))
        }

        unlocked.add(skinId)
        dao.upsertWeaponSkin(
            state.copy(
                equippedSkinId = skinId,
                unlockedSkinIdsCsv = unlocked.joinToString(",")
            )
        )
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(
                title = "${WeaponCatalog.byId(weaponId).name} • ${skinSpec.name}",
                goldDelta = -skinSpec.costCredits,
                gemsDelta = -skinSpec.costGems
            )
        )
        return true to "Unlocked & equipped ${skinSpec.name} on ${WeaponCatalog.byId(weaponId).name}!"
    }

    suspend fun unlockOrEquipCharacterSkin(skinId: String): Pair<Boolean, String> {
        val skin = CharacterSkinCatalog.byId(skinId)
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val unlocked = profile.unlockedCharacterSkinsCsv.split(",").filter { it.isNotBlank() }.toMutableSet()

        if (skinId in unlocked || (skin.costCredits == 0 && skin.costGems == 0)) {
            dao.upsertProfile(profile.copy(equippedCharacterSkinId = skinId))
            return true to "Equipped ${skin.name} character skin!"
        }

        if (skin.costGems > 0) {
            if (profile.gems < skin.costGems) {
                return false to "Need ${skin.costGems} Gems to unlock ${skin.name}."
            }
            unlocked.add(skinId)
            dao.upsertProfile(
                profile.copy(
                    gems = profile.gems - skin.costGems,
                    equippedCharacterSkinId = skinId,
                    unlockedCharacterSkinsCsv = unlocked.joinToString(",")
                )
            )
        } else {
            if (profile.credits < skin.costCredits) {
                return false to "Need ${skin.costCredits} Gold to unlock ${skin.name}."
            }
            unlocked.add(skinId)
            dao.upsertProfile(
                profile.copy(
                    credits = profile.credits - skin.costCredits,
                    equippedCharacterSkinId = skinId,
                    unlockedCharacterSkinsCsv = unlocked.joinToString(",")
                )
            )
        }

        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(
                title = "Skin • ${skin.name}",
                goldDelta = -skin.costCredits,
                gemsDelta = -skin.costGems
            )
        )
        return true to "Unlocked & equipped ${skin.name}!"
    }

    suspend fun unlockOrEquipEmote(emoteId: String): Pair<Boolean, String> {
        val emote = EmoteCatalog.byId(emoteId)
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val unlocked = profile.unlockedEmotesCsv.split(",").filter { it.isNotBlank() }.toMutableSet()

        if (emoteId in unlocked || (emote.costCredits == 0 && emote.costGems == 0)) {
            dao.upsertProfile(profile.copy(equippedEmoteId = emoteId))
            return true to "Equipped ${emote.name} emote!"
        }

        if (emote.costGems > 0) {
            if (profile.gems < emote.costGems) {
                return false to "Need ${emote.costGems} Gems to unlock ${emote.name}."
            }
            unlocked.add(emoteId)
            dao.upsertProfile(
                profile.copy(
                    gems = profile.gems - emote.costGems,
                    equippedEmoteId = emoteId,
                    unlockedEmotesCsv = unlocked.joinToString(",")
                )
            )
        } else {
            if (profile.credits < emote.costCredits) {
                return false to "Need ${emote.costCredits} Gold to unlock ${emote.name}."
            }
            unlocked.add(emoteId)
            dao.upsertProfile(
                profile.copy(
                    credits = profile.credits - emote.costCredits,
                    equippedEmoteId = emoteId,
                    unlockedEmotesCsv = unlocked.joinToString(",")
                )
            )
        }

        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(
                title = "Emote • ${emote.name}",
                goldDelta = -emote.costCredits,
                gemsDelta = -emote.costGems
            )
        )
        return true to "Unlocked & equipped ${emote.name} emote!"
    }

    suspend fun buyDropMedkitSupply(): Pair<Boolean, String> {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (profile.bonusMedkitDrops >= 4) {
            return false to "Drop Medkit pack already maxed (+4 Bonus Medkits ready)!"
        }
        if (profile.credits < 250) {
            return false to "Need 250 Gold for Field Medkit Supply."
        }
        dao.upsertProfile(
            profile.copy(
                credits = profile.credits - 250,
                bonusMedkitDrops = (profile.bonusMedkitDrops + 2).coerceAtMost(4)
            )
        )
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(title = "Drop Supply: +2 Medkits", goldDelta = -250, gemsDelta = 0)
        )
        return true to "Equipped +2 Bonus Medkits for your next ranked drop!"
    }

    suspend fun buyDropShieldSupply(): Pair<Boolean, String> {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (profile.bonusShieldDrops >= 25) {
            return false to "Kevlar Armor Plate already active (+25 Bonus Shield ready)!"
        }
        if (profile.credits < 350) {
            return false to "Need 350 Gold for Kevlar Armor Plate."
        }
        dao.upsertProfile(
            profile.copy(
                credits = profile.credits - 350,
                bonusShieldDrops = 25
            )
        )
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(title = "Drop Supply: +25 Armor", goldDelta = -350, gemsDelta = 0)
        )
        return true to "Equipped +25 Starting Armor for your next ranked drop!"
    }

    suspend fun exchangeGoldForGems(): Pair<Boolean, String> {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (profile.credits < 1000) {
            return false to "Need 1,000 Gold to mint 35 Infine Gems."
        }
        dao.upsertProfile(
            profile.copy(
                credits = profile.credits - 1000,
                gems = profile.gems + 35
            )
        )
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(title = "Vault Exchange: Gold → Gems", goldDelta = -1000, gemsDelta = 35)
        )
        return true to "Converted 1,000 Gold into +35 Infine Gems!"
    }

    suspend fun exchangeGemsForGold(): Pair<Boolean, String> {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (profile.gems < 25) {
            return false to "Need 25 Infine Gems to exchange for 450 Gold."
        }
        dao.upsertProfile(
            profile.copy(
                credits = profile.credits + 450,
                gems = profile.gems - 25
            )
        )
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(title = "Vault Exchange: Gems → Gold", goldDelta = 450, gemsDelta = -25)
        )
        return true to "Converted 25 Gems into +450 Infine Gold!"
    }

    suspend fun updateCallsign(callsign: String) {
        val cleaned = callsign.trim().take(16).ifBlank { "APEX-01" }
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(current.copy(callsign = cleaned))
    }

    suspend fun equipWeapon(weaponId: WeaponId) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(current.copy(equippedWeaponName = weaponId.name))
    }

    suspend fun equipOperator(operatorId: String) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(current.copy(equippedOperatorId = operatorId))
    }

    suspend fun selectMode(mode: GameMode, squadMode: SquadMode) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(
            current.copy(
                preferredModeName = mode.name,
                preferredSquadName = squadMode.name
            )
        )
    }

    suspend fun selectArenaLevel(level: Int) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(current.copy(selectedArenaLevel = level.coerceAtLeast(1)))
    }

    suspend fun recordMatchResult(
        mode: GameMode,
        squadMode: SquadMode,
        arenaLevel: Int,
        won: Boolean,
        placementRank: Int,
        kills: Int,
        revives: Int,
        damageDealt: Int,
        rpDelta: Int,
        xpEarned: Int,
        goldEarned: Int,
        gemsEarned: Int,
        lastLocation: String,
        weaponName: String,
        abandonedEarly: Boolean = false
    ) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        val newBrRp = if (mode == GameMode.BATTLE_ROYALE_RANKED) {
            (current.brRp + rpDelta).coerceAtLeast(1000)
        } else {
            current.brRp
        }
        val newCsRp = if (mode == GameMode.CLASH_SQUAD_RANKED) {
            (current.csRp + rpDelta).coerceAtLeast(1000)
        } else {
            current.csRp
        }
        val honorDelta = when {
            abandonedEarly && mode.isRanked -> -RoboHonorPolicy.PENALTY_RANKED_QUIT
            abandonedEarly && mode == GameMode.BATTLE_ROYALE_CASUAL -> -RoboHonorPolicy.PENALTY_CASUAL_QUIT
            abandonedEarly -> 0
            mode == GameMode.BATTLE_ROYALE_CASUAL -> RoboHonorPolicy.GAIN_CASUAL_FINISH
            mode == GameMode.TRAINING_GROUND -> RoboHonorPolicy.GAIN_TRAINING_DRILL
            else -> RoboHonorPolicy.GAIN_RANKED_CLEAN
        }
        val newHonor = (current.honorScore + honorDelta).coerceIn(0, RoboHonorPolicy.MAX_HONOR)
        val honorNotice = when {
            honorDelta < 0 -> "Robo Penalty $honorDelta Honor (Early Exit) -> $newHonor/100"
            honorDelta > 0 && newHonor > current.honorScore -> "Robo +$honorDelta Honor (${mode.shortTitle}) -> $newHonor/100"
            honorDelta > 0 -> "Robo Verified Clean Match -> $newHonor/100"
            else -> current.lastHonorNotice
        }
        val nextPreferredMode = if (!RoboHonorPolicy.canPlayRanked(newHonor) && mode.isRanked) {
            GameMode.BATTLE_ROYALE_CASUAL.name
        } else {
            current.preferredModeName
        }

        dao.upsertProfile(
            current.copy(
                brRp = newBrRp,
                csRp = newCsRp,
                totalXp = current.totalXp + xpEarned,
                credits = current.credits + goldEarned,
                gems = current.gems + gemsEarned,
                bonusMedkitDrops = if (mode.isRanked) 0 else current.bonusMedkitDrops,
                bonusShieldDrops = if (mode.isRanked) 0 else current.bonusShieldDrops,
                totalKills = current.totalKills + kills,
                totalWins = current.totalWins + if (won) 1 else 0,
                totalMatches = current.totalMatches + if (mode.isRanked) 1 else 0,
                totalRevives = current.totalRevives + revives,
                honorScore = newHonor,
                lastHonorNotice = honorNotice,
                preferredModeName = nextPreferredMode
            )
        )
        if (goldEarned > 0 || gemsEarned > 0) {
            dao.insertCurrencyTransaction(
                CurrencyTransactionEntity(
                    title = "${mode.shortTitle} Match Reward",
                    goldDelta = goldEarned,
                    gemsDelta = gemsEarned
                )
            )
        }

        val placementText = when (mode) {
            GameMode.BATTLE_ROYALE_RANKED -> if (abandonedEarly) "LEFT EARLY" else if (won) "BOOYAH #1" else "#$placementRank"
            GameMode.CLASH_SQUAD_RANKED -> if (abandonedEarly) "LEFT EARLY" else if (won) "VICTORY" else "DEFEAT"
            GameMode.BATTLE_ROYALE_CASUAL -> if (abandonedEarly) "LEFT EARLY" else if (won) "CASUAL WIN" else "CASUAL #$placementRank"
            GameMode.TRAINING_GROUND -> "TRAINING"
        }

        dao.insertMatchHistory(
            MatchHistoryEntity(
                modeName = mode.shortTitle,
                squadName = if (mode == GameMode.BATTLE_ROYALE_RANKED || mode == GameMode.BATTLE_ROYALE_CASUAL) squadMode.label else if (mode == GameMode.CLASH_SQUAD_RANKED) "4v4" else "Sandbox",
                placementText = placementText,
                kills = kills,
                revives = revives,
                damageDealt = damageDealt,
                rpDelta = rpDelta,
                xpEarned = xpEarned,
                arenaLevel = arenaLevel,
                mapLocation = lastLocation,
                weaponUsed = weaponName
            )
        )

        if (mode.isRanked) {
            advanceGlobalLeaderboardRivals()
        }

        // Advance active Clan XP & Clan-Only Missions
        val joinedClan = dao.getJoinedClanOnce()
        if (joinedClan != null) {
            val matchClanXp = ((if (won) 110 else 45) + kills * 18 + revives * 20).coerceAtLeast(30)
            // Squadmates also contribute shared Clan XP
            val squadBonusXp = (joinedClan.memberCount - 1).coerceAtLeast(0) * 22
            val newClanXp = joinedClan.clanXp + matchClanXp + squadBonusXp
            val oldLevel = ClanLevelInfo.fromClanXp(joinedClan.clanXp).level
            val newLevel = ClanLevelInfo.fromClanXp(newClanXp).level
            var bannerId = joinedClan.equippedBannerId
            if (newLevel > oldLevel) {
                val newlyUnlocked = ClanBannerCatalog.all.lastOrNull { it.unlockLevel <= newLevel }
                if (newlyUnlocked != null && newlyUnlocked.unlockLevel > ClanBannerCatalog.byId(bannerId).unlockLevel) {
                    bannerId = newlyUnlocked.id
                }
            }
            dao.upsertClan(joinedClan.copy(clanXp = newClanXp, equippedBannerId = bannerId))

            val existingMissions = dao.getClanMissionsOnce().ifEmpty {
                ClanMissionCatalog.all.map { ClanMissionProgressEntity(missionId = it.id, clanId = joinedClan.id) }
            }
            val updatedMissions = existingMissions.map { prog ->
                val spec = ClanMissionCatalog.byId(prog.missionId)
                val delta = when (spec.kind) {
                    ClanMissionKind.ELIMINATIONS -> kills
                    ClanMissionKind.MATCHES_PLAYED -> if (mode.isRanked) 1 else 0
                    ClanMissionKind.REVIVES -> revives
                    ClanMissionKind.WIN_MATCHES -> if (won && mode.isRanked) 1 else 0
                    ClanMissionKind.DAMAGE_DEALT -> damageDealt
                }
                prog.copy(progressCount = (prog.progressCount + delta).coerceAtMost(spec.targetCount))
            }
            dao.upsertClanMissions(updatedMissions)
        }
    }

    suspend fun submitIncidentReport(
        reportedCallsign: String,
        reason: ReportReason,
        details: String,
        modeName: String,
        arenaLevel: Int,
        mapLocation: String,
        phaseLabel: String
    ): IncidentReportEntity {
        val cleanedTarget = reportedCallsign.trim().take(24).ifBlank { "Unknown Player" }
        val cleanedDetails = details.trim().take(220)
        val report = IncidentReportEntity(
            reportedCallsign = cleanedTarget,
            reasonLabel = reason.label,
            details = cleanedDetails,
            modeName = modeName,
            arenaLevel = arenaLevel,
            mapLocation = mapLocation,
            phaseLabel = phaseLabel,
            statusLabel = "Logged to DB"
        )
        dao.insertIncidentReport(report)
        return report
    }

    suspend fun completeTrainingDrill(targetsHit: Int, durationSec: Int): String {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        val gain = RoboHonorPolicy.GAIN_TRAINING_DRILL
        val newHonor = (current.honorScore + gain).coerceAtMost(RoboHonorPolicy.MAX_HONOR)
        val bonusGold = 45 + (targetsHit.coerceAtMost(10) * 5)
        val bonusXp = 25 + targetsHit * 8
        val notice = if (newHonor > current.honorScore) {
            "Robo +${newHonor - current.honorScore} Honor (Training Drill) -> $newHonor/100"
        } else {
            "Training Drill Complete -> Honor $newHonor/100"
        }
        dao.upsertProfile(
            current.copy(
                honorScore = newHonor,
                lastHonorNotice = notice,
                credits = current.credits + bonusGold,
                totalXp = current.totalXp + bonusXp
            )
        )
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(
                title = "Training Drill (+$gain Honor)",
                goldDelta = bonusGold,
                gemsDelta = 0
            )
        )
        return notice
    }

    suspend fun saveMatchReplay(
        summary: com.example.ui.match.MatchEndSummary,
        frames: List<ReplayFrameSnapshot>
    ): Long {
        if (frames.isEmpty()) return -1L
        val entity = MatchReplayEntity(
            modeName = summary.mode.shortTitle,
            squadName = if (summary.mode == GameMode.BATTLE_ROYALE_RANKED || summary.mode == GameMode.BATTLE_ROYALE_CASUAL) summary.squadMode.label else if (summary.mode == GameMode.CLASH_SQUAD_RANKED) "4v4" else "Sandbox",
            arenaLevel = summary.arenaLevel,
            won = summary.won,
            placementRank = summary.placementRank,
            kills = summary.kills,
            durationSeconds = summary.durationSeconds,
            mapLocation = summary.lastLocation,
            weaponUsed = WeaponCatalog.byId(summary.weaponUsed).name,
            framesJson = MatchReplayCodec.encode(frames)
        )
        return dao.insertMatchReplay(entity)
    }

    suspend fun createClan(name: String, tag: String, motto: String): Pair<Boolean, String> {
        val cleanedName = name.trim().take(22)
        val cleanedTag = tag.trim().uppercase().filter { it.isLetterOrDigit() }.take(5)
        if (cleanedName.length < 3) return false to "Clan name must be at least 3 characters."
        if (cleanedTag.length < 2) return false to "Clan tag must be 2–5 letters/numbers."

        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val prevJoined = dao.getJoinedClanOnce()
        if (prevJoined != null) {
            val cleanedMembers = prevJoined.membersCsv.split(",").filter {
                it.isNotBlank() && !it.equals(profile.callsign, ignoreCase = true)
            }
            dao.upsertClan(
                prevJoined.copy(
                    isJoined = false,
                    memberCount = cleanedMembers.size.coerceAtLeast(1),
                    membersCsv = cleanedMembers.joinToString(",")
                )
            )
        }
        dao.clearJoinedClans()

        val newClan = ClanEntity(
            name = cleanedName,
            tag = cleanedTag,
            motto = motto.trim().take(60).ifBlank { "Squad up and dominate Infine." },
            leaderCallsign = profile.callsign,
            isJoined = true,
            isPlayerCreated = true,
            memberCount = 1,
            clanXp = 0,
            equippedBannerId = "vanguard_crest",
            membersCsv = profile.callsign
        )
        val newId = dao.upsertClan(newClan).toInt()
        seedClanMissions(newId)
        return true to "Created & joined [$cleanedTag] $cleanedName!"
    }

    suspend fun joinClan(clanId: Int): Pair<Boolean, String> {
        val target = dao.getClanByIdOnce(clanId) ?: return false to "Clan not found."
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (target.isJoined) return true to "Already active in [${target.tag}] ${target.name}."

        val prevJoined = dao.getJoinedClanOnce()
        if (prevJoined != null && prevJoined.id != clanId) {
            val cleaned = prevJoined.membersCsv.split(",").filter {
                it.isNotBlank() && !it.equals(profile.callsign, ignoreCase = true)
            }
            dao.upsertClan(
                prevJoined.copy(
                    isJoined = false,
                    memberCount = cleaned.size.coerceAtLeast(1),
                    membersCsv = cleaned.joinToString(",")
                )
            )
        }
        dao.clearJoinedClans()

        val members = target.membersCsv.split(",").filter { it.isNotBlank() }.toMutableList()
        if (members.none { it.equals(profile.callsign, ignoreCase = true) }) {
            members.add(0, profile.callsign)
        }
        dao.upsertClan(
            target.copy(
                isJoined = true,
                memberCount = members.size,
                membersCsv = members.joinToString(",")
            )
        )
        seedClanMissions(target.id)
        return true to "Joined [${target.tag}] ${target.name}!"
    }

    suspend fun leaveCurrentClan(): String {
        val current = dao.getJoinedClanOnce() ?: return "You are not in a clan."
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val remaining = current.membersCsv.split(",").filter {
            it.isNotBlank() && !it.equals(profile.callsign, ignoreCase = true)
        }
        dao.upsertClan(
            current.copy(
                isJoined = false,
                memberCount = remaining.size.coerceAtLeast(1),
                membersCsv = remaining.joinToString(",")
            )
        )
        dao.clearClanMissions()
        return "Left [${current.tag}] ${current.name}."
    }

    suspend fun recruitSquadmateToClan(callsign: String): Pair<Boolean, String> {
        val current = dao.getJoinedClanOnce() ?: return false to "Join or create a clan first."
        val cleaned = callsign.trim().take(16)
        if (cleaned.isBlank()) return false to "Enter a valid callsign."
        val members = current.membersCsv.split(",").filter { it.isNotBlank() }.toMutableList()
        if (members.any { it.equals(cleaned, ignoreCase = true) }) {
            return false to "$cleaned is already in [${current.tag}]."
        }
        if (members.size >= 8) {
            return false to "Clan squad roster is full (8/8)."
        }
        members.add(cleaned)
        // New member joins with immediate shared XP contribution!
        dao.upsertClan(
            current.copy(
                memberCount = members.size,
                membersCsv = members.joinToString(","),
                clanXp = current.clanXp + 45
            )
        )
        return true to "Recruited $cleaned to [${current.tag}] (+45 Shared Clan XP)!"
    }

    suspend fun equipClanBanner(bannerId: String): Pair<Boolean, String> {
        val current = dao.getJoinedClanOnce() ?: return false to "Join or create a clan first."
        val banner = ClanBannerCatalog.byId(bannerId)
        val clanLevel = ClanLevelInfo.fromClanXp(current.clanXp).level
        if (clanLevel < banner.unlockLevel) {
            return false to "Reach Clan Level ${banner.unlockLevel} to unlock ${banner.name}."
        }
        dao.upsertClan(current.copy(equippedBannerId = banner.id))
        return true to "Equipped Clan Banner: ${banner.name}!"
    }

    suspend fun claimClanMission(missionId: String): Pair<Boolean, String> {
        val current = dao.getJoinedClanOnce() ?: return false to "Join or create a clan first."
        val spec = ClanMissionCatalog.byId(missionId)
        val row = dao.getClanMissionsOnce().firstOrNull { it.missionId == missionId }
            ?: return false to "Mission unavailable."
        if (row.isClaimed) return false to "Mission reward already claimed."
        if (row.progressCount < spec.targetCount) {
            return false to "Complete ${spec.targetCount} progress first (${row.progressCount}/${spec.targetCount})."
        }

        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val oldLevel = ClanLevelInfo.fromClanXp(current.clanXp).level
        val newXp = current.clanXp + spec.clanXpReward
        val newLevel = ClanLevelInfo.fromClanXp(newXp).level

        dao.upsertClanMission(row.copy(isClaimed = true))
        dao.upsertClan(current.copy(clanXp = newXp))
        dao.upsertProfile(profile.copy(credits = profile.credits + spec.goldReward))
        dao.insertCurrencyTransaction(
            CurrencyTransactionEntity(
                title = "Clan Mission: ${spec.title}",
                goldDelta = spec.goldReward,
                gemsDelta = 0
            )
        )

        val unlockedNote = if (newLevel > oldLevel) {
            val newlyUnlocked = ClanBannerCatalog.all.filter { it.unlockLevel in (oldLevel + 1)..newLevel }
            if (newlyUnlocked.isNotEmpty()) {
                " • Unlocked Banner: ${newlyUnlocked.joinToString { it.name }}!"
            } else {
                " • Clan Lv.$newLevel reached!"
            }
        } else ""

        return true to "Claimed +${spec.clanXpReward} Clan XP & +${spec.goldReward} Gold$unlockedNote"
    }

    suspend fun runClanSquadDrill(): Pair<Boolean, String> {
        val current = dao.getJoinedClanOnce() ?: return false to "Join or create a clan first."
        val missions = dao.getClanMissionsOnce().ifEmpty {
            seedClanMissions(current.id)
            dao.getClanMissionsOnce()
        }
        val drillXp = 85 + (current.memberCount * 15)
        val oldLevel = ClanLevelInfo.fromClanXp(current.clanXp).level
        val newXp = current.clanXp + drillXp
        val newLevel = ClanLevelInfo.fromClanXp(newXp).level
        dao.upsertClan(current.copy(clanXp = newXp))

        val updated = missions.map { m ->
            val spec = ClanMissionCatalog.byId(m.missionId)
            val boost = when (spec.kind) {
                ClanMissionKind.ELIMINATIONS -> 2
                ClanMissionKind.MATCHES_PLAYED -> 1
                ClanMissionKind.REVIVES -> 1
                ClanMissionKind.WIN_MATCHES -> 0
                ClanMissionKind.DAMAGE_DEALT -> 150
            }
            m.copy(progressCount = (m.progressCount + boost).coerceAtMost(spec.targetCount))
        }
        dao.upsertClanMissions(updated)

        val levelNote = if (newLevel > oldLevel) " • Level Up to Clan Lv.$newLevel!" else ""
        return true to "Clan Squad Drill complete: +$drillXp Shared Clan XP$levelNote"
    }

    private suspend fun seedClanMissions(clanId: Int) {
        val seeded = ClanMissionCatalog.all.map { spec ->
            ClanMissionProgressEntity(
                missionId = spec.id,
                clanId = clanId,
                progressCount = 0,
                isClaimed = false
            )
        }
        dao.upsertClanMissions(seeded)
    }

    private suspend fun advanceGlobalLeaderboardRivals() {
        val allEntries = dao.getLeaderboardEntriesOnce()
        if (allEntries.isEmpty()) return
        val activeCallsigns = allEntries.shuffled().take(4).map { it.callsign }.toSet()
        val now = System.currentTimeMillis()
        val updated = allEntries.map { entry ->
            if (entry.callsign in activeCallsigns) {
                val rivalWon = Random.nextFloat() < 0.34f
                val rivalKills = Random.nextInt(1, 6)
                val rpChange = if (rivalWon) Random.nextInt(22, 46) else Random.nextInt(-10, 18)
                val note = buildString {
                    if (rivalWon) append("+1 Win • ")
                    append("+$rivalKills K")
                }
                entry.copy(
                    rp = (entry.rp + rpChange).coerceAtLeast(1000),
                    totalWins = entry.totalWins + if (rivalWon) 1 else 0,
                    totalKills = entry.totalKills + rivalKills,
                    totalMatches = entry.totalMatches + 1,
                    lastDeltaNote = note,
                    updatedAtMs = now
                )
            } else {
                if (entry.lastDeltaNote.isNotEmpty()) entry.copy(lastDeltaNote = "") else entry
            }
        }
        dao.upsertLeaderboardEntries(updated)
    }
}

