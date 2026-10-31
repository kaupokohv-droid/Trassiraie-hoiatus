package ee.rmk.trassihoiatus

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import java.io.File

class MainActivity:Activity() {
 private lateinit var status:TextView
 private val picker=42
 private val receiver=object:BroadcastReceiver() {
  override fun onReceive(context:Context?,intent:Intent?) { status.text=intent?.getStringExtra(TrackingService.EXTRA_STATUS)?:"" }
 }
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val layout=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(32,48,32,24) }
  fun label(s:String)=TextView(this).also { it.text=s; it.textSize=17f; it.setPadding(0,8,0,20); layout.addView(it) }
  label("TRASSIHOIATUS · prototüüp 0.1")
  label("Impordi QGISist lubatud koridori Polygon/MultiPolygon GeoJSON (WGS84 / EPSG:4326).")
  Button(this).apply { text="Vali GeoJSON-fail"; setOnClickListener {
   startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type="*/*"; addCategory(Intent.CATEGORY_OPENABLE) },picker)
  }; layout.addView(this) }
  Button(this).apply { text="Käivita jälgimine"; setOnClickListener { startTracking() }; layout.addView(this) }
  Button(this).apply { text="Peata jälgimine"; setOnClickListener {
   startService(Intent(this@MainActivity,TrackingService::class.java).setAction("STOP")); status.text="Jälgimine peatatud"
  }; layout.addView(this) }
  status=label("Vali trassifail ja käivita jälgimine")
  label("Hoiatus: <5 m piirist, 3 järjestikust GPS-mõõtmist. Kui GPS täpsus >12 m, antakse eraldi hoiatus. Prototüüp ei ole ohutuskriitiline navigatsioonisüsteem.")
  setContentView(ScrollView(this).apply { addView(layout) })
 }
 private fun startTracking() {
  if(!File(filesDir,"corridor.geojson").exists()) { Toast.makeText(this,"Impordi esmalt GeoJSON",Toast.LENGTH_LONG).show(); return }
  val permissions=mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
  if(Build.VERSION.SDK_INT>=33) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
  if(permissions.any { checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED }) { requestPermissions(permissions.toTypedArray(),7); return }
  try { startForegroundService(Intent(this,TrackingService::class.java)); status.text="Jälgimine käivitatud" }
  catch(e:Exception) { status.text="Käivitamise viga: ${e.message}" }
 }
 override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray) {
  super.onRequestPermissionsResult(requestCode,permissions,grantResults)
  if(requestCode==7 && grantResults.isNotEmpty() && grantResults.all { it==PackageManager.PERMISSION_GRANTED }) startTracking()
 }
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
  super.onActivityResult(requestCode,resultCode,data)
  if(requestCode==picker && resultCode==RESULT_OK) try {
   val uri=data?.data ?: error("Faili ei valitud")
   val txt=contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("Ei saanud faili lugeda")
   Corridor.parse(txt)
   File(filesDir,"corridor.geojson").writeText(txt)
   status.text="GeoJSON imporditud. Võid käivitada jälgimise."
  } catch(e:Exception) { status.text="Impordi viga: ${e.message}" }
 }
 override fun onStart() { super.onStart(); val f=android.content.IntentFilter(TrackingService.ACTION_UPDATE)
  if(Build.VERSION.SDK_INT>=33) registerReceiver(receiver,f,RECEIVER_NOT_EXPORTED) else @Suppress("DEPRECATION") registerReceiver(receiver,f)
 }
 override fun onStop() { unregisterReceiver(receiver); super.onStop() }
}
