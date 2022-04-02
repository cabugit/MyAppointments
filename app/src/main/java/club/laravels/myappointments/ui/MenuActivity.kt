package club.laravels.myappointments.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import club.laravels.myappointments.util.PreferenceHelper
import club.laravels.myappointments.util.PreferenceHelper.set
import club.laravels.myappointments.util.PreferenceHelper.get
import club.laravels.myappointments.R
import club.laravels.myappointments.io.ApiService
import club.laravels.myappointments.util.toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy {
        PreferenceHelper.defaultPrefs(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val storeToken = intent.getBooleanExtra("store_token", false)
        if (storeToken) storeToken()

        val btnCreateAppointment = findViewById<Button>(R.id.btnCreateAppointment)
        val btnMyAppointments = findViewById<Button>(R.id.btnMyAppointments)

        btnCreateAppointment.setOnClickListener {
            val intent = Intent(this, CreateAppointmentActivity::class.java)
            startActivity(intent)
        }

        btnMyAppointments.setOnClickListener {
            val intent = Intent(this, AppointmentsActivity::class.java)
            startActivity(intent)
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout(){
        val jwt = preferences["jwt", ""]
        val intent = Intent(this, MainActivity::class.java)
        val call = apiService.postLogout("Bearer $jwt")

        call.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                clearSessionPreference()
                startActivity(intent)
                finish()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                toast(t.localizedMessage!!)
            }

        })
    }

    private fun clearSessionPreference(){
        /*
        val preferences = getSharedPreferences("General", Context.MODE_PRIVATE)
         val editor = preferences.edit()
         editor.putBoolean("session", false)
         editor.apply()
         */
        preferences["jwt"] = ""
    }

    private fun storeToken(){
        val jwt = preferences["jwt", ""]
        val authHeader= "Bearer $jwt"
        // Get token
        Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            //val msg = getString(R.string.msg_token_fmt, token)
            //Log.d(TAG, msg)
            //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            val call = apiService.postToken(authHeader, token)
            call.enqueue(object: Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        Log.d(TAG, "Token registrado correctamente.")
                    }
                    else {
                        Log.d(TAG, "Hubo un problema al registrar el token.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    toast(t.localizedMessage!!)
                }

            })
        })
    }

    companion object {
        private const val TAG = "FCMService"
    }
}