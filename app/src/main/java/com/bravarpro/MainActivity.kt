
package com.bravarpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BravarProApp() }
    }
}

fun broj(s: String): Double = s.replace(",", ".").toDoubleOrNull() ?: 0.0
fun format(n: Double): String = String.format(Locale.GERMANY, "%.2f", n)

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
    var plocina by remember { mutableStateOf("1000") }
    var plocina2 by remember { mutableStateOf("500") }
    var debljina by remember { mutableStateOf("5") }

    var sipka by remember { mutableStateOf("6000") }
    var rezovi by remember { mutableStateOf("1200, 800, 1500") }

    var cijenaMaterijala by remember { mutableStateOf("2") }
    var sati by remember { mutableStateOf("2") }
    var satnica by remember { mutableStateOf("15") }
    var ostalo by remember { mutableStateOf("0") }

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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("BRAVAR PRO", style = MaterialTheme.typography.headlineLarge)
            Text("Bravarski kalkulator")

            if (ekran == "Početna") {
                listOf(
                    "Izračun profila i cijevi",
                    "Izračun težine materijala",
                    "Plan rezanja",
                    "Izračun cijene"
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
                OutlinedButton(onClick = { ekran = "Početna"; rezultat = "" }) {
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

                            val povrsina = a * b -
                                (a - 2 * t).coerceAtLeast(0.0) *
                                (b - 2 * t).coerceAtLeast(0.0)

                            val kgm = povrsina * 0.00785
                            rezultat = if (a > 0 && b > 0 && t > 0 &&
                                t * 2 < minOf(a, b) && l > 0 && k > 0) {
                                "Težina po metru: ${format(kgm)} kg\n" +
                                "Ukupna duljina: ${format(l * k)} m\n" +
                                "Ukupna težina: ${format(kgm * l * k)} kg"
                            } else "Provjeri unesene dimenzije."
                        }) { Text("IZRAČUNAJ") }
                    }

                    "Izračun težine materijala" -> {
                        Text("Vrsta materijala")
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            listOf("Čelik", "Inox", "Aluminij").forEach {
                                FilterChip(
                                    selected = materijal == it,
                                    onClick = { materijal = it },
                                    label = { Text(it) }
                                )
                            }
                        }
                        Polje("Duljina lima (mm)", plocina) { plocina = it }
                        Polje("Širina lima (mm)", plocina2) { plocina2 = it }
                        Polje("Debljina lima (mm)", debljina) { debljina = it }
                        Polje("Broj komada", komada) { komada = it }

                        Button(onClick = {
                            val volumen = broj(plocina) * broj(plocina2) *
                                broj(debljina) * broj(komada)
                            val kg = volumen * gustoce / 1000000000.0
                            rezultat = if (volumen > 0)
                                "Materijal: $materijal\nUkupna težina: ${format(kg)} kg"
                            else "Provjeri unesene dimenzije."
                        }) { Text("IZRAČUNAJ TEŽINU") }
                    }

                    "Plan rezanja" -> {
                        Polje("Duljina šipke (mm)", sipka) { sipka = it }
                        Polje("Duljine komada u mm (odvojene zarezom)", rezovi) {
                            rezovi = it
                        }
                        Button(onClick = {
                            val lista = rezovi.split(",")
                                .mapNotNull { it.trim().toDoubleOrNull() }
                                .filter { it > 0 }
                            val ukupno = lista.sum()
                            val ostatak = broj(sipka) - ukupno
                            rezultat = if (lista.isNotEmpty() && broj(sipka) > 0) {
                                if (ostatak >= 0)
                                    "Broj komada: ${lista.size}\n" +
                                    "Ukupno potrebno: ${format(ukupno)} mm\n" +
                                    "Ostatak: ${format(ostatak)} mm\n" +
                                    "Rezanje stane u jednu šipku."
                                else
                                    "Nedostaje ${format(-ostatak)} mm materijala.\n" +
                                    "Komadi ne stanu u jednu šipku."
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
                            val materijalCijena = broj(cijenaMaterijala)
                            val rad = broj(sati) * broj(satnica)
                            val dodatno = broj(ostalo)
                            val ukupno = materijalCijena + rad + dodatno
                            rezultat = if (materijalCijena >= 0 && rad >= 0 && dodatno >= 0)
                                "Materijal: ${format(materijalCijena)} €\n" +
                                "Rad: ${format(rad)} €\n" +
                                "Ostalo: ${format(dodatno)} €\n" +
                                "UKUPNO: ${format(ukupno)} €"
                            else "Provjeri unesene cijene."
                        }) { Text("IZRAČUNAJ CIJENU") }
                    }
                }

                if (rezultat.isNotEmpty()) {
                    HorizontalDivider()
                    Text("REZULTAT", style = MaterialTheme.typography.titleLarge)
                    Text(rezultat, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
