package com.example.danceplayer.ui.pages

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.danceplayer.util.PreferenceUtil
import java.io.File

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
    val showFileTree = remember { mutableStateOf(false) }
    val currentPath = remember { mutableStateOf(if (profile.folder.isBlank()) "/storage" else profile.folder) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showFileTree.value = true
        }
    }



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
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    
                    if (hasPermission) {
                        showFileTree.value = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
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

    if (showFileTree.value) {
        val subfolders = remember(currentPath.value) {
            val dir = File(currentPath.value)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.filter { it.isDirectory }?.sortedBy { it.name } ?: emptyList()
            } else {
                emptyList()
            }
        }

        AlertDialog(
            onDismissRequest = { showFileTree.value = false },
            title = { Text("Select Music Folder") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = currentPath.value,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    Divider()
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        if(currentPath.value != "/storage") {
                            Text(
                                text = ".."
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        //currentPath.value = folder.absolutePath
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            )
                        }
                        items(subfolders) { folder ->
                            Text(
                                text = folder.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentPath.value = folder.absolutePath
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFileTree.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showFileTree.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
