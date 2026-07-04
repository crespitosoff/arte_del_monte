package com.example.arte_del_monte.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.brandDataStore: DataStore<Preferences> by preferencesDataStore(name = "brand_settings")

class BrandSettingsDataStore(private val context: Context) {
    private object Keys {
        val BUSINESS_NAME    = stringPreferencesKey("business_name")
        val SLOGAN           = stringPreferencesKey("slogan")
        val PHONE            = stringPreferencesKey("phone")
        val LOCATION         = stringPreferencesKey("location")
        val NIT              = stringPreferencesKey("nit")
        val LOGO_PATH        = stringPreferencesKey("logo_path")
        val COLOR_PRIMARY    = intPreferencesKey("color_primary")
        val COLOR_SECONDARY  = intPreferencesKey("color_secondary")
        val COLOR_EMPHASIS   = intPreferencesKey("color_emphasis")
        val COLOR_BACKGROUND = intPreferencesKey("color_background")
        val TAX_ENABLED      = booleanPreferencesKey("tax_enabled")
        val TAX_RATE         = floatPreferencesKey("tax_rate")
    }

    val settings: Flow<BrandSettings> = context.brandDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            BrandSettings(
                businessName  = prefs[Keys.BUSINESS_NAME]    ?: "Arte de Monte",
                slogan        = prefs[Keys.SLOGAN]           ?: "Mobiliario macizo de alta gama",
                phone         = prefs[Keys.PHONE]            ?: "",
                location      = prefs[Keys.LOCATION]         ?: "",
                nit           = prefs[Keys.NIT]              ?: "",
                logoPath      = prefs[Keys.LOGO_PATH]        ?: "",
                colorPrimary  = prefs[Keys.COLOR_PRIMARY]    ?: 0xFF3C2619.toInt(),
                colorSecondary= prefs[Keys.COLOR_SECONDARY]  ?: 0xFF745038.toInt(),
                colorEmphasis = prefs[Keys.COLOR_EMPHASIS]   ?: 0xFF201E1D.toInt(),
                colorBackground= prefs[Keys.COLOR_BACKGROUND]?: 0xFFE9DECE.toInt(),
                taxEnabled    = prefs[Keys.TAX_ENABLED]      ?: false,
                taxRate       = (prefs[Keys.TAX_RATE]        ?: 0f).toDouble()
            )
        }

    suspend fun save(s: BrandSettings) {
        context.brandDataStore.edit { prefs ->
            prefs[Keys.BUSINESS_NAME]    = s.businessName
            prefs[Keys.SLOGAN]           = s.slogan
            prefs[Keys.PHONE]            = s.phone
            prefs[Keys.LOCATION]         = s.location
            prefs[Keys.NIT]              = s.nit
            prefs[Keys.LOGO_PATH]        = s.logoPath
            prefs[Keys.COLOR_PRIMARY]    = s.colorPrimary
            prefs[Keys.COLOR_SECONDARY]  = s.colorSecondary
            prefs[Keys.COLOR_EMPHASIS]   = s.colorEmphasis
            prefs[Keys.COLOR_BACKGROUND] = s.colorBackground
            prefs[Keys.TAX_ENABLED]      = s.taxEnabled
            prefs[Keys.TAX_RATE]         = s.taxRate.toFloat()
        }
    }
}
