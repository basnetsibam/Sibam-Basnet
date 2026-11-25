package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.GameMode
import com.example.data.SquadMode
import com.example.data.WeaponSkinCatalog
import com.example.ui.MainViewModel
import com.example.ui.match.MatchScreen
import com.example.ui.screens.ArmoryScreen
import com.example.ui.screens.CareerScreen
import com.example.ui.screens.ClanScreen
import com.example.ui.screens.CurrencyWalletDialog
import com.example.ui.screens.DailyRewardDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FriendsSquadDialog
import com.example.ui.screens.InfineMapScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.theme.InfineArenaTheme
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalAmberSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

const val ROUTE_LOBBY = "lobby"
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_ARMORY = "armory"
const val ROUTE_MAP = "infine_map"
const val ROUTE_CAREER = "career"
const val ROUTE_LEADERBOARD = "leaderboard"
const val ROUTE_STORE = "store"
const val ROUTE_CLAN = "clan"
const val ROUTE_MATCH = "match/{mode}/{squad}"

private data class HubTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val hubTabs = listOf(
    HubTab(ROUTE_LOBBY, R.string.nav_lobby, Icons.Filled.Home, Icons.Outlined.Home),
    HubTab(ROUTE_DASHBOARD, R.string.nav_dashboard, Icons.Filled.Analytics, Icons.Outlined.Analytics),
    HubTab(ROUTE_ARMORY, R.string.nav_armory, Icons.Filled.Shield, Icons.Outlined.Shield),
    HubTab(ROUTE_MAP, R.string.nav_map, Icons.Filled.Map, Icons.Outlined.Map),
    HubTab(ROUTE_CAREER, R.string.nav_career, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InfineArenaTheme {
                InfineArenaApp()
            }
        }
    }
}

