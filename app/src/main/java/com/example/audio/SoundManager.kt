package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.R
import com.example.data.VehicleType
import com.example.data.WeaponCatalog
import com.example.data.WeaponCategory
import com.example.data.WeaponId
import com.example.data.WeatherCondition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

enum class SoundEffect(@RawRes val rawResId: Int) {
    WEAPON_RIFLE(R.raw.sfx_weapon_rifle),
    WEAPON_SMG(R.raw.sfx_weapon_smg),
    WEAPON_SNIPER(R.raw.sfx_weapon_sniper),
    WEAPON_HEAVY(R.raw.sfx_weapon_shotgun),
    FOOTSTEP(R.raw.sfx_footstep),
    VEHICLE_ENGINE(R.raw.sfx_vehicle_engine),
    UI_CLICK(R.raw.sfx_ui_click),
    UI_CONFIRM(R.raw.sfx_ui_confirm),
    RELOAD(R.raw.sfx_reload),
    MEDKIT(R.raw.sfx_medkit),
    HIT_MARKER(R.raw.sfx_hit_marker),
    EMOTE(R.raw.sfx_emote),
    THUNDER(R.raw.sfx_thunder)
}

data class SoundSettingsState(
    val isSfxEnabled: Boolean = true,
    val masterVolume: Float = 0.85f
)

class SoundManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        SoundSettingsState(
            isSfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true),
            masterVolume = prefs.getFloat(KEY_MASTER_VOLUME, 0.85f).coerceIn(0f, 1f)
        )
    )
    val settings: StateFlow<SoundSettingsState> = _settings.asStateFlow()

    private val soundIds = ConcurrentHashMap<SoundEffect, Int>()
    private val loadedSampleIds = ConcurrentHashMap.newKeySet<Int>()
    private val lastPlayTimesMs = ConcurrentHashMap<SoundEffect, Long>()

    private var soundPool: SoundPool? = null
    private var engineStreamId: Int = 0
    private var activeEngineVehicle: VehicleType? = null
    private var activeEngineMoving: Boolean = false

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val pool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attrs)
                .build()
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSampleIds.add(sampleId)
                }
            }
            SoundEffect.entries.forEach { effect ->
                val sid = pool.load(appContext, effect.rawResId, 1)
                soundIds[effect] = sid
            }
            soundPool = pool
        } catch (_: Throwable) {
            soundPool = null
        }
    }

    fun toggleSfx() {
        val nextEnabled = !_settings.value.isSfxEnabled
        prefs.edit().putBoolean(KEY_SFX_ENABLED, nextEnabled).apply()
        _settings.update { it.copy(isSfxEnabled = nextEnabled) }
        if (!nextEnabled) {
            stopVehicleEngine()
        } else {
            playUiClick()
        }
    }

    fun setMasterVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat(KEY_MASTER_VOLUME, clamped).apply()
        _settings.update { it.copy(masterVolume = clamped) }
        if (engineStreamId != 0) {
            try {
                val vol = clamped * 0.42f
                soundPool?.setVolume(engineStreamId, vol, vol)
            } catch (_: Throwable) {
            }
        }
    }

    fun playWeaponFire(weaponId: WeaponId) {
        val spec = WeaponCatalog.byId(weaponId)
        val effect = when (spec.category) {
            WeaponCategory.SNIPER -> SoundEffect.WEAPON_SNIPER
            WeaponCategory.MARKSMAN -> SoundEffect.WEAPON_HEAVY
            WeaponCategory.SMG -> SoundEffect.WEAPON_SMG
            WeaponCategory.AR -> SoundEffect.WEAPON_RIFLE
        }
        val minGapMs = when (spec.category) {
            WeaponCategory.SMG -> 60L
            WeaponCategory.AR -> 85L
            else -> 120L
        }
        val rate = 0.95f + Random.nextFloat() * 0.10f
        playOneShot(effect = effect, gain = 0.85f, rate = rate, minIntervalMs = minGapMs)
    }

    fun playFootstep(weather: WeatherCondition = WeatherCondition.CLEAR) {
        val baseRate = when (weather) {
            WeatherCondition.RAIN, WeatherCondition.STORM -> 1.07f
            WeatherCondition.FOG -> 0.94f
            WeatherCondition.CLEAR -> 1.0f
        }
        val jitter = (Random.nextFloat() - 0.5f) * 0.08f
        playOneShot(
            effect = SoundEffect.FOOTSTEP,
            gain = 0.42f,
            rate = (baseRate + jitter).coerceIn(0.8f, 1.3f),
            minIntervalMs = 310L
        )
    }

    fun updateVehicleEngine(vehicleType: VehicleType?, isMoving: Boolean) {
        if (!_settings.value.isSfxEnabled || vehicleType == null) {
            stopVehicleEngine()
            return
        }
        val targetRate = engineRateFor(vehicleType, isMoving)
        val targetVol = _settings.value.masterVolume * if (isMoving) 0.44f else 0.24f
        val pool = soundPool ?: return
        val sid = soundIds[SoundEffect.VEHICLE_ENGINE] ?: return
        if (sid !in loadedSampleIds) return

        try {
            if (engineStreamId == 0) {
                engineStreamId = pool.play(sid, targetVol, targetVol, 1, -1, targetRate)
                activeEngineVehicle = vehicleType
                activeEngineMoving = isMoving
            } else if (activeEngineVehicle != vehicleType || activeEngineMoving != isMoving) {
                pool.setRate(engineStreamId, targetRate)
                pool.setVolume(engineStreamId, targetVol, targetVol)
                activeEngineVehicle = vehicleType
                activeEngineMoving = isMoving
            }
        } catch (_: Throwable) {
        }
    }

    fun stopVehicleEngine() {
        if (engineStreamId != 0) {
            try {
                soundPool?.stop(engineStreamId)
            } catch (_: Throwable) {
            }
            engineStreamId = 0
        }
        activeEngineVehicle = null
        activeEngineMoving = false
    }

    fun playUiClick() {
        playOneShot(SoundEffect.UI_CLICK, gain = 0.55f, rate = 1.0f, minIntervalMs = 45L)
    }

    fun playUiConfirm() {
        playOneShot(SoundEffect.UI_CONFIRM, gain = 0.65f, rate = 1.0f, minIntervalMs = 80L)
    }

    fun playReload() {
        playOneShot(SoundEffect.RELOAD, gain = 0.72f, rate = 1.0f, minIntervalMs = 150L)
    }

    fun playMedkit() {
        playOneShot(SoundEffect.MEDKIT, gain = 0.70f, rate = 1.0f, minIntervalMs = 180L)
    }

    fun playHitMarker(isKill: Boolean = false) {
        playOneShot(
            effect = SoundEffect.HIT_MARKER,
            gain = if (isKill) 0.85f else 0.58f,
            rate = if (isKill) 1.24f else 1.0f,
            minIntervalMs = 55L
        )
    }

    fun playEmoteChime() {
        playOneShot(SoundEffect.EMOTE, gain = 0.75f, rate = 1.0f, minIntervalMs = 140L)
    }

    fun playThunder() {
        playOneShot(SoundEffect.THUNDER, gain = 0.80f, rate = 0.96f, minIntervalMs = 900L)
    }

    private fun engineRateFor(vehicleType: VehicleType, isMoving: Boolean): Float = when (vehicleType) {
        VehicleType.JEEP -> if (isMoving) 1.18f else 0.86f
        VehicleType.TUK_TUK -> if (isMoving) 1.38f else 1.05f
        VehicleType.SUPERCAR -> if (isMoving) 1.62f else 1.18f
        VehicleType.BIKE -> if (isMoving) 1.78f else 1.26f
    }

    private fun playOneShot(
        effect: SoundEffect,
        gain: Float,
        rate: Float,
        minIntervalMs: Long
    ) {
        val current = _settings.value
        if (!current.isSfxEnabled || current.masterVolume <= 0.01f) return
        val now = System.currentTimeMillis()
        val prev = lastPlayTimesMs[effect] ?: 0L
        if (now - prev < minIntervalMs) return

        val pool = soundPool ?: return
        val sid = soundIds[effect] ?: return
        if (sid !in loadedSampleIds) return

        lastPlayTimesMs[effect] = now
        val vol = (current.masterVolume * gain).coerceIn(0f, 1f)
        try {
            pool.play(sid, vol, vol, 1, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (_: Throwable) {
        }
    }

    fun release() {
        stopVehicleEngine()
        try {
            soundPool?.release()
        } catch (_: Throwable) {
        }
        soundPool = null
        loadedSampleIds.clear()
        soundIds.clear()
    }

    companion object {
        private const val PREFS_NAME = "infine_audio_prefs"
        private const val KEY_SFX_ENABLED = "sfx_enabled"
        private const val KEY_MASTER_VOLUME = "master_volume"

        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
