package club.laravels.myappointments.model

data class Schedule(val morning: ArrayList<HourInterval>, val afternoon: ArrayList<HourInterval>)