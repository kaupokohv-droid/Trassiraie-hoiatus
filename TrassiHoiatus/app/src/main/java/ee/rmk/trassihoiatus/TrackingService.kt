package ee.rmk.trassihoiatus

import android.app.*
import android.content.*
import android.location.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import java.io.File
import java.util.Locale

class TrackingService : Service(), LocationListener {
 companion object {
  const val CHANNEL="trassihoiatus"; const val ACTION_UPDATE="ee.rmk.trassihoiatus.UPDATE"
  const val EXTRA_STATUS="status"
 }
 private lateinit var manager: LocationManager
 private var corridor: Corridor?=null
 private var badCount=0
 private var lastAlarm=0L
 private val tone by lazy { ToneGenerator(AudioManager.STREAM_ALARM,90) }
 override fun onBind(intent: Intent?) : IBinder?=null
 override fun onCreate() {
  super.onCreate()
  val cm=getSystemService(NotificationManager::class.java)
  cm.createNotificationChannel(NotificationChannel(CHANNEL,"Trassihoiatused",NotificationManager.IMPORTANCE_HIGH))
  manager=getSystemService(LOCATION_SERVICE) as LocationManager
 }
 private fun notification(message:String, alarm:Boolean=false): Notification {
  val builder=Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_map)
   .setContentTitle(if(alarm) "TRASSIHOIATUS" else "Trassi jälgimine").setContentText(message)
   .setOngoing(!alarm).setContentIntent(PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE))
  if(alarm) builder.setCategory(Notification.CATEGORY_ALARM).setAutoCancel(true)
  return builder.build()
 }
 private fun status(s:String) { sendBroadcast(Intent(ACTION_UPDATE).setPackage(packageName).putExtra(EXTRA_STATUS,s)) }
 private fun alarm(s:String) {
  val now=SystemClock.elapsedRealtime()
  if(now-lastAlarm<15000) return
  lastAlarm=now
  tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,1100)
  (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(2,notification(s,true))
 }
 @Suppress("MissingPermission")
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
  if(intent?.action=="STOP") { stopSelf(); return START_NOT_STICKY }
  try { corridor=Corridor.parse(File(filesDir,"corridor.geojson").readText()) }
  catch(e:Exception) { status("Trassifail puudub või on vigane: ${e.message}"); stopSelf(); return START_NOT_STICKY }
  startForeground(1,notification("GPS-i jälgimine on aktiivne"))
  try {
   manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,0f,this,Looper.getMainLooper())
  } catch(e:Exception) { status("GPS-i käivitamine ebaõnnestus: ${e.message}"); stopSelf() }
  return START_STICKY
 }
 override fun onLocationChanged(location:Location) {
  val acc=if(location.hasAccuracy()) location.accuracy else Float.POSITIVE_INFINITY
  if(acc>12f) { badCount=0; status("GPS ebatäpne: ±${acc.toInt()} m; asukohahoiatus peatatud") ; alarm("GPS-i täpsus on ebapiisav"); return }
  val d=corridor?.signedDistanceMeters(location.longitude,location.latitude) ?: return
  val state=when { d<0 -> "VÄLJAS"; d<5 -> "PIIRI LÄHEDAL"; else -> "TRASSIL" }
  status(String.format(Locale.US,"%s | piirini %.1f m | GPS ±%.1f m",state,d,acc))
  if(d<5) badCount++ else badCount=0
  if(badCount>=3) alarm(if(d<0) "Masin on lubatud trassist väljas" else "Lähened lubatud trassi piirile")
 }
 override fun onProviderDisabled(provider:String) { status("GPS on välja lülitatud"); alarm("GPS on välja lülitatud") }
 override fun onDestroy() { manager.removeUpdates(this); tone.release(); super.onDestroy() }
}
