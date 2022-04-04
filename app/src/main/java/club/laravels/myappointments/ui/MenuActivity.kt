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
import club.laravels.myappointments.model.User
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

    private val authHeader by lazy {
        val jwt = preferences["jwt", ""]
        "Bearer $jwt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val storeToken = intent.getBooleanExtra("store_token", false)
        if (storeToken) storeToken()

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnCreateAppointment = findViewById<Button>(R.id.btnCreateAppointment)
        val btnMyAppointments = findViewById<Button>(R.id.btnMyAppointments)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnProfile.setOnClickListener {
            editProfile()
        }
        btnCreateAppointment.setOnClickListener {
            createAppointment()
        }
        btnMyAppointments.setOnClickListener {
            val intent = Intent(this, AppointmentsActivity::class.java)
            startActivity(intent)
        }
        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun createAppointment() {
        val call = apiService.getUser(authHeader)
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        val phoneLength = user.phone.length
                        if (phoneLength != 10) {
                            toast(getString(R.string.you_need_a_phone))
                            editProfile()
                            return
                        }
                    }
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                toast(t.localizedMessage!!)
            }
        })
        val intent = Intent(this, CreateAppointmentActivity::class.java)
        startActivity(intent)
    }

    private fun editProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun performLogout() {
        val intent = Intent(this, MainActivity::class.java)
        val call = apiService.postLogout(authHeader)

        call.enqueue(object : Callback<Void> {
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

    private fun clearSessionPreference() {
        preferences["jwt"] = ""
    }

    private fun storeToken() {
        // Get token
        Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            val call = apiService.postToken(authHeader, token)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Token registrado correctamente.")
                    } else {
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