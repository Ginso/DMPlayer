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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.danceplayer.util.PreferenceUtil

@Composable
fun SettingsPage() {
    val profile = PreferenceUtil.getCurrentProfile()

    val profileKeys = remember { mutableStateOf(PreferenceUtil.getProfileKeys()) }
    val selectedProfile = remember { mutableStateOf(PreferenceUtil.getCurrentProfileKey()) }
    val isDropdownOpen = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val dialogTitle = remember { mutableStateOf("") }
    val dialogText = remember { mutableStateOf("") }
    val action = remember { mutableStateOf(PreferenceUtil::createNewProfile) }
    val showFileTree = remember { mutableStateOf(false) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)


        ) {
            Text("Profile:")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)

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
                Text("Rename")
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
                Text("New Profile")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Music Folder:")
            Text(profile.folder, modifier = Modifier.padding(start=16.dp))
            Button (
                onClick = { showFileTree.value=true }
            ) { Text("change")}
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
}
