package club.laravels.myappointments.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import club.laravels.myappointments.R
import club.laravels.myappointments.io.ApiService
import club.laravels.myappointments.io.LoginResponse
import club.laravels.myappointments.util.PreferenceHelper
import club.laravels.myappointments.util.PreferenceHelper.set
import club.laravels.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private val apiService : ApiService by lazy {
        ApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val clickme = findViewById<TextView>(R.id.tvGoToLogin)
        val btnConfirmRegister = findViewById<Button>(R.id.btnConfirmRegister)

        clickme.setOnClickListener {
            Toast.makeText(this, getString(R.string.please_login_with_your_credentials), Toast.LENGTH_SHORT).show()
            val intent = Intent( this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnConfirmRegister.setOnClickListener {
            performRegister()
        }
    }

    private fun performRegister(){
        val etRegisterName = findViewById<EditText>(R.id.etRegisterName)
        val etRegisterEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etRegisterPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etRegisterPasswordConfirmation = findViewById<EditText>(R.id.etRegisterPasswordConfirmation)
        val name = etRegisterName.text.toString().trim()
        val email = etRegisterEmail.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()
        val passwordConfirmation = etRegisterPasswordConfirmation.text.toString().trim()

        if(name.isEmpty() || email.isEmpty() ||
            password.isEmpty() || passwordConfirmation.isEmpty()){
            toast(getString(R.string.error_registe_empty_fields))
            return
        }
        if(password.length < 8){
            toast(getString(R.string.error_register_password_less_8_characters))
            return
        }
        if(password != passwordConfirmation){
            toast(getString(R.string.error_register_passwords_do_not_match))
            return
        }
        val call = apiService.postRegister(name, email, password, passwordConfirmation)
        call.enqueue(object: Callback<LoginResponse> {
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
                        goToMenuActivity()
                    } else {
                        toast(getString(R.string.error_invalid_credentials))
                    }
                } else {
                    toast(getString(R.string.error_register_validation))
                    return
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

    private fun goToMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}