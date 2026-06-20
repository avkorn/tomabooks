package com.example.tomabooks

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tomabooks.ui.theme.TomaBooksTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TomaBooksTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            viewModel,
                            onNavigateToSettings = { navController.navigate("settings") })
                    }
                    composable("settings") {
                        SettingsScreen(viewModel, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel, onNavigateToSettings: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isBlindMode) {
        BlindMainScreen(uiState, viewModel, onNavigateToSettings)
    } else {
        StandardMainScreen(uiState, viewModel, onNavigateToSettings)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StandardMainScreen(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artwork / Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.artwork != null) {
                    Image(
                        bitmap = uiState.artwork!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.no_book_selected),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Book Title (Metadata)
            val titleStyle = MaterialTheme.typography.headlineMedium
            val displayName = uiState.bookTitle
                ?: if (uiState.currentBook != null) stringResource(R.string.loading_metadata) else stringResource(
                    R.string.no_book_selected
                )
            Text(
                text = displayName,
//                style = MaterialTheme.typography.headlineMedium,
                style = titleStyle.copy(
                    fontSize = minOf((titleStyle.fontSize * fontScale).value, 20f).sp,
                    fontWeight = FontWeight.Bold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = minOf((titleStyle.fontSize * fontScale).value, 20f).sp * 0.9f,

                    ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Author (Metadata)
            if (!uiState.author.isNullOrEmpty()) {
                val authorStyle = MaterialTheme.typography.headlineSmall
                Text(
                    text = uiState.author,
                    //style = MaterialTheme.typography.titleMedium,
                    style = authorStyle.copy(
                        fontSize = minOf((authorStyle.fontSize * fontScale).value, 20f).sp

                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Progress Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = uiState.currentPosition.longToFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..(uiState.duration.coerceAtLeast(1L).toFloat()),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(uiState.currentPosition),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatTime(uiState.duration),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                // To the start
                Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { viewModel.seekToStart() }) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = stringResource(R.string.to_the_start),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Rewind N seconds
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { viewModel.rewind() }) {
                            Icon(
                                Icons.Default.FastRewind,
                                contentDescription = stringResource(R.string.rewind),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Text(
                        text = uiState.rewindForwardSeconds.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Play/Pause Control
                Button(
                    onClick = { viewModel.playPause() },
                    modifier = Modifier.size(width = 72.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) stringResource(R.string.pause) else stringResource(
                            R.string.play
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Forward N seconds
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { viewModel.forward() }) {
                            Icon(
                                Icons.Default.FastForward,
                                contentDescription = stringResource(R.string.forward),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Text(
                        text = uiState.rewindForwardSeconds.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // To the end (next book)
                Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { viewModel.seekToEnd() }) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.to_the_end),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BlindMainScreen(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var clickCount by remember { mutableIntStateOf(0) }
    val tripleClickThreshold = 500L // Time window (ms) to complete the next click
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(32.dp)
                            .combinedClickable(
                                onClick = { /* Ignore single click */ },
                                onDoubleClick = onNavigateToSettings
                            ),
//                            .pointerInput(Unit) {
//                                detectTapGestures(
//                                    onTap = {
//                                        val currentTime = System.currentTimeMillis()
//                                        if (currentTime - lastClickTime < tripleClickThreshold) {
//                                            clickCount++
//                                        } else {
//                                            clickCount = 1 // Reset if too much time passed
//                                        }
//                                        lastClickTime = currentTime
//
//                                        if (clickCount >= 3) {
//                                            onNavigateToSettings()
//                                            clickCount = 0 // Reset after success
//                                        }
//                                    }
//                                )
//                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Author and Book Name in large text
            val authorStyle = MaterialTheme.typography.headlineSmall
            val titleStyle = MaterialTheme.typography.headlineMedium
            Text(
                text = uiState.author ?: "",
//                style = MaterialTheme.typography.headlineSmall,
                style = authorStyle.copy(
                    fontSize = minOf((authorStyle.fontSize * fontScale).value, 20f).sp
                ),
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = uiState.bookTitle
                    ?: if (uiState.currentBook != null) stringResource(R.string.loading_metadata) else stringResource(
                        R.string.no_book_selected
                    ),
//                style = MaterialTheme.typography.headlineMedium,
                style = titleStyle.copy(
                    fontSize = minOf((titleStyle.fontSize * fontScale).value, 20f).sp,
                    fontWeight = FontWeight.Bold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = minOf(
                        (titleStyle.fontSize * fontScale).value,
                        20f
                    ).sp * 0.9f,
                ),
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Read-only Progress Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Slider(
                    value = uiState.currentPosition.longToFloat(),
                    onValueChange = { /* Read only */ },
                    valueRange = 0f..(uiState.duration.coerceAtLeast(1L).toFloat()),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(uiState.currentPosition),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = formatTime(uiState.duration),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Giant Play/Pause Button - Fills the rest of the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (uiState.isPlaying) Color.Red else Color.Green) // Pure Red/Green
                    .combinedClickable(
                        onClick = { viewModel.playPause() },
                        onLongClick = { viewModel.playPause() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) stringResource(R.string.pause) else stringResource(
                        R.string.play
                    ),
                    modifier = Modifier.fillMaxSize(0.7f),
                    tint = Color.White
                )
            }
        }
    }
}

fun Long.longToFloat(): Float = this.toFloat()

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale.coerceAtMost(1.2f)
    )
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.setFolderUri(it) }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    var deleteFromStorage by remember { mutableStateOf(false) }

    if (showDeleteDialog && bookToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                bookToDelete = null
            },
            title = { Text(stringResource(R.string.remove_book)) },
            text = {
                Column {
                    Text(
                        stringResource(
                            R.string.remove_book_confirmation,
                            bookToDelete?.title ?: bookToDelete?.name ?: ""
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            deleteFromStorage = !deleteFromStorage
                        }
                    ) {
                        Checkbox(
                            checked = deleteFromStorage,
                            onCheckedChange = { deleteFromStorage = it }
                        )
                        Text(
                            stringResource(R.string.delete_from_storage),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    bookToDelete?.let { viewModel.removeBook(it, deleteFromStorage) }
                    showDeleteDialog = false
                    bookToDelete = null
                    deleteFromStorage = false
                }) {
                    Text(
                        stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    bookToDelete = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    CompositionLocalProvider(LocalDensity provides customDensity) {
        val uiState by viewModel.uiState.collectAsState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { launcher.launch(null) },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.scanning))
                        } else {
                            Text(stringResource(R.string.select_folder))
                        }
                    }

                    IconButton(
                        onClick = { viewModel.reloadBooks() },
                        enabled = !uiState.isLoading && uiState.folderUri != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.reload_books),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = stringResource(
                        R.string.current_folder,
                        uiState.folderUri?.path ?: stringResource(R.string.not_selected)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Playback Settings
//            Text(stringResource(R.string.playback_settings), style = MaterialTheme.typography.titleMedium)
//            Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.rewind_forward_step),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            viewModel.setRewindForwardSeconds(
                                (uiState.rewindForwardSeconds - 10).coerceAtLeast(
                                    10
                                )
                            )
                        }) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = stringResource(R.string.decrease)
                            )
                        }
                        Text(
                            text = "${uiState.rewindForwardSeconds}s",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 48.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = {
                            viewModel.setRewindForwardSeconds(uiState.rewindForwardSeconds + 10)
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.increase)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Blind Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f) // This forces the column to shrink/wrap instead of pushing the Switch
                            .padding(end = 16.dp) // Gap between the text and the switch
                    ) {
                        Text(
                            stringResource(R.string.blind_mode),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            stringResource(R.string.blind_mode_desc),
                            style = MaterialTheme.typography.bodySmall,
                            softWrap = true,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Switch(
                        checked = uiState.isBlindMode,
                        onCheckedChange = { viewModel.setBlindMode(it) },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    stringResource(R.string.books_list),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.books.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.no_books_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        items(uiState.books) { book ->
                            val isSelected =
                                book.uri.toString() == uiState.currentBook?.uri.toString()

                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = book.author
                                            ?: stringResource(R.string.unknown_author),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = book.title ?: book.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.PlayArrow else Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = {
                                        bookToDelete = book
                                        showDeleteDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.remove_from_list)
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.selectBook(book)
                                        onBack()
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
