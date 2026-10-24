package com.lockedin
import android.app.*
import android.content.*
import android.os.*

object Scheduler{
 fun all(c:Context){Store.tasks(c).forEach{at(c,it.id,it.h,it.m)};audit(c)}
 fun audit(c:Context)=at(c,"audit",Store.p(c).getInt("ah",22),Store.p(c).getInt("am",0))
 private fun pi(c:Context,id:String)=PendingIntent.getBroadcast(c,id.hashCode(),Intent(c,AlarmReceiver::class.java).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun at(c:Context,id:String,h:Int,m:Int){
  val cal=java.util.Calendar.getInstance().apply{set(java.util.Calendar.HOUR_OF_DAY,h);set(java.util.Calendar.MINUTE,m);set(java.util.Calendar.SECOND,0);set(java.util.Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(java.util.Calendar.DAY_OF_YEAR,1)}
  val am=c.getSystemService(AlarmManager::class.java)
  if(Build.VERSION.SDK_INT<31||am.canScheduleExactAlarms())am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.timeInMillis,pi(c,id))
  else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.timeInMillis,pi(c,id))}
 fun cancel(c:Context,id:String){c.getSystemService(AlarmManager::class.java).cancel(pi(c,id))}
}
object N{
 fun ch(c:Context){val nm=c.getSystemService(NotificationManager::class.java)
  nm.createNotificationChannel(NotificationChannel("nag","Strict task alerts",NotificationManager.IMPORTANCE_HIGH))
  nm.createNotificationChannel(NotificationChannel("mini","Mini reminders",NotificationManager.IMPORTANCE_DEFAULT))}
 private fun open(c:Context,extra:String?=null)=PendingIntent.getActivity(c,7,Intent(c,MainActivity::class.java).apply{flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP;if(extra!=null)putExtra(extra,true)},PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 private fun act(c:Context,id:String,mode:String)=PendingIntent.getBroadcast(c,(id+mode).hashCode(),Intent(c,StartReceiver::class.java).putExtra("id",id).putExtra("mode",mode),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun nag(c:Context,t:Task):Notification{ch(c);val txt=Store.nag(c)
  return Notification.Builder(c,"nag").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(t.title+" ("+t.time+")").setContentText(txt).setStyle(Notification.BigTextStyle().bigText(txt))
   .setOngoing(true).setCategory(Notification.CATEGORY_ALARM).setFullScreenIntent(open(c),true).setContentIntent(open(c))
   .addAction(0,"Start",act(c,t.id,"start")).addAction(0,"Aaj nahi",act(c,t.id,"skip")).build()}
 fun idle(c:Context):Notification{ch(c);return Notification.Builder(c,"mini").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Locked In").build()}
 fun mini(c:Context,t:Task){ch(c);c.getSystemService(NotificationManager::class.java).notify(t.id.hashCode(),Notification.Builder(c,"mini").setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle(t.title).setContentText("Mini reminder, "+t.time).setAutoCancel(true).setContentIntent(open(c)).build())}
 fun auditNote(c:Context){ch(c);c.getSystemService(NotificationManager::class.java).notify(3,Notification.Builder(c,"nag").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Din ka audit time").setContentText("Sach bata, aaj kya kiya? Tap kar.").setAutoCancel(true).setContentIntent(open(c,"audit")).setFullScreenIntent(open(c,"audit"),true).build())}
}
class AlarmReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){
 val id=i.getStringExtra("id")?:return
 if(id=="audit"){Scheduler.audit(c);N.auditNote(c);return}
 val t=Store.tasks(c).firstOrNull{it.id==id}?:return
 Scheduler.at(c,t.id,t.h,t.m)
 if(t.strict)c.startForegroundService(Intent(c,NagService::class.java)) else N.mini(c,t)}}
class StartReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){
 val id=i.getStringExtra("id")?:return
 if(i.getStringExtra("mode")=="start")Store.start(c,id) else Store.skip(c,id)
 c.getSystemService(NotificationManager::class.java).cancel(1)}}
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){Scheduler.all(c);if(Store.pending(c)!=null)c.startForegroundService(Intent(c,NagService::class.java))}}
class NagService:Service(){
 private val h=Handler(Looper.getMainLooper())
 private val loop=object:Runnable{override fun run(){
  val t=Store.pending(this@NagService)
  if(t==null){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return}
  getSystemService(NotificationManager::class.java).notify(1,N.nag(this@NagService,t))
  getSystemService(Vibrator::class.java).vibrate(VibrationEffect.createWaveform(longArrayOf(0,400,200,400),-1))
  h.postDelayed(this,20000)}}
 override fun onBind(i:Intent?):IBinder?=null
 override fun onStartCommand(i:Intent?,f:Int,s:Int):Int{
  val n=Store.pending(this)?.let{N.nag(this,it)}?:N.idle(this)
  if(Build.VERSION.SDK_INT>=34)startForeground(1,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(1,n)
  h.removeCallbacks(loop);h.post(loop);return START_STICKY}
 override fun onDestroy(){h.removeCallbacks(loop)}
}
