package com.example.danceplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.danceplayer.MainActivity

abstract class Fragment {

    companion object {
        private var nextStackEntryId = 0L
    }

    private val stackEntryId = nextStackEntryId++

    open val stackIdentity: Any
        get() = javaClass


    abstract fun getTitle(): String
    @Composable
    abstract fun Content()

    fun matchesStackEntry(other: Fragment): Boolean {
        return javaClass == other.javaClass && stackIdentity == other.stackIdentity
    }

    fun getStackEntryId(): Long {
        return stackEntryId
    }

    open fun sameType(other: Fragment): Boolean {
        return javaClass == other.javaClass
    }

    @Composable
    fun Main(center:Boolean=false, content: @Composable ColumnScope.() -> Unit) {
         
        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp, 4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = if(center) Alignment.CenterHorizontally else Alignment.Start
        ) {
            content()
        }
    }

    
    
}