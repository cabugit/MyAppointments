package club.laravels.myappointments.model

data class Specialty(val id:Int, val name:String) {
    override fun toString(): String {
        return name
    }
}