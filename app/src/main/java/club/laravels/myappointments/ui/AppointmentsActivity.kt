package club.laravels.myappointments.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import club.laravels.myappointments.R
import club.laravels.myappointments.io.ApiService
import club.laravels.myappointments.model.Appointment
import club.laravels.myappointments.util.PreferenceHelper
import club.laravels.myappointments.util.PreferenceHelper.get
import club.laravels.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentsActivity : AppCompatActivity() {

    private val apiService : ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy{
        PreferenceHelper.defaultPrefs(this)
    }

    private val appointmentAdapter = AppointmentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)
    }

    override fun onStart() {
        super.onStart()
        loadAppointments()
        val rvAppointments = findViewById<RecyclerView>(R.id.rvAppointments)
        rvAppointments.layoutManager = LinearLayoutManager(this)
        rvAppointments.adapter = appointmentAdapter
    }

    private fun loadAppointments(){
        val jwt = preferences["jwt", ""]
        val call = apiService.getAppointments("Bearer $jwt")
        call.enqueue(object: Callback<ArrayList<Appointment>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ArrayList<Appointment>>,
                response: Response<ArrayList<Appointment>>
            ) {
                if(response.isSuccessful){
                    response.body()?.let {
                        appointmentAdapter.appointments = it
                        appointmentAdapter.notifyDataSetChanged()
                    }

                } else {
                    toast("Se presentó un fallo inesperado al obtener las citas.")
                }
            }
            override fun onFailure(call: Call<ArrayList<Appointment>>, t: Throwable) {
                toast(t.localizedMessage!!)
            }
        })
    }
}