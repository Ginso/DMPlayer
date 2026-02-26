package com.example.danceplayer.ui.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.ui.subpages.settings.CustomTagsPage
import kotlinx.coroutines.launch


@Composable
fun SettingsRow(label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = 16.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.weight(1f))
        Text("›", color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SettingsPage() {
    val profile = PreferenceUtil.getCurrentProfile()
    val context = LocalContext.current

    val profileKeys = remember { mutableStateOf(PreferenceUtil.getProfileKeys()) }
    val selectedProfile = remember { mutableStateOf(PreferenceUtil.getCurrentProfileKey()) }
    val isDropdownOpen = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val dialogTitle = remember { mutableStateOf("") }
    val dialogText = remember { mutableStateOf("") }
    val action = remember { mutableStateOf(PreferenceUtil::createNewProfile) }
    val folder = remember { mutableStateOf(profile.folder.substringAfterLast("/")) }
    val showCustomTags = remember { mutableStateOf(false) }
    val subPage = remember {mutableStateOf(0)}

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    
    val treeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // dauerhaft behalten
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            profile.folder = it.toString()
            PreferenceUtil.saveProfile()
            folder.value = profile.folder.substringAfterLast("/")
            coroutineScope.launch {
                val musicFiles = MusicLibrary.getMusicFiles(context)
                println("Gefundene Musikdateien: ${musicFiles.size}")
            }
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)


            ) {
                Text("Profile:", color = MaterialTheme.colorScheme.onBackground)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)

                ) {
                    OutlinedButton(
                        onClick = { isDropdownOpen.value = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedProfile.value, color = MaterialTheme.colorScheme.onBackground)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }

                    DropdownMenu(
                        expanded = isDropdownOpen.value,
                        onDismissRequest = { isDropdownOpen.value = false }
                    ) {
                        profileKeys.value.forEach { profile ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        profile,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                onClick = {
                                    selectedProfile.value = profile
                                    PreferenceUtil.changeProfile(profile)
                                    profileKeys.value = PreferenceUtil.getProfileKeys()
                                    isDropdownOpen.value = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = {
                        showDialog.value = true
                        dialogTitle.value = "Rename Profile"
                        dialogText.value = selectedProfile.value
                        action.value = PreferenceUtil::renameProfile
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Rename", color = MaterialTheme.colorScheme.onBackground)
                }

                Button(
                    onClick = {
                        showDialog.value = true
                        dialogTitle.value = "New Profile"
                        dialogText.value = selectedProfile.value
                        action.value = PreferenceUtil::createNewProfile
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("New Profile", color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Music Folder:", color = MaterialTheme.colorScheme.onBackground)
                Text(
                    profile.folder,
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = {
                        treeLauncher.launch(null)
                    }
                ) { Text("change", color = MaterialTheme.colorScheme.onBackground) }
            }
            SettingsRow(label = "Custom Tags") { subPage.value = 1 }
            SettingsRow(label = "Import/Export Tag Info") { subPage.value = 2 }
        }


    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(dialogTitle.value) },
            text = {
                TextField(
                    value = dialogText.value,
                    onValueChange = { dialogText.value = it },
                    placeholder = { Text("Neuer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialogText.value.isNotBlank()) {
                            action.value(dialogText.value)
                            selectedProfile.value = dialogText.value
                            profileKeys.value = PreferenceUtil.getProfileKeys()
                            dialogText.value = ""
                            showDialog.value = false
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        dialogText.value = ""
                        showDialog.value = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if(subPage.value == 1) CustomTagsPage{ subPage.value = 0 }

    
}

