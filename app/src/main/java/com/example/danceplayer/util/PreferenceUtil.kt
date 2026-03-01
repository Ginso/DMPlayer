package com.example.danceplayer.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import androidx.core.content.edit

object PreferenceUtil {
    private const val PREFS_NAME = "dm_player_profiles"
    private const val CURRENT_PROFILE_KEY = "current_profile_key"
    private const val TAG_FILE = "TAG_FILE"

    private lateinit var sharedPreferences: SharedPreferences
    private var currentProfile: Profile = Profile()
    private var currentProfileKey: String = "Default"

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
            val profileJson = sharedPreferences.getString("profile_$savedKey", null)
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

    fun getTagFile(): String {
        return sharedPreferences.getString(TAG_FILE, "")!!
    }

    fun setTagFile(path: String) {
        sharedPreferences.edit { putString(TAG_FILE, path) }
    }

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
            .filter { it.startsWith("profile_") }
            .toList()
    }

    /**
     * Lädt das Profil mit dem gegebenen Key und setzt es als aktuelles Profil.
     */
    fun changeProfile(key: String) {
        val profileJson = sharedPreferences.getString("profile_$key", null)
        if (profileJson != null) {
            currentProfile = Profile.deserialize(profileJson)
            currentProfileKey = key
            sharedPreferences.edit { putString(CURRENT_PROFILE_KEY, key) }
        }
    }

    fun renameProfile(key:String) {
        val json = currentProfile.serialize()
        sharedPreferences.edit {
            remove("profile_$currentProfileKey")
                .putString(CURRENT_PROFILE_KEY, key)
                .putString("profile_$key", json)
        }
        currentProfileKey = key
    }

    /**
     * Speichert die Änderungen im aktuellen Profil persistent.
     */
    fun saveProfile() {
        if (currentProfileKey.isNotEmpty()) {
            saveProfilToPreferences(currentProfileKey, currentProfile)
        }
    }

    private fun saveProfilToPreferences(key: String, profile: Profile) {
        val json = profile.serialize()
        sharedPreferences.edit { putString("profile_$key", json) }
    }

    

}

data class Profile(
    var folder: String = "",
    var keepScreenOn: Boolean = false,
    var showOnLock: Boolean = false
) {
    companion object {
        fun deserialize(jsonString: String): Profile {
            return try {
                val json = JSONObject(jsonString)
                Profile(
                    folder = json.getString("folder"),
                    keepScreenOn = json.getBoolean("keepScreenOn"),
                    showOnLock = json.getBoolean("showOnLock")
                )
            } catch (_: Exception) {
                Profile()
            }
        }
    }

    fun serialize(): String {
        val json = JSONObject()
        json.put("folder", folder)
        json.put("keepScreenOn", keepScreenOn)
        json.put("showOnLock", showOnLock)
        return json.toString()
    }
}
