package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        MatchHistoryEntity::class,
        FriendEntity::class,
        WeaponSkinStateEntity::class,
        LeaderboardEntryEntity::class,
        CurrencyTransactionEntity::class,
        IncidentReportEntity::class,
        ClanEntity::class,
        ClanMissionProgressEntity::class,
        MatchReplayEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "infine_arena.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
