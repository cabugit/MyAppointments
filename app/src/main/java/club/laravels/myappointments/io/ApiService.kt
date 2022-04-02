package club.laravels.myappointments.io

import club.laravels.myappointments.model.Appointment
import club.laravels.myappointments.model.Doctor
import club.laravels.myappointments.model.Schedule
import club.laravels.myappointments.model.Specialty
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @GET("specialties")
    fun getSpecialties() : Call<ArrayList<Specialty>>

    @GET("specialties/{specialty}/doctors")
    fun getDoctors(@Path("specialty") specialtyId: Int) : Call<ArrayList<Doctor>>

    @GET("schedule/hours")
    fun getHours(@Query("doctor_id") doctorId: Int,
                 @Query ("date") date : String) : Call<Schedule>

    @POST("login")
    fun postLogin(@Query("email") email: String,
                 @Query ("password") password : String)
                 : Call<LoginResponse>

    @POST("logout")
    fun postLogout(@Header("Authorization") authHeader: String) : Call<Void>

    @GET("appointments")
    fun getAppointments(@Header("Authorization") authHeader: String) :
            Call<ArrayList<Appointment>>

    @POST("appointments")
    @Headers("Accept: application/json")
    fun storeAppointment(
         @Header("Authorization") authHeader: String,
         @Query("description") description : String,
         @Query("specialty_id") specialtyId : Int,
         @Query("doctor_id") doctorId : Int,
         @Query("scheduled_date") scheduledDate : String,
         @Query("scheduled_time") scheduledTime : String,
         @Query("type") type : String
         ) : Call<SimpleResponse>

    @POST("register")
    @Headers("Accept: application/json")
    fun postRegister(
        @Query("name") name : String,
        @Query("email") email : String,
        @Query("password") password : String,
        @Query("password_confirmation") password_confirmation : String
    ) : Call<LoginResponse>

    @POST("fcm/token")
    @Headers("Accept: application/json")
    fun postToken(
        @Header("Authorization") authHeader: String,
        @Query("device_token") Token: String
    ) : Call<Void>

    companion object Factory {
        private const val BASE_URL = "https://laravels.club/api/"

        fun create(): ApiService {
            val interceptor = HttpLoggingInterceptor()                                  // add
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)                     // add
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()     // add
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())              // add
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)                                                         // add
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}