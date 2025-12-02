package com.example.catalogoproductos.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catalogoproductos.viewmodel.NoticiasViewModel

@Composable
fun NoticiasScreen(
    navController: NavController,
    viewModel: NoticiasViewModel
) {
    val noticias by viewModel.noticias.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.buscarNoticias() }

    val context = LocalContext.current

    Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Noticias", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(noticias) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                item.url?.let { u ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(u))
                                    context.startActivity(intent)
                                }
                            }
                    ) {
                        if (!item.image.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(item.image),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.title ?: "", fontWeight = FontWeight.Bold)
                        val srcName = item.source?.name ?: ""
                        if (srcName.isNotBlank()) {
                            Text(text = srcName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!item.description.isNullOrBlank()) {
                            Text(text = item.description ?: "")
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}
