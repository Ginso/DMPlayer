package com.example.danceplayer.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object PreferenceUtil {
    private lateinit var sharedPreferences: SharedPreferences
    private var currentProfile: Profile = Profile()
    private var currentProfileKey: String = "Default"
    private const val PREFS_NAME = "dm_player_profiles"
    private const val CURRENT_PROFILE_KEY = "current_profile_key"

    /**
     * Initialisiert den PreferenceUtil mit dem Application Context.
     * Muss einmalig beim App-Start aufgerufen werden.
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Lade das zuletzt verwendete Profil
        val savedKey = sharedPreferences.getString(CURRENT_PROFILE_KEY, "default")
        if (savedKey != null) {
            currentProfileKey = savedKey
            val profileJson = sharedPreferences.getString(savedKey, null)
            if (profileJson != null) {
                currentProfile = Profile.deserialize(profileJson)
            }
        }
    }

    /**
     * Gibt das aktuell ausgewählte Profil zurück.
     */
    fun getCurrentProfile(): Profile = currentProfile
    fun getCurrentProfileKey(): String = currentProfileKey

    /**
     * Kopiert das aktuelle Profil und speichert es unter einem neuen Key.
     */
    fun createNewProfile(key: String) {
        saveProfilToPreferences(key, currentProfile)
    }

    /**
     * Gibt alle Keys zurück, unter denen ein Profil gespeichert ist.
     */
    fun getProfileKeys(): List<String> {
        return sharedPreferences.all.keys
            .filter { it != CURRENT_PROFILE_KEY }
            .toList()
    }

    /**
     * Lädt das Profil mit dem gegebenen Key und setzt es als aktuelles Profil.
     */
    fun changeProfile(key: String) {
        val profileJson = sharedPreferences.getString(key, null)
        if (profileJson != null) {
            currentProfile = Profile.deserialize(profileJson)
            currentProfileKey = key
            sharedPreferences.edit().putString(CURRENT_PROFILE_KEY, key).apply()
        }
    }

    fun renameProfile(key:String) {
        val json = currentProfile.serialize()
        sharedPreferences.edit()
            .remove(currentProfileKey)
            .putString(CURRENT_PROFILE_KEY, key)
            .putString(key, json)
            .apply()
        currentProfileKey = key
    }

    /**
     * Speichert die Änderungen im aktuellen Profil persistent.
     */
    fun save() {
        if (currentProfileKey.isNotEmpty()) {
            saveProfilToPreferences(currentProfileKey, currentProfile)
        }
    }

    private fun saveProfilToPreferences(key: String, profile: Profile) {
        val json = profile.serialize()
        sharedPreferences.edit().putString(key, json).apply()
    }

    

}

data class Profile(
    var folder: String = "",
    var keepScreenOn: Boolean = false
) {
    companion object {
        fun deserialize(jsonString: String): Profile {
            return try {
                val json = JSONObject(jsonString)
                Profile(
                    folder = json.getString("folder"),
                    keepScreenOn = json.getBoolean("keepScreenOn")
                )
            } catch (e: Exception) {
                Profile()
            }
        }
    }

    fun serialize(): String {
        val json = JSONObject()
        json.put("folder", folder)
        json.put("keepScreenOn", keepScreenOn)
        return json.toString()
    }
}
