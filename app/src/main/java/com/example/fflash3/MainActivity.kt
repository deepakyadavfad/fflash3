package com.example.fflash3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fflash3.ui.theme.Fflash3Theme
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    companion object {
        var flashcards = mutableStateListOf<Pair<String, String>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Fflash3Theme {
                FlashCardApp { uri ->
                    loadFlashcards(uri)
                }
            }
        }
    }

    private fun loadFlashcards(uri: Uri) {
        flashcards.clear()

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.forEachLine { line ->
                val parts = line.split(":", limit = 2)

                if (parts.size >= 2) {
                    val question = parts[0].trim()
                    val answer = parts[1].trim()
                    flashcards.add(Pair(question, answer))
                }
            }

            reader.close()

            Toast.makeText(
                this,
                "Loaded ${flashcards.size} flashcards!",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashCardApp(onFilePicked: (Uri) -> Unit) {
    val flashcards = MainActivity.flashcards

    var currentCard by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showingAnswer by remember { mutableStateOf(false) }

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    onFilePicked(uri)

                    if (flashcards.isNotEmpty()) {
                        currentCard = flashcards.random()
                        showingAnswer = false
                    }
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlashCards") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "text/plain"
                    }
                    filePickerLauncher.launch(intent)
                }
            ) {
                Text("Load Flashcards File")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when {
                    currentCard == null -> "Load a deck..."
                    showingAnswer -> currentCard!!.second
                    else -> currentCard!!.first
                },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(24.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (currentCard != null) {
                            showingAnswer = true
                        }
                    }
                ) {
                    Text("Show Answer")
                }

                Button(
                    onClick = {
                        if (flashcards.isNotEmpty()) {
                            currentCard = flashcards.random(
                                Random(System.currentTimeMillis())
                            )
                            showingAnswer = false
                        }
                    }
                ) {
                    Text("Next Card")
                }
            }

            Image(
                painter = painterResource(id = R.drawable.gpt),
                contentDescription = "Flashcard related image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(top = 16.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}