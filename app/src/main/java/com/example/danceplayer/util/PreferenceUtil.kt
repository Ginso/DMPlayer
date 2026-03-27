package com.example.danceplayer.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import org.json.JSONObject
import androidx.core.content.edit
import com.example.danceplayer.ui.subpages.settings.FilterPage
import com.example.danceplayer.ui.subpages.settings.ItemLayoutsPage
import org.json.JSONArray
import java.io.File

object PreferenceUtil {
    private const val PREFS_NAME = "dm_player_profiles"
    private const val CURRENT_PROFILE_KEY = "current_profile_key"
    private const val TAG_FILE = "TAG_FILE"
    private const val PLAYER_MODE = "PLAYER_MODE"



    private const val DEFAULT_TAG_FILE_NAME = "DancePlayerTags.json"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appContext: Context
    private var currentProfile: Profile = Profile()
    private var currentProfileKey: String = "Default"

    /**
     * Initialisiert den PreferenceUtil mit dem Application Context.
     * Muss einmalig beim App-Start aufgerufen werden.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
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

    fun getAppContextOrNull(): Context? {
        return if (::appContext.isInitialized) appContext else null
    }

    fun getTagFile(): String {
        val configuredPath = sharedPreferences.getString(TAG_FILE, "")!!
        if (configuredPath.isNotBlank()) return configuredPath

        if (!::appContext.isInitialized) return ""
        val defaultFile = File(appContext.filesDir, DEFAULT_TAG_FILE_NAME)
        return Uri.fromFile(defaultFile).toString()
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

    fun setPlayerMode(mode: Int) {
        sharedPreferences.edit { putInt(PLAYER_MODE, mode) }
    }

    fun getPlayerMode(): Int {
        return sharedPreferences.getInt(PLAYER_MODE, ExoPlayer.REPEAT_MODE_OFF)
    }

    

}

data class Profile(
    var folder: String = "",
    var keepScreenOn: Boolean = false,
    var showOnLock: Boolean = false,
    var filterOptions: JSONArray = FilterPage.getDefaultFilterOptions(),
    var itemLayoutBrowser: JSONObject = ItemLayoutsPage.getDefaultLayout(0),
    var itemLayoutPlaylists: JSONObject = ItemLayoutsPage.getDefaultLayout(1),
    var itemLayoutQueue: JSONObject = ItemLayoutsPage.getDefaultLayout(2),
    var itemLayoutQueueParty: JSONObject = ItemLayoutsPage.getDefaultLayout(3)
) {

    companion object {
        fun deserialize(jsonString: String): Profile {
            return try {
                val json = JSONObject(jsonString)
                Profile(
                    folder = json.optString("folder", ""),
                    keepScreenOn = json.optBoolean("keepScreenOn", false),
                    showOnLock = json.optBoolean("showOnLock", false),
                    filterOptions = json.optJSONArray("filterOptions") ?: FilterPage.getDefaultFilterOptions(),
                    itemLayoutBrowser = json.optJSONObject("itemLayoutBrowser") ?: ItemLayoutsPage.getDefaultLayout(0),
                    itemLayoutPlaylists = json.optJSONObject("itemLayoutPlaylists") ?: ItemLayoutsPage.getDefaultLayout(1),
                    itemLayoutQueue = json.optJSONObject("itemLayoutQueue") ?: ItemLayoutsPage.getDefaultLayout(2),
                    itemLayoutQueueParty = json.optJSONObject("itemLayoutQueueParty") ?: ItemLayoutsPage.getDefaultLayout(3)
                )
            } catch (_: Exception) {
                Profile()
            }
        }
    }

    fun getLayout(category: Int): JSONObject {
        return when(category) {
            0 -> itemLayoutBrowser
            1 -> itemLayoutPlaylists
            2 -> itemLayoutQueue
            3 -> itemLayoutQueueParty
            else -> JSONObject() // default empty layout
        }
    }

    fun serialize(): String {
        val json = JSONObject()
        json.put("folder", folder)
        json.put("keepScreenOn", keepScreenOn)
        json.put("showOnLock", showOnLock)
        json.put("filterOptions", filterOptions)
        return json.toString()
    }
}
