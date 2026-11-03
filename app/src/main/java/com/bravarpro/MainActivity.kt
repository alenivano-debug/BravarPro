package com.bravarpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BravarProApp() }
    }
}

private fun broj(s: String) = s.replace(",", ".").toDoubleOrNull() ?: 0.0
private fun fmt(n: Double) = String.format(Locale.GERMANY, "%.2f", n)

private data class Crta(val x1: Float, val y1: Float, val x2: Float, val y2: Float)
private data class P3(val x: Float, val y: Float, val z: Float)
private data class P2(val x: Float, val y: Float, val depth: Float)

@Composable
private fun Polje(naziv: String, value: String, promjena: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = promjena,
        label = { Text(naziv) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun BravarProApp() {
    var ekran by remember { mutableStateOf("Početna") }
    var sirina by remember { mutableStateOf("40") }
    var visina by remember { mutableStateOf("40") }
    var stijenka by remember { mutableStateOf("2") }
    var duzina by remember { mutableStateOf("6") }
    var komada by remember { mutableStateOf("1") }
    var materijal by remember { mutableStateOf("Čelik") }
    var limD by remember { mutableStateOf("1000") }
    var limS by remember { mutableStateOf("500") }
    var debljina by remember { mutableStateOf("5") }
    var sipka by remember { mutableStateOf("6000") }
    var rezovi by remember { mutableStateOf("1200, 800, 1500") }
    var cijenaMaterijala by remember { mutableStateOf("2") }
    var sati by remember { mutableStateOf("2") }
    var satnica by remember { mutableStateOf("15") }
    var ostalo by remember { mutableStateOf("0") }
    var cadDuzina by remember { mutableStateOf("1000") }
    var cadKut by remember { mutableStateOf("45") }
    var pravokutnikA by remember { mutableStateOf("1000") }
    var pravokutnikB by remember { mutableStateOf("500") }

    var modelSirina by remember { mutableStateOf("40") }
    var modelVisina by remember { mutableStateOf("40") }
    var modelDuzina by remember { mutableStateOf("300") }
    var modelStijenka by remember { mutableStateOf("2") }

    val crte = remember { mutableStateListOf<Crta>() }
    var rezultat by remember { mutableStateOf("") }

    val gustoce = when (materijal) {
        "Aluminij" -> 2700.0
        "Inox" -> 8000.0
        else -> 7850.0
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("BRAVAR PRO", style = MaterialTheme.typography.headlineLarge)
            Text("Bravarski kalkulator, 2D i 3D tehničko crtanje")

            if (ekran == "Početna") {
                listOf(
                    "Izračun profila i cijevi",
                    "Izračun težine materijala",
                    "Plan rezanja",
                    "Izračun cijene",
                    "CAD – 2D crtanje",
                    "CAD – 3D model"
                ).forEach { naziv ->
                    Button(
                        onClick = {
                            ekran = naziv
                            rezultat = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(naziv)
                    }
                }
            } else {
                OutlinedButton(onClick = {
                    ekran = "Početna"
                    rezultat = ""
                }) {
                    Text("← Natrag")
                }

                Text(ekran, style = MaterialTheme.typography.headlineSmall)

                when (ekran) {
                    "Izračun profila i cijevi" -> {
                        Polje("Širina profila (mm)", sirina) { sirina = it }
                        Polje("Visina profila (mm)", visina) { visina = it }
                        Polje("Debljina stijenke (mm)", stijenka) { stijenka = it }
                        Polje("Duljina jednog komada (m)", duzina) { duzina = it }
                        Polje("Broj komada", komada) { komada = it }

                        Button(onClick = {
                            val a = broj(sirina)
                            val b = broj(visina)
                            val t = broj(stijenka)
                            val l = broj(duzina)
                            val k = broj(komada)

                            val pov = a * b -
                                (a - 2 * t).coerceAtLeast(0.0) *
                                (b - 2 * t).coerceAtLeast(0.0)

                            val kgm = pov * 0.00785

                            rezultat = if (
                                a > 0 && b > 0 && t > 0 &&
                                t * 2 < minOf(a, b) && l > 0 && k > 0
                            ) {
                                "Težina po metru: ${fmt(kgm)} kg\n" +
                                "Ukupna duljina: ${fmt(l * k)} m\n" +
                                "Ukupna težina: ${fmt(kgm * l * k)} kg"
                            } else {
                                "Provjeri dimenzije."
                            }
                        }) {
                            Text("IZRAČUNAJ")
                        }
                    }

                    "Izračun težine materijala" -> {
                        Text("Materijal")

                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            listOf("Čelik", "Inox", "Aluminij").forEach {
                                FilterChip(
                                    selected = materijal == it,
                                    onClick = { materijal = it },
                                    label = { Text(it) }
                                )
                            }
                        }

                        Polje("Duljina lima (mm)", limD) { limD = it }
                        Polje("Širina lima (mm)", limS) { limS = it }
                        Polje("Debljina lima (mm)", debljina) { debljina = it }
                        Polje("Broj komada", komada) { komada = it }

                        Button(onClick = {
                            val volumen = broj(limD) * broj(limS) *
                                broj(debljina) * broj(komada)

                            val kg = volumen * gustoce / 1_000_000_000.0

                            rezultat = if (volumen > 0) {
                                "Materijal: $materijal\nTežina: ${fmt(kg)} kg"
                            } else {
                                "Provjeri dimenzije."
                            }
                        }) {
                            Text("IZRAČUNAJ TEŽINU")
                        }
                    }

                    "Plan rezanja" -> {
                        Polje("Duljina šipke (mm)", sipka) { sipka = it }

                        Polje("Duljine komada (odvojene zarezom)", rezovi) {
                            rezovi = it
                        }

                        Button(onClick = {
                            val lista = rezovi.split(",")
                                .mapNotNull {
                                    broj(it.trim()).takeIf { v -> v > 0 }
                                }

                            val ukupno = lista.sum()
                            val ostatak = broj(sipka) - ukupno

                            rezultat = if (lista.isNotEmpty() && broj(sipka) > 0) {
                                if (ostatak >= 0) {
                                    "Broj komada: ${lista.size}\n" +
                                    "Ukupno: ${fmt(ukupno)} mm\n" +
                                    "Ostatak: ${fmt(ostatak)} mm\n" +
                                    "Stane u jednu šipku."
                                } else {
                                    "Nedostaje ${fmt(-ostatak)} mm."
                                }
                            } else {
                                "Provjeri unesene duljine."
                            }
                        }) {
                            Text("IZRAČUNAJ REZANJE")
                        }
                    }

                    "Izračun cijene" -> {
                        Polje("Cijena materijala (€)", cijenaMaterijala) {
                            cijenaMaterijala = it
                        }

                        Polje("Radni sati", sati) { sati = it }
                        Polje("Cijena sata rada (€)", satnica) { satnica = it }
                        Polje("Ostali troškovi (€)", ostalo) { ostalo = it }

                        Button(onClick = {
                            val m = broj(cijenaMaterijala)
                            val r = broj(sati) * broj(satnica)
                            val o = broj(ostalo)

                            rezultat = if (m >= 0 && r >= 0 && o >= 0) {
                                "Materijal: ${fmt(m)} €\n" +
                                "Rad: ${fmt(r)} €\n" +
                                "Ostalo: ${fmt(o)} €\n" +
                                "UKUPNO: ${fmt(m + r + o)} €"
                            } else {
                                "Provjeri cijene."
                            }
                        }) {
                            Text("IZRAČUNAJ CIJENU")
                        }
                    }

                    "CAD – 2D crtanje" -> {
                        Polje("Duljina linije (mm)", cadDuzina) {
                            cadDuzina = it
                        }

                        Polje("Kut linije (stupnjevi)", cadKut) {
                            cadKut = it
                        }

                        Button(onClick = {
                            val l = broj(cadDuzina)
                            val kut = broj(cadKut)

                            if (l > 0 && kut in 0.0..360.0) {
                                val last = crte.lastOrNull()
                                val x = last?.x2 ?: 0f
                                val y = last?.y2 ?: 0f
                                val px = (l * 0.08).toFloat()
                                val rad = Math.toRadians(kut)

                                crte.add(
                                    Crta(
                                        x, y,
                                        x + px * cos(rad).toFloat(),
                                        y - px * sin(rad).toFloat()
                                    )
                                )

                                rezultat = "Dodana linija: ${fmt(l)} mm\nKut: ${fmt(kut)}°"
                            } else {
                                rezultat = "Unesi ispravnu duljinu i kut."
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("DODAJ LINIJU")
                        }

                        Button(
                            onClick = {
                                crte.clear()
                                rezultat = "Nacrt je obrisan."
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OBRIŠI NACRT")
                        }

                        Text("Povuci prstom po mreži za slobodno crtanje.")

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                                .background(Color(0xFFF4F4F4))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDrag = { change, _ ->
                                            val p = change.position
                                            val prev = change.previousPosition

                                            crte.add(
                                                Crta(
                                                    prev.x - size.width / 2f,
                                                    prev.y - size.height / 2f,
                                                    p.x - size.width / 2f,
                                                    p.y - size.height / 2f
                                                )
                                            )
                                            change.consume()
                                        }
                                    )
                                }
                        ) {
                            var x = 0f
                            while (x < size.width) {
                                drawLine(
                                    Color.LightGray,
                                    Offset(x, 0f),
                                    Offset(x, size.height),
                                    1f
                                )
                                x += 25f
                            }

                            var y = 0f
                            while (y < size.height) {
                                drawLine(
                                    Color.LightGray,
                                    Offset(0f, y),
                                    Offset(size.width, y),
                                    1f
                                )
                                y += 25f
                            }

                            crte.forEach {
                                drawLine(
                                    Color.Blue,
                                    Offset(size.width / 2f + it.x1, size.height / 2f + it.y1),
                                    Offset(size.width / 2f + it.x2, size.height / 2f + it.y2),
                                    5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        Text("Izračun pravokutnika")
                        Polje("Širina (mm)", pravokutnikA) { pravokutnikA = it }
                        Polje("Visina (mm)", pravokutnikB) { pravokutnikB = it }

                        Button(onClick = {
                            val a = broj(pravokutnikA)
                            val b = broj(pravokutnikB)

                            rezultat = if (a > 0 && b > 0) {
                                "Dijagonala: ${fmt(sqrt(a * a + b * b))} mm\n" +
                                "Kut dijagonale: ${fmt(Math.toDegrees(atan2(b, a)))}°"
                            } else {
                                "Unesi ispravne mjere."
                            }
                        }) {
                            Text("IZRAČUNAJ DIJAGONALU")
                        }
                    }

                    "CAD – 3D model" -> {
                        Text("3D pravokutna cijev – unesite dimenzije u mm.")

                        Polje("Vanjska širina (mm)", modelSirina) {
                            modelSirina = it
                        }
                        Polje("Vanjska visina (mm)", modelVisina) {
                            modelVisina = it
                        }
                        Polje("Duljina cijevi (mm)", modelDuzina) {
                            modelDuzina = it
                        }
                        Polje("Debljina stijenke (mm)", modelStijenka) {
                            modelStijenka = it
                        }

                        var rotX by remember { mutableFloatStateOf(-0.35f) }
                        var rotY by remember { mutableFloatStateOf(0.65f) }
                        var zoom by remember { mutableFloatStateOf(1f) }

                        val a = broj(modelSirina).toFloat()
                        val b = broj(modelVisina).toFloat()
                        val l = broj(modelDuzina).toFloat()
                        val t = broj(modelStijenka).toFloat()

                        val valid = a > 0 && b > 0 && l > 0 &&
                            t > 0 && 2 * t < min(a, b)

                        Text("Povuci jednim prstom za okretanje. Koristi dva prsta za zumiranje.")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                rotX = -0.35f
                                rotY = 0.65f
                                zoom = 1f
                            }) {
                                Text("RESET POGLEDA")
                            }

                            OutlinedButton(onClick = {
                                zoom = (zoom * 1.2f).coerceAtMost(3f)
                            }) {
                                Text("+")
                            }

                            OutlinedButton(onClick = {
                                zoom = (zoom / 1.2f).coerceAtLeast(0.5f)
                            }) {
                                Text("−")
                            }
                        }

                        if (!valid) {
                            Text(
                                "Provjeri dimenzije. Stijenka mora biti manja od polovice širine i visine.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(390.dp)
                                .background(Color(0xFF202832))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, drag ->
                                        rotY += drag.x * 0.012f
                                        rotX = (rotX + drag.y * 0.012f)
                                            .coerceIn(-1.45f, 1.45f)
                                        change.consume()
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, _, scale, _ ->
                                        zoom = (zoom * scale).coerceIn(0.5f, 3f)
                                    }
                                }
                        ) {
                            if (valid) {
                                val scale = min(size.width, size.height) *
                                    0.62f / max(a, max(b, l * 0.55f)) * zoom
                                
                                fun project(p: P3): P2 {
                                    val cy = cos(rotY)
                                    val sy = sin(rotY)
                                    val cx = cos(rotX)
                                    val sx = sin(rotX)

                                    val x1 = p.x * cy - p.z * sy
                                    val z1 = p.x * sy + p.z * cy
                                    val y1 = p.y * cx - z1 * sx
                                    val z2 = p.y * sx + z1 * cx

                                    val perspective = 700f / (700f + z2 * scale * 0.25f)

                                    return P2(
                                        size.width / 2f + x1 * scale * perspective,
                                        size.height / 2f - y1 * scale * perspective,
                                        z2
                                    )
                                }

                                val w = a / 2f
                                val h = b / 2f
                                val d = l / 2f

                                val outer = listOf(
                                    P3(-w, -h, -d), P3(w, -h, -d),
                                    P3(w, h, -d), P3(-w, h, -d),
                                    P3(-w, -h, d), P3(w, -h, d),
                                    P3(w, h, d), P3(-w, h, d)
                                )

                                val innerW = w - t
                                val innerH = h - t

                                val inner = listOf(
                                    P3(-innerW, -innerH, -d),
                                    P3(innerW, -innerH, -d),
                                    P3(innerW, innerH, -d),
                                    P3(-innerW, innerH, -d),
                                    P3(-innerW, -innerH, d),
                                    P3(innerW, -innerH, d),
                                    P3(innerW, innerH, d),
                                    P3(-innerW, innerH, d)
                                )

                                val po = outer.map { project(it) }
                                val pi = inner.map { project(it) }

                                fun polygon(points: List<P2>, color: Color) {
                                    val path = Path().apply {
                                        moveTo(points[0].x, points[0].y)
                                        for (i in 1 until points.size) {
                                            lineTo(points[i].x, points[i].y)
                                        }
                                        close()
                                    }
                                    drawPath(path, color)
                                }

                                val faces = listOf(
                                    listOf(0, 1, 5, 4),
                                    listOf(1, 2, 6, 5),
                                    listOf(2, 3, 7, 6),
                                    listOf(3, 0, 4, 7)
                                ).sortedByDescending { face ->
                                    face.map { po[it].depth }.average()
                                }

                                faces.forEachIndexed { index, face ->
                                    polygon(
                                        face.map { po[it] },
                                        listOf(
                                            Color(0xFF607D8B),
                                            Color(0xFF78909C),
                                            Color(0xFF455A64),
                                            Color(0xFF90A4AE)
                                        )[index]
                                    )
                                }

                                val edges = listOf(
                                    0 to 1, 1 to 2, 2 to 3, 3 to 0,
                                    4 to 5, 5 to 6, 6 to 7, 7 to 4,
                                    0 to 4, 1 to 5, 2 to 6, 3 to 7
                                )

                                edges.forEach { (i, j) ->
                                    drawLine(
                                        Color.White,
                                        Offset(po[i].x, po[i].y),
                                        Offset(po[j].x, po[j].y),
                                        2f
                                    )
                                }

                                val nearOuter = (4..7).map { po[it] }
                                val nearInner = (4..7).map { pi[it] }

                                polygon(nearOuter, Color(0xFFB0BEC5))

                                val hole = Path().apply {
                                    moveTo(nearInner[0].x, nearInner[0].y)
                                    nearInner.drop(1).forEach {
                                        lineTo(it.x, it.y)
                                    }
                                    close()
                                }

                                drawPath(hole, Color(0xFF202832))

                                for (i in 0..3) {
                                    val j = (i + 1) % 4

                                    drawLine(
                                        Color.White,
                                        Offset(nearOuter[i].x, nearOuter[i].y),
                                        Offset(nearInner[i].x, nearInner[i].y),
                                        2f
                                    )

                                    drawLine(
                                        Color.White,
                                        Offset(nearInner[i].x, nearInner[i].y),
                                        Offset(nearInner[j].x, nearInner[j].y),
                                        2f
                                    )
                                }
                            }
                        }

                        if (valid) {
                            Text(
                                "Model: ${fmt(a.toDouble())} × " +
                                "${fmt(b.toDouble())} × ${fmt(l.toDouble())} mm"
                            )
                            Text(
                                "Unutarnji otvor: " +
                                "${fmt((a - 2 * t).toDouble())} × " +
                                "${fmt((b - 2 * t).toDouble())} mm"
                            )
                        }
                    }
                }

                if (rezultat.isNotEmpty()) {
                    HorizontalDivider()
                    Text(
                        "REZULTAT",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(rezultat)
                }
            }
        }
    }
}
                            
