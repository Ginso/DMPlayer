package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.ui.Fragment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagFilePage(onBack: () -> Unit) {
    val errorText = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }

    Fragment("Import/Export Tag Info", onBack) {
    
        Text("The tags that you defined and the values for every song are being stored in a file. Here you can set the location of that file(to export it) or choose an existing file to import.", color = MaterialTheme.colorScheme.onBackground)
        HorizontalDivider()
        Text("Export", style = MaterialTheme.typography.titleLarge)
        Text("Specify the location where the file should be saved at. This file will be used from then on.", color = MaterialTheme.colorScheme.onBackground)
        // launcher for creating or choosing a file location
        val context = LocalContext.current
        val exportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri -> export(context, uri, errorText)}
        // use OpenDocument instead of GetContent so we can take persistable
        // URI permissions; GetContent only grants a one-time permission which
        // causes a SecurityException when we try to persist it.
        val importLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            coroutineScope.launch {
                import(context, uri, errorText)
            }
        }

        Button(
            onClick = {
                // launch with a suggested filename
                exportLauncher.launch("DancePlayerTags.json")
            }
        ) {
            Text("Export / Choose Location")
        }
        HorizontalDivider()
        Text("Import", style = MaterialTheme.typography.titleLarge)
        Text("Choose an existing file to import tag information from. That file will be used from then on. Your current tag information will be lost.", color = MaterialTheme.colorScheme.onBackground)
        Button(
            onClick = {
                // OpenDocument expects an array of MIME types
                importLauncher.launch(arrayOf("application/json"))
            }
        ) {
            Text("Import / Choose File")
        }

    }

    
    if (errorText.value.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { errorText.value = "" },
            title = { Text("Error") },
            text = {
                Text(
                    errorText.value,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Red
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        errorText.value = ""
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    if (isLoading.value) {
        AlertDialog(
            onDismissRequest = { /* do nothing */ },
            title = { Text("Loading...") },
            text = {
                Text(
                    "Please wait...",
                    modifier = Modifier.fillMaxWidth().centerHorizontally(),
                )
            },
            confirmButton = { /* no buttons */ }
        )
    }
}

fun export(context: Context, uri: Uri?, isLoading: MutableState<Boolean>, errorText: MutableState<String>) {
    uri?.let {
        isLoading.value = true
        val resolver = context.contentResolver
        var existed = false
        // check if file already has content
        resolver.openFileDescriptor(it, "r")?.use { pfd ->
            if (pfd.statSize > 0) {
                existed = true
            }
        }
        if (existed) {
            errorText.value = "File already exists. " +
                "If you want to use that file(and overwrite the current tags), use Import. " +
                "If you want to overwrite this file, please delete it first and then export again."
        } else {
            // write JSON data to new file
            resolver.openOutputStream(it)?.use { out ->
                out.write(MusicLibrary.asJSON().toString().toByteArray())
            }
            Toast.makeText(context, "Datei erstellt und gespeichert", Toast.LENGTH_SHORT).show()
        }
        // take persistable permission and store path (may throw if URI isn't
        // persistable)
        try {
            resolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // permission not persistable; nothing to do
        }
        PreferenceUtil.setTagFile(it.toString())
        MusicLibrary.getMusicFiles(context) // reload music files to update tags
        isLoading.value = false
    }
}

suspend fun import(context: Context, uri: Uri?, isLoading: MutableState<Boolean>, errorText: MutableState<String>) {
    uri?.let {
        val resolver = context.contentResolver
        isLoading.value = true

        val success = MusicLibrary.loadTagFile(context, it) { msg ->
            errorText.value = msg
        }
        if (!success) {
            isLoading.value = false
            return
        }

        // try to keep access to the chosen document. OpenDocument will usually
        // grant a persistable permission, but wrap in a try/catch just in case.
        try {
            resolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // not persistable, ignore
        }
        PreferenceUtil.setTagFile(it.toString())
        MusicLibrary.getMusicFiles(context) // reload music files to update tags
        isLoading.value = false
    }
}