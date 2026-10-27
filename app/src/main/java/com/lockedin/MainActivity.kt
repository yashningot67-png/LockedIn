package com.lockedin
import android.Manifest
import android.animation.*
import android.app.*
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.media.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:Activity(){
 private lateinit var root:LinearLayout;private lateinit var nav:LinearLayout;private lateinit var sv:ScrollView;private lateinit var shell:LinearLayout
 private lateinit var ring:RingView;private lateinit var flame:FlameView;private lateinit var bars:BarsView;private lateinit var heat:HeatView
 private val H=Handler(Looper.getMainLooper());private var first=true;private var tab="today";private var page="";private var popId="";private var pv:MediaPlayer?=null
 private val items=ArrayList<View>()
 private val tick=object:Runnable{override fun run(){render();H.postDelayed(this,30000)}}
 private val M=ViewGroup.LayoutParams.MATCH_PARENT;private val W=ViewGroup.LayoutParams.WRAP_CONTENT
 private var nEt:EditText?=null;private var gEt:EditText?=null
 private fun t(k:String)=L.t(this,k)
 private fun pr()=Store.p(this)
 private fun d(v:Float)=dp(v).toInt()
 private fun crashScreen(s:String){val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(d(16f),d(48f),d(16f),d(24f));setBackgroundColor(BG)}
  l.addView(tx("APP CRASH - send screenshot",16f,RED,true));l.addView(TextView(this).apply{text=s;textSize=10f;setTextColor(TX);typeface=Typeface.MONOSPACE})
  l.addView(Button(this).apply{text="Clear";setOnClickListener{pr().edit().remove("crash").commit();recreate()}});setContentView(ScrollView(this).apply{addView(l)})}
 override fun onCreate(b:Bundle?){super.onCreate(b)
  val old=pr().getString("crash",null);if(old!=null){crashScreen(old);return}
  Thread.setDefaultUncaughtExceptionHandler{_,e->try{Store.p(applicationContext).edit().putString("crash",Log.getStackTraceString(e).take(2200)).commit()}catch(x:Exception){};Process.killProcess(Process.myPid())}
  N.ch(this);ring=RingView(this);flame=FlameView(this);bars=BarsView(this);heat=HeatView(this)
  sv=ScrollView(this);root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(d(18f),d(48f),d(18f),d(20f))};sv.addView(root)
  nav=LinearLayout(this).apply{setPadding(0,d(4f),0,d(8f))}
  shell=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(sv,LinearLayout.LayoutParams(M,0,1f));addView(nav,LinearLayout.LayoutParams(M,W))};setContentView(shell)
  if(Build.VERSION.SDK_INT>=33)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
  Scheduler.all(this)}
 override fun onNewIntent(i:Intent){super.onNewIntent(i);setIntent(i)}
 override fun onResume(){super.onResume();if(!::root.isInitialized)return;H.removeCallbacks(tick);H.post(tick);if(guardAsk)checkGuard() else GuardService.ensure(this);var ex=false
  if(intent.getBooleanExtra("audit",false)){intent.removeExtra("audit");ex=true;audit()}
  val sid=intent.getStringExtra("skipId");if(sid!=null){intent.removeExtra("skipId");ex=true;skipDialog(sid)}
  if(!ex&&pr().getBoolean("onb",false)&&Store.nowMin()>=pr().getInt("ah",22)*60+pr().getInt("am",0)&&!pr().getBoolean("aud_"+Store.day(),false)&&pr().getString("autoaud","")!=Store.day()&&Store.todays(this).any{it.strict}){pr().edit().putString("autoaud",Store.day()).apply();ex=true;audit()}
  if(!ex)H.postDelayed({dailyPopup()},700)}
 override fun onPause(){super.onPause();H.removeCallbacks(tick)}
 // ---------- ui helpers ----------
 private fun bgd(c:Int,r:Float=16f,st:Int=0)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r);if(st!=0)setStroke(d(1f),st)}
 private fun tx(s:String,sz:Float,col:Int=TX,bold:Boolean=false)=TextView(this).apply{text=s;textSize=sz;setTextColor(col);if(bold)typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)}
 private fun lab(s:String)=tx(s,12f,MUT,true).apply{letterSpacing=.16f;setPadding(0,d(14f),0,d(8f))}
 private fun card(stroke:Int=LINE)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=bgd(CARD,16f,stroke);setPadding(d(16f),d(14f),d(16f),d(14f));layoutParams=LinearLayout.LayoutParams(M,W).apply{bottomMargin=d(12f)}}
 private fun touch(v:View){v.setOnTouchListener{x,e->when(e.action){MotionEvent.ACTION_DOWN->x.animate().scaleX(.97f).scaleY(.97f).setDuration(70).start();MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->x.animate().scaleX(1f).scaleY(1f).setDuration(110).start()};false}}
 private fun btn(s:String,primary:Boolean=true,f:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=14f;setTextColor(TX);typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD);stateListAnimator=null
  background=if(primary)bgd(RED,14f) else bgd(CARD,14f,0xFF2C2C30.toInt());layoutParams=LinearLayout.LayoutParams(M,d(48f)).apply{bottomMargin=d(8f)};setOnClickListener{hap();f()};touch(this)}
 private fun small(s:String,f:()->Unit)=tx(s,12f,Color.WHITE,true).apply{setPadding(d(14f),d(8f),d(14f),d(8f));background=bgd(RED,10f);setOnClickListener{f()};touch(this)}
 private fun sw(on:Boolean,f:(Boolean)->Unit)=Switch(this).apply{isChecked=on;val st=arrayOf(intArrayOf(android.R.attr.state_checked),intArrayOf())
  thumbTintList=ColorStateList(st,intArrayOf(Color.WHITE,0xFF9A9AA2.toInt()));trackTintList=ColorStateList(st,intArrayOf(RED,0xFF2C2C30.toInt()));setOnCheckedChangeListener{_,v->hap();f(v)}}
 private fun hap(){if(pr().getBoolean("hap",true))try{getSystemService(Vibrator::class.java).vibrate(VibrationEffect.createOneShot(22,90))}catch(e:Exception){}}
 private fun detach(v:View){(v.parent as? ViewGroup)?.removeView(v)}
 private fun put(v:View){root.addView(v);items.add(v)}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
 private fun dlg()=AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Dialog_Alert)
 private fun guardReady()=pr().getBoolean("guard",false)&&Perm.usage(this)&&Perm.overlay(this)
 private var guardAsk=false
 private fun guardFlow(){guardAsk=true;checkGuard()}
 private fun checkGuard(){if(!guardAsk)return
  if(!Perm.usage(this)){gdlg(1,"g_u"){try{startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).setData(Uri.parse("package:$packageName")))}catch(e:Exception){system(Settings.ACTION_USAGE_ACCESS_SETTINGS,false)}};return}
  if(!Perm.overlay(this)){gdlg(2,"g_o"){system(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)};return}
  guardAsk=false;GuardService.ensure(this);toast(t("g_on"));render()}
 private fun gdlg(n:Int,k:String,go:()->Unit){dlg().setTitle(L.f(this,"g_step","n" to n.toString())).setMessage(t(k)).setPositiveButton(t("g_open")){_,_->go()}.setNegativeButton(t("back")){_,_->guardAsk=false;pr().edit().putBoolean("guard",false).apply();render()}.setCancelable(false).show()}
 private fun themeName():String{val th=pr().getInt("theme",-1);return if(th<0)t("themeauto") else Store.RANKS[minOf(th,Store.rankIdx(this))].first}
 private fun themePick(){val n=Store.rankIdx(this);val names=arrayOf(t("themeauto"))+Array(n+1){Store.RANKS[it].first};dlg().setTitle(t("theme")).setItems(names){_,i->pr().edit().putInt("theme",i-1).apply();render()}.show()}
 private fun fmt(h:Int,m:Int)="%02d:%02d".format(h,m)
 private fun timeStr(ms:Long)=SimpleDateFormat("d MMM, h:mm a",Locale.getDefault()).format(Date(ms))
 private fun pick(h:Int,m:Int,f:(Int,Int)->Unit){TimePickerDialog(this,android.R.style.Theme_DeviceDefault_Dialog,{_,hh,mm->f(hh,mm)},h,m,true).show()}
 private fun resumeNag(){if(Store.pending(this)!=null)startForegroundService(Intent(this,NagService::class.java))}
 private fun system(a:String,pkg:Boolean=true){try{startActivity(Intent(a).apply{if(pkg)data=Uri.parse("package:$packageName")})}catch(e:Exception){toast("Open Settings manually")}}
 private fun row(title:String,desc:String,status:String,col:Int=MUT,f:(()->Unit)?=null)=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,d(10f),0,d(10f))
  addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(title,15f,TX,true));addView(tx(desc,11f,MUT))},LinearLayout.LayoutParams(0,W,1f))
  addView(tx(status,12f,col,true).apply{setPadding(d(8f),0,0,0)});if(f!=null){setOnClickListener{hap();f()};touch(this)}}
 private fun grp(title:String,body:LinearLayout.()->Unit){put(lab(title));put(card().apply{body()})}
 private fun divider()=View(this).apply{setBackgroundColor(LINE);layoutParams=LinearLayout.LayoutParams(M,d(1f))}

 // ---------- render ----------
 private fun render(celebrate:Boolean=false){
  REDUCE=pr().getBoolean("reduce",false);val th=pr().getInt("theme",-1);applyTheme(if(th>=0)minOf(th,Store.rankIdx(this)) else Store.rankIdx(this));window.statusBarColor=BG;window.navigationBarColor=BG;sv.setBackgroundColor(BG);shell.setBackgroundColor(BG)
  items.clear();root.removeAllViews()
  val onb=!pr().getBoolean("onb",false)
  if(onb||page=="profile"){nav.visibility=View.GONE;profilePage(onb);return}
  nav.visibility=View.VISIBLE
  val hdr=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,0,0,d(10f))}
  hdr.addView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(tx("LOCKED IN",30f,TX,true));addView(tx(t("tagline"),10f,MUT,true).apply{letterSpacing=.2f})},LinearLayout.LayoutParams(0,W,1f))
  hdr.addView(tx(Store.name(this).take(1).uppercase(),17f,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(RED)};setOnClickListener{page="profile";render()};touch(this)},LinearLayout.LayoutParams(d(42f),d(42f)))
  put(hdr)
  when(tab){"stats"->statsTab();"rank"->rankTab();"set"->settingsTab();else->homeTab(celebrate)}
  nav.removeAllViews();nav.setBackgroundColor(CARD)
  for((k,n) in listOf("today" to "tab_today","stats" to "tab_stats","rank" to "tab_rank","set" to "tab_set"))nav.addView(tx(t(n),13f,if(tab==k)RED else MUT,true).apply{gravity=Gravity.CENTER;setPadding(0,d(12f),0,d(12f));setOnClickListener{tab=k;sv.scrollTo(0,0);render()}},LinearLayout.LayoutParams(0,W,1f))
  if(first){first=false;if(!REDUCE)items.forEachIndexed{i,v->v.alpha=0f;v.translationY=d(24f).toFloat();v.animate().alpha(1f).translationY(0f).setStartDelay(i*45L).setDuration(350).start()}}
  val rk=Store.rankIdx(this);val last=pr().getInt("rk",-1);val xp=Store.xp(this)
  if(last<0||rk<last)pr().edit().putInt("rk",rk).putInt("xpseen",xp).apply() else if(rk>last){val e=xp-pr().getInt("xpseen",0);pr().edit().putInt("rk",rk).putInt("xpseen",xp).apply();rankUp(rk,e)}}

 private fun homeTab(celebrate:Boolean){
  put(card().apply{addView(tx(t("dose"),11f,MUT,true).apply{letterSpacing=.2f});addView(tx(L.dose(this@MainActivity),15f).apply{setPadding(0,d(6f),0,0)})})
  val (dn,tt)=Store.liveProgress(this);ring.label=t("today");ring.set(if(tt==0)0f else dn*100f/tt);flame.set(Store.streak(this),celebrate);detach(ring);detach(flame)
  put(card().apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
   addView(ring,LinearLayout.LayoutParams(d(132f),d(132f)))
   addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(d(8f),0,0,0)
    addView(flame,LinearLayout.LayoutParams(d(84f),d(104f)));addView(tx(Store.streak(this@MainActivity).toString()+" "+t("days"),20f,TX,true).apply{setPadding(0,d(6f),0,0)});addView(tx(t("streak"),11f,MUT,true).apply{letterSpacing=.12f;setPadding(0,d(6f),0,0)});addView(tx(t("best")+" "+Store.best(this@MainActivity),12f,MUT,true))},LinearLayout.LayoutParams(0,W,1f))})
  // controls
  val ctl=LinearLayout(this).apply{layoutParams=LinearLayout.LayoutParams(M,W).apply{bottomMargin=d(12f)}}
  fun sq(title:String,status:String,on:Boolean,last:Boolean,f:(Boolean)->Unit){ctl.addView(card().apply{layoutParams=LinearLayout.LayoutParams(0,d(112f),1f).apply{if(!last)marginEnd=d(8f)};setPadding(d(12f),d(12f),d(12f),d(10f))
   addView(tx(title,12f,TX,true));addView(tx(status,10f,if(on)GREEN else MUT).apply{setPadding(0,d(2f),0,0)});addView(View(this@MainActivity),LinearLayout.LayoutParams(M,0,1f));addView(sw(on,f))})}
  val p=pr();val fo=Store.focusOn(this)
  sq(t("ctl_dw"),if(!p.getBoolean("dw",true))t("st_off") else if(Store.apps(this).isEmpty())t("st_noapps") else if(!guardReady())t("st_noblock") else if(fo)t("st_until").replace("{t}",SimpleDateFormat("h:mm a",Locale.getDefault()).format(Date(p.getLong("focus",0)))) else t("st_wait"),p.getBoolean("dw",true),false){p.edit().putBoolean("dw",it).apply();render()}
  sq(t("ctl_sun"),if(p.getBoolean("ss",false))fmt(p.getInt("sh",23),0)+"-"+fmt(p.getInt("eh",6),0) else t("st_off"),p.getBoolean("ss",false),false){p.edit().putBoolean("ss",it).apply();render()}
  sq(t("ctl_eve"),if(p.getBoolean("eve",true))fmt(p.getInt("evh",20),p.getInt("evm",0)) else t("st_off"),p.getBoolean("eve",true),true){p.edit().putBoolean("eve",it).apply();render()}
  put(ctl)
  // locked apps
  val apps=Store.apps(this);val act=fo||Store.sunsetOn(this)||Store.locks(this).keys().asSequence().any{Store.locks(this).optLong(it,0)>System.currentTimeMillis()}
  put(card().apply{setOnClickListener{tab="set";render()}
   addView(tx(t("lockedapps"),11f,MUT,true).apply{letterSpacing=.16f})
   addView(tx(if(apps.isEmpty())t("noapps") else t(if(act)"active_n" else "ready_n").replace("{n}",apps.size.toString()),13f,if(act&&apps.isNotEmpty())GREEN else TX).apply{setPadding(0,d(4f),0,d(8f))})
   addView(LinearLayout(this@MainActivity).apply{apps.forEach{a->addView(appIcon(a.second,26f))}})})
  val lk=Store.locks(this);lk.keys().forEach{k->val u=lk.getLong(k);if(u>System.currentTimeMillis())put(card(RED).apply{addView(tx(t("lockedfor")+": "+Store.label(this@MainActivity,k),15f,RED,true));addView(tx(timeStr(u),12f,MUT))})}
  if(Store.cheatOn(this))put(card(GREEN).apply{addView(tx(t("cheaton"),14f,GREEN,true))})
  // missions
  put(lab(t("mission")));val log=Store.log(this);val td=Store.todays(this).sortedBy{it.mins}
  if(td.isEmpty())put(card().apply{gravity=Gravity.CENTER;setPadding(d(16f),d(24f),d(16f),d(24f));addView(tx(t("empty1"),18f,TX,true).apply{gravity=Gravity.CENTER});addView(tx(t("empty2"),13f,MUT).apply{gravity=Gravity.CENTER;setPadding(0,d(6f),0,0)})})
  td.forEach{tk->val e=log.optJSONObject(tk.id);val done=e?.has("e")==true;val run=e?.has("s")==true&&!done;val skp=e?.has("x")==true
   put(card().apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
    addView(tx(if(done)"✓" else "",15f,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=GradientDrawable().apply{shape=GradientDrawable.OVAL;if(done)setColor(GREEN) else setStroke(d(2f),if(run)RED else 0xFF3A3A40.toInt())}
     if(popId==tk.id){scaleX=.3f;scaleY=.3f;animate().scaleX(1f).scaleY(1f).setInterpolator(OvershootInterpolator()).setDuration(380).start();popId=""}},LinearLayout.LayoutParams(d(28f),d(28f)))
    addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(d(14f),0,d(8f),0);addView(tx(tk.title,16f,TX,true));addView(tx(tk.time+(if(tk.strict)"  •  "+tk.dur+" "+t("strictmin") else "  •  "+t("minirem")),12f,MUT))},LinearLayout.LayoutParams(0,W,1f))
    val st:View=when{!tk.strict->tx(t("mini"),11f,MUT,true);skp->tx(t("skipped"),11f,MUT,true);done->tx(t("done"),12f,GREEN,true)
     run->small(t("complete")){Store.finish(this@MainActivity,tk.id);popId=tk.id;render()}
     Store.nowMin()>=tk.mins->small(t("start")){Store.start(this@MainActivity,tk.id);stopService(Intent(this@MainActivity,NagService::class.java));getSystemService(NotificationManager::class.java).cancel(1);resumeNag();render()}
     else->tx(t("upcoming"),11f,MUT,true)}
    addView(st);setOnLongClickListener{Store.saveTasks(this@MainActivity,Store.tasks(this@MainActivity).filter{x->x.id!=tk.id});Scheduler.cancel(this@MainActivity,tk.id);render();true}})}
  val tm=Store.tomorrows(this).sortedBy{it.mins}
  if(tm.isNotEmpty()){put(lab(t("tomorrow")));put(card().apply{tm.forEach{tk->addView(row(tk.title,tk.time+"  •  "+tk.dur+" min","",MUT){Store.saveTasks(this@MainActivity,Store.tasks(this@MainActivity).filter{x->x.id!=tk.id});Scheduler.cancel(this@MainActivity,tk.id);render()})}})}
  put(btn(t("addtask")){addTask()})}
 private fun appIcon(pkg:String,sz:Float)=ImageView(this).apply{try{setImageDrawable(packageManager.getApplicationIcon(pkg))}catch(e:Exception){};layoutParams=LinearLayout.LayoutParams(d(sz),d(sz)).apply{marginEnd=d(8f)}}

 private fun statsTab(){
  val fm=SimpleDateFormat("EE",Locale.getDefault())
  bars.set(IntArray(7){Store.pctAt(this,6-it)},List(7){i->val cl=Calendar.getInstance();cl.add(Calendar.DAY_OF_YEAR,-(6-i));fm.format(cl.time).take(2)});heat.set(IntArray(371){Store.pctAt(this,370-it)});detach(bars);detach(heat)
  put(lab(t("weekly")));put(card().apply{addView(bars)})
  put(lab(t("yearly")));put(card().apply{addView(heat);val lg=listOf(t("lg1"),t("lg2"),t("lg3"));addView(LinearLayout(this@MainActivity).apply{setPadding(0,d(10f),0,0)
   for((i,c) in listOf(GREEN,AMBER,0xFF3A3A40.toInt()).withIndex()){addView(View(this@MainActivity).apply{background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(c)}},LinearLayout.LayoutParams(d(8f),d(8f)).apply{marginEnd=d(6f);gravity=Gravity.CENTER_VERTICAL});addView(tx(lg.getOrElse(i){""},11f,MUT).apply{setPadding(0,0,d(14f),0)})}})})
  put(lab(t("pattern")));put(card().apply{val pt=Store.pattern(this@MainActivity)
   addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL;addView(TargetIcon(this@MainActivity),LinearLayout.LayoutParams(d(56f),d(56f)))
    addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(d(16f),0,0,0)
     if(pt==null)addView(tx(t("pat_none"),13f,MUT)) else {addView(tx(t("pat_best"),12f,MUT));addView(tx(t("tb"+pt.first),15f,TX,true));addView(tx(t("pat_cons"),12f,MUT).apply{setPadding(0,d(10f),0,0)});addView(tx(t(if(pt.second==0)"wd" else "we"),15f,TX,true))}},LinearLayout.LayoutParams(0,W,1f))})
   Store.insights(this@MainActivity).forEach{addView(tx(it,12f,MUT).apply{setPadding(0,d(8f),0,0)})}})
  put(rankCard())}
 private fun rankCard():View{val i=Store.rankIdx(this);val rp=Store.rankPct(this)
  return card().apply{addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL
   addView(RankEmblem(this@MainActivity,i,2),LinearLayout.LayoutParams(d(54f),d(54f)))
   addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(d(14f),0,0,0);addView(tx(t("rank"),10f,MUT,true).apply{letterSpacing=.16f});addView(tx(Store.rankName(this@MainActivity),22f,TX,true))
    addView(tx("XP "+Store.xp(this@MainActivity)+(if(i<Store.RANKS.lastIndex)" / "+Store.RANKS[i+1].second else ""),12f,MUT))},LinearLayout.LayoutParams(0,W,1f))
   if(i<Store.RANKS.lastIndex)addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.END;addView(tx(t("next"),10f,MUT,true));addView(tx(Store.RANKS[i+1].first,13f,TX,true))})})
   addView(LinearLayout(this@MainActivity).apply{setPadding(0,d(12f),0,d(10f));addView(View(this@MainActivity).apply{background=bgd(RED,3f)},LinearLayout.LayoutParams(0,d(6f),rp.toFloat()));addView(View(this@MainActivity).apply{background=bgd(0xFF26262A.toInt(),3f)},LinearLayout.LayoutParams(0,d(6f),(100-rp).toFloat()))})
   addView(LinearLayout(this@MainActivity).apply{
    addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(t("beststreak"),10f,MUT,true));addView(tx(Store.best(this@MainActivity).toString()+" "+t("days"),15f,TX,true))},LinearLayout.LayoutParams(0,W,1f))
    addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(t("curstreak"),10f,MUT,true));addView(tx(Store.streak(this@MainActivity).toString()+" "+t("days"),15f,TX,true))},LinearLayout.LayoutParams(0,W,1f))})}}

 private fun rankTab(){
  put(rankCard());val sd=pr().getInt("shield",0);val cb=pr().getInt("cb",0)
  put(tx("SHIELD "+sd+"   •   CHEAT HOUR "+pr().getInt("cheatn",0)+(if(cb>0)"   •   COMEBACK: "+cb else ""),11f,MUT,true).apply{setPadding(0,0,0,d(6f))})
  if(pr().getInt("cheatn",0)>0&&!Store.cheatOn(this))put(btn(t("cheatbtn"),false){Store.useCheat(this);render()})
  put(lab(t("ladder")));val cur=Store.rankIdx(this)
  Store.RANKS.forEachIndexed{k,r->put(card(if(k==cur)RED else LINE).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(d(14f),d(12f),d(14f),d(12f))
   addView(RankEmblem(this@MainActivity,k,if(k==cur)2 else if(k<cur)1 else 0),LinearLayout.LayoutParams(d(52f),d(52f)))
   addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(d(14f),0,d(8f),0)
    addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL;addView(tx("%02d.  ".format(k+1),14f,if(k==cur)RED else MUT,true));addView(tx(r.first,16f,if(k<=cur)TX else MUT,true));if(k==cur)addView(tx("  "+t("current"),9f,RED,true))})
    addView(tx(t("rt"+k),11f,MUT))
    if(k==cur&&k<Store.RANKS.lastIndex)addView(LinearLayout(this@MainActivity).apply{setPadding(0,d(8f),0,0);val rp=Store.rankPct(this@MainActivity);addView(View(this@MainActivity).apply{background=bgd(RED,3f)},LinearLayout.LayoutParams(0,d(5f),rp.toFloat()));addView(View(this@MainActivity).apply{background=bgd(0xFF26262A.toInt(),3f)},LinearLayout.LayoutParams(0,d(5f),(100-rp).toFloat()))})},LinearLayout.LayoutParams(0,W,1f))
   addView(tx(r.second.toString()+" XP",12f,MUT));addView(tx(if(k<=cur)"  ✓" else "",15f,GREEN,true))})}}

 // ---------- settings ----------
 private fun permOk(k:String):Boolean=when(k){"n"->getSystemService(NotificationManager::class.java).areNotificationsEnabled()
  "x"->Build.VERSION.SDK_INT<31||getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
  "f"->Build.VERSION.SDK_INT<34||getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
  "b"->getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
  "u"->Perm.usage(this);else->Perm.overlay(this)}
 private fun toneName():String{val ts=pr().getString("tone",null)?:return t("tonedef");return try{RingtoneManager.getRingtone(this,Uri.parse(ts)).getTitle(this)}catch(e:Exception){ts.substringAfterLast('/')}}
 private fun preview(){pv?.release();val ts=pr().getString("tone",null);val u=if(ts!=null)Uri.parse(ts) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
  pv=try{MediaPlayer().apply{setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());setDataSource(this@MainActivity,u);prepare();start()}}catch(e:Exception){toast(t("e_tone"));null}
  H.postDelayed({try{pv?.release()}catch(e:Exception){};pv=null},4000)}
 private fun settingsTab(){
  val p=pr()
  grp(t("set_sched")){addView(row(t("sunwin"),t("sunwin_d"),fmt(p.getInt("sh",23),0)+" - "+fmt(p.getInt("eh",6),0)){pick(p.getInt("sh",23),0){h,_->pick(p.getInt("eh",6),0){h2,_->p.edit().putInt("sh",h).putInt("eh",h2).apply();render()}}});addView(divider())
   addView(row(t("eveset"),t("eve_d"),fmt(p.getInt("evh",20),p.getInt("evm",0))){pick(p.getInt("evh",20),p.getInt("evm",0)){h,m->p.edit().putInt("evh",h).putInt("evm",m).apply();Scheduler.evening(this@MainActivity);render()}});addView(divider())
   addView(row(t("audtime"),t("aud_d"),fmt(p.getInt("ah",22),p.getInt("am",0))){pick(p.getInt("ah",22),p.getInt("am",0)){h,m->p.edit().putInt("ah",h).putInt("am",m).apply();Scheduler.audit(this@MainActivity);render()}})}
  grp(t("set_audio")){addView(row(t("tonecur"),toneName(),t("preview"),RED){preview()});addView(divider())
   addView(row(t("tonesys"),"","›"){try{startActivityForResult(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,true),11)}catch(e:Exception){toast("Picker not available")}});addView(divider())
   addView(row(t("tonefile"),"","›"){try{startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("audio/*"),12)}catch(e:Exception){toast("Picker not available")}});addView(divider())
   addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,d(10f),0,d(4f));addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(t("vib"),15f,TX,true));addView(tx(t("vib_d"),11f,MUT))},LinearLayout.LayoutParams(0,W,1f));addView(sw(p.getBoolean("vib",true)){p.edit().putBoolean("vib",it).apply()})})}
  grp(t("set_apps")){Store.apps(this@MainActivity).forEach{a->val u=Store.locks(this@MainActivity).optLong(a.second,0)
   addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,d(8f),0,d(8f));addView(appIcon(a.second,34f).apply{(layoutParams as LinearLayout.LayoutParams).marginEnd=d(12f)})
    addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(a.first,15f,TX,true));addView(tx(if(u>System.currentTimeMillis())t("st_locked").replace("{t}",timeStr(u)) else if(p.getBoolean("dw",true)||p.getBoolean("ss",false))t("st_auto") else t("st_ready"),11f,if(u>System.currentTimeMillis())RED else MUT))},LinearLayout.LayoutParams(0,W,1f))
    addView(tx("✕",16f,MUT,true).apply{setPadding(d(12f),d(6f),d(4f),d(6f));setOnClickListener{Store.saveApps(this@MainActivity,Store.apps(this@MainActivity).filter{x->x.second!=a.second});render()}})})}
   addView(btn(t("addapp"),false){pickApp()});addView(btn(t("testlock"),false){pr().edit().putLong("testlock",System.currentTimeMillis()+60000).apply();toast(t("teston"))})}
  grp(t("blocker")){addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL;addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(t("blocker"),15f,TX,true));addView(tx(t("blocker_d"),11f,MUT))},LinearLayout.LayoutParams(0,W,1f));addView(sw(p.getBoolean("guard",false)){on->p.edit().putBoolean("guard",on).apply();if(on)guardFlow() else{stopService(Intent(this@MainActivity,GuardService::class.java));render()}})})}
  grp(t("set_perm")){val ks=listOf("n","x","f","b","u","o");var allOk=true
   ks.forEach{k->val ok=permOk(k);if(!ok&&k!="f")allOk=false;addView(row(t("p_"+k),t("p_"+k+"_d"),if(ok)t("granted") else t("needed"),if(ok)GREEN else RED){when(k){"n"->try{startActivity(Intent("android.settings.APP_NOTIFICATION_SETTINGS").putExtra("android.provider.extra.APP_PACKAGE",packageName))}catch(e:Exception){toast("Open Settings manually")};"x"->system(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);"f"->system("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT");"b"->system(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);"u"->try{startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).setData(Uri.parse("package:$packageName")))}catch(e:Exception){system(Settings.ACTION_USAGE_ACCESS_SETTINGS,false)};else->system(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)}});addView(divider())}
   addView(row(t("health"),"",if(allOk)t("h_ok") else t("h_bad"),if(allOk)GREEN else AMBER));addView(divider())
   val lb=p.getLong("lastbk",0);addView(row(t("lastbk"),"",if(lb==0L)t("never") else timeStr(lb)))
   addView(btn(t("backup"),false){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,Store.backup(this@MainActivity)),"Backup"));render()})
   addView(btn(t("restore"),false){val cm=getSystemService(ClipboardManager::class.java);val tt=cm.primaryClip?.getItemAt(0)?.text?.toString()?:""
    if(Store.restore(this@MainActivity,tt)){Scheduler.all(this@MainActivity);toast(t("restok"));render()}else toast(t("restno"))})}
  grp(t("set_pref")){addView(row(t("theme"),"",themeName()){themePick()});addView(divider());addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,d(8f),0,d(8f));addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(t("reduce"),15f,TX,true));addView(tx(t("reduce_d"),11f,MUT))},LinearLayout.LayoutParams(0,W,1f));addView(sw(p.getBoolean("reduce",false)){p.edit().putBoolean("reduce",it).apply();render()})});addView(divider());addView(LinearLayout(this@MainActivity).apply{gravity=Gravity.CENTER_VERTICAL;addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;addView(tx(t("hap"),15f,TX,true));addView(tx(t("hap_d"),11f,MUT))},LinearLayout.LayoutParams(0,W,1f));addView(sw(p.getBoolean("hap",true)){p.edit().putBoolean("hap",it).apply()})})}}

 // ---------- profile ----------
 private fun saveProfile(){val e=pr().edit();nEt?.let{e.putString("name",it.text.toString().trim().ifBlank{"Yash"})};gEt?.let{e.putString("goal",it.text.toString().trim().ifBlank{"my goal"})};e.apply()}
 private fun profilePage(onb:Boolean){
  if(!onb)put(btn(t("back"),false){saveProfile();page="";render()})
  put(tx(if(onb)t("welcome") else t("profile"),26f,TX,true).apply{setPadding(0,d(6f),0,d(6f))})
  put(lab(t("lang")));for((i,n) in listOf("English","Hinglish","हिन्दी").withIndex())put(btn(n,pr().getInt("lang",1)==i){saveProfile();pr().edit().putInt("lang",i).apply();render()})
  put(lab(t("name")));nEt=EditText(this).apply{setText(Store.name(this@MainActivity));setTextColor(TX);setHintTextColor(MUT);hint=t("name")};put(card().apply{addView(nEt)})
  put(lab(t("goal")));gEt=EditText(this).apply{setText(if(pr().getString("goal",null)==null&&onb)"" else Store.goal(this@MainActivity));setTextColor(TX);setHintTextColor(MUT);hint=t("goalhint");inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;minLines=5;gravity=Gravity.TOP;textSize=14f}
  put(card().apply{addView(gEt)})
  put(lab(t("inten")));for((i,k) in listOf("gentle","strictl","brutal").withIndex())put(btn(t(k),L.inten(this)==i){saveProfile();pr().edit().putInt("int",i).apply();render()})
  put(btn(if(onb)t("begin") else t("save")){saveProfile();pr().edit().putBoolean("onb",true).apply();Scheduler.all(this);toast(t("saved"));page="";if(onb)tab="set";render()})}

 private fun dailyPopup(){
  if(!pr().getBoolean("onb",false)||pr().getString("pop","")==Store.day())return;pr().edit().putString("pop",Store.day()).apply()
  dlg().setTitle(t("dose")).setMessage(L.dose(this)).setPositiveButton(t("ok"),null).show()}
 private fun rankUp(idx:Int,earned:Int){hap()
  val dl=Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setBackgroundColor(BG);setPadding(d(32f),0,d(32f),0)}
  val em=RankEmblem(this,idx,2).apply{scaleX=.2f;scaleY=.2f;alpha=0f;animate().scaleX(1f).scaleY(1f).alpha(1f).setInterpolator(OvershootInterpolator()).setDuration(600).start()}
  l.addView(tx(t("rankup"),14f,MUT,true).apply{letterSpacing=.3f;gravity=Gravity.CENTER});l.addView(em,LinearLayout.LayoutParams(d(120f),d(120f)).apply{topMargin=d(20f);bottomMargin=d(20f)})
  l.addView(tx(Store.RANKS[idx].first,34f,TX,true).apply{gravity=Gravity.CENTER});if(earned>0)l.addView(tx("+"+earned+" "+t("xpearn"),14f,GREEN,true).apply{gravity=Gravity.CENTER;setPadding(0,d(8f),0,d(24f))})
  l.addView(btn(t("cont")){dl.dismiss()});dl.setContentView(l);dl.show()}
 override fun onActivityResult(rq:Int,rs:Int,d:Intent?){super.onActivityResult(rq,rs,d);if(rs!=RESULT_OK||d==null)return
  val u:Uri?=if(rq==11)picked(d) else d.data
  if(rq==12&&u!=null)try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(e:Exception){}
  if(u!=null)pr().edit().putString("tone",u.toString()).apply() else pr().edit().remove("tone").apply();toast(t("toneset"));render()}
 @Suppress("DEPRECATION") private fun picked(d:Intent):Uri?=if(Build.VERSION.SDK_INT>=33)d.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI,Uri::class.java) else d.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

 private fun addTask(){
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  val tt=EditText(this).apply{hint=t("taskph")};val dd=EditText(this).apply{hint=t("minutes");inputType=InputType.TYPE_CLASS_NUMBER;setText("60")};val s=CheckBox(this).apply{text=t("strictcb");isChecked=true}
  val rt=RadioButton(this).apply{text=t("dayt");id=View.generateViewId();isChecked=true};val rn=RadioButton(this).apply{text=t("dayn");id=View.generateViewId()}
  val rg=RadioGroup(this).apply{orientation=LinearLayout.HORIZONTAL;addView(rt);addView(rn)};val rp=CheckBox(this).apply{text=t("repeat")}
  l.addView(tt);l.addView(dd);l.addView(rg);l.addView(rp);l.addView(s)
  dlg().setTitle(t("newtask")).setView(l).setPositiveButton(t("picktime")){_,_->
   pick(8,0){h,m->val tom=rn.isChecked;val passed=h*60+m<=Store.nowMin()
    if(!tom&&!rp.isChecked&&passed){toast(t("pasttime"));return@pick}
    val date=if(rp.isChecked)(if(tom||passed)"~"+Store.key(-1) else "") else if(tom)Store.key(-1) else Store.day()
    val k=Task("t"+System.currentTimeMillis(),tt.text.toString().ifBlank{"Task"},h,m,dd.text.toString().toIntOrNull()?:30,s.isChecked,date)
    Store.saveTasks(this,Store.tasks(this)+k);Scheduler.all(this);render()}}.show()}
 private fun pickApp(){
  if(Store.apps(this).size>=5){toast(t("max5"));return}
  val pm=packageManager;val bad=listOf("com.android.settings","com.android.phone","com.android.dialer","com.google.android.dialer","com.android.emergency")
  val li=pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0).map{it.loadLabel(pm).toString() to it.activityInfo.packageName}.filter{it.second!=packageName&&!bad.contains(it.second)}.distinctBy{it.second}.sortedBy{it.first.lowercase()}
  dlg().setTitle(t("pickapp")).setItems(li.map{it.first}.toTypedArray()){_,i->Store.saveApps(this,Store.apps(this)+li[i]);render()}.show()}
 private fun skipDialog(id:String){
  val tk=Store.tasks(this).firstOrNull{it.id==id}?:return
  stopService(Intent(this,NagService::class.java));getSystemService(NotificationManager::class.java).cancel(1)
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  l.addView(tx(L.f(this,"skipbody","g" to L.theme(this),"t" to tk.title),14f));val r=EditText(this).apply{hint=t("reason");setTextColor(TX);setHintTextColor(MUT)};l.addView(r)
  val dl=dlg().setTitle(t("skiptitle")).setView(l).setNegativeButton(t("nokeep"),null).setPositiveButton(t("skipbtn"),null).create()
  dl.show();val pb=dl.getButton(AlertDialog.BUTTON_POSITIVE);pb.isEnabled=false
  val timer=object:CountDownTimer(30000,1000){override fun onTick(ms:Long){pb.text=t("skipbtn")+" ("+ms/1000+")"};override fun onFinish(){pb.text=t("skipbtn");pb.isEnabled=true}}
  timer.start();dl.setOnDismissListener{timer.cancel();resumeNag()}
  pb.setOnClickListener{if(r.text.trim().length<6){toast(t("needreason"));return@setOnClickListener};Store.skip(this,id);Store.addExcuse(this,tk.title,r.text.toString().trim());dl.dismiss();render()}}
 private fun audit(){
  val st=Store.todays(this).filter{it.strict}
  if(st.isEmpty()){toast(t("nostrict"));return}
  if(pr().getBoolean("aud_"+Store.day(),false)){toast(t("auddone"));return}
  val log=Store.log(this);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)};val ins=ArrayList<Pair<EditText,Spinner>>()
  st.forEach{tk->val e=log.optJSONObject(tk.id);val tr=if(e!=null&&e.has("s")&&e.has("e"))((e.getLong("e")-e.getLong("s"))/60000).toInt() else 0
   l.addView(tx(tk.title+" ("+tk.dur+" min)",16f,TX,true).apply{setPadding(0,d(10f),0,0)})
   val r=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;hint=t("actual");setText(tr.toString())}
   val q=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf(t("q1"),t("q2"),t("q3"),t("q4")))}
   l.addView(r);l.addView(q);ins.add(r to q)}
  dlg().setTitle(t("audtitle")).setView(ScrollView(this).apply{addView(l)}).setPositiveButton(t("submit")){_,_->
   val fails=st.indices.filter{i->val e=log.optJSONObject(st[i].id);e==null||e.has("x")||ins[i].second.selectedItemPosition>=2||(ins[i].first.text.toString().toIntOrNull()?:0)<st[i].dur/2}
   var xpT=0;for(i in st.indices){val e=log.optJSONObject(st[i].id);if(e!=null&&e.has("e")&&e.has("s")){val el=((e.getLong("e")-e.getLong("s"))/60000).toInt();xpT+=if(el>=st[i].dur/2)10+minOf(el,st[i].dur)/10 else 3}}
   for(i in st.indices)Store.addEvent(this,st[i].title,!fails.contains(i))
   val res=Store.applyAudit(this,fails.size,st.size,minOf(xpT,60));pr().edit().putBoolean("aud_"+Store.day(),true).apply()
   var msg:String
   if(res.pass)msg=L.f(this,"passmsg","n" to Store.name(this))+(if(res.reward.isNotEmpty())"\n\n"+res.reward else "")
   else{msg=L.line(this,"ins"+L.inten(this))+"\n\n"+t("failed")+": "+fails.joinToString{st[it].title}
    if(res.shieldUsed)msg+="\n\n"+t("shield")
    val free=Store.apps(this).filter{Store.locks(this).optLong(it.second,0)<System.currentTimeMillis()}.shuffled().take(res.lockCount)
    if(free.isNotEmpty()){free.forEach{Store.lock(this,it.second,res.lockHours)};msg+="\n\n"+L.f(this,"punline","a" to free.joinToString{it.first},"h" to res.lockHours.toString())}else msg+="\n\n"+t("punhint")}
   dlg().setTitle(t("audres")).setMessage(msg).setPositiveButton(t("ok")){_,_->hap();render(res.pass||res.shieldUsed)}.show()}.show()}
}
