package club.laravels.myappointments.model

import com.google.gson.annotations.SerializedName

data class Appointment (
    val id: Int,
    val description: String,
    val type: String,
    val status: String,

    @SerializedName("scheduled_date") val sheduledDate: String,
    @SerializedName("scheduled_time_12") val sheduledTime: String,
    @SerializedName("created_at") val createdAt: String,

    val specialty: Specialty,
    val doctor: Doctor
)