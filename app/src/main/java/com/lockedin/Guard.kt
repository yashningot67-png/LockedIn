package com.lockedin
import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

object Perm{
 fun usage(c:Context):Boolean{val a=c.getSystemService(AppOpsManager::class.java);@Suppress("DEPRECATION") val m=a.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),c.packageName);return m==AppOpsManager.MODE_ALLOWED}
 fun overlay(c:Context)=Settings.canDrawOverlays(c)
}
class GuardService:Service(){
 private val h=Handler(Looper.getMainLooper());private var last="";private var lastAt=0L;private var ov:View?=null
 private val loop=object:Runnable{override fun run(){try{check()}catch(e:Exception){};h.postDelayed(this,700)}}
 companion object{fun ensure(c:Context){if(Store.p(c).getBoolean("guard",false)&&Perm.usage(c)&&Perm.overlay(c))try{c.startForegroundService(Intent(c,GuardService::class.java))}catch(e:Exception){}}}
 override fun onBind(i:Intent?):IBinder?=null
 override fun onStartCommand(i:Intent?,f:Int,s:Int):Int{N.ch(this)
  val n=Notification.Builder(this,"guard").setSmallIcon(android.R.drawable.ic_lock_idle_lock).setContentTitle("Locked In").setContentText(L.t(this,"g_on")).setOngoing(true).build()
  if(Build.VERSION.SDK_INT>=34)startForeground(5,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(5,n)
  h.removeCallbacks(loop);h.post(loop);return START_STICKY}
 private fun fg():String?{val um=getSystemService(UsageStatsManager::class.java);val now=System.currentTimeMillis();val ev=um.queryEvents(now-8000,now);val e=UsageEvents.Event();var pkg:String?=null
  while(ev.hasNextEvent()){ev.getNextEvent(e);if(e.eventType==UsageEvents.Event.ACTIVITY_RESUMED)pkg=e.packageName else if(e.eventType==UsageEvents.Event.ACTIVITY_PAUSED&&e.packageName==pkg)pkg=null};return pkg}
 private fun check(){val pkg=fg()?:return;if(pkg==packageName)return
  val r=Store.reason(this,pkg)?:return;val now=System.currentTimeMillis();if(pkg==last&&now-lastAt<2500)return;last=pkg;lastAt=now
  startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  val m=when(r){"lock"->L.f(this,"b_lock","a" to Store.label(this,pkg),"g" to L.theme(this));"focus"->L.t(this,"b_focus");else->L.t(this,"b_sun")}
  try{val wm=getSystemService(WindowManager::class.java);ov?.let{wm.removeView(it)}
   val v=TextView(this).apply{text=m;setTextColor(Color.WHITE);textSize=20f;gravity=Gravity.CENTER;setBackgroundColor(0xEE0A0A0B.toInt());setPadding(60,60,60,60)}
   wm.addView(v,WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,PixelFormat.TRANSLUCENT));ov=v
   h.postDelayed({try{wm.removeView(v)}catch(e:Exception){};if(ov===v)ov=null},1800)}catch(e:Exception){}}
 override fun onDestroy(){h.removeCallbacks(loop);try{ov?.let{getSystemService(WindowManager::class.java).removeView(it)}}catch(e:Exception){}}
}
