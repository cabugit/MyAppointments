package club.laravels.myappointments.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import club.laravels.myappointments.R
import club.laravels.myappointments.model.Appointment

class AppointmentAdapter :
    RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    var appointments = ArrayList<Appointment>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private val tvAppointmentId = itemView.findViewById<TextView>(R.id.tvAppointmentId)
        private val tvDoctorName    = itemView.findViewById<TextView>(R.id.tvDoctorName)
        private val tvSheduledDate  = itemView.findViewById<TextView>(R.id.tvSheduledDate)
        private val tvSheduledTime  = itemView.findViewById<TextView>(R.id.tvSheduledTime)
        private val tvStatus  = itemView.findViewById<TextView>(R.id.tvStatus)
        private val ibExpand  = itemView.findViewById<ImageButton>(R.id.ibExpand)
        private val linearLayoutDetails  = itemView.findViewById<LinearLayout>(R.id.linearLayoutDetails)
        private val tvSpecialty  = itemView.findViewById<TextView>(R.id.tvSpecialty)
        private val tvType  = itemView.findViewById<TextView>(R.id.tvType)
        private val tvCreatedAt  = itemView.findViewById<TextView>(R.id.tvCreatedAt)
        private val tvDescription  = itemView.findViewById<TextView>(R.id.tvDescription)

        fun bindItems(data: Appointment) {
            with(itemView.context) {
                tvAppointmentId.text = getString(R.string.item_appointment_id, data.id)
                tvDoctorName.text = data.doctor.name
                tvSheduledDate.text =
                    getString(R.string.item_appointment_date, data.sheduledDate)
                tvSheduledTime.text =
                    getString(R.string.item_appointment_time, data.sheduledTime)

                // Detail frame
                tvStatus.text = data.status
                tvSpecialty.text = data.specialty.toString()
                tvType.text = data.type
                tvCreatedAt.text = getString(R.string.item_appointment_created_at, data.createdAt)
                tvDescription.text = data.description

                ibExpand.setOnClickListener {

                    // Falla la transición
                    //TransitionManager.beginDelayedTransition(parent as ViewGroup, AutoTransition())

                    if(linearLayoutDetails.visibility == View.VISIBLE){
                        linearLayoutDetails.visibility = View.GONE
                        ibExpand.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                    } else {
                        linearLayoutDetails.visibility = View.VISIBLE
                        ibExpand.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
                    }
                }
            }
        }
    }

    // Inflates XML items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(v)
    }

    // Binds data
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = appointments[position]
        data.let { holder.bindItems(it) }
    }

    // Number of Elements
    override fun getItemCount() = appointments.size
}