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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.danceplayer.ui.Fragment

@Composable
fun TagFilePage(onBack: () -> Unit) {
    Fragment("Import/Export Tag Info", onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("The tags that you defined and the values for every song are being stored in a file. Here you can set the location of that file(to export it) or choose an existing file to import.", color = MaterialTheme.colorScheme.onBackground)
            Text("Export", style = MaterialTheme.typography.titleLarge)


        }
    }
}