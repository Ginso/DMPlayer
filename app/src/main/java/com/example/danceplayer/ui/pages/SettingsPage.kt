package com.example.danceplayer.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.danceplayer.util.PreferenceUtil

@Composable
fun SettingsPage() {
    val profileKeys = remember { mutableStateOf(PreferenceUtil.getProfileKeys()) }
    val selectedProfile = remember { mutableStateOf(PreferenceUtil.getProfileKeys().firstOrNull() ?: "Default") }
    val isDropdownOpen = remember { mutableStateOf(false) }
    val showRenameDialog = remember { mutableStateOf(false) }
    val showCreateDialog = remember { mutableStateOf(false) }
    val dialogText = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Profil auswählen:", modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { isDropdownOpen.value = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedProfile.value)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }

                DropdownMenu(
                    expanded = isDropdownOpen.value,
                    onDismissRequest = { isDropdownOpen.value = false }
                ) {
                    profileKeys.value.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile) },
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
                onClick = { showRenameDialog.value = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Umbenennen")
            }

            Button(
                onClick = { showCreateDialog.value = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Neues Profil")
            }
        }
    }

    if (showRenameDialog.value) {
        AlertDialog(
            onDismissRequest = { showRenameDialog.value = false },
            title = { Text("Profil umbenennen") },
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
                            PreferenceUtil.renameProfile(dialogText.value)
                            selectedProfile.value = dialogText.value
                            profileKeys.value = PreferenceUtil.getProfileKeys()
                            dialogText.value = ""
                            showRenameDialog.value = false
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
                        showRenameDialog.value = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (showCreateDialog.value) {
        AlertDialog(
            onDismissRequest = { showCreateDialog.value = false },
            title = { Text("Neues Profil erstellen") },
            text = {
                TextField(
                    value = dialogText.value,
                    onValueChange = { dialogText.value = it },
                    placeholder = { Text("Profilname") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialogText.value.isNotBlank()) {
                            PreferenceUtil.createNewProfile(dialogText.value)
                            PreferenceUtil.changeProfile(dialogText.value)
                            selectedProfile.value = dialogText.value
                            profileKeys.value = PreferenceUtil.getProfileKeys()
                            dialogText.value = ""
                            showCreateDialog.value = false
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
                        showCreateDialog.value = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
