package club.laravels.myappointments.io

import club.laravels.myappointments.model.User

data class LoginResponse (val success: String, val user: User, val jwt: String)