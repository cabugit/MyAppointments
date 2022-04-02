package club.laravels.myappointments.ui

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import club.laravels.myappointments.R
import club.laravels.myappointments.io.ApiService
import club.laravels.myappointments.io.SimpleResponse
import club.laravels.myappointments.model.Doctor
import club.laravels.myappointments.model.Schedule
import club.laravels.myappointments.model.Specialty
import club.laravels.myappointments.util.PreferenceHelper
import club.laravels.myappointments.util.toast
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import club.laravels.myappointments.util.PreferenceHelper.get

class CreateAppointmentActivity : AppCompatActivity() {

    private val selectedCalendar = Calendar.getInstance()
    private var selectedTimeRadioBtn: RadioButton? = null

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy{
        PreferenceHelper.defaultPrefs(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_appointment)

        val createAppointmentLinearLayout = findViewById<LinearLayout>(R.id.createAppointmentLinearLayout)

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etScheduledDate = findViewById<EditText>(R.id.etSheduledDate)
        /*val radioGroupType = findViewById<RadioGroup>(R.id.radioGroupType)
        val spinnerSpecialties = findViewById<Spinner>(R.id.spinnerSpecialties)
        val spinnerDoctors = findViewById<Spinner>(R.id.spinnerDoctors)
        val radioGroupLeft = findViewById<LinearLayout>(R.id.radioGroupLeft)
        val radioGroupRight = findViewById<LinearLayout>(R.id.radioGroupRight)
        val tvSelectDoctorAndDate = findViewById<TextView>(R.id.tvSelectDoctorAndDate)
        val tvNotAvailableHours   = findViewById<TextView>(R.id.tvNotAvailableHours)*/

        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnNext2 = findViewById<Button>(R.id.btnNext2)
        val cvStep1 = findViewById<androidx.cardview.widget.CardView>(R.id.cvStep1)
        val cvStep2 = findViewById<androidx.cardview.widget.CardView>(R.id.cvStep2)
        val cvStep3 = findViewById<androidx.cardview.widget.CardView>(R.id.cvStep3)
        val btnConfirmAppointment = findViewById<Button>(R.id.btnConfirmAppointment)



        etScheduledDate.setOnClickListener {
            val year = selectedCalendar.get(Calendar.YEAR)
            val month = selectedCalendar.get(Calendar.MONTH)
            val dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)
            val listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                //Toast.makeText(this, "$y-$m-$d", Toast.LENGTH_SHORT).show()
                selectedCalendar.set(y, m, d)
                //etScheduledDate.setText(resources.getString(R.string.date_format, y, m, d))
                etScheduledDate.setText(
                    resources.getString(
                        R.string.date_format, y,
                        (m+1).twoDigits(), // DatePicker (Mes de 0 a 11)
                        d.twoDigits(),
                    )
                )
                etScheduledDate.error = null
            }

