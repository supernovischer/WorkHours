package eu.alletsee.workhours

import java.util.Date

data class WorkEntry(
    val date: Date,
    val hours: Double,
    val hourlyRate: Double,
    val startTime: String = "",
    val endTime: String = "",
    val timeDisplay: String = ""
) {
    val earnings: Double
        get() = hours * hourlyRate
}