@Composable
fun InfineArenaApp(
    mainViewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isInMatch = currentRoute?.startsWith("match") == true

    val profile by mainViewModel.profile.collectAsStateWithLifecycle()
    val history by mainViewModel.matchHistory.collectAsStateWithLifecycle()
    val matchmaking by mainViewModel.matchmaking.collectAsStateWithLifecycle()
    val friends by mainViewModel.friends.collectAsStateWithLifecycle()
    val weaponSkins by mainViewModel.weaponSkins.collectAsStateWithLifecycle()
    val dailyPopup by mainViewModel.dailyPopup.collectAsStateWithLifecycle()
    val armoryBannerMessage by mainViewModel.armoryBannerMessage.collectAsStateWithLifecycle()
    val leaderboardRows by mainViewModel.leaderboardRows.collectAsStateWithLifecycle()
    val leaderboardSortBy by mainViewModel.leaderboardSortBy.collectAsStateWithLifecycle()
    val lastLeaderboardNotice by mainViewModel.lastLeaderboardNotice.collectAsStateWithLifecycle()
    val globalWinsRank by mainViewModel.playerWinsRank.collectAsStateWithLifecycle()
    val globalKdRank by mainViewModel.playerKdRank.collectAsStateWithLifecycle()
    val currencyTransactions by mainViewModel.currencyTransactions.collectAsStateWithLifecycle()
    val currencyStoreMessage by mainViewModel.currencyStoreMessage.collectAsStateWithLifecycle()
    val storeStatusMessage by mainViewModel.storeStatusMessage.collectAsStateWithLifecycle()
    val incidentReports by mainViewModel.incidentReports.collectAsStateWithLifecycle()
    val reportStatusMessage by mainViewModel.reportStatusMessage.collectAsStateWithLifecycle()
    val clans by mainViewModel.clans.collectAsStateWithLifecycle()
    val clanMissions by mainViewModel.clanMissions.collectAsStateWithLifecycle()
    val matchReplays by mainViewModel.matchReplays.collectAsStateWithLifecycle()
    val clanStatusMessage by mainViewModel.clanStatusMessage.collectAsStateWithLifecycle()
    val honorLockNotice by mainViewModel.honorLockNotice.collectAsStateWithLifecycle()
    val voiceChatState by mainViewModel.voiceChatState.collectAsStateWithLifecycle()
    val soundSettings by mainViewModel.soundSettings.collectAsStateWithLifecycle()
    var showFriendsDialog by remember { mutableStateOf(false) }
    var showCurrencyVault by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp && !isInMatch

        Scaffold(
            containerColor = ObsidianVoid,
            contentWindowInsets = if (isInMatch) WindowInsets(0, 0, 0, 0) else WindowInsets.safeDrawing,
            bottomBar = {
                if (!isInMatch && !useRail) {
                    NavigationBar(containerColor = ObsidianSurface) {
                        hubTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    mainViewModel.playUiClick()
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TacticalAmber,
                                    selectedTextColor = TacticalAmber,
                                    indicatorColor = TacticalAmberSoft,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = ObsidianSurface) {
                        hubTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    mainViewModel.playUiClick()
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = TacticalAmber,
                                    selectedTextColor = TacticalAmber,
                                    indicatorColor = TacticalAmberSoft,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_LOBBY
                    ) {
                        composable(ROUTE_LOBBY) {
                            val activeWeaponId = runCatching {
                                com.example.data.WeaponId.valueOf(profile.equippedWeaponName)
                            }.getOrDefault(com.example.data.WeaponId.AK47)
                            val activeSkinId = weaponSkins[activeWeaponId]?.equippedSkinId ?: "standard"
                            val equippedSkinName = WeaponSkinCatalog.byId(activeSkinId).name
                            LobbyScreen(
                                profile = profile,
                                matchmaking = matchmaking,
                                friends = friends,
                                joinedClan = clans.firstOrNull { it.isJoined },
                                friendsCount = friends.size,
                                invitedSquadCount = friends.count { it.isInSquad },
                                equippedSkinName = equippedSkinName,
                                canClaimDaily = dailyPopup.canClaimToday,
                                globalWinsRank = globalWinsRank,
                                globalKdRank = globalKdRank,
                                voiceChatState = voiceChatState,
                                honorLockNotice = honorLockNotice,
                                onClearHonorLockNotice = { mainViewModel.clearHonorLockNotice() },
                                onSelectMode = { mode, squad -> mainViewModel.selectMode(mode, squad) },
                                onSelectArenaLevel = { level -> mainViewModel.selectArenaLevel(level) },
                                onStartMatchmaking = {
                                    mainViewModel.playUiConfirm()
                                    mainViewModel.startMatchmaking { mode, squad ->
                                        mainViewModel.playUiConfirm()
                                        navController.navigate("match/${mode.name}/${squad.name}")
                                    }
                                },
                                onCancelMatchmaking = {
                                    mainViewModel.playUiClick()
                                    mainViewModel.cancelMatchmaking()
                                },
                                onOpenArmory = {
                                    mainViewModel.playUiClick()
                                    navController.navigate(ROUTE_ARMORY)
                                },
                                onOpenStore = {
                                    mainViewModel.playUiClick()
                                    navController.navigate(ROUTE_STORE)
                                },
                                onOpenClan = {
                                    mainViewModel.playUiClick()
                                    navController.navigate(ROUTE_CLAN)
                                },
                                onOpenInfineMap = {
                                    mainViewModel.playUiClick()
                                    navController.navigate(ROUTE_MAP)
                                },
                                onOpenDailyRewards = {
                                    mainViewModel.playUiClick()
                                    mainViewModel.openDailyRewardDialog()
                                },
                                onOpenFriends = {
                                    mainViewModel.playUiClick()
                                    showFriendsDialog = true
                                },
                                onToggleFriendSquad = { friend -> mainViewModel.toggleFriendSquad(friend) },
                                onEquipOperator = { opId ->
                                    mainViewModel.playUiConfirm()
                                    mainViewModel.equipOperator(opId)
                                },
                                onEquipWeapon = { weaponId ->
                                    mainViewModel.playUiConfirm()
                                    mainViewModel.equipWeapon(weaponId)
                                },
                                onSelectCharacterSkin = { skinId -> mainViewModel.unlockOrEquipCharacterSkin(skinId) },
                                onOpenLeaderboard = {
                                    mainViewModel.playUiClick()
                                    navController.navigate(ROUTE_LEADERBOARD)
                                },
                                onOpenCurrencyVault = {
                                    mainViewModel.playUiClick()
                                    mainViewModel.clearCurrencyStoreMessage()
                                    showCurrencyVault = true
                                },
                                onToggleVoiceMic = { mainViewModel.toggleVoiceMic() },
                                onVoicePermissionResult = { granted -> mainViewModel.onVoicePermissionResult(granted) },
                                onToggleVoiceSpeaker = { mainViewModel.toggleVoiceSpeaker() },
                                isSfxEnabled = soundSettings.isSfxEnabled,
                                onToggleSfx = { mainViewModel.toggleSfx() }
                            )
                        }

                        composable(ROUTE_DASHBOARD) {
                            DashboardScreen(
                                profile = profile,
                                history = history,
                                globalWinsRank = globalWinsRank,
                                globalKdRank = globalKdRank,
                                onGoToLobby = {
                                    navController.navigate(ROUTE_LOBBY) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenLeaderboard = { navController.navigate(ROUTE_LEADERBOARD) }
                            )
                        }

                        composable(ROUTE_ARMORY) {
                            ArmoryScreen(
                                profile = profile,
                                weaponSkins = weaponSkins,
                                bannerMessage = armoryBannerMessage,
                                onEquipWeapon = { mainViewModel.equipWeapon(it) },
                                onEquipOperator = { mainViewModel.equipOperator(it) },
                                onUnlockOrEquipSkin = { weaponId, skinId ->
                                    mainViewModel.unlockOrEquipSkin(weaponId, skinId)
                                },
                                onDismissBanner = { mainViewModel.clearArmoryMessage() },
                                onOpenCurrencyVault = {
                                    mainViewModel.clearCurrencyStoreMessage()
                                    showCurrencyVault = true
                                },
                                onOpenStore = { navController.navigate(ROUTE_STORE) }
                            )
                        }

                        composable(ROUTE_MAP) {
                            InfineMapScreen()
                        }

                        composable(ROUTE_CAREER) {
                            CareerScreen(
                                profile = profile,
                                history = history,
                                incidentReports = incidentReports,
                                matchReplays = matchReplays,
                                leaderboardRows = leaderboardRows,
                                leaderboardSortBy = leaderboardSortBy,
                                lastLeaderboardNotice = lastLeaderboardNotice,
                                globalWinsRank = globalWinsRank,
                                globalKdRank = globalKdRank,
                                onSelectSortBy = { mainViewModel.setLeaderboardSortBy(it) },
                                onDismissLeaderboardNotice = { mainViewModel.clearLeaderboardNotice() },
                                onUpdateCallsign = { mainViewModel.updateCallsign(it) }
                            )
                        }

                        composable(ROUTE_LEADERBOARD) {
                            LeaderboardScreen(
                                rows = leaderboardRows,
                                sortBy = leaderboardSortBy,
                                lastNotice = lastLeaderboardNotice,
                                onSelectSortBy = { mainViewModel.setLeaderboardSortBy(it) },
                                onDismissNotice = { mainViewModel.clearLeaderboardNotice() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ROUTE_STORE) {
                            StoreScreen(
                                profile = profile,
                                weaponSkins = weaponSkins,
                                statusMessage = storeStatusMessage,
                                onUnlockOrEquipCharacterSkin = { mainViewModel.unlockOrEquipCharacterSkin(it) },
                                onUnlockOrEquipWeaponWrap = { weaponId, skinId ->
                                    mainViewModel.unlockOrEquipSkin(weaponId, skinId)
                                },
                                onUnlockOrEquipEmote = { mainViewModel.unlockOrEquipEmote(it) },
                                onClearStatusMessage = { mainViewModel.clearStoreStatusMessage() },
                                onOpenCurrencyVault = {
                                    mainViewModel.clearCurrencyStoreMessage()
                                    showCurrencyVault = true
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ROUTE_CLAN) {
                            ClanScreen(
                                profile = profile,
                                clans = clans,
                                clanMissions = clanMissions,
                                friends = friends,
                                statusMessage = clanStatusMessage,
                                onCreateClan = { name, tag, motto -> mainViewModel.createClan(name, tag, motto) },
                                onJoinClan = { clanId -> mainViewModel.joinClan(clanId) },
                                onLeaveClan = { mainViewModel.leaveCurrentClan() },
                                onRecruitMember = { callsign -> mainViewModel.recruitSquadmateToClan(callsign) },
                                onEquipBanner = { bannerId -> mainViewModel.equipClanBanner(bannerId) },
                                onClaimMission = { missionId -> mainViewModel.claimClanMission(missionId) },
                                onRunSquadDrill = { mainViewModel.runClanSquadDrill() },
                                onClearStatusMessage = { mainViewModel.clearClanStatusMessage() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = ROUTE_MATCH,
                            arguments = listOf(
                                navArgument("mode") { type = NavType.StringType },
                                navArgument("squad") { type = NavType.StringType }
                            )
                        ) { entry ->
                            val modeArg = entry.arguments?.getString("mode") ?: GameMode.BATTLE_ROYALE_RANKED.name
                            val squadArg = entry.arguments?.getString("squad") ?: SquadMode.SQUAD.name
                            val mode = runCatching { GameMode.valueOf(modeArg) }
                                .getOrDefault(GameMode.BATTLE_ROYALE_RANKED)
                            val squad = runCatching { SquadMode.valueOf(squadArg) }
                                .getOrDefault(SquadMode.SQUAD)

                            MatchScreen(
                                mode = mode,
                                squadMode = squad,
                                arenaLevel = profile.selectedArenaLevel,
                                equippedWeapon = mainViewModel.equippedWeapon(),
                                weaponSkinId = mainViewModel.equippedWeaponSkinId(),
                                characterSkinId = profile.equippedCharacterSkinId,
                                emoteId = profile.equippedEmoteId,
                                squadFriends = mainViewModel.invitedSquadCallsigns(),
                                equippedOperatorId = profile.equippedOperatorId,
                                callsign = profile.callsign,
                                bonusMedkits = profile.bonusMedkitDrops,
                                bonusShield = profile.bonusShieldDrops,
                                unlockedEmoteIds = profile.unlockedEmotesCsv.split(",").filter { it.isNotBlank() }.toSet(),
                                honorScore = profile.honorScore,
                                incidentReportsCount = incidentReports.size,
                                onEquipEmote = { selectedId -> mainViewModel.unlockOrEquipEmote(selectedId) },
                                voiceChatState = voiceChatState,
                                onToggleVoiceMic = { mainViewModel.toggleVoiceMic() },
                                onVoicePermissionResult = { granted -> mainViewModel.onVoicePermissionResult(granted) },
                                onToggleVoiceSpeaker = { mainViewModel.toggleVoiceSpeaker() },
                                onSendVoiceCallout = { message -> mainViewModel.sendVoiceCallout(message) },
                                reportStatusMessage = reportStatusMessage,
                                onSubmitIncidentReport = { suspect, reason, details, modeName, level, mapLoc, phase ->
                                    mainViewModel.submitIncidentReport(
                                        reportedCallsign = suspect,
                                        reason = reason,
                                        details = details,
                                        modeName = modeName,
                                        arenaLevel = level,
                                        mapLocation = mapLoc,
                                        phaseLabel = phase
                                    )
                                },
                                onClearReportStatusMessage = { mainViewModel.clearReportStatusMessage() },
                                onMatchFinished = { summary ->
                                    mainViewModel.recordMatchResult(
                                        mode = summary.mode,
                                        squadMode = summary.squadMode,
                                        arenaLevel = summary.arenaLevel,
                                        won = summary.won,
                                        placementRank = summary.placementRank,
                                        kills = summary.kills,
                                        revives = summary.revives,
                                        damageDealt = summary.damageDealt,
                                        rpDelta = summary.rpDelta,
                                        xpEarned = summary.xpEarned,
                                        goldEarned = summary.goldEarned,
                                        gemsEarned = summary.gemsEarned,
                                        lastLocation = summary.lastLocation,
                                        weaponUsed = summary.weaponUsed,
                                        durationSeconds = summary.durationSeconds,
                                        replayFrames = summary.replayFrames,
                                        abandonedEarly = summary.abandonedEarly
                                    )
                                    navController.popBackStack(ROUTE_LOBBY, inclusive = false)
                                },
                                onOpenLeaderboardAfterMatch = { summary ->
                                    mainViewModel.recordMatchResult(
                                        mode = summary.mode,
                                        squadMode = summary.squadMode,
                                        arenaLevel = summary.arenaLevel,
                                        won = summary.won,
                                        placementRank = summary.placementRank,
                                        kills = summary.kills,
                                        revives = summary.revives,
                                        damageDealt = summary.damageDealt,
                                        rpDelta = summary.rpDelta,
                                        xpEarned = summary.xpEarned,
                                        goldEarned = summary.goldEarned,
                                        gemsEarned = summary.gemsEarned,
                                        lastLocation = summary.lastLocation,
                                        weaponUsed = summary.weaponUsed,
                                        durationSeconds = summary.durationSeconds,
                                        replayFrames = summary.replayFrames,
                                        abandonedEarly = summary.abandonedEarly
                                    )
                                    navController.popBackStack(ROUTE_LOBBY, inclusive = false)
                                    navController.navigate(ROUTE_LEADERBOARD)
                                },
                                onCompleteTrainingDrill = { targetsHit, durationSec ->
                                    mainViewModel.completeTrainingDrill(targetsHit, durationSec)
                                },
                                onExitWithoutSaving = {
                                    navController.popBackStack(ROUTE_LOBBY, inclusive = false)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (dailyPopup.visible && !isInMatch) {
            DailyRewardDialog(
                state = dailyPopup,
                currentStreak = profile.loginStreak,
                onClaim = { mainViewModel.claimDailyReward() },
                onDismiss = { mainViewModel.dismissDailyPopup() }
            )
        }

        if (showFriendsDialog && !isInMatch) {
            FriendsSquadDialog(
                friends = friends,
                onAddFriend = { callsign, rank -> mainViewModel.addFriend(callsign, rank) },
                onToggleSquad = { friend -> mainViewModel.toggleFriendSquad(friend) },
                onRemoveFriend = { friendId -> mainViewModel.removeFriend(friendId) },
                onDismiss = { showFriendsDialog = false }
            )
        }

        if (showCurrencyVault && !isInMatch) {
            CurrencyWalletDialog(
                profile = profile,
                transactions = currencyTransactions,
                statusMessage = currencyStoreMessage,
                onBuyMedkitSupply = { mainViewModel.buyDropMedkitSupply() },
                onBuyShieldSupply = { mainViewModel.buyDropShieldSupply() },
                onExchangeGoldForGems = { mainViewModel.exchangeGoldForGems() },
                onExchangeGemsForGold = { mainViewModel.exchangeGemsForGold() },
                onOpenStore = { navController.navigate(ROUTE_STORE) },
                onDismiss = { showCurrencyVault = false }
            )
        }
    }
}
