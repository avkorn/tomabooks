package com.example.tomabooks

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.net.toUri

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val player: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var positionUpdateJob: Job? = null

    data class UiState(
        val folderUri: Uri? = null,
        val books: List<Book> = emptyList(),
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val currentBook: Book? = null,
        val artwork: Bitmap? = null,
        val currentPosition: Long = 0L,
        val duration: Long = 0L,
        val bookTitle: String? = null,
        val author: String? = null,
        val rewindForwardSeconds: Int = 20,
        val isBlindMode: Boolean = false
    )

    init {
        viewModelScope.launch {
            repository.isBlindMode.collect { blindMode ->
                _uiState.value = _uiState.value.copy(isBlindMode = blindMode)
            }
        }

        viewModelScope.launch {
            val savedUriString = repository.folderUri.first()
            val rewindSeconds = repository.rewindForwardSeconds.first()
            _uiState.value = _uiState.value.copy(rewindForwardSeconds = rewindSeconds)

            if (savedUriString != null) {
                val uri = savedUriString.toUri()
                _uiState.value = _uiState.value.copy(folderUri = uri)
                if (loadBooksFromSavedList() || loadBooksFromFolderSuspend(uri)) {
                    restorePlaybackState()
                }
            }
        }

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startPositionUpdate()
                } else {
                    stopPositionUpdate()
                    savePlaybackState()
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val bitmap = mediaMetadata.artworkData?.let { data ->
                    BitmapFactory.decodeByteArray(data, 0, data.size)
                }

                _uiState.value = _uiState.value.copy(
                    artwork = bitmap ?: _uiState.value.artwork,
                    bookTitle = mediaMetadata.albumTitle?.toString() ?: mediaMetadata.displayTitle?.toString() ?: _uiState.value.bookTitle,
                    author = mediaMetadata.artist?.toString() ?: mediaMetadata.albumArtist?.toString() ?: _uiState.value.author
                )
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.value = _uiState.value.copy(
                        duration = player.duration,
                        currentPosition = player.currentPosition
                    )
                } else if (playbackState == Player.STATE_ENDED) {
                    playNextBook()
                }
            }
        })
    }

    private fun playNextBook() {
        val books = _uiState.value.books
        val currentBook = _uiState.value.currentBook ?: return
        val currentIndex = books.indexOfFirst { it.uri.toString() == currentBook.uri.toString() }

        if (currentIndex != -1) {
            // Reset position for the book that just finished
            viewModelScope.launch {
                repository.saveLastPlayback(currentBook.uri.toString(), 0L)
            }

            if (currentIndex < books.size - 1) {
                val nextBook = books[currentIndex + 1]
                selectBook(nextBook)
            }
        }
    }

