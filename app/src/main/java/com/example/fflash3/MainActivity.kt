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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fflash3.ui.theme.Fflash3Theme
import java.io.BufferedReader
import java.io.InputStreamReader

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
                "Loaded ${flashcards.size} messages!",
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

    val normalFont = FontFamily.SansSerif
    val messageFont = FontFamily.Cursive

    var currentCard by remember { mutableStateOf<Pair<String, String>?>(null) }
    var currentIndex by remember { mutableStateOf(0) }
    var showingAnswer by remember { mutableStateOf(false) }

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    onFilePicked(uri)

                    if (flashcards.isNotEmpty()) {
                        currentIndex = 0
                        currentCard = flashcards[currentIndex]
                        showingAnswer = false
                    }
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "I have messages for you di❤️😊",
                        fontFamily = normalFont,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.33f)
                    .padding(top = 0.dp, bottom = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.photo),
                    contentDescription = "Flashcard related image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "text/plain"
                    }
                    filePickerLauncher.launch(intent)
                }
            ) {
                Text(
                    text = "Select the message file",
                    fontFamily = normalFont,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = when {
                    currentCard == null -> "Load a deck..."
                    showingAnswer -> currentCard!!.second
                    else -> currentCard!!.first
                },
                color = when {
                    currentCard == null -> Color.Gray
                    showingAnswer -> Color(0xFF2E7D32)
                    else -> Color(0xFFC62828)
                },
                fontFamily = messageFont,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
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
                    Text(
                        text = "What is it?",
                        fontFamily = normalFont,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (flashcards.isNotEmpty()) {
                            currentIndex++

                            if (currentIndex >= flashcards.size) {
                                currentIndex = 0
                            }

                            currentCard = flashcards[currentIndex]
                            showingAnswer = false
                        }
                    }
                ) {
                    Text(
                        text = "Next message please",
                        fontFamily = normalFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}