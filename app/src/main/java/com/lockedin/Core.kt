package com.lockedin
import android.app.*
import android.content.*
import android.os.*
import android.media.*

object Scheduler{
 fun all(c:Context){Store.tasks(c).forEach{val dt=it.date;if(dt==""||(dt.startsWith("~")&&dt.substring(1)<=Store.day()))at(c,it.id,it.h,it.m) else atDate(c,if(dt.startsWith("~"))it.copy(date=dt.substring(1)) else it)};audit(c);evening(c)}
 fun atDate(c:Context,t:Task){val p=t.date.split("-");if(p.size<3)return;val cal=java.util.Calendar.getInstance().apply{set(p[0].toInt(),p[1].toInt()-1,p[2].toInt(),t.h,t.m,0);set(java.util.Calendar.MILLISECOND,0)};if(cal.timeInMillis<=System.currentTimeMillis())return;sched(c,t.id,cal.timeInMillis)}
 fun evening(c:Context)=at(c,"evening",Store.p(c).getInt("evh",20),Store.p(c).getInt("evm",0))
 fun audit(c:Context)=at(c,"audit",Store.p(c).getInt("ah",22),Store.p(c).getInt("am",0))
 private fun pi(c:Context,id:String)=PendingIntent.getBroadcast(c,id.hashCode(),Intent(c,AlarmReceiver::class.java).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun at(c:Context,id:String,h:Int,m:Int){
  val cal=java.util.Calendar.getInstance().apply{set(java.util.Calendar.HOUR_OF_DAY,h);set(java.util.Calendar.MINUTE,m);set(java.util.Calendar.SECOND,0);set(java.util.Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(java.util.Calendar.DAY_OF_YEAR,1)}
  sched(c,id,cal.timeInMillis)}
 fun sched(c:Context,id:String,whenMs:Long){
  val am=c.getSystemService(AlarmManager::class.java)
  val show=PendingIntent.getActivity(c,9,Intent(c,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE)
  try{
   if(Build.VERSION.SDK_INT<31||am.canScheduleExactAlarms())am.setAlarmClock(AlarmManager.AlarmClockInfo(whenMs,show),pi(c,id))
   else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,whenMs,pi(c,id))
  }catch(e:SecurityException){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,whenMs,pi(c,id))}}
 fun cancel(c:Context,id:String){c.getSystemService(AlarmManager::class.java).cancel(pi(c,id))}
}
object N{
 fun ch(c:Context){val nm=c.getSystemService(NotificationManager::class.java)
  nm.createNotificationChannel(NotificationChannel("nag2","Strict task alerts",NotificationManager.IMPORTANCE_HIGH).apply{setSound(null,null);enableVibration(true)})
  nm.createNotificationChannel(NotificationChannel("mini","Mini reminders",NotificationManager.IMPORTANCE_DEFAULT))}
 private fun open(c:Context,extra:String?=null)=PendingIntent.getActivity(c,7,Intent(c,MainActivity::class.java).apply{flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP;if(extra!=null)putExtra(extra,true)},PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 private fun act(c:Context,id:String,mode:String)=PendingIntent.getBroadcast(c,(id+mode).hashCode(),Intent(c,StartReceiver::class.java).putExtra("id",id).putExtra("mode",mode),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 private fun skipPi(c:Context,id:String)=PendingIntent.getActivity(c,(id+"skipui").hashCode(),Intent(c,MainActivity::class.java).putExtra("skipId",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun nag(c:Context,t:Task):Notification{ch(c);val txt=L.line(c,"nag"+L.inten(c))
  return Notification.Builder(c,"nag2").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(t.title+" ("+t.time+")").setContentText(txt).setStyle(Notification.BigTextStyle().bigText(txt))
   .setOngoing(true).setCategory(Notification.CATEGORY_ALARM).setFullScreenIntent(open(c),true).setContentIntent(open(c))
   .addAction(0,L.t(c,"start_a"),act(c,t.id,"start")).addAction(0,L.t(c,"skip_a"),skipPi(c,t.id)).build()}
 fun idle(c:Context):Notification{ch(c);return Notification.Builder(c,"mini").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Locked In").build()}
 fun mini(c:Context,t:Task){ch(c);c.getSystemService(NotificationManager::class.java).notify(t.id.hashCode(),Notification.Builder(c,"mini").setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle(t.title).setContentText(L.t(c,"minirem")+", "+t.time).setAutoCancel(true).setContentIntent(open(c)).build())}
 fun evening(c:Context){if(!Store.p(c).getBoolean("eve",true))return;ch(c)
  val u=Store.undone(c);val txt=if(u>0)L.line(c,"eve"+L.inten(c)) else L.line(c,"eveok")
  c.getSystemService(NotificationManager::class.java).notify(4,Notification.Builder(c,"mini").setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Locked In").setContentText(txt).setStyle(Notification.BigTextStyle().bigText(txt)).setAutoCancel(true).setContentIntent(open(c)).build())}
 fun auditNote(c:Context){ch(c);c.getSystemService(NotificationManager::class.java).notify(3,Notification.Builder(c,"nag2").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(L.t(c,"audnote")).setContentText(L.t(c,"audnote2")).setAutoCancel(true).setContentIntent(open(c,"audit")).setFullScreenIntent(open(c,"audit"),true).build())}
}
class AlarmReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){
 val id=i.getStringExtra("id")?:return
 if(id=="audit"){Scheduler.audit(c);N.auditNote(c);return}
 if(id=="evening"){Scheduler.evening(c);N.evening(c);return}
 val t=Store.tasks(c).firstOrNull{it.id==id}?:return
 if(t.date!=""&&!t.date.startsWith("~")&&t.date!=Store.day())return
 if(t.date==""||t.date.startsWith("~"))Scheduler.at(c,t.id,t.h,t.m)
 if(t.strict)c.startForegroundService(Intent(c,NagService::class.java)) else N.mini(c,t)}}
class StartReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){
 val id=i.getStringExtra("id")?:return
 if(i.getStringExtra("mode")=="start")Store.start(c,id) else Store.skip(c,id)
 c.getSystemService(NotificationManager::class.java).cancel(1)}}
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){Scheduler.all(c);if(Store.pending(c)!=null)c.startForegroundService(Intent(c,NagService::class.java))}}
class NagService:Service(){
 private val h=Handler(Looper.getMainLooper())
 private var mp:MediaPlayer?=null
 private fun ring(){if(mp?.isPlaying==true)return
  try{val u=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?:RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
   val ts=Store.p(this).getString("tone",null)
   fun mk(x:android.net.Uri)=MediaPlayer().apply{setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());setDataSource(this@NagService,x);isLooping=true;prepare();start()}
   mp=try{mk(if(ts!=null)android.net.Uri.parse(ts) else u)}catch(e:Exception){mk(u)}}catch(e:Exception){}}
 private fun hush(){try{mp?.release()}catch(e:Exception){};mp=null}
 private val loop=object:Runnable{override fun run(){
  val t=Store.pending(this@NagService)
  if(t==null){hush();stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return}
  @Suppress("DEPRECATION")
  getSystemService(PowerManager::class.java).newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,"li:nag").acquire(10000)
  ring()
  getSystemService(NotificationManager::class.java).notify(1,N.nag(this@NagService,t))
  if(Store.p(this@NagService).getBoolean("vib",true))getSystemService(Vibrator::class.java).vibrate(VibrationEffect.createWaveform(longArrayOf(0,500,250,500),-1))
  h.postDelayed(this,10000)}}
 override fun onBind(i:Intent?):IBinder?=null
 override fun onStartCommand(i:Intent?,f:Int,s:Int):Int{
  val n=Store.pending(this)?.let{N.nag(this,it)}?:N.idle(this)
  if(Build.VERSION.SDK_INT>=34)startForeground(1,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(1,n)
  h.removeCallbacks(loop);h.post(loop);return START_STICKY}
 override fun onDestroy(){h.removeCallbacks(loop);hush()}
}
