
package com.example.stockpredictor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockPredictorApp()
        }
    }
}

@Composable
fun StockPredictorApp() {
    var data by remember { mutableStateOf(listOf<Float>()) }
    var predictions by remember { mutableStateOf(listOf<Float>()) }

    LaunchedEffect(Unit) {
        val weights = loadModelWeights()
        data = generateSampleStockData()
        predictions = predictNextDays(data, weights)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("📈 Stock Price Predictor", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            StockChart(data, predictions)
        }
    }
}

suspend fun loadModelWeights(): List<Float> {
    return withContext(Dispatchers.IO) {
        val inputStream: InputStream = this@StockPredictorApp.javaClass.classLoader
            ?.getResourceAsStream("assets/weights.json")!!
        val json = JSONObject(inputStream.bufferedReader().use { it.readText() })
        val arr = json.getJSONArray("weights")
        List(arr.length()) { idx -> arr.getDouble(idx).toFloat() }
    }
}

fun generateSampleStockData(): List<Float> {
    return listOf(100f, 101f, 99f, 98f, 102f, 104f, 103f, 105f)
}

fun predictNextDays(data: List<Float>, weights: List<Float>): List<Float> {
    val next7 = mutableListOf<Float>()
    val last = data.last()
    for (i in 1..7) {
        val pred = last + weights.sum() * i
        next7.add(pred)
    }
    return next7
}

@Composable
fun StockChart(history: List<Float>, predictions: List<Float>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.LightGray)
    ) {
        val totalPoints = history + predictions
        val maxY = totalPoints.maxOrNull() ?: 1f
        val scaleX = size.width / totalPoints.size
        val scaleY = size.height / maxY

        for (i in 0 until totalPoints.size - 1) {
            drawLine(
                color = if (i < history.size - 1) Color.Blue else Color.Green,
                start = androidx.compose.ui.geometry.Offset(i * scaleX, size.height - totalPoints[i] * scaleY),
                end = androidx.compose.ui.geometry.Offset((i + 1) * scaleX, size.height - totalPoints[i + 1] * scaleY),
                strokeWidth = 4f
            )
        }
    }
}