//    fun playPreviousBook() {
//        val books = _uiState.value.books
//        val currentBook = _uiState.value.currentBook ?: return
//        val currentIndex = books.indexOfFirst { it.uri.toString() == currentBook.uri.toString() }
//
//        if (currentIndex > 0) {
//            val prevBook = books[currentIndex - 1]
//            selectBook(prevBook)
//        } else {
//            seekTo(0L)
//        }
//    }

    fun rewind() {
        val newPos = (player.currentPosition - _uiState.value.rewindForwardSeconds * 1000).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun forward() {
        val newPos = (player.currentPosition + _uiState.value.rewindForwardSeconds * 1000).coerceAtMost(player.duration)
        seekTo(newPos)
    }

    fun seekToStart() {
        seekTo(0L)
    }

    fun seekToEnd() {
        playNextBook()
    }

    fun setRewindForwardSeconds(seconds: Int) {
        viewModelScope.launch {
            repository.saveRewindForwardSeconds(seconds)
            _uiState.value = _uiState.value.copy(rewindForwardSeconds = seconds)
        }
    }

    fun setBlindMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveBlindMode(enabled)
        }
    }

    private suspend fun restorePlaybackState() {
        val lastBookUri = repository.lastBookUri.first() ?: return
        val lastPosition = repository.lastPosition.first()

        val book = _uiState.value.books.find { it.uri.toString() == lastBookUri }
        if (book != null) {
            _uiState.value = _uiState.value.copy(
                currentBook = book,
                currentPosition = lastPosition,
                bookTitle = book.title,
                author = book.author
            )
            loadMetadataManually(book)
            player.setMediaItem(MediaItem.fromUri(book.uri), lastPosition)
            player.prepare()
        }
    }

    private fun startPositionUpdate() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                val pos = player.currentPosition
                _uiState.value = _uiState.value.copy(currentPosition = pos)
                if (pos / 1000 % 10 == 0L) {
                    savePlaybackState()
                }
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdate() {
        positionUpdateJob?.cancel()
    }

    private fun savePlaybackState() {
        val currentBook = _uiState.value.currentBook ?: return
        val currentPosition = player.currentPosition
        viewModelScope.launch {
            repository.saveLastPlayback(currentBook.uri.toString(), currentPosition)
        }
    }

    fun setFolderUri(uri: Uri) {
        viewModelScope.launch {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            repository.saveFolderUri(uri.toString())
            _uiState.value = _uiState.value.copy(folderUri = uri)
            loadBooksFromFolderSuspend(uri)
        }
    }

    fun reloadBooks() {
        val uri = _uiState.value.folderUri ?: return
        viewModelScope.launch {
            loadBooksFromFolderSuspend(uri)
        }
    }

    private suspend fun loadBooksFromSavedList(): Boolean {
        val savedBooksJson = repository.booksList.first() ?: return false
        return try {
            val jsonArray = JSONArray(savedBooksJson)
            val books = mutableListOf<Book>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                books.add(Book(
                    name = obj.getString("name"),
                    uri = obj.getString("uri").toUri(),
                    title = obj.optString("title").takeIf { it != "null" && it.isNotEmpty() },
                    author = obj.optString("author").takeIf { it != "null" && it.isNotEmpty() },
                ))
            }
            val sortedBooks = sortBooks(books)
            _uiState.value = _uiState.value.copy(books = sortedBooks)
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun loadBooksFromFolderSuspend(uri: Uri): Boolean {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val books = withContext(Dispatchers.IO) {
            val root = DocumentFile.fromTreeUri(getApplication(), uri)
            val list = mutableListOf<Book>()
            if (root != null) {
                findM4bFilesRecursive(root, list)
            }
            sortBooks(list)
        }
        _uiState.value = _uiState.value.copy(books = books, isLoading = false)
        saveBooks(books)
        return books.isNotEmpty()
    }

    private fun sortBooks(books: List<Book>): List<Book> {
        return books.sortedWith(
            compareBy(
                { it.author?.lowercase() ?: "яяяяяяя" },
                { it.name.lowercase() }
//                { it.title?.lowercase() ?: it.name.lowercase() }
            )
        )
    }

    private fun findM4bFilesRecursive(directory: DocumentFile, books: MutableList<Book>) {
        directory.listFiles().forEach { file ->
            if (file.isDirectory) {
                findM4bFilesRecursive(file, books)
            } else if (file.name?.endsWith(".m4b", ignoreCase = true) == true) {
                val retriever = MediaMetadataRetriever()
                var title: String? = null
                var author: String? = null
                try {
                    retriever.setDataSource(getApplication(), file.uri)
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?:
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?:
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
                } catch (_: Exception) {
                } finally {
                    retriever.release()
                }
                books.add(Book(file.name ?: "Unknown", file.uri, title, author))
            }
        }
    }

    fun removeBook(book: Book) {
        val isCurrent = book.uri.toString() == _uiState.value.currentBook?.uri.toString()
        if (isCurrent) {
            player.stop()
        }

        val newList = _uiState.value.books.filter { it.uri.toString() != book.uri.toString() }
        _uiState.value = _uiState.value.copy(
            books = newList,
            currentBook = if (isCurrent) null else _uiState.value.currentBook,
            artwork = if (isCurrent) null else _uiState.value.artwork,
            bookTitle = if (isCurrent) null else _uiState.value.bookTitle,
            author = if (isCurrent) null else _uiState.value.author,
            isPlaying = if (isCurrent) false else _uiState.value.isPlaying,
            currentPosition = if (isCurrent) 0L else _uiState.value.currentPosition,
            duration = if (isCurrent) 0L else _uiState.value.duration
        )
        saveBooks(newList)
    }

    private fun saveBooks(books: List<Book>) {
        viewModelScope.launch {
            val jsonArray = JSONArray()
            books.forEach {
                val jsonObject = JSONObject()
                jsonObject.put("name", it.name)
                jsonObject.put("uri", it.uri.toString())
                jsonObject.put("title", it.title ?: "")
                jsonObject.put("author", it.author ?: "")
                jsonArray.put(jsonObject)
            }
            repository.saveBooksList(jsonArray.toString())
        }
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
            savePlaybackState()
        } else {
            if (player.mediaItemCount == 0 && _uiState.value.books.isNotEmpty()) {
                val bookToPlay = _uiState.value.currentBook ?: _uiState.value.books[0]
                selectBook(bookToPlay)
            } else {
                player.play()
            }
        }
    }

    fun selectBook(book: Book) {
        _uiState.value = _uiState.value.copy(
            currentBook = book,
            artwork = null,
            bookTitle = book.title,
            author = book.author,
            currentPosition = 0L,
            duration = 0L
        )

        loadMetadataManually(book)

        player.setMediaItem(MediaItem.fromUri(book.uri))
        player.prepare()
        player.play()
        savePlaybackState()
    }

    private fun loadMetadataManually(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(getApplication(), book.uri)
                val art = retriever.embeddedPicture?.let { bytes ->
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        artwork = art ?: _uiState.value.artwork,
                        bookTitle = title ?: _uiState.value.bookTitle,
                        author = author ?: _uiState.value.author
                    )
                }
            } catch (_: Exception) {
            } finally {
                retriever.release()
            }
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        _uiState.value = _uiState.value.copy(currentPosition = position)
        savePlaybackState()
    }

    override fun onCleared() {
        super.onCleared()
        savePlaybackState()
        stopPositionUpdate()
        player.release()
    }
}