            //DatePickerDialog(this, listener, year, month, dayOfMonth).show()
            val datePickerDialog = DatePickerDialog(this, listener, year, month, dayOfMonth)
            val datePicker = datePickerDialog.datePicker
            val calendar = Calendar.getInstance()
            // Min date
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            datePicker.minDate = calendar.timeInMillis //+1
            // Max Date
            calendar.add(Calendar.DAY_OF_MONTH, 29)
            datePicker.maxDate = calendar.timeInMillis //+30
            datePickerDialog.show()
        }

        btnNext.setOnClickListener {
            if (etDescription.text.toString().length < 3) {
                etDescription.error = getString(R.string.validate_appointment_description)
            } else {
                // Continue to step 2
                cvStep1.visibility = View.GONE
                cvStep2.visibility = View.VISIBLE
            }
        }

        btnNext2.setOnClickListener {
            when {
                etScheduledDate.text.toString().isEmpty() ->
                {
                    etDescription.error = getString(R.string.validate_appointment_date)
                    Snackbar.make(createAppointmentLinearLayout,
                        R.string.validate_appointment_date, Snackbar.LENGTH_SHORT).show()
                }
                selectedTimeRadioBtn == null ->
                {
                    Snackbar.make(createAppointmentLinearLayout,
                        R.string.validate_appointment_time, Snackbar.LENGTH_SHORT).show()
                }
                else ->
                {
                    showAppointmentDataConfirm()
                    // Continue to step 3
                    cvStep2.visibility = View.GONE
                    cvStep3.visibility = View.VISIBLE
                }
            }
        }

        btnConfirmAppointment.setOnClickListener {
            performStoreAppointment()
        }

        loadSpecialties()
        listenSpecialtyChanges()
        listenDoctorAndDateChanges()
    }

    private fun performStoreAppointment(){
        val btnConfirmAppointment = findViewById<Button>(R.id.btnConfirmAppointment)
        btnConfirmAppointment.isClickable = false

        val tvConfirmDescription = findViewById<TextView>(R.id.tvConfirmDescription)
        val spinnerSpecialties = findViewById<Spinner>(R.id.spinnerSpecialties)
        val specialty = spinnerSpecialties.selectedItem as Specialty
        val spinnerDoctors = findViewById<Spinner>(R.id.spinnerDoctors)
        val doctor = spinnerDoctors.selectedItem as Doctor
        val tvConfirmDate = findViewById<TextView>(R.id.tvConfirmDate)
        val tvConfirmTime = findViewById<TextView>(R.id.tvConfirmTime)
        val tvConfirmType = findViewById<TextView>(R.id.tvConfirmType)
        val jwt = preferences["jwt", ""]
        val authHeader = "Bearer $jwt"
        val description = tvConfirmDescription.text.toString()
        val scheduledDate = tvConfirmDate.text.toString()
        val scheduledTime = tvConfirmTime.text.toString()
        val type = tvConfirmType.text.toString()
        val call = apiService.storeAppointment(
            authHeader, description, specialty.id, doctor.id,
            scheduledDate, scheduledTime,type
        )
        call.enqueue(object: Callback<SimpleResponse>{
            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {
                if(response.isSuccessful) {
                    toast(getString(R.string.create_appointment_success))
                    finish()
                } else {
                    toast(getString(R.string.create_appointment_error))
                    btnConfirmAppointment.isClickable = true
                }
            }
            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                toast(t.localizedMessage!!)
                btnConfirmAppointment.isClickable = true
            }
        })
    }

    private fun loadSpecialties(){
        val spinnerSpecialties = findViewById<Spinner>(R.id.spinnerSpecialties)
        val call = apiService.getSpecialties()
        call.enqueue(object: Callback<ArrayList<Specialty>> {
            override fun onResponse(
                call: Call<ArrayList<Specialty>>,
                response: Response<ArrayList<Specialty>>
            ) {
                if(response.isSuccessful){
                    val specialties = response.body()!!.toMutableList()
                    spinnerSpecialties.adapter = ArrayAdapter(this@CreateAppointmentActivity, android.R.layout.simple_list_item_1, specialties)
                }
            }
            override fun onFailure(call: Call<ArrayList<Specialty>>, t: Throwable) {
                Toast.makeText(this@CreateAppointmentActivity,
                    getString(R.string.error_loading_specialties), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun listenSpecialtyChanges(){
        val spinnerSpecialties = findViewById<Spinner>(R.id.spinnerSpecialties)
        spinnerSpecialties.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val speciality: Specialty = adapter?.getItemAtPosition(position) as Specialty
                //Toast.makeText(this@CreateAppointmentActivity, "Id: ${speciality.id}", Toast.LENGTH_SHORT).show()
                loadDoctors(speciality.id)
            }
            override fun onNothingSelected(adapter: AdapterView<*>?) {

            }
        }
    }

    private fun loadDoctors(specialityId: Int){
        val spinnerDoctors = findViewById<Spinner>(R.id.spinnerDoctors)
        val call = apiService.getDoctors(specialityId)
        call.enqueue(object: Callback<ArrayList<Doctor>> {
            override fun onResponse(
                call: Call<ArrayList<Doctor>>,
                response: Response<ArrayList<Doctor>>
            ) {
                if(response.isSuccessful){
                    val doctors = response.body()!!.toMutableList()
                    spinnerDoctors.adapter = ArrayAdapter(this@CreateAppointmentActivity, android.R.layout.simple_list_item_1, doctors)
                }
            }
            override fun onFailure(call: Call<ArrayList<Doctor>>, t: Throwable) {
                Toast.makeText(this@CreateAppointmentActivity,
                    getString(R.string.error_loading_doctors), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun listenDoctorAndDateChanges() {
        val spinnerDoctors = findViewById<Spinner>(R.id.spinnerDoctors)
        val etScheduledDate = findViewById<EditText>(R.id.etSheduledDate)

        // Doctors
        spinnerDoctors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapter: AdapterView<*>?,
                p1: View?,
                position: Int,
                p3: Long
            ) {
                val doctor: Doctor = adapter?.getItemAtPosition(position) as Doctor
                //Toast.makeText(this@CreateAppointmentActivity, "Id: ${doctor.id}", Toast.LENGTH_SHORT).show()
                loadHours(doctor.id, etScheduledDate.text.toString())
            }

            override fun onNothingSelected(adapter: AdapterView<*>?) {
            }
        }
        // Date
        etScheduledDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val doctor: Doctor = spinnerDoctors.selectedItem as Doctor
                loadHours(doctor.id, etScheduledDate.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

    }

    private fun loadHours(doctorId: Int, date: String){
        /*val radioGroupLeft = findViewById<LinearLayout>(R.id.radioGroupLeft)
        val radioGroupRight = findViewById<LinearLayout>(R.id.radioGroupRight)*/
        val call = apiService.getHours(doctorId, date)
        if(date.isEmpty()) {
            return
        }
        call.enqueue(object : Callback<Schedule> {
            override fun onResponse(call: Call<Schedule>, response: Response<Schedule>) {
                if (response.isSuccessful) {
                    val schedule = response.body()
                    /*Toast.makeText(
                        this@CreateAppointmentActivity,
                        "Morning: ${schedule?.morning?.size}, Afternoon: ${schedule?.afternoon?.size}",
                        Toast.LENGTH_SHORT).show()*/
                    val hours = ArrayList<String>()
                    schedule?.let {
                        val intervals = it.morning + it.afternoon
                        intervals.forEach { interval->
                            hours.add(interval.start)
                        }
                    }
                    displayIntervalRadios(hours)
                }
            }

            override fun onFailure(call: Call<Schedule>, t: Throwable) {
                Toast.makeText(
                    this@CreateAppointmentActivity,
                    getString(R.string.error_loading_hours), Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        })

        /*Toast.makeText(this@CreateAppointmentActivity,
            "Doctor: $doctorId, Date: $date", Toast.LENGTH_SHORT).show()*/
    }

    private fun showAppointmentDataConfirm() {
        /*val radioGroupLeft = findViewById<LinearLayout>(R.id.radioGroupLeft)
        val radioGroupRight = findViewById<LinearLayout>(R.id.radioGroupRight)*/
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val radioGroupType = findViewById<RadioGroup>(R.id.radioGroupType)
        val etScheduledDate = findViewById<EditText>(R.id.etSheduledDate)
        val spinnerSpecialties = findViewById<Spinner>(R.id.spinnerSpecialties)
        val spinnerDoctors = findViewById<Spinner>(R.id.spinnerDoctors)
        val tvConfirmDoctorName = findViewById<TextView>(R.id.tvConfirmDoctorName)
        val tvConfirmSpecialty = findViewById<TextView>(R.id.tvConfirmSpecialty)
        val tvConfirmDate = findViewById<TextView>(R.id.tvConfirmDate)
        val tvConfirmTime = findViewById<TextView>(R.id.tvConfirmTime)
        val tvConfirmType = findViewById<TextView>(R.id.tvConfirmType)
        val tvConfirmDescription = findViewById<TextView>(R.id.tvConfirmDescription)

        tvConfirmDoctorName.text = spinnerDoctors.selectedItem.toString()
        tvConfirmSpecialty.text = spinnerSpecialties.selectedItem.toString()
        tvConfirmDate.text = etScheduledDate.text.toString()
        tvConfirmTime.text = selectedTimeRadioBtn?.text.toString()
        val selectedRadioButtonId = radioGroupType.checkedRadioButtonId
        val selectedRadioType = radioGroupType.findViewById<RadioButton>(selectedRadioButtonId)
        tvConfirmType.text = selectedRadioType.text.toString()
        tvConfirmDescription.text = etDescription.text.toString()
    }

    private fun displayIntervalRadios(hours: ArrayList<String>) {
        val radioGroupLeft = findViewById<LinearLayout>(R.id.radioGroupLeft)
        val radioGroupRight = findViewById<LinearLayout>(R.id.radioGroupRight)
        val tvSelectDoctorAndDate = findViewById<TextView>(R.id.tvSelectDoctorAndDate)
        val tvNotAvailableHours   = findViewById<TextView>(R.id.tvNotAvailableHours)
        radioGroupLeft.removeAllViews()
        radioGroupRight.removeAllViews()
        selectedTimeRadioBtn = null

        //val hours = arrayOf("8:30 AM", "9:00 AM", "3:00 PM", "4:30 PM")
        if(hours.isEmpty()){
            tvNotAvailableHours.visibility = View.VISIBLE
            return
        }
        tvSelectDoctorAndDate.visibility = View.GONE
        tvNotAvailableHours.visibility = View.GONE

        var goToLeft = true
        hours.forEach {
            val radioButton = RadioButton(this)
            radioButton.id = View.generateViewId()
            radioButton.text = it
            radioButton.setOnClickListener { View ->
                selectedTimeRadioBtn?.isChecked = false
                selectedTimeRadioBtn = View as RadioButton?
                selectedTimeRadioBtn?.isChecked = true
            }
            if (goToLeft)
                radioGroupLeft.addView(radioButton)
            else
                radioGroupRight.addView(radioButton)
            goToLeft = !goToLeft
        }
    }

    private fun Int.twoDigits() = if (this > 9) "$this" else "0$this"

    override fun onBackPressed() {
        val cvStep1 = findViewById<androidx.cardview.widget.CardView>(R.id.cvStep1)
        val cvStep2 = findViewById<androidx.cardview.widget.CardView>(R.id.cvStep2)
        val cvStep3 = findViewById<androidx.cardview.widget.CardView>(R.id.cvStep3)
        when {
            cvStep3.visibility == View.VISIBLE -> {
                cvStep3.visibility = View.GONE
                cvStep2.visibility = View.VISIBLE
            }
            cvStep2.visibility == View.VISIBLE -> {
                cvStep2.visibility = View.GONE
                cvStep1.visibility = View.VISIBLE
            }
            cvStep1.visibility == View.VISIBLE -> {
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.dialog_create_appointment_exit_title))
                builder.setMessage(getString(R.string.dialog_create_appointment_exit_message))
                builder.setPositiveButton(getString(R.string.dialog_create_appointment_exit_positive_button)) { _, _ ->
                    finish()
                }
                builder.setNegativeButton(getString(R.string.dialog_create_appointment_exit_negative_button)) { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }
}