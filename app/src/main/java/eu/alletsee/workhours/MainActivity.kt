package eu.alletsee.workhours

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var etStundenlohn: EditText
    private lateinit var etGearbeiteteStunden: EditText
    private lateinit var etDatum: EditText
    private lateinit var tvHeuteVerdientZahl: TextView
    private lateinit var tvGesamtlohnZahl: TextView
    private lateinit var btnAddEntry: Button
    private lateinit var rvWorkEntries: RecyclerView
    private lateinit var etStartlohn: EditText
    private lateinit var etStartzeit: EditText
    private lateinit var etEndzeit: EditText

    private val workEntries = mutableListOf<WorkEntry>()
    private lateinit var adapter: WorkEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etStundenlohn = findViewById(R.id.etStundenlohn)
        etGearbeiteteStunden = findViewById(R.id.etGearbeiteteStunden)
        etDatum = findViewById(R.id.etDatum)
        tvHeuteVerdientZahl = findViewById(R.id.tvHeuteVerdientZahl)
        tvGesamtlohnZahl = findViewById(R.id.tvGesamtlohnZahl)
        btnAddEntry = findViewById(R.id.btnAddEntry)
        rvWorkEntries = findViewById(R.id.rvWorkEntries)
        etStartlohn = findViewById(R.id.etStartlohn)
        etStartzeit = findViewById(R.id.etStartzeit2) // Verwende ID mit "2" am Ende
        etEndzeit = findViewById(R.id.etEndzeit2) // Verwende ID mit "2" am Ende

        adapter = WorkEntryAdapter(workEntries)
        rvWorkEntries.layoutManager = LinearLayoutManager(this)
        rvWorkEntries.adapter = adapter

        etStundenlohn.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "Stundenlohn: $s")
                computeTotal()
            }
        })

        etGearbeiteteStunden.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "Gearbeitete Stunden: $s")
                computeTotal()
            }
        })

        etStartlohn.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "Gesamtlohn: $s")
                computeTotal()
            }
        })

        etDatum.setOnClickListener {
            showDatePickerDialog()
        }

        btnAddEntry.setOnClickListener {
            addWorkEntry()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate =
                getString(R.string.formatted_date, selectedDay, selectedMonth + 1, selectedYear)
            etDatum.setText(formattedDate)
        }, year, month, day).show()
    }

    private fun calculateWorkedHours(): Double? {
        val startzeit = etStartzeit.text.toString()
        val endzeit = etEndzeit.text.toString()

        if (startzeit.isNotEmpty() && endzeit.isNotEmpty()) {
            val startParts = startzeit.split(":")
            val endParts = endzeit.split(":")

            if (startParts.size == 2 && endParts.size == 2) {
                val startHour = startParts[0].toIntOrNull()
                val startMinute = startParts[1].toIntOrNull()
                val endHour = endParts[0].toIntOrNull()
                val endMinute = endParts[1].toIntOrNull()

                if (startHour != null && startMinute != null && endHour != null && endMinute != null) {
                    val startTimeInMinutes = startHour * 60 + startMinute
                    val endTimeInMinutes = endHour * 60 + endMinute

                    if (endTimeInMinutes >= startTimeInMinutes) {
                        return (endTimeInMinutes - startTimeInMinutes) / 60.0
                    }
                }
            }
        }
        return null
    }

    private fun addWorkEntry() {
        val stundenlohn = etStundenlohn.text.toString().toDoubleOrNull()
        val manuelleStunden = etGearbeiteteStunden.text.toString().toDoubleOrNull()
        val berechnetStunden = calculateWorkedHours()
        val gearbeiteteStunden = berechnetStunden ?: manuelleStunden
        val datum = etDatum.text.toString()

        if (stundenlohn != null && gearbeiteteStunden != null && datum.isNotEmpty()) {
            val dateParts = datum.split(".")
            if (dateParts.size == 3) {
                try {
                    val calendar = Calendar.getInstance()
                    calendar.set(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
                    val date = calendar.time

                    // Speichere Start- und Endzeit im WorkEntry
                    val startzeit = etStartzeit.text.toString()
                    val endzeit = etEndzeit.text.toString()

                    // Prüfen, ob bereits ein Eintrag für dieses Datum existiert
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val currentDateStr = dateFormat.format(date)

                    val existingEntryIndex = workEntries.indexOfFirst {
                        dateFormat.format(it.date) == currentDateStr
                    }

                    if (existingEntryIndex >= 0) {
                        // Eintrag für dieses Datum bereits vorhanden, füge Stunden und Zeiten zusammen
                        val existingEntry = workEntries[existingEntryIndex]

                        // Berechne neue Gesamtstunden
                        val totalHours = existingEntry.hours + gearbeiteteStunden

                        // Verarbeite die Zeitangaben für die intelligente Anzeige
                        var timeDisplay = ""

                        if (existingEntry.startTime.isNotEmpty() && existingEntry.endTime.isNotEmpty() &&
                            startzeit.isNotEmpty() && endzeit.isNotEmpty()) {

                            // Konvertiere Zeiten für den Vergleich
                            val existingStartMinutes = convertTimeToMinutes(existingEntry.startTime)
                            val existingEndMinutes = convertTimeToMinutes(existingEntry.endTime)
                            val newStartMinutes = convertTimeToMinutes(startzeit)
                            val newEndMinutes = convertTimeToMinutes(endzeit)

                            // Verschiedene Szenarien für die Zusammenfassung der Zeiten
                            if (existingEndMinutes < newStartMinutes) {
                                // Fall 1: Neue Zeit beginnt nach dem Ende der existierenden Zeit (Pause)
                                timeDisplay = "${existingEntry.startTime}-${existingEntry.endTime}, $startzeit-$endzeit"
                            } else if (newEndMinutes < existingStartMinutes) {
                                // Fall 2: Neue Zeit endet vor dem Start der existierenden Zeit (Pause)
                                timeDisplay = "$startzeit-$endzeit, ${existingEntry.startTime}-${existingEntry.endTime}"
                            } else {
                                // Fall 3: Überlappung oder Fortsetzung - nehme früheste Start- und späteste Endzeit
                                val combinedStartTime = if (existingStartMinutes <= newStartMinutes)
                                                        existingEntry.startTime else startzeit
                                val combinedEndTime = if (existingEndMinutes >= newEndMinutes)
                                                      existingEntry.endTime else endzeit
                                timeDisplay = "$combinedStartTime-$combinedEndTime"
                            }
                        } else if (existingEntry.startTime.isNotEmpty() && existingEntry.endTime.isNotEmpty()) {
                            timeDisplay = "${existingEntry.startTime}-${existingEntry.endTime}"
                        } else if (startzeit.isNotEmpty() && endzeit.isNotEmpty()) {
                            timeDisplay = "$startzeit-$endzeit"
                        }

                        // Erstelle neuen Eintrag mit kombinierten Werten
                        val newEntry = WorkEntry(
                            date = existingEntry.date,
                            hours = totalHours,
                            hourlyRate = stundenlohn, // Verwende aktuellen Stundenlohn
                            startTime = if (timeDisplay.contains(",")) existingEntry.startTime else
                                       if (existingEntry.startTime.isNotEmpty()) existingEntry.startTime else startzeit,
                            endTime = if (timeDisplay.contains(",")) existingEntry.endTime else
                                     if (endzeit.isNotEmpty()) endzeit else existingEntry.endTime,
                            timeDisplay = timeDisplay // Neues Feld für die zusammengefasste Zeitanzeige
                        )

                        // Ersetze alten Eintrag mit neuem
                        workEntries[existingEntryIndex] = newEntry

                        // Zeige Nachricht, dass Einträge zusammengefasst wurden
                        Toast.makeText(
                            this,
                            "Einträge für $currentDateStr zusammengefasst (${String.format("%.1f", totalHours)} Std)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Neuer Eintrag für dieses Datum
                        val timeDisplay = if (startzeit.isNotEmpty() && endzeit.isNotEmpty()) {
                            "$startzeit-$endzeit"
                        } else {
                            ""
                        }

                        val entry = WorkEntry(
                            date = date,
                            hours = gearbeiteteStunden,
                            hourlyRate = stundenlohn,
                            startTime = startzeit,
                            endTime = endzeit,
                            timeDisplay = timeDisplay
                        )
                        workEntries.add(entry)
                    }

                    // Liste aktualisieren
                    adapter.notifyDataSetChanged()

                    // Zeige die berechnete Zeit im Stunden-Feld an, wenn sie vorhanden ist
                    if (berechnetStunden != null) {
                        etGearbeiteteStunden.setText(String.format("%.2f", berechnetStunden))
                    }

                    computeTotal()

                    // Felder zurücksetzen nach dem Hinzufügen
                    etStartzeit.setText("")
                    etEndzeit.setText("")
                } catch (e: Exception) {
                    // Fehlerbehandlung für ungültiges Datumsformat
                    Log.e(TAG, "Fehler beim Parsen des Datums: $e")
                }
            }
        }
    }

    // Hilfsfunktion zur Konvertierung einer Zeitangabe (HH:MM) in Minuten für einfachere Vergleiche
    private fun convertTimeToMinutes(timeString: String): Int {
        if (timeString.isEmpty()) return 0

        val parts = timeString.split(":")
        if (parts.size != 2) return 0

        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0

        return hours * 60 + minutes
    }

    //computes the value of Heute verdient and Gesamtlohn
    private fun computeTotal() {
        val stundenlohn = etStundenlohn.text.toString().toDoubleOrNull()
        val gearbeiteteStunden = etGearbeiteteStunden.text.toString().toDoubleOrNull()
        val startlohn = etStartlohn.text.toString().toDoubleOrNull()

        if (stundenlohn != null && gearbeiteteStunden != null && startlohn != null) {
            val heuteVerdient = stundenlohn * gearbeiteteStunden
            val gesamtlohn = startlohn + heuteVerdient

            tvHeuteVerdientZahl.text = "${"%.2f".format(heuteVerdient)}€"
            tvGesamtlohnZahl.text = "${"%.2f".format(gesamtlohn)}€"
        } else if (stundenlohn != null && gearbeiteteStunden != null) {
            val heuteVerdient = stundenlohn * gearbeiteteStunden
            tvHeuteVerdientZahl.text = "${"%.2f".format(heuteVerdient)}€"
            tvGesamtlohnZahl.text = "${"%.2f".format(heuteVerdient)}€"
        } else {
            tvHeuteVerdientZahl.text = "0.00€"
            tvGesamtlohnZahl.text = "0.00€"
        }

        val totalEarnings = workEntries.sumOf { it.earnings }
        tvGesamtlohnZahl.text = getString(R.string.total_earnings, totalEarnings)
    }

    //saves the values of the EditTexts
    override fun onPause() {
        super.onPause()
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("stundenlohn", etStundenlohn.text.toString())
            putString("gearbeiteteStunden", etGearbeiteteStunden.text.toString())
            putString("startlohn", etStartlohn.text.toString())
            putString("startzeit", etStartzeit.text.toString())
            putString("endzeit", etEndzeit.text.toString())
            apply()
        }
    }

    //loads the values of the EditTexts
    override fun onResume() {
        super.onResume()
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        etStundenlohn.setText(sharedPref.getString("stundenlohn", ""))
        etGearbeiteteStunden.setText(sharedPref.getString("gearbeiteteStunden", ""))
        etStartlohn.setText(sharedPref.getString("startlohn", ""))
        etStartzeit.setText(sharedPref.getString("startzeit", ""))
        etEndzeit.setText(sharedPref.getString("endzeit", ""))
    }
}
