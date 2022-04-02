package club.laravels.myappointments.ui

//import club.laravels.myappointments.databinding.ActivityMainBinding
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import club.laravels.myappointments.R
import club.laravels.myappointments.io.ApiService
import club.laravels.myappointments.io.LoginResponse
import club.laravels.myappointments.util.PreferenceHelper
import club.laravels.myappointments.util.PreferenceHelper.get
import club.laravels.myappointments.util.PreferenceHelper.set
import club.laravels.myappointments.util.toast
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val snack by lazy {
        Snackbar.make(
            findViewById(R.id.mainLayout),
            R.string.press_back_again, Snackbar.LENGTH_SHORT
        )
    }

    private val apiService : ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy{
        PreferenceHelper.defaultPrefs(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW)
            )
        }

        // Handle possible data accompanying notification message.
        /*intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                //Log.d(TAG, "Key: $key Value: $value")
            }
        }*/

        if (preferences["jwt", ""].contains("|"))
            goToMenuActivity()

        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvGoToRegister.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.please_fill_your_register_data),
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin(){
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        if(email == "" || password == ""){
            toast(getString(R.string.error_empty_credentials))
            return
        }
        val call = apiService.postLogin(email, password)
        call.enqueue(object: Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful){
                    val loginResponse = response.body()
                    if(loginResponse == null){
                        toast(getString(R.string.error_login_response))
                        return
                    }
                    if(loginResponse.success=="true"){
                        createSessionPreference(loginResponse.jwt)
                        toast(getString(R.string.welcome_name, loginResponse.user.name))
                        goToMenuActivity(true)
                    } else {
                        toast(getString(R.string.error_invalid_credentials))
                    }
                } else {
                    toast(getString(R.string.error_login_response))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                toast(t.localizedMessage!!)
            }

        })
    }

    private fun createSessionPreference(jwt: String) {
        val preferences = PreferenceHelper.defaultPrefs(this)
        preferences["jwt"] = jwt
    }

    private fun goToMenuActivity(isUserInput: Boolean = false) {
        val intent = Intent(this, MenuActivity::class.java)
        if(isUserInput){
            intent.putExtra("store_token", true)
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (snack.isShown)
            super.onBackPressed()
        else
            snack.show()
    }

    /*companion object {
        private const val TAG = "FCMService"
    }*/
}