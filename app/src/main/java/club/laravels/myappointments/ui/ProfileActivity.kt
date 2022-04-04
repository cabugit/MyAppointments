package club.laravels.myappointments.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import club.laravels.myappointments.R
import club.laravels.myappointments.io.ApiService
import club.laravels.myappointments.model.User
import club.laravels.myappointments.util.PreferenceHelper
import club.laravels.myappointments.util.PreferenceHelper.get
import club.laravels.myappointments.util.toast
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy{
        PreferenceHelper.defaultPrefs(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val jwt = preferences["jwt", ""]
        val authHeader = "Bearer $jwt"
        val call = apiService.getUser(authHeader)
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if(user != null)
                        displayProfileData(user)
                }

            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                toast(t.localizedMessage!!)
            }
        })

        /*Handler().postDelayed({
            displayProfileData()
        }, 3000)*/
    }

    private fun displayProfileData(user: User) {
        val progressBarProfile = findViewById<ProgressBar>(R.id.progressBarProfile)
        val linearLayoutProfile = findViewById<LinearLayout>(R.id.linearLayoutProfile)
        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)

        etName.setText(user.name)
        etPhone.setText(user.phone)
        etAddress.setText(user.address)

        progressBarProfile.visibility = View.GONE
        linearLayoutProfile.visibility = View.VISIBLE

        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile(){
        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val name = etName.text.toString()
        val phone = etPhone.text.toString()
        val address = etAddress.text.toString()
        // Validation
        val inputLayoutName  = findViewById<TextInputLayout>(R.id.inputLayoutName)
        val inputLayoutPhone = findViewById<TextInputLayout>(R.id.inputLayoutPhone)
        if(name.length < 4){
            inputLayoutName.error = getString(R.string.error_profile_name)
            return
        }
        if(phone.length != 10){
            inputLayoutPhone.error = getString(R.string.error_profile_phone)
            return
        }
        val jwt = preferences["jwt", ""]
        val authHeader = "Bearer $jwt"
        val call = apiService.postUser(authHeader,name, phone, address)
        call.enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    toast(getString(R.string.profile_success_message))
                    finish()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                toast(t.localizedMessage!!)
            }
        })
    }
}