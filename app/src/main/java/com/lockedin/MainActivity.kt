package com.lockedin
import android.Manifest
import android.animation.*
import android.app.*
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:Activity(){
 private lateinit var root:LinearLayout;private lateinit var nav:LinearLayout;private lateinit var sv:ScrollView;private lateinit var shell:LinearLayout
 private lateinit var ring:RingView;private lateinit var flame:FlameView;private lateinit var bars:BarsView;private lateinit var heat:HeatView
 private val H=Handler(Looper.getMainLooper());private var first=true;private var tab="today";private var page="";private var handled=false
 private val items=ArrayList<View>();private val anims=ArrayList<Animator>()
 private val tick=object:Runnable{override fun run(){render();H.postDelayed(this,30000)}}
 private val M=ViewGroup.LayoutParams.MATCH_PARENT;private val W=ViewGroup.LayoutParams.WRAP_CONTENT
 private val GREEN=0xFF39D98A.toInt()
 private var nEt:EditText?=null;private var gEt:EditText?=null
 private fun t(k:String)=L.t(this,k)
 private fun crashScreen(s:String){val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(d(16f),d(48f),d(16f),d(24f));setBackgroundColor(BG)}
  l.addView(tx("APP CRASH - screenshot bhej",16f,RED,true));l.addView(TextView(this).apply{text=s;textSize=10f;setTextColor(TX);typeface=Typeface.MONOSPACE})
  l.addView(Button(this).apply{text="Clear";setOnClickListener{Store.p(this@MainActivity).edit().remove("crash").commit();recreate()}});setContentView(ScrollView(this).apply{addView(l)})}
 override fun onCreate(b:Bundle?){super.onCreate(b)
  val old=Store.p(this).getString("crash",null);if(old!=null){crashScreen(old);return}
  Thread.setDefaultUncaughtExceptionHandler{_,e->try{Store.p(applicationContext).edit().putString("crash",Log.getStackTraceString(e).take(2200)).commit()}catch(x:Exception){};Process.killProcess(Process.myPid())}
  N.ch(this);ring=RingView(this);flame=FlameView(this);bars=BarsView(this);heat=HeatView(this)
  sv=ScrollView(this);root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(d(18f),d(52f),d(18f),d(24f))};sv.addView(root)
  nav=LinearLayout(this).apply{setPadding(0,d(6f),0,d(10f))}
  shell=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(sv,LinearLayout.LayoutParams(M,0,1f));addView(nav,LinearLayout.LayoutParams(M,W))};setContentView(shell)
  if(Build.VERSION.SDK_INT>=33)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
  Scheduler.all(this)}
 override fun onNewIntent(i:Intent){super.onNewIntent(i);setIntent(i)}
 override fun onResume(){super.onResume();if(!::root.isInitialized)return;H.removeCallbacks(tick);H.post(tick)
  var ex=false
  if(intent.getBooleanExtra("audit",false)){intent.removeExtra("audit");ex=true;audit()}
  val sid=intent.getStringExtra("skipId");if(sid!=null){intent.removeExtra("skipId");ex=true;skipDialog(sid)}
  if(!ex)H.postDelayed({dailyPopup()},700)}
 override fun onPause(){super.onPause();H.removeCallbacks(tick)}
 private fun d(v:Float)=dp(v).toInt()
 private fun bgd(c:Int,r:Float,st:Int=0)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r);if(st!=0)setStroke(d(1f),st)}
 private fun grad(r:Float)=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(RED,EMBER)).apply{cornerRadius=dp(r)}
 private fun tx(s:String,sz:Float,col:Int=TX,bold:Boolean=false)=TextView(this).apply{text=s;textSize=sz;setTextColor(col);if(bold)typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)}
 private fun lab(s:String)=tx(s,12f,MUT,true).apply{letterSpacing=.18f;setPadding(0,d(16f),0,d(8f))}
 private fun card(stroke:Int=0xFF242428.toInt())=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=bgd(CARD,20f,stroke);setPadding(d(18f),d(16f),d(18f),d(16f));layoutParams=LinearLayout.LayoutParams(M,W).apply{bottomMargin=d(12f)}}
 private fun touch(v:View){v.setOnTouchListener{x,e->when(e.action){MotionEvent.ACTION_DOWN->x.animate().scaleX(.96f).scaleY(.96f).setDuration(80).start();MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->x.animate().scaleX(1f).scaleY(1f).setDuration(120).start()};false}}
 private fun btn(s:String,primary:Boolean=true,f:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=15f;setTextColor(TX);typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD);stateListAnimator=null
  background=if(primary)grad(14f) else bgd(0xFF1D1D20.toInt(),14f,0xFF2C2C30.toInt());layoutParams=LinearLayout.LayoutParams(M,d(52f)).apply{bottomMargin=d(8f)};setOnClickListener{f()};touch(this)}
 private fun small(s:String,f:()->Unit)=tx(s,12f,Color.WHITE,true).apply{setPadding(d(14f),d(9f),d(14f),d(9f));background=grad(12f);setOnClickListener{f()};touch(this)}
 private fun detach(v:View){(v.parent as? ViewGroup)?.removeView(v)}
 private fun put(v:View){root.addView(v);items.add(v)}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
 private fun system(a:String,pkg:Boolean=true){try{startActivity(Intent(a).apply{if(pkg)data=Uri.parse("package:$packageName")})}catch(e:Exception){toast("Open Settings manually")}}
 private fun dlg()=AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Dialog_Alert)
 private fun resumeNag(){if(Store.pending(this)!=null)startForegroundService(Intent(this,NagService::class.java))}
 private fun fmt(h:Int,m:Int)="%02d:%02d".format(h,m)

 private fun render(celebrate:Boolean=false){
  applyTheme(Store.rankIdx(this));window.statusBarColor=BG;window.navigationBarColor=BG;sv.setBackgroundColor(BG);shell.setBackgroundColor(BG)
  anims.forEach{it.cancel()};anims.clear();items.clear();root.removeAllViews()
  val onb=!Store.p(this).getBoolean("onb",false)
  if(onb||page=="profile"){nav.visibility=View.GONE;profilePage(onb);return}
  nav.visibility=View.VISIBLE
  val hdr=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
  hdr.addView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(tx("LOCKED IN",32f,TX,true));addView(tx(SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(Date()),13f,MUT))},LinearLayout.LayoutParams(0,W,1f))
  hdr.addView(tx(Store.name(this).take(1).uppercase(),18f,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(RED,EMBER)).apply{shape=GradientDrawable.OVAL};setOnClickListener{page="profile";render()};touch(this)},LinearLayout.LayoutParams(d(44f),d(44f)))
  put(hdr.apply{setPadding(0,0,0,d(12f))})
  when(tab){"stats"->statsTab();"rank"->rankTab();"set"->settingsTab();else->todayTab(celebrate)}
  buildNav()
  if(first){first=false;items.forEachIndexed{i,v->v.alpha=0f;v.translationY=d(30f).toFloat();v.animate().alpha(1f).translationY(0f).setStartDelay(i*60L).setDuration(450).start()}}
  val rk=Store.rankIdx(this);val last=Store.p(this).getInt("rk",-1)
  if(last<0||rk<last)Store.p(this).edit().putInt("rk",rk).apply() else if(rk>last){Store.p(this).edit().putInt("rk",rk).apply();dlg().setTitle(t("rankup")).setMessage(Store.rankName(this)).setPositiveButton(t("ok"),null).show()}}
 private fun buildNav(){nav.removeAllViews();nav.setBackgroundColor(CARD)
  for((k,n) in listOf("today" to "tab_today","stats" to "tab_stats","rank" to "tab_rank","set" to "tab_set")){
   nav.addView(tx(t(n),13f,if(tab==k)RED else MUT,true).apply{gravity=Gravity.CENTER;setPadding(0,d(12f),0,d(12f));setOnClickListener{tab=k;sv.scrollTo(0,0);render()}},LinearLayout.LayoutParams(0,W,1f))}}

 private fun todayTab(celebrate:Boolean){
  put(card(RED).apply{addView(tx(t("dose"),11f,RED,true).apply{letterSpacing=.2f});addView(tx(L.line(this@MainActivity,"q",L.seed()),17f).apply{setPadding(0,d(6f),0,0)})})
  val (dn,tt)=Store.liveProgress(this);ring.label=t("today");ring.set(if(tt==0)0f else dn*100f/tt);flame.set(Store.streak(this),celebrate);flame.tint=Store.rankIdx(this)
  detach(ring);detach(flame)
  val hero=card().apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
   addView(ring,LinearLayout.LayoutParams(d(150f),d(150f)))
   addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(d(10f),0,0,0)
    addView(flame,LinearLayout.LayoutParams(d(86f),d(108f)));addView(tx(t("streak"),12f,EMBER,true).apply{letterSpacing=.15f;setPadding(0,d(6f),0,0)});addView(tx(t("best")+" "+Store.best(this@MainActivity),12f,MUT,true))},LinearLayout.LayoutParams(0,W,1f))}
  if(PREMIUM){val a=ValueAnimator.ofObject(ArgbEvaluator(),RED,EMBER).apply{duration=1400;repeatMode=ValueAnimator.REVERSE;repeatCount=ValueAnimator.INFINITE;addUpdateListener{hero.background=bgd(CARD,20f,it.animatedValue as Int)}};a.start();anims.add(a)}
  put(hero)
  if(Store.focusOn(this))put(card(RED).apply{addView(tx(t("dwon"),15f,RED,true));addView(tx(t("dwtxt")+" "+SimpleDateFormat("h:mm a",Locale.getDefault()).format(Date(Store.p(this@MainActivity).getLong("focus",0))),12f,MUT))})
  if(Store.cheatOn(this))put(card(GREEN).apply{addView(tx(t("cheaton"),15f,GREEN,true))})
  val lk=Store.locks(this);lk.keys().forEach{p->val u=lk.getLong(p);if(u>System.currentTimeMillis())put(card(RED).apply{addView(tx(t("lockedfor")+": "+Store.label(this@MainActivity,p),16f,RED,true));addView(tx(SimpleDateFormat("d MMM, h:mm a",Locale.getDefault()).format(Date(u)),12f,MUT))})}
  put(lab(t("mission")));val log=Store.log(this)
  Store.tasks(this).forEach{tk->val e=log.optJSONObject(tk.id)
   put(card().apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
    addView(tx(tk.time,13f,Color.WHITE,true).apply{setPadding(d(10f),d(6f),d(10f),d(6f));background=bgd(Color.argb(51,Color.red(RED),Color.green(RED),Color.blue(RED)),10f)})
    addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(d(14f),0,d(8f),0);addView(tx(tk.title,17f,TX,true));addView(tx(if(tk.strict)tk.dur.toString()+" "+t("strictmin") else t("minirem"),12f,MUT))},LinearLayout.LayoutParams(0,W,1f))
    val st:View=when{!tk.strict->tx(t("mini"),12f,MUT,true)
     e!=null&&e.has("x")->tx(t("skipped"),12f,MUT,true)
     e!=null&&e.has("e")->tx(t("done"),13f,GREEN,true)
     e!=null&&e.has("s")->small(t("complete")){Store.finish(this@MainActivity,tk.id);render()}
     Store.nowMin()>=tk.mins->small(t("start")){Store.start(this@MainActivity,tk.id);getSystemService(NotificationManager::class.java).cancel(1);render()}
     else->tx(t("upcoming"),12f,MUT,true)}
    addView(st);setOnLongClickListener{Store.saveTasks(this@MainActivity,Store.tasks(this@MainActivity).filter{x->x.id!=tk.id});Scheduler.cancel(this@MainActivity,tk.id);render();true}})}
  put(btn(t("addtask")){addTask()});put(btn(t("audit"),false){audit()})
  if(Store.p(this).getInt("cheatn",0)>0&&!Store.cheatOn(this))put(btn(t("cheatbtn"),false){Store.useCheat(this);render()})}

 private fun statsTab(){
  val fm=SimpleDateFormat("EE",Locale.getDefault())
  bars.set(IntArray(7){Store.pctAt(this,6-it)},List(7){i->val cl=Calendar.getInstance();cl.add(Calendar.DAY_OF_YEAR,-(6-i));fm.format(cl.time).take(2)});heat.set(IntArray(371){Store.pctAt(this,370-it)});detach(bars);detach(heat)
  put(lab(t("weekly")));put(card().apply{addView(bars)});put(lab(t("yearly")));put(card().apply{addView(heat)})
  put(lab(t("pattern")));put(card().apply{Store.insights(this@MainActivity).forEach{addView(tx(it,14f).apply{setPadding(0,d(4f),0,d(4f))})}})}

 private fun rankTab(){
  val i=Store.rankIdx(this);val rp=Store.rankPct(this);val sd=Store.p(this).getInt("shield",0);val cb=Store.p(this).getInt("cb",0)
  put(card(RED).apply{addView(tx(t("rank"),11f,RED,true).apply{letterSpacing=.2f});addView(tx(Store.rankName(this@MainActivity),34f,TX,true));addView(tx("XP "+Store.xp(this@MainActivity)+(if(i<Store.RANKS.lastIndex)"  |  "+t("next")+": "+Store.RANKS[i+1].first+" ("+Store.RANKS[i+1].second+")" else "  |  "+t("maxrank")),13f,EMBER,true))
   addView(LinearLayout(this@MainActivity).apply{setPadding(0,d(12f),0,d(8f))
    addView(View(this@MainActivity).apply{background=bgd(RED,4f)},LinearLayout.LayoutParams(0,d(8f),rp.toFloat()));addView(View(this@MainActivity).apply{background=bgd(0xFF26262A.toInt(),4f)},LinearLayout.LayoutParams(0,d(8f),(100-rp).toFloat()))})
   addView(tx("SHIELD "+sd+"  |  CHEAT HOUR "+Store.p(this@MainActivity).getInt("cheatn",0)+(if(cb>0)"  |  COMEBACK: "+cb else ""),12f,MUT,true))})
  put(lab(t("ladder")))
  Store.RANKS.forEachIndexed{k,r->put(card(if(k==i)RED else 0xFF242428.toInt()).apply{orientation=LinearLayout.HORIZONTAL;addView(tx((if(k<=i)"✓  " else "     ")+r.first,15f,if(k<=i)TX else MUT,true),LinearLayout.LayoutParams(0,W,1f));addView(tx(r.second.toString()+" XP",13f,MUT))})}}

 private fun settingsTab(){
  val pr=Store.p(this);fun oo(b:Boolean)=if(b)t("on") else t("off")
  put(lab(t("disc")))
  put(btn(t("dwset")+": "+oo(pr.getBoolean("dw",true)),false){pr.edit().putBoolean("dw",!pr.getBoolean("dw",true)).apply();render()})
  put(btn(t("sunset")+": "+oo(pr.getBoolean("ss",false))+" ("+pr.getInt("sh",23)+":00-"+pr.getInt("eh",6)+":00)",false){pr.edit().putBoolean("ss",!pr.getBoolean("ss",false)).apply();render()})
  put(btn(t("sunfrom"),false){TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,h,_->pr.edit().putInt("sh",h).apply();render()},pr.getInt("sh",23),0,true).show()})
  put(btn(t("sunto"),false){TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,h,_->pr.edit().putInt("eh",h).apply();render()},pr.getInt("eh",6),0,true).show()})
  put(btn(t("eveset")+": "+oo(pr.getBoolean("eve",true)),false){pr.edit().putBoolean("eve",!pr.getBoolean("eve",true)).apply();render()})
  put(btn(t("evetime")+": "+fmt(pr.getInt("evh",20),pr.getInt("evm",0)),false){TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,h,m->pr.edit().putInt("evh",h).putInt("evm",m).apply();Scheduler.evening(this);render()},pr.getInt("evh",20),pr.getInt("evm",0),true).show()})
  put(btn(t("audtime")+": "+fmt(pr.getInt("ah",22),pr.getInt("am",0)),false){TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,h,m->pr.edit().putInt("ah",h).putInt("am",m).apply();Scheduler.audit(this);render()},pr.getInt("ah",22),pr.getInt("am",0),true).show()})
  put(btn(t("tonesys"),false){try{startActivityForResult(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,true),11)}catch(e:Exception){toast("Picker not available")}})
  put(btn(t("tonefile"),false){try{startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("audio/*"),12)}catch(e:Exception){toast("Picker not available")}})
  put(lab(t("punish")))
  Store.apps(this).forEach{a->put(btn("X  "+a.first,false){Store.saveApps(this,Store.apps(this).filter{x->x.second!=a.second});render()})}
  put(btn(t("addapp"),false){pickApp()})
  put(btn(t("backup"),false){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,Store.backup(this)),"Backup"))})
  put(btn(t("restore"),false){val cm=getSystemService(ClipboardManager::class.java);val tt=cm.primaryClip?.getItemAt(0)?.text?.toString()?:""
   if(Store.restore(this,tt)){Scheduler.all(this);toast(t("restok"));render()}else toast(t("restno"))})
  put(lab(t("setup")))
  if(Build.VERSION.SDK_INT>=31)put(btn(t("s1"),false){system(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)})
  if(Build.VERSION.SDK_INT>=34)put(btn(t("s2"),false){system(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)})
  put(btn(t("s3"),false){system(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)})
  put(btn(t("s4"),false){system(Settings.ACTION_ACCESSIBILITY_SETTINGS,false)})}

 private fun saveProfile(){val pr=Store.p(this).edit();nEt?.let{pr.putString("name",it.text.toString().trim().ifBlank{"Yash"})};gEt?.let{pr.putString("goal",it.text.toString().trim().ifBlank{"my goal"})};pr.apply()}
 private fun profilePage(onb:Boolean){
  val pr=Store.p(this)
  if(!onb)put(btn(t("back"),false){saveProfile();page="";render()})
  put(tx(if(onb)t("welcome") else t("profile"),26f,TX,true).apply{setPadding(0,d(8f),0,d(8f))})
  put(lab(t("lang")))
  for((i,n) in listOf("English","Hinglish","हिन्दी").withIndex())put(btn(n,pr.getInt("lang",1)==i){saveProfile();pr.edit().putInt("lang",i).apply();render()})
  put(lab(t("name")));nEt=EditText(this).apply{setText(Store.name(this@MainActivity));setTextColor(TX);setHintTextColor(MUT);hint=t("name")};put(card().apply{addView(nEt)})
  put(lab(t("goal")));gEt=EditText(this).apply{setText(if(pr.getString("goal",null)==null&&onb)"" else Store.goal(this@MainActivity));setTextColor(TX);setHintTextColor(MUT);hint=t("goal")};put(card().apply{addView(gEt)})
  put(lab(t("inten")))
  for((i,k) in listOf("gentle","strictl","brutal").withIndex())put(btn(t(k),L.inten(this)==i){saveProfile();pr.edit().putInt("int",i).apply();render()})
  put(btn(if(onb)t("begin") else t("save")){saveProfile();pr.edit().putBoolean("onb",true).apply();Scheduler.all(this);page="";toast(t("saved"));render()})}

 private fun dailyPopup(){
  val pr=Store.p(this);if(!pr.getBoolean("onb",false)||pr.getString("pop","")==Store.day())return;pr.edit().putString("pop",Store.day()).apply()
  dlg().setTitle(t("dose")).setMessage(L.line(this,"q",L.seed())+"\n\n"+t("streak")+": "+Store.streak(this)).setPositiveButton(t("ok"),null).show()}
 override fun onActivityResult(rq:Int,rs:Int,d:Intent?){super.onActivityResult(rq,rs,d);if(rs!=RESULT_OK||d==null)return
  val u:Uri?=if(rq==11)picked(d) else d.data
  if(rq==12&&u!=null)try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(e:Exception){}
  if(u!=null)Store.p(this).edit().putString("tone",u.toString()).apply() else Store.p(this).edit().remove("tone").apply();toast(t("toneset"))}
 @Suppress("DEPRECATION") private fun picked(d:Intent):Uri?=if(Build.VERSION.SDK_INT>=33)d.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI,Uri::class.java) else d.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

 private fun addTask(){
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  val tt=EditText(this).apply{hint=t("taskph")};val dd=EditText(this).apply{hint=t("minutes");inputType=InputType.TYPE_CLASS_NUMBER;setText("60")};val s=CheckBox(this).apply{text=t("strictcb");isChecked=true}
  l.addView(tt);l.addView(dd);l.addView(s)
  dlg().setTitle(t("newtask")).setView(l).setPositiveButton(t("picktime")){_,_->
   TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,h,m->val k=Task("t"+System.currentTimeMillis(),tt.text.toString().ifBlank{"Task"},h,m,dd.text.toString().toIntOrNull()?:30,s.isChecked)
    Store.saveTasks(this,Store.tasks(this)+k);Scheduler.at(this,k.id,k.h,k.m);render()},8,0,false).show()}.show()}
 private fun pickApp(){
  if(Store.apps(this).size>=5){toast(t("max5"));return}
  val pm=packageManager;val bad=listOf("com.android.settings","com.android.phone","com.android.dialer","com.google.android.dialer","com.android.emergency")
  val li=pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0).map{it.loadLabel(pm).toString() to it.activityInfo.packageName}.filter{it.second!=packageName&&!bad.contains(it.second)}.distinctBy{it.second}.sortedBy{it.first.lowercase()}
  dlg().setTitle(t("pickapp")).setItems(li.map{it.first}.toTypedArray()){_,i->Store.saveApps(this,Store.apps(this)+li[i]);render()}.show()}

 private fun skipDialog(id:String){
  val tk=Store.tasks(this).firstOrNull{it.id==id}?:return
  stopService(Intent(this,NagService::class.java));getSystemService(NotificationManager::class.java).cancel(1)
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  l.addView(tx(L.f(this,"skipbody","g" to Store.goal(this),"t" to tk.title),14f))
  val r=EditText(this).apply{hint=t("reason");setTextColor(TX);setHintTextColor(MUT)};l.addView(r)
  val dl=dlg().setTitle(t("skiptitle")).setView(l).setNegativeButton(t("nokeep"),null).setPositiveButton(t("skipbtn"),null).create()
  dl.show();val pb=dl.getButton(AlertDialog.BUTTON_POSITIVE);pb.isEnabled=false
  val timer=object:CountDownTimer(30000,1000){override fun onTick(ms:Long){pb.text=t("skipbtn")+" ("+ms/1000+")"};override fun onFinish(){pb.text=t("skipbtn");pb.isEnabled=true}}
  timer.start();dl.setOnDismissListener{timer.cancel();resumeNag()}
  pb.setOnClickListener{if(r.text.trim().length<6){toast(t("needreason"));return@setOnClickListener};Store.skip(this,id);Store.addExcuse(this,tk.title,r.text.toString().trim());dl.dismiss();render()}}

 private fun audit(){
  val st=Store.tasks(this).filter{it.strict}
  if(st.isEmpty()){toast(t("nostrict"));return}
  if(Store.p(this).getBoolean("aud_"+Store.day(),false)){toast(t("auddone"));return}
  val log=Store.log(this);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)};val ins=ArrayList<Pair<EditText,Spinner>>()
  st.forEach{tk->val e=log.optJSONObject(tk.id);val tr=if(e!=null&&e.has("s")&&e.has("e"))((e.getLong("e")-e.getLong("s"))/60000).toInt() else 0
   l.addView(tx(tk.title+" ("+tk.dur+" min)",16f,TX,true).apply{setPadding(0,d(10f),0,0)})
   val r=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;hint=t("actual");setText(tr.toString())}
   val q=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf(t("q1"),t("q2"),t("q3"),t("q4")))}
   l.addView(r);l.addView(q);ins.add(r to q)}
  dlg().setTitle(t("audtitle")).setView(ScrollView(this).apply{addView(l)}).setPositiveButton(t("submit")){_,_->
   val fails=st.indices.filter{i->val e=log.optJSONObject(st[i].id);e==null||e.has("x")||ins[i].second.selectedItemPosition>=2||(ins[i].first.text.toString().toIntOrNull()?:0)<st[i].dur/2}
   for(i in st.indices)Store.addEvent(this,st[i].title,!fails.contains(i))
   val res=Store.applyAudit(this,fails.size,st.size);Store.p(this).edit().putBoolean("aud_"+Store.day(),true).apply()
   var msg:String
   if(res.pass)msg=L.f(this,"passmsg","n" to Store.name(this))+(if(res.reward.isNotEmpty())"\n\n"+res.reward else "")
   else{msg=L.line(this,"ins"+L.inten(this))+"\n\n"+t("failed")+": "+fails.joinToString{st[it].title}
    if(res.shieldUsed)msg+="\n\n"+t("shield")
    val free=Store.apps(this).filter{Store.locks(this).optLong(it.second,0)<System.currentTimeMillis()}.shuffled().take(res.lockCount)
    if(free.isNotEmpty()){free.forEach{Store.lock(this,it.second,res.lockHours)};msg+="\n\n"+L.f(this,"punline","a" to free.joinToString{it.first},"h" to res.lockHours.toString())}else msg+="\n\n"+t("punhint")}
   dlg().setTitle(t("audres")).setMessage(msg).setPositiveButton(t("ok")){_,_->render(res.pass||res.shieldUsed)}.show()}.show()}
}
