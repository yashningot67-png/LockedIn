package com.lockedin
import android.Manifest
import android.app.*
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.InputType
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:Activity(){
 private lateinit var root:LinearLayout
 private lateinit var ring:RingView;private lateinit var flame:FlameView;private lateinit var bars:BarsView;private lateinit var heat:HeatView
 private val H=Handler(Looper.getMainLooper());private var first=true
 private val tick=object:Runnable{override fun run(){render();H.postDelayed(this,30000)}}
 private val M=ViewGroup.LayoutParams.MATCH_PARENT;private val W=ViewGroup.LayoutParams.WRAP_CONTENT
 private val GREEN=0xFF39D98A.toInt()
 override fun onCreate(b:Bundle?){super.onCreate(b);N.ch(this)
  window.statusBarColor=BG;window.navigationBarColor=BG
  ring=RingView(this);flame=FlameView(this);bars=BarsView(this);heat=HeatView(this)
  val sv=ScrollView(this).apply{setBackgroundColor(BG)}
  root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(d(18f),d(56f),d(18f),d(40f))};sv.addView(root);setContentView(sv)
  if(Build.VERSION.SDK_INT>=33)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
  Scheduler.all(this)}
 override fun onNewIntent(i:Intent){super.onNewIntent(i);setIntent(i)}
 override fun onResume(){super.onResume();H.removeCallbacks(tick);H.post(tick);if(intent.getBooleanExtra("audit",false)){intent.removeExtra("audit");audit()}}
 override fun onPause(){super.onPause();H.removeCallbacks(tick)}
 private fun d(v:Float)=dp(v).toInt()
 private fun bgd(c:Int,r:Float,st:Int=0)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r);if(st!=0)setStroke(d(1f),st)}
 private fun tx(s:String,sz:Float,col:Int=TX,bold:Boolean=false)=TextView(this).apply{text=s;textSize=sz;setTextColor(col);if(bold)typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)}
 private fun lab(s:String)=tx(s,12f,MUT,true).apply{letterSpacing=.18f;setPadding(0,d(16f),0,d(8f))}
 private fun card(stroke:Int=0xFF242428.toInt())=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=bgd(CARD,20f,stroke);setPadding(d(18f),d(16f),d(18f),d(16f));layoutParams=LinearLayout.LayoutParams(M,W).apply{bottomMargin=d(12f)}}
 private fun touch(v:View){v.setOnTouchListener{x,e->when(e.action){MotionEvent.ACTION_DOWN->x.animate().scaleX(.96f).scaleY(.96f).setDuration(80).start();MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->x.animate().scaleX(1f).scaleY(1f).setDuration(120).start()};false}}
 private fun btn(s:String,primary:Boolean=true,f:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=15f;setTextColor(TX);typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD);stateListAnimator=null
  background=if(primary)GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(RED,0xFFFF5A1F.toInt())).apply{cornerRadius=dp(14f)} else bgd(0xFF1D1D20.toInt(),14f,0xFF2C2C30.toInt())
  layoutParams=LinearLayout.LayoutParams(M,d(52f)).apply{bottomMargin=d(8f)};setOnClickListener{f()};touch(this)}
 private fun small(s:String,f:()->Unit)=tx(s,12f,Color.WHITE,true).apply{setPadding(d(14f),d(9f),d(14f),d(9f));background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(RED,0xFFFF5A1F.toInt())).apply{cornerRadius=dp(12f)};setOnClickListener{f()};touch(this)}
 private fun detach(v:View){(v.parent as? ViewGroup)?.removeView(v)}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
 private fun system(a:String,pkg:Boolean=true){try{startActivity(Intent(a).apply{if(pkg)data=Uri.parse("package:$packageName")})}catch(e:Exception){toast("Settings nahi khuli, manually on kar")}}
 private fun dlg()=AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Dialog_Alert)

 private fun render(celebrate:Boolean=false){
  root.removeAllViews();val items=ArrayList<View>()
  fun put(v:View){root.addView(v);items.add(v)}
  put(tx("LOCKED IN",36f,TX,true))
  put(tx(SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(Date()),13f,MUT).apply{setPadding(0,0,0,d(12f))})
  put(card(RED).apply{addView(tx("AAJ KA DOSE",11f,RED,true).apply{letterSpacing=.2f});addView(tx(Store.quote(this@MainActivity),17f).apply{setPadding(0,d(6f),0,0)})})
  val (dn,tt)=Store.liveProgress(this)
  ring.set(if(tt==0)0f else dn*100f/tt);flame.set(Store.streak(this),celebrate)
  detach(ring);detach(flame)
  put(card().apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
   addView(ring,LinearLayout.LayoutParams(d(150f),d(150f)))
   addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(d(10f),0,0,0)
    addView(flame,LinearLayout.LayoutParams(d(86f),d(108f)));addView(tx("DAY STREAK",12f,EMBER,true).apply{letterSpacing=.15f;setPadding(0,d(6f),0,0)});addView(tx("BEST "+Store.best(this@MainActivity),12f,MUT,true))},LinearLayout.LayoutParams(0,W,1f))})
  val lk=Store.locks(this)
  lk.keys().forEach{p->val u=lk.getLong(p);if(u>System.currentTimeMillis())put(card(RED).apply{addView(tx("LOCKED: "+Store.label(this@MainActivity,p),16f,RED,true));addView(tx(SimpleDateFormat("d MMM, h:mm a",Locale.getDefault()).format(Date(u))+" tak mat khol",12f,MUT))})}
  put(lab("AAJ KA MISSION  (delete: long press)"))
  val log=Store.log(this)
  Store.tasks(this).forEach{t->val e=log.optJSONObject(t.id)
   put(card().apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
    addView(tx(t.time,13f,Color.WHITE,true).apply{setPadding(d(10f),d(6f),d(10f),d(6f));background=bgd(0x33FF2D2D,10f)})
    addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(d(14f),0,d(8f),0);addView(tx(t.title,17f,TX,true));addView(tx(if(t.strict)t.dur.toString()+" min, strict" else "mini reminder",12f,MUT))},LinearLayout.LayoutParams(0,W,1f))
    val st:View=when{!t.strict->tx("MINI",12f,MUT,true)
     e!=null&&e.has("x")->tx("SKIP",12f,MUT,true)
     e!=null&&e.has("e")->tx("DONE",13f,GREEN,true)
     e!=null&&e.has("s")->small("COMPLETE"){Store.finish(this@MainActivity,t.id);render()}
     Store.nowMin()>=t.mins->small("START"){Store.start(this@MainActivity,t.id);getSystemService(NotificationManager::class.java).cancel(1);render()}
     else->tx("UPCOMING",12f,MUT,true)}
    addView(st)
    setOnLongClickListener{Store.saveTasks(this@MainActivity,Store.tasks(this@MainActivity).filter{x->x.id!=t.id});Scheduler.cancel(this@MainActivity,t.id);render();true}})}
  put(btn("+ TASK ADD KAR"){addTask()});put(btn("DAY AUDIT",false){audit()})
  val fm=SimpleDateFormat("EE",Locale.getDefault())
  bars.set(IntArray(7){Store.pctAt(this,6-it)},List(7){i->val cl=Calendar.getInstance();cl.add(Calendar.DAY_OF_YEAR,-(6-i));fm.format(cl.time).take(2)})
  heat.set(IntArray(371){Store.pctAt(this,370-it)})
  detach(bars);detach(heat)
  put(lab("WEEKLY PERFORMANCE"));put(card().apply{addView(bars)})
  put(lab("YEARLY CONSISTENCY"));put(card().apply{addView(heat);addView(tx("Jitna laal-orange, utna zyada kaam. Khali = din chhoota.",11f,MUT).apply{setPadding(0,d(8f),0,0)})})
  put(lab("PUNISHMENT APPS (max 5, tap = hata)"))
  Store.apps(this).forEach{a->put(btn("X  "+a.first,false){Store.saveApps(this,Store.apps(this).filter{x->x.second!=a.second});render()})}
  put(btn("+ APP ADD KAR",false){pickApp()})
  put(lab("PROFILE"))
  val n=EditText(this).apply{hint="Naam";setText(Store.name(this@MainActivity));setTextColor(TX);setHintTextColor(MUT)}
  val g=EditText(this).apply{hint="Goal";setText(Store.goal(this@MainActivity));setTextColor(TX);setHintTextColor(MUT)}
  put(card().apply{addView(n);addView(g)})
  put(btn("SAVE"){Store.p(this).edit().putString("name",n.text.toString().ifBlank{"Yash"}).putString("goal",g.text.toString().ifBlank{"apna goal"}).apply();toast("Save ho gaya");render()})
  put(btn("Audit time: "+Store.p(this).getInt("ah",22)+":"+"%02d".format(Store.p(this).getInt("am",0)),false){TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,h,m->Store.p(this).edit().putInt("ah",h).putInt("am",m).apply();Scheduler.audit(this);render()},Store.p(this).getInt("ah",22),0,false).show()})
  put(lab("EK BAAR SETUP (sab on kar)"))
  if(Build.VERSION.SDK_INT>=31)put(btn("1. Exact alarms allow",false){system(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)})
  if(Build.VERSION.SDK_INT>=34)put(btn("2. Full-screen alerts allow",false){system(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)})
  put(btn("3. Battery unrestricted",false){system(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)})
  put(btn("4. App blocker on (Accessibility > Locked In)",false){system(Settings.ACTION_ACCESSIBILITY_SETTINGS,false)})
  if(first){first=false;items.forEachIndexed{i,v->v.alpha=0f;v.translationY=d(30f).toFloat();v.animate().alpha(1f).translationY(0f).setStartDelay(i*60L).setDuration(450).start()}}}

 private fun addTask(){
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  val t=EditText(this).apply{hint="Task (Workout, Skill block, 100g peanuts)"}
  val dd=EditText(this).apply{hint="Minutes";inputType=InputType.TYPE_CLASS_NUMBER;setText("60")}
  val s=CheckBox(this).apply{text="Strict (alarm jab tak start na karu)";isChecked=true}
  l.addView(t);l.addView(dd);l.addView(s)
  dlg().setTitle("Naya task").setView(l).setPositiveButton("Time chun"){_,_->
   TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,h,m->val k=Task("t"+System.currentTimeMillis(),t.text.toString().ifBlank{"Task"},h,m,dd.text.toString().toIntOrNull()?:30,s.isChecked)
    Store.saveTasks(this,Store.tasks(this)+k);Scheduler.at(this,k.id,k.h,k.m);render()},8,0,false).show()}.show()}
 private fun pickApp(){
  if(Store.apps(this).size>=5){toast("Max 5 apps");return}
  val pm=packageManager;val bad=listOf("com.android.settings","com.android.phone","com.android.dialer","com.google.android.dialer","com.android.emergency")
  val li=pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0).map{it.loadLabel(pm).toString() to it.activityInfo.packageName}.filter{it.second!=packageName&&!bad.contains(it.second)}.distinctBy{it.second}.sortedBy{it.first.lowercase()}
  dlg().setTitle("App chun").setItems(li.map{it.first}.toTypedArray()){_,i->Store.saveApps(this,Store.apps(this)+li[i]);render()}.show()}
 private fun audit(){
  val st=Store.tasks(this).filter{it.strict}
  if(st.isEmpty()){toast("Strict task nahi hai");return}
  if(Store.p(this).getBoolean("aud_"+Store.day(),false)){toast("Aaj ka audit ho chuka");return}
  val log=Store.log(this);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  val ins=ArrayList<Pair<EditText,Spinner>>()
  st.forEach{t->val e=log.optJSONObject(t.id)
   val tr=if(e!=null&&e.has("s")&&e.has("e"))((e.getLong("e")-e.getLong("s"))/60000).toInt() else 0
   l.addView(tx(t.title+" (plan "+t.dur+" min)",16f,TX,true).apply{setPadding(0,d(10f),0,0)})
   val r=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;hint="actual minutes";setText(tr.toString())}
   val q=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Poore focus se","Aadha dhyaan","Bas aise hi","Nahi kar paya"))}
   l.addView(r);l.addView(q);ins.add(r to q)}
  dlg().setTitle("Strict audit: sach bol").setView(ScrollView(this).apply{addView(l)}).setPositiveButton("Submit"){_,_->
   val fails=st.indices.filter{i->val e=log.optJSONObject(st[i].id);e==null||e.has("x")||ins[i].second.selectedItemPosition>=2||(ins[i].first.text.toString().toIntOrNull()?:0)<st[i].dur/2}
   var msg=""
   if(fails.isEmpty())msg="Aaj tu sach me locked in tha, "+Store.name(this)+". Streak aage badh gayi."
   else{msg=Store.insult(this)+"\n\nFail: "+fails.joinToString{st[it].title}
    val free=Store.apps(this).filter{Store.locks(this).optLong(it.second,0)<System.currentTimeMillis()}
    if(free.isNotEmpty()){val a=free.random();Store.lock(this,a.second);msg+="\n\nPunishment: "+a.first+" 20 ghante ke liye locked."}
    else msg+="\n\nPunishment apps add kar, warna dant adhoori rahegi."}
   Store.p(this).edit().putBoolean("aud_"+Store.day(),true).apply()
   Store.setHist(this,(st.size-fails.size)*100/st.size,fails.isEmpty())
   dlg().setTitle("Audit result").setMessage(msg).setPositiveButton("Ok"){_,_->render(fails.isEmpty())}.show()}.show()}
}
