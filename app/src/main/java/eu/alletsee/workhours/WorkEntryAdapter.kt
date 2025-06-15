package eu.alletsee.workhours

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class WorkEntryAdapter(private val entries: List<WorkEntry>) : RecyclerView.Adapter<WorkEntryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.tvEntryDate)
        val hoursTextView: TextView = view.findViewById(R.id.tvEntryHours)
        val earningsTextView: TextView = view.findViewById(R.id.tvEntryEarnings)
        val timeTextView: TextView = view.findViewById(R.id.tvEntryTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.work_entry_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(entry.date)
        holder.hoursTextView.text = String.format("%.1f Std", entry.hours)
        holder.earningsTextView.text = String.format("%.2f€", entry.earnings)

        // Verwende das timeDisplay-Feld, wenn es vorhanden ist, sonst die einfache Start-End-Zeit
        if (entry.timeDisplay.isNotEmpty()) {
            holder.timeTextView.text = entry.timeDisplay
            holder.timeTextView.visibility = View.VISIBLE
        } else if (entry.startTime.isNotEmpty() && entry.endTime.isNotEmpty()) {
            holder.timeTextView.text = "${entry.startTime} - ${entry.endTime}"
            holder.timeTextView.visibility = View.VISIBLE
        } else {
            holder.timeTextView.visibility = View.GONE
        }
    }

    override fun getItemCount() = entries.size
}
