package com.sagar.fluenty.ui.managers

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

interface EncryptedSharedPreferencesManager {
    fun save(key: String, value: String)
    fun get(key: String): String?
}

class EncryptedSharedPreferencesManagerImpl(
    context: Context,
) : EncryptedSharedPreferencesManager {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Step 2: Initialize/open an instance of EncryptedSharedPreferences
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "PreferencesFilename",
        masterKeyAlias,
        context.applicationContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun save(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
}