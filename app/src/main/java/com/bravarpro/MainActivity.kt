
package com.bravarpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BravarProApp() }
    }
}

fun broj(s: String) = s.replace(",", ".").toDoubleOrNull() ?: 0.0
fun fmt(n: Double) = String.format(Locale.GERMANY, "%.2f", n)

data class Crta(val x1: Float, val y1: Float, val x2: Float, val y2: Float)

@Composable
fun Polje(naziv: String, value: String, promjena: (String) -> Unit) {
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

    val crte = remember { mutableStateListOf<Crta>() }
    var rezultat by remember { mutableStateOf("") }

    val gustoce = when (materijal) {
        "Aluminij" -> 2700.0
        "Inox" -> 8000.0
        else -> 7850.0
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("BRAVAR PRO", style = MaterialTheme.typography.headlineLarge)
            Text("Bravarski kalkulator i tehničko crtanje")

            if (ekran == "Početna") {
                listOf(
                    "Izračun profila i cijevi",
                    "Izračun težine materijala",
                    "Plan rezanja",
                    "Izračun cijene",
                    "CAD – tehničko crtanje"
                ).forEach { naziv ->
                    Button(
                        onClick = { ekran = naziv; rezultat = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(naziv) }
                }
            } else {
                OutlinedButton(onClick = {
                    ekran = "Početna"
                    rezultat = ""
                }) { Text("← Natrag") }

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
                                (a - 2*t).coerceAtLeast(0.0) *
                                (b - 2*t).coerceAtLeast(0.0)
                            val kgm = pov * 0.00785
                            rezultat = if (a > 0 && b > 0 && t > 0 &&
                                t*2 < minOf(a,b) && l > 0 && k > 0)
                                "Težina po metru: ${fmt(kgm)} kg\n" +
                                "Ukupna duljina: ${fmt(l*k)} m\n" +
                                "Ukupna težina: ${fmt(kgm*l*k)} kg"
                            else "Provjeri dimenzije."
                        }) { Text("IZRAČUNAJ") }
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
                            val volumen = broj(limD)*broj(limS)*
                                broj(debljina)*broj(komada)
                            val kg = volumen*gustoce/1000000000.0
                            rezultat = if (volumen > 0)
                                "Materijal: $materijal\nTežina: ${fmt(kg)} kg"
                            else "Provjeri dimenzije."
                        }) { Text("IZRAČUNAJ TEŽINU") }
                    }

                    "Plan rezanja" -> {
                        Polje("Duljina šipke (mm)", sipka) { sipka = it }
                        Polje("Duljine komada (odvojene zarezom)", rezovi) {
                            rezovi = it
                        }
                        Button(onClick = {
                            val lista = rezovi.split(",")
                                .mapNotNull { broj(it.trim()).takeIf { v -> v > 0 } }
                            val ukupno = lista.sum()
                            val ostatak = broj(sipka)-ukupno
                            rezultat = if (lista.isNotEmpty() && broj(sipka)>0) {
                                if (ostatak >= 0)
                                    "Broj komada: ${lista.size}\n" +
                                    "Ukupno: ${fmt(ukupno)} mm\n" +
                                    "Ostatak: ${fmt(ostatak)} mm\n" +
                                    "Stane u jednu šipku."
                                else "Nedostaje ${fmt(-ostatak)} mm."
                            } else "Provjeri unesene duljine."
                        }) { Text("IZRAČUNAJ REZANJE") }
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
                            val r = broj(sati)*broj(satnica)
                            val o = broj(ostalo)
                            rezultat = if (m >= 0 && r >= 0 && o >= 0)
                                "Materijal: ${fmt(m)} €\nRad: ${fmt(r)} €\n" +
                                "Ostalo: ${fmt(o)} €\nUKUPNO: ${fmt(m+r+o)} €"
                            else "Provjeri cijene."
                        }) { Text("IZRAČUNAJ CIJENU") }
                    }

                    "CAD – tehničko crtanje" -> {
                        Text("Dodavanje linije u nacrt")
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
                                val zadnja = crte.lastOrNull()
                                val x = zadnja?.x2 ?: 0f
                                val y = zadnja?.y2 ?: 0f
                                val px = l * 0.08
                                val rad = Math.toRadians(kut)
                                val nx = x + (px*cos(rad)).toFloat()
                                val ny = y - (px*sin(rad)).toFloat()
                                crte.add(Crta(x,y,nx,ny))
                                rezultat = "Dodana linija: ${fmt(l)} mm\nKut: ${fmt(kut)}°"
                            } else rezultat = "Unesi duljinu veću od 0 i kut od 0° do 360°."
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("DODAJ LINIJU")
                        }

                        Button(onClick = { crte.clear(); rezultat = "Nacrt je obrisan." },
                            modifier = Modifier.fillMaxWidth()) {
                            Text("OBRIŠI NACRT")
                        }

                        Text("SKICA – 2D", style = MaterialTheme.typography.titleLarge)
                        Canvas(
                            modifier = Modifier.fillMaxWidth()
                                .height(300.dp)
                                .background(Color(0xFFF4F4F4))
                        ) {
                            val korak = 25f
                            var gx = 0f
                            while (gx < size.width) {
                                drawLine(Color.LightGray, Offset(gx,0f),
                                    Offset(gx,size.height), 1f)
                                gx += korak
                            }
                            var gy = 0f
                            while (gy < size.height) {
                                drawLine(Color.LightGray, Offset(0f,gy),
                                    Offset(size.width,gy), 1f)
                                gy += korak
                            }
                            val pomakX = size.width/2
                            val pomakY = size.height/2
                            crte.forEach {
                                drawLine(
                                    Color.Blue,
                                    Offset(pomakX+it.x1,pomakY+it.y1),
                                    Offset(pomakX+it.x2,pomakY+it.y2),
                                    strokeWidth = 5f,
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
                            rezultat = if (a > 0 && b > 0)
                                "Dijagonala: ${fmt(sqrt(a*a+b*b))} mm\n" +
                                "Kut dijagonale: ${fmt(Math.toDegrees(atan2(b,a)))}°"
                            else "Unesi ispravne mjere."
                        }) { Text("IZRAČUNAJ DIJAGONALU") }
                    }
                }

                if (rezultat.isNotEmpty()) {
                    HorizontalDivider()
                    Text("REZULTAT", style = MaterialTheme.typography.titleLarge)
                    Text(rezultat)
                }
            }
        }
    }
}
