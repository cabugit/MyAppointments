package club.laravels.myappointments.io.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import club.laravels.myappointments.R
import club.laravels.myappointments.io.ApiService
import club.laravels.myappointments.ui.MainActivity
import club.laravels.myappointments.util.PreferenceHelper
import club.laravels.myappointments.util.PreferenceHelper.get
import club.laravels.myappointments.util.toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FCMService : FirebaseMessagingService() {

    private val apiService : ApiService by lazy {
        ApiService.create()
    }

    private  val preferences by lazy{
        PreferenceHelper.defaultPrefs(this)
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // Handle FCM messages here.
        Log.d(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            handleNow()
        }

        // Check if message contains a notification payload.
        remoteMessage.notification.let {
            val body = remoteMessage.notification?.body.toString()
            if(remoteMessage.notification?.title.isNullOrEmpty()){
                Log.d(TAG, "Message Notification Title: ${R.string.app_name}")
                sendNotification(R.string.app_name.toString(), body)
            } else {
                Log.d(TAG, "Message Notification Title: ${remoteMessage.notification?.title.toString()}")
                sendNotification(remoteMessage.notification?.title.toString(), body)
            }
            Log.d(TAG, "Message Notification Body: $body")


        }

        // sendNotification()
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]
    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(newtoken: String?) {
        // TODO: Implement this method to send token to your app server.
        //Log.d(TAG, "sendRegistrationTokenToServer($token)")
        if(newtoken == null)
            return
        val jwt = preferences["jwt", ""]
        if(jwt.isEmpty())
            return
        val authHeader= "Bearer $jwt"
        val call = apiService.postToken(authHeader, newtoken)
        call.enqueue(object: Callback<Void> {
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
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(messageTitle: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_calendar)
            /*.setContentTitle(messageTitle)
            .setContentText(messageBody)*/
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(messageTitle)
        bigTextStyle.bigText(messageBody)
        notificationBuilder.setStyle(bigTextStyle)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager!!.createNotificationChannel(channel)
        }

        notificationManager!!.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FCMService"
    }
}