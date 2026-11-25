package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.audio.SoundSettingsState
import com.example.audio.VoiceChatController
import com.example.audio.VoiceChatState
import com.example.data.DailyRewardCatalog
import com.example.data.DailyRewardDay
import com.example.data.GameMode
import com.example.data.GameRepository
import com.example.data.GlobalLeaderboardRow
import com.example.data.LeaderboardSortBy
import com.example.data.OperatorCatalog
import com.example.data.RankTier
import com.example.data.ReplayFrameSnapshot
import com.example.data.ReportReason
import com.example.data.RoboHonorPolicy
import com.example.data.SquadMode
import com.example.data.WeaponCatalog
import com.example.data.WeaponId
import com.example.data.local.ClanEntity
import com.example.data.local.ClanMissionProgressEntity
import com.example.data.local.CurrencyTransactionEntity
import com.example.data.local.FriendEntity
import com.example.data.local.GameDatabase
import com.example.data.local.IncidentReportEntity
import com.example.data.local.LeaderboardEntryEntity
import com.example.data.local.MatchHistoryEntity
import com.example.data.local.MatchReplayEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.WeaponSkinStateEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

data class MatchedCompetitor(
    val callsign: String,
    val rankTier: RankTier,
    val rp: Int,
    val teamLabel: String,
    val isLocalPlayer: Boolean = false,
    val isReady: Boolean = true
)

data class MatchmakingState(
    val isSearching: Boolean = false,
    val isLobbyLocked: Boolean = false,
    val elapsedSeconds: Int = 0,
    val statusMessage: String = "",
    val competitors: List<MatchedCompetitor> = emptyList(),
    val targetCount: Int = 8
)

data class DailyRewardPopupState(
    val visible: Boolean = false,
    val canClaimToday: Boolean = false,
    val nextDayNumber: Int = 1,
    val claimedReward: DailyRewardDay? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(GameDatabase.getInstance(application).gameDao())

    val profile: StateFlow<PlayerProfileEntity> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProfileEntity())

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = repository.matchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friends: StateFlow<List<FriendEntity>> = repository.friendsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weaponSkins: StateFlow<Map<WeaponId, WeaponSkinStateEntity>> = repository.weaponSkinsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _matchmaking = MutableStateFlow(MatchmakingState())
    val matchmaking: StateFlow<MatchmakingState> = _matchmaking.asStateFlow()

    private val _dailyPopup = MutableStateFlow(DailyRewardPopupState())
    val dailyPopup: StateFlow<DailyRewardPopupState> = _dailyPopup.asStateFlow()

    private val _armoryBannerMessage = MutableStateFlow<String?>(null)
    val armoryBannerMessage: StateFlow<String?> = _armoryBannerMessage.asStateFlow()

    private val _leaderboardSortBy = MutableStateFlow(LeaderboardSortBy.TOTAL_WINS)
    val leaderboardSortBy: StateFlow<LeaderboardSortBy> = _leaderboardSortBy.asStateFlow()

    private val _lastLeaderboardNotice = MutableStateFlow<String?>(null)
    val lastLeaderboardNotice: StateFlow<String?> = _lastLeaderboardNotice.asStateFlow()

    val leaderboardRows: StateFlow<List<GlobalLeaderboardRow>> = combine(
        profile,
        repository.leaderboardEntriesFlow,
        friends,
        _leaderboardSortBy
    ) { currentProfile, entries, friendList, sortBy ->
        buildLeaderboard(currentProfile, entries, friendList, sortBy)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencyTransactions: StateFlow<List<CurrencyTransactionEntity>> = repository.currencyTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidentReports: StateFlow<List<IncidentReportEntity>> = repository.incidentReportsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clans: StateFlow<List<ClanEntity>> = repository.clansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clanMissions: StateFlow<List<ClanMissionProgressEntity>> = repository.clanMissionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchReplays: StateFlow<List<MatchReplayEntity>> = repository.matchReplaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currencyStoreMessage = MutableStateFlow<String?>(null)
    val currencyStoreMessage: StateFlow<String?> = _currencyStoreMessage.asStateFlow()

    private val _storeStatusMessage = MutableStateFlow<String?>(null)
    val storeStatusMessage: StateFlow<String?> = _storeStatusMessage.asStateFlow()

    private val _reportStatusMessage = MutableStateFlow<String?>(null)
    val reportStatusMessage: StateFlow<String?> = _reportStatusMessage.asStateFlow()

    private val _clanStatusMessage = MutableStateFlow<String?>(null)
    val clanStatusMessage: StateFlow<String?> = _clanStatusMessage.asStateFlow()

    private val _honorLockNotice = MutableStateFlow<String?>(null)
    val honorLockNotice: StateFlow<String?> = _honorLockNotice.asStateFlow()

    private val voiceChatController = VoiceChatController(application, viewModelScope)
    val voiceChatState: StateFlow<VoiceChatState> = voiceChatController.state

    private val soundManager = SoundManager.getInstance(application)
    val soundSettings: StateFlow<SoundSettingsState> = soundManager.settings

    val playerWinsRank: StateFlow<Int> = combine(
        profile,
        repository.leaderboardEntriesFlow,
        friends
    ) { currentProfile, entries, friendList ->
        buildLeaderboard(currentProfile, entries, friendList, LeaderboardSortBy.TOTAL_WINS)
            .firstOrNull { it.isLocalPlayer }?.rank ?: 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16)

    val playerKdRank: StateFlow<Int> = combine(
        profile,
        repository.leaderboardEntriesFlow,
        friends
    ) { currentProfile, entries, friendList ->
        buildLeaderboard(currentProfile, entries, friendList, LeaderboardSortBy.KD_RATIO)
            .firstOrNull { it.isLocalPlayer }?.rank ?: 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16)

    private var queueJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            val canClaim = repository.canClaimDailyReward()
            val p = profile.value
            val nextDay = ((p.loginStreak) % DailyRewardCatalog.cycle.size) + 1
            if (canClaim) {
                _dailyPopup.value = DailyRewardPopupState(
                    visible = true,
                    canClaimToday = true,
                    nextDayNumber = nextDay,
                    claimedReward = null
                )
            }
        }
    }

    fun openDailyRewardDialog() {
        viewModelScope.launch {
            val canClaim = repository.canClaimDailyReward()
            val streak = profile.value.loginStreak
            val nextDay = if (canClaim) {
                (streak % DailyRewardCatalog.cycle.size) + 1
            } else {
                ((streak - 1).coerceAtLeast(0) % DailyRewardCatalog.cycle.size) + 1
            }
            _dailyPopup.value = DailyRewardPopupState(
                visible = true,
                canClaimToday = canClaim,
                nextDayNumber = nextDay,
                claimedReward = null
            )
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val claimed = repository.claimDailyReward()
            if (claimed != null) {
                soundManager.playUiConfirm()
                _dailyPopup.value = DailyRewardPopupState(
                    visible = true,
                    canClaimToday = false,
                    nextDayNumber = claimed.day,
                    claimedReward = claimed
                )
            }
        }
    }

    fun dismissDailyPopup() {
        soundManager.playUiClick()
        _dailyPopup.update { it.copy(visible = false) }
    }

    fun addFriend(callsign: String, rankLabel: String) {
        soundManager.playUiConfirm()
        viewModelScope.launch {
            repository.addFriend(callsign, rankLabel)
        }
    }

    fun toggleFriendSquad(friend: FriendEntity) {
        soundManager.playUiClick()
        viewModelScope.launch {
            repository.toggleFriendSquad(friend)
        }
    }

    fun removeFriend(friendId: Int) {
        soundManager.playUiClick()
        viewModelScope.launch {
            repository.removeFriend(friendId)
        }
    }

    fun unlockOrEquipSkin(weaponId: WeaponId, skinId: String) {
        viewModelScope.launch {
            val (success, msg) = repository.unlockOrEquipSkin(weaponId, skinId)
            if (success) soundManager.playUiConfirm() else soundManager.playUiClick()
            _armoryBannerMessage.value = if (success) null else msg
            _storeStatusMessage.value = msg
        }
    }

    fun unlockOrEquipCharacterSkin(skinId: String) {
        viewModelScope.launch {
            val (success, msg) = repository.unlockOrEquipCharacterSkin(skinId)
            if (success) soundManager.playUiConfirm() else soundManager.playUiClick()
            _storeStatusMessage.value = msg
        }
    }

    fun unlockOrEquipEmote(emoteId: String) {
        viewModelScope.launch {
            val (success, msg) = repository.unlockOrEquipEmote(emoteId)
            if (success) soundManager.playUiConfirm() else soundManager.playUiClick()
            _storeStatusMessage.value = msg
        }
    }

    fun clearStoreStatusMessage() {
        _storeStatusMessage.value = null
    }

    fun submitIncidentReport(
        reportedCallsign: String,
        reason: ReportReason,
        details: String,
        modeName: String,
        arenaLevel: Int,
        mapLocation: String,
        phaseLabel: String
    ) {
        viewModelScope.launch {
            val saved = repository.submitIncidentReport(
                reportedCallsign = reportedCallsign,
                reason = reason,
                details = details,
                modeName = modeName,
                arenaLevel = arenaLevel,
                mapLocation = mapLocation,
                phaseLabel = phaseLabel
            )
            _reportStatusMessage.value = "Incident report #${saved.id.takeIf { it > 0 } ?: ""} saved for ${saved.reportedCallsign} (${saved.reasonLabel})"
        }
    }

    fun clearReportStatusMessage() {
        _reportStatusMessage.value = null
    }

    fun createClan(name: String, tag: String, motto: String) {
        viewModelScope.launch {
            _clanStatusMessage.value = repository.createClan(name, tag, motto).second
        }
    }

    fun joinClan(clanId: Int) {
        viewModelScope.launch {
            _clanStatusMessage.value = repository.joinClan(clanId).second
        }
    }

    fun leaveCurrentClan() {
        viewModelScope.launch {
            _clanStatusMessage.value = repository.leaveCurrentClan()
        }
    }

    fun recruitSquadmateToClan(callsign: String) {
        viewModelScope.launch {
            _clanStatusMessage.value = repository.recruitSquadmateToClan(callsign).second
        }
    }

    fun equipClanBanner(bannerId: String) {
        viewModelScope.launch {
            _clanStatusMessage.value = repository.equipClanBanner(bannerId).second
        }
    }

    fun claimClanMission(missionId: String) {
        viewModelScope.launch {
            _clanStatusMessage.value = repository.claimClanMission(missionId).second
        }
    }

    fun runClanSquadDrill() {
        viewModelScope.launch {
            _clanStatusMessage.value = repository.runClanSquadDrill().second
        }
    }

    fun clearClanStatusMessage() {
        _clanStatusMessage.value = null
    }

    fun clearHonorLockNotice() {
        _honorLockNotice.value = null
    }

    fun completeTrainingDrill(targetsHit: Int, durationSec: Int) {
        viewModelScope.launch {
            val notice = repository.completeTrainingDrill(targetsHit = targetsHit, durationSec = durationSec)
            _honorLockNotice.value = notice
        }
    }

    fun clearArmoryMessage() {
        _armoryBannerMessage.value = null
    }

    fun toggleVoiceMic() {
        voiceChatController.toggleMic(profile.value.callsign)
    }

    fun onVoicePermissionResult(granted: Boolean) {
        voiceChatController.onPermissionResult(granted, profile.value.callsign)
    }

    fun toggleVoiceSpeaker() {
        voiceChatController.toggleSpeaker()
    }

    fun sendVoiceCallout(message: String) {
        voiceChatController.sendSquadCallout(profile.value.callsign, message)
    }

    fun clearVoiceNotice() {
        voiceChatController.clearStatusNotice()
    }

    fun buyDropMedkitSupply() {
        viewModelScope.launch {
            _currencyStoreMessage.value = repository.buyDropMedkitSupply().second
        }
    }

    fun buyDropShieldSupply() {
        viewModelScope.launch {
            _currencyStoreMessage.value = repository.buyDropShieldSupply().second
        }
    }

    fun exchangeGoldForGems() {
        viewModelScope.launch {
            _currencyStoreMessage.value = repository.exchangeGoldForGems().second
        }
    }

    fun exchangeGemsForGold() {
        viewModelScope.launch {
            _currencyStoreMessage.value = repository.exchangeGemsForGold().second
        }
    }

    fun clearCurrencyStoreMessage() {
        _currencyStoreMessage.value = null
    }

    fun setLeaderboardSortBy(sortBy: LeaderboardSortBy) {
        _leaderboardSortBy.value = sortBy
    }

    fun clearLeaderboardNotice() {
        _lastLeaderboardNotice.value = null
    }

    private fun buildLeaderboard(
        currentProfile: PlayerProfileEntity,
        entries: List<LeaderboardEntryEntity>,
        friendList: List<FriendEntity>,
        sortBy: LeaderboardSortBy
    ): List<GlobalLeaderboardRow> {
        val friendCallsigns = friendList.map { it.callsign.lowercase() }.toSet()
        val rawRows = entries
            .filterNot { it.callsign.equals(currentProfile.callsign, ignoreCase = true) }
            .map { entry ->
                val deaths = (entry.totalMatches - entry.totalWins).coerceAtLeast(0)
                val kd = if (deaths == 0) entry.totalKills.toFloat() else entry.totalKills.toFloat() / deaths.toFloat()
                val winRate = if (entry.totalMatches == 0) 0 else ((entry.totalWins.toFloat() / entry.totalMatches) * 100f).toInt()
                GlobalLeaderboardRow(
                    rank = 0,
                    callsign = entry.callsign,
                    regionTag = entry.regionTag,
                    rp = entry.rp,
                    rankTier = RankTier.fromRp(entry.rp),
                    totalWins = entry.totalWins,
                    totalKills = entry.totalKills,
                    totalMatches = entry.totalMatches,
                    deaths = deaths,
                    kdRatio = kd,
                    winRatePercent = winRate,
                    isLocalPlayer = false,
                    isFriend = entry.isFriend || entry.callsign.lowercase() in friendCallsigns,
                    lastDeltaNote = entry.lastDeltaNote
                )
            } + run {
                val playerDeaths = (currentProfile.totalMatches - currentProfile.totalWins).coerceAtLeast(0)
                val playerKd = if (playerDeaths == 0) {
                    currentProfile.totalKills.toFloat()
                } else {
                    currentProfile.totalKills.toFloat() / playerDeaths.toFloat()
                }
                val playerWinRate = if (currentProfile.totalMatches == 0) {
                    0
                } else {
                    ((currentProfile.totalWins.toFloat() / currentProfile.totalMatches) * 100f).toInt()
                }
                val bestRp = max(currentProfile.brRp, currentProfile.csRp)
                GlobalLeaderboardRow(
                    rank = 0,
                    callsign = currentProfile.callsign,
                    regionTag = "YOU",
                    rp = bestRp,
                    rankTier = RankTier.fromRp(bestRp),
                    totalWins = currentProfile.totalWins,
                    totalKills = currentProfile.totalKills,
                    totalMatches = currentProfile.totalMatches,
                    deaths = playerDeaths,
                    kdRatio = playerKd,
                    winRatePercent = playerWinRate,
                    isLocalPlayer = true,
                    isFriend = false,
                    lastDeltaNote = if (currentProfile.totalMatches > 0) "LIVE" else ""
                )
            }

        val sorted = when (sortBy) {
            LeaderboardSortBy.TOTAL_WINS -> rawRows.sortedWith(
                compareByDescending<GlobalLeaderboardRow> { it.totalWins }
                    .thenByDescending { it.kdRatio }
                    .thenByDescending { it.rp }
            )
            LeaderboardSortBy.KD_RATIO -> rawRows.sortedWith(
                compareByDescending<GlobalLeaderboardRow> { it.kdRatio }
                    .thenByDescending { it.totalWins }
                    .thenByDescending { it.totalKills }
            )
        }
        return sorted.mapIndexed { index, row -> row.copy(rank = index + 1) }
    }

    fun equipWeapon(weaponId: WeaponId) {
        viewModelScope.launch {
            repository.equipWeapon(weaponId)
        }
    }

    fun equipOperator(operatorId: String) {
        viewModelScope.launch {
            repository.equipOperator(operatorId)
        }
    }

    fun selectMode(mode: GameMode, squadMode: SquadMode) {
        val currentHonor = profile.value.honorScore
        if (mode.isRanked && !RoboHonorPolicy.canPlayRanked(currentHonor)) {
            _honorLockNotice.value = "Robo-Sentinel locked ${mode.shortTitle}! Honor is $currentHonor/100 (need 90). Finish Casual BR (+8) or Training Drill (+5) to raise your score."
            return
        }
        viewModelScope.launch {
            repository.selectMode(mode, squadMode)
        }
    }

    fun selectArenaLevel(level: Int) {
        viewModelScope.launch {
            repository.selectArenaLevel(level)
        }
    }

    fun updateCallsign(callsign: String) {
        viewModelScope.launch {
            repository.updateCallsign(callsign)
        }
    }

    fun startMatchmaking(onReadyToDeploy: (GameMode, SquadMode) -> Unit) {
        val currentProfile = profile.value
        val mode = runCatching { GameMode.valueOf(currentProfile.preferredModeName) }
            .getOrDefault(GameMode.BATTLE_ROYALE_RANKED)
        val squadMode = runCatching { SquadMode.valueOf(currentProfile.preferredSquadName) }
            .getOrDefault(SquadMode.SQUAD)

        if (mode.isRanked && !RoboHonorPolicy.canPlayRanked(currentProfile.honorScore)) {
            _honorLockNotice.value = "Robo-Sentinel blocked Ranked! Honor is ${currentProfile.honorScore}/100 (need 90). Play Casual BR (+8) or Training (+5)!"
            viewModelScope.launch {
                repository.selectMode(GameMode.BATTLE_ROYALE_CASUAL, squadMode)
            }
            return
        }

        if (mode == GameMode.TRAINING_GROUND) {
            onReadyToDeploy(mode, squadMode)
            return
        }

        queueJob?.cancel()
        val playerRp = if (mode == GameMode.CLASH_SQUAD_RANKED) currentProfile.csRp else currentProfile.brRp
        val playerRank = RankTier.fromRp(playerRp)
        val targetCount = if (mode == GameMode.CLASH_SQUAD_RANKED) 8 else 10

        val squadFriends = friends.value.filter { it.isInSquad }.take(3)
        val poolNames = listOf(
            "VortexX", "Kira_INF", "ZedRush", "NovaSniper", "ShadowAK",
            "Reaper77", "DriftKing", "AuraMend", "TitanMP5", "PyroM7",
            "Falcon_CS", "PulseCG", "BlazeOps"
        ).filterNot { name -> squadFriends.any { it.callsign.equals(name, ignoreCase = true) } }.shuffled()

        val selfCompetitor = MatchedCompetitor(
            callsign = currentProfile.callsign,
            rankTier = playerRank,
            rp = playerRp,
            teamLabel = if (mode == GameMode.CLASH_SQUAD_RANKED || squadMode == SquadMode.SQUAD) "Squad A" else "Solo",
            isLocalPlayer = true,
            isReady = true
        )

        _matchmaking.value = MatchmakingState(
            isSearching = true,
            isLobbyLocked = false,
            elapsedSeconds = 0,
            statusMessage = if (mode.isRanked) {
                "Scanning ${playerRank.label} tier competitors for Infine..."
            } else {
                "Finding Infine Casual (Unranked) squad lobby..."
            },
            competitors = listOf(selfCompetitor),
            targetCount = targetCount
        )

        queueJob = viewModelScope.launch {
            for (step in 1 until targetCount) {
                delay(260L)
                val isSquadSlot = (mode == GameMode.CLASH_SQUAD_RANKED || squadMode == SquadMode.SQUAD) && step < 4
                val squadFriend = if (isSquadSlot) squadFriends.getOrNull(step - 1) else null
                val peerRp = (playerRp + (-180..220).random()).coerceAtLeast(1000)
                val teamLabel = when {
                    mode == GameMode.CLASH_SQUAD_RANKED && step < 4 -> "Squad A"
                    mode == GameMode.CLASH_SQUAD_RANKED -> "Squad B"
                    squadMode == SquadMode.SQUAD && step < 4 -> "Squad A"
                    squadMode == SquadMode.SQUAD -> "Rival Squad"
                    else -> "Rival"
                }
                val peer = MatchedCompetitor(
                    callsign = squadFriend?.callsign ?: poolNames[(step - 1) % poolNames.size],
                    rankTier = RankTier.fromRp(peerRp),
                    rp = peerRp,
                    teamLabel = if (squadFriend != null) "Squad A (Friend)" else teamLabel,
                    isLocalPlayer = false,
                    isReady = true
                )
                _matchmaking.update { state ->
                    state.copy(
                        elapsedSeconds = (step * 260 / 1000) + 1,
                        competitors = state.competitors + peer,
                        statusMessage = "Matching Infine competitors (${state.competitors.size + 1}/$targetCount)..."
                    )
                }
            }

            _matchmaking.update { state ->
                state.copy(
                    isLobbyLocked = true,
                    statusMessage = "Match Found! Dropping into Infine..."
                )
            }
            delay(650L)
            _matchmaking.value = MatchmakingState()
            onReadyToDeploy(mode, squadMode)
        }
    }

    fun cancelMatchmaking() {
        queueJob?.cancel()
        queueJob = null
        _matchmaking.value = MatchmakingState()
    }

    fun recordMatchResult(
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
        weaponUsed: WeaponId,
        durationSeconds: Int = 1,
        replayFrames: List<ReplayFrameSnapshot> = emptyList(),
        abandonedEarly: Boolean = false
    ) {
        viewModelScope.launch {
            val prevHonor = profile.value.honorScore
            repository.recordMatchResult(
                mode = mode,
                squadMode = squadMode,
                arenaLevel = arenaLevel,
                won = won,
                placementRank = placementRank,
                kills = kills,
                revives = revives,
                damageDealt = damageDealt,
                rpDelta = rpDelta,
                xpEarned = xpEarned,
                goldEarned = goldEarned,
                gemsEarned = gemsEarned,
                lastLocation = lastLocation,
                weaponName = WeaponCatalog.byId(weaponUsed).name,
                abandonedEarly = abandonedEarly
            )
            if (replayFrames.isNotEmpty()) {
                repository.saveMatchReplay(
                    summary = com.example.ui.match.MatchEndSummary(
                        mode = mode,
                        squadMode = squadMode,
                        arenaLevel = arenaLevel,
                        won = won,
                        placementRank = placementRank,
                        kills = kills,
                        revives = revives,
                        damageDealt = damageDealt,
                        rpDelta = rpDelta,
                        xpEarned = xpEarned,
                        goldEarned = goldEarned,
                        gemsEarned = gemsEarned,
                        durationSeconds = durationSeconds,
                        squadResults = emptyList(),
                        lastLocation = lastLocation,
                        weaponUsed = weaponUsed,
                        abandonedEarly = abandonedEarly
                    ),
                    frames = replayFrames
                )
            }
            if (abandonedEarly) {
                val penalty = if (mode.isRanked) RoboHonorPolicy.PENALTY_RANKED_QUIT else RoboHonorPolicy.PENALTY_CASUAL_QUIT
                val newHonor = (prevHonor - penalty).coerceIn(0, RoboHonorPolicy.MAX_HONOR)
                _honorLockNotice.value = if (!RoboHonorPolicy.canPlayRanked(newHonor)) {
                    "Robo-Sentinel deducted -$penalty Honor (now $newHonor/100)! Ranked modes are LOCKED — play Casual BR (+8) or Training (+5)!"
                } else {
                    "Robo-Sentinel deducted -$penalty Honor for leaving early (now $newHonor/100)."
                }
            } else if (mode == GameMode.BATTLE_ROYALE_CASUAL) {
                val newHonor = (prevHonor + RoboHonorPolicy.GAIN_CASUAL_FINISH).coerceAtMost(RoboHonorPolicy.MAX_HONOR)
                _honorLockNotice.value = if (!RoboHonorPolicy.canPlayRanked(prevHonor) && RoboHonorPolicy.canPlayRanked(newHonor)) {
                    "Robo-Sentinel restored +8 Honor (now $newHonor/100)! Ranked BR & Clash Squad are UNLOCKED!"
                } else {
                    "Robo-Sentinel added +8 Honor for finishing Casual BR ($newHonor/100)."
                }
            }
            if (mode.isRanked && !abandonedEarly) {
                val resultTag = if (won) "+1 Win" else "#$placementRank"
                _lastLeaderboardNotice.value = "Updated after ${mode.shortTitle}: $resultTag • +$kills Kills"
            }
        }
    }

    fun equippedWeapon(): WeaponId {
        return runCatching { WeaponId.valueOf(profile.value.equippedWeaponName) }
            .getOrDefault(WeaponId.AK47)
    }

    fun equippedWeaponSkinId(): String {
        val weaponId = equippedWeapon()
        return weaponSkins.value[weaponId]?.equippedSkinId ?: "standard"
    }

    fun invitedSquadCallsigns(): List<String> {
        return friends.value.filter { it.isInSquad }.map { it.callsign }.take(3)
    }

    fun equippedOperator() = OperatorCatalog.byId(profile.value.equippedOperatorId)

    fun toggleSfx() {
        soundManager.toggleSfx()
    }

    fun playUiClick() {
        soundManager.playUiClick()
    }

    fun playUiConfirm() {
        soundManager.playUiConfirm()
    }

    override fun onCleared() {
        super.onCleared()
        voiceChatController.release()
    }
}
