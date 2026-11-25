package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM match_history ORDER BY timestampMs DESC LIMIT 30")
    fun observeMatchHistory(): Flow<List<MatchHistoryEntity>>

    @Insert
    suspend fun insertMatchHistory(entry: MatchHistoryEntity)

    // Social Friends
    @Query("SELECT * FROM social_friends ORDER BY isInSquad DESC, addedAtMs DESC")
    fun observeFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM social_friends WHERE callsign = :callsign LIMIT 1")
    suspend fun getFriendByCallsign(callsign: String): FriendEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFriend(friend: FriendEntity)

    @Query("UPDATE social_friends SET isInSquad = :inSquad WHERE id = :id")
    suspend fun setFriendInSquad(id: Int, inSquad: Boolean)

    @Query("DELETE FROM social_friends WHERE id = :id")
    suspend fun deleteFriendById(id: Int)

    // Weapon Skins
    @Query("SELECT * FROM weapon_skins")
    fun observeWeaponSkins(): Flow<List<WeaponSkinStateEntity>>

    @Query("SELECT * FROM weapon_skins WHERE weaponName = :weaponName LIMIT 1")
    suspend fun getWeaponSkinOnce(weaponName: String): WeaponSkinStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeaponSkin(state: WeaponSkinStateEntity)

    // Global Leaderboard
    @Query("SELECT * FROM leaderboard_entries")
    fun observeLeaderboardEntries(): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries")
    suspend fun getLeaderboardEntriesOnce(): List<LeaderboardEntryEntity>

    @Query("SELECT * FROM leaderboard_entries WHERE callsign = :callsign LIMIT 1")
    suspend fun getLeaderboardEntryByCallsign(callsign: String): LeaderboardEntryEntity?

    @Query("SELECT COUNT(*) FROM leaderboard_entries")
    suspend fun getLeaderboardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLeaderboardEntries(entries: List<LeaderboardEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLeaderboardEntry(entry: LeaderboardEntryEntity)

    // Currency Ledger
    @Query("SELECT * FROM currency_transactions ORDER BY timestampMs DESC LIMIT 25")
    fun observeCurrencyTransactions(): Flow<List<CurrencyTransactionEntity>>

    @Insert
    suspend fun insertCurrencyTransaction(entry: CurrencyTransactionEntity)

    // Suspicious Behavior / Anti-Cheat Incident Reports
    @Query("SELECT * FROM incident_reports ORDER BY timestampMs DESC LIMIT 30")
    fun observeIncidentReports(): Flow<List<IncidentReportEntity>>

    @Insert
    suspend fun insertIncidentReport(report: IncidentReportEntity)

    // Clans & Squads
    @Query("SELECT * FROM clans ORDER BY isJoined DESC, clanXp DESC")
    fun observeClans(): Flow<List<ClanEntity>>

    @Query("SELECT * FROM clans")
    suspend fun getClansOnce(): List<ClanEntity>

    @Query("SELECT * FROM clans WHERE isJoined = 1 LIMIT 1")
    suspend fun getJoinedClanOnce(): ClanEntity?

    @Query("SELECT * FROM clans WHERE id = :id LIMIT 1")
    suspend fun getClanByIdOnce(id: Int): ClanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClan(clan: ClanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClans(clans: List<ClanEntity>)

    @Query("UPDATE clans SET isJoined = 0")
    suspend fun clearJoinedClans()

    // Clan-Only Missions
    @Query("SELECT * FROM clan_missions")
    fun observeClanMissions(): Flow<List<ClanMissionProgressEntity>>

    @Query("SELECT * FROM clan_missions")
    suspend fun getClanMissionsOnce(): List<ClanMissionProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClanMissions(missions: List<ClanMissionProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClanMission(mission: ClanMissionProgressEntity)

    @Query("DELETE FROM clan_missions")
    suspend fun clearClanMissions()

    // Match Replays
    @Query("SELECT * FROM match_replays ORDER BY timestampMs DESC LIMIT 20")
    fun observeMatchReplays(): Flow<List<MatchReplayEntity>>

    @Query("SELECT * FROM match_replays WHERE id = :id LIMIT 1")
    suspend fun getMatchReplayById(id: Int): MatchReplayEntity?

    @Insert
    suspend fun insertMatchReplay(replay: MatchReplayEntity): Long
}




