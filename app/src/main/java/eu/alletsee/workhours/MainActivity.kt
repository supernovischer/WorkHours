package eu.alletsee.workhours

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.compose.material3.Scaffold

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var etStundenlohn: EditText
    private lateinit var etGearbeiteteStunden: EditText
    private lateinit var tvHeuteVerdientZahl: TextView
    private lateinit var etStartlohn: EditText
    private lateinit var tvGesamtlohnZahl: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etStundenlohn = findViewById(R.id.etStundenlohn)
        etGearbeiteteStunden = findViewById(R.id.etGearbeiteteStunden)
        tvHeuteVerdientZahl = findViewById(R.id.tvHeuteVerdientZahl)
        etStartlohn = findViewById(R.id.etStartlohn)
        tvGesamtlohnZahl = findViewById(R.id.tvGesamtlohnZahl)

        etStundenlohn.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "Stundenlohn: $s")
                computeTotal()
            }
        })

        etGearbeiteteStunden.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "Gearbeitete Stunden: $s")
                computeTotal()
            }
        })

        etStartlohn.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "Gesamtlohn: $s")
                computeTotal()
            }
        })

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
        } else if(stundenlohn != null && gearbeiteteStunden != null){
val heuteVerdient = stundenlohn * gearbeiteteStunden
            tvHeuteVerdientZahl.text = "${"%.2f".format(heuteVerdient)}€"
            tvGesamtlohnZahl.text = "${"%.2f".format(heuteVerdient)}€"
        } else {
            tvHeuteVerdientZahl.text = "0.00€"
            tvGesamtlohnZahl.text = "0.00€"
        }
    }

    //saves the values of the EditTexts
    override fun onPause() {
        super.onPause()
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("stundenlohn", etStundenlohn.text.toString())
            putString("gearbeiteteStunden", etGearbeiteteStunden.text.toString())
            putString("startlohn", etStartlohn.text.toString())
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

    }





}