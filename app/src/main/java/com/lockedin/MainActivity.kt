package com.lockedin
import android.Manifest
import android.app.*
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:Activity(){
 private lateinit var root:LinearLayout
 private val H=Handler(Looper.getMainLooper())
 private val tick=object:Runnable{override fun run(){render();H.postDelayed(this,30000)}}
 override fun onCreate(b:Bundle?){super.onCreate(b);N.ch(this)
  val sv=ScrollView(this);root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,90,40,90)};sv.addView(root);setContentView(sv)
  if(Build.VERSION.SDK_INT>=33)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
  Scheduler.all(this)}
 override fun onNewIntent(i:Intent){super.onNewIntent(i);setIntent(i)}
 override fun onResume(){super.onResume();H.removeCallbacks(tick);H.post(tick)
  if(intent.getBooleanExtra("audit",false)){intent.removeExtra("audit");audit()}}
 override fun onPause(){super.onPause();H.removeCallbacks(tick)}
 private fun tv(s:String,sz:Float=16f,b:Boolean=false)=TextView(this).apply{text=s;textSize=sz;if(b)setTypeface(null,Typeface.BOLD);setPadding(0,12,0,12)}
 private fun bt(s:String,f:()->Unit)=Button(this).apply{text=s;setOnClickListener{f()}}
 private fun add(v:View){root.addView(v)}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
 private fun system(a:String,pkg:Boolean=true){try{startActivity(Intent(a).apply{if(pkg)data=Uri.parse("package:$packageName")})}catch(e:Exception){toast("Settings nahi khuli, manually on kar")}}
 private fun render(){root.removeAllViews()
  add(tv("Locked In",30f,true));add(tv(Store.quote(this),17f))
  val lk=Store.locks(this);val fmt=SimpleDateFormat("d MMM, h:mm a",Locale.getDefault())
  lk.keys().forEach{p->val u=lk.getLong(p);if(u>System.currentTimeMillis())add(tv("LOCKED: "+Store.label(this,p)+" ("+fmt.format(Date(u))+" tak)",15f,true))}
  add(tv("Aaj ka schedule (delete: long press)",20f,true))
  val log=Store.log(this)
  Store.tasks(this).forEach{t->val e=log.optJSONObject(t.id)
   val st=when{!t.strict->"mini";e==null->if(Store.nowMin()>=t.mins)"ALARM: start kar" else "upcoming";e.has("x")->"skip kiya";e.has("e")->"done";else->"chal raha hai"}
   add(tv(t.time+"  "+t.title+"  ["+(if(t.strict)t.dur.toString()+" min strict" else "mini")+"] "+st).apply{setOnLongClickListener{Store.saveTasks(this@MainActivity,Store.tasks(this@MainActivity).filter{x->x.id!=t.id});Scheduler.cancel(this@MainActivity,t.id);render();true}})
   if(t.strict&&e==null)add(bt("Start: "+t.title){Store.start(this,t.id);getSystemService(NotificationManager::class.java).cancel(1);render()})
   if(t.strict&&e!=null&&e.has("s")&&!e.has("e"))add(bt("Complete: "+t.title){Store.finish(this,t.id);render()})}
  add(bt("+ Task add kar"){addTask()});add(bt("Day audit"){audit()})
  add(tv("Punishment apps (max 5, tap = hata)",20f,true))
  Store.apps(this).forEach{a->add(bt(a.first){Store.saveApps(this,Store.apps(this).filter{x->x.second!=a.second});render()})}
  add(bt("+ App add kar"){pickApp()})
  add(tv("Profile",20f,true))
  val n=EditText(this).apply{hint="Naam";setText(Store.name(this@MainActivity))};val g=EditText(this).apply{hint="Goal";setText(Store.goal(this@MainActivity))}
  add(n);add(g);add(bt("Save"){Store.p(this).edit().putString("name",n.text.toString().ifBlank{"Yash"}).putString("goal",g.text.toString().ifBlank{"apna goal"}).apply();toast("Save ho gaya");render()})
  add(bt("Audit time (abhi "+Store.p(this).getInt("ah",22)+":"+"%02d".format(Store.p(this).getInt("am",0))+")"){TimePickerDialog(this,{_,h,m->Store.p(this).edit().putInt("ah",h).putInt("am",m).apply();Scheduler.audit(this);render()},Store.p(this).getInt("ah",22),0,false).show()})
  add(tv("Ek baar setup (sab on kar)",20f,true))
  if(Build.VERSION.SDK_INT>=31)add(bt("1. Exact alarms allow"){system(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)})
  if(Build.VERSION.SDK_INT>=34)add(bt("2. Full-screen alerts allow"){system(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)})
  add(bt("3. Battery unrestricted"){system(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)})
  add(bt("4. App blocker on (Accessibility > Locked In)"){system(Settings.ACTION_ACCESSIBILITY_SETTINGS,false)})}
 private fun addTask(){
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  val t=EditText(this).apply{hint="Task (Workout, Skill block, 100g peanuts)"}
  val d=EditText(this).apply{hint="Minutes";inputType=InputType.TYPE_CLASS_NUMBER;setText("60")}
  val s=CheckBox(this).apply{text="Strict (alarm jab tak start na karu)";isChecked=true}
  l.addView(t);l.addView(d);l.addView(s)
  AlertDialog.Builder(this).setTitle("Naya task").setView(l).setPositiveButton("Time chun"){_,_->
   TimePickerDialog(this,{_,h,m->val k=Task("t"+System.currentTimeMillis(),t.text.toString().ifBlank{"Task"},h,m,d.text.toString().toIntOrNull()?:30,s.isChecked)
    Store.saveTasks(this,Store.tasks(this)+k);Scheduler.at(this,k.id,k.h,k.m);render()},8,0,false).show()}.show()}
 private fun pickApp(){
  if(Store.apps(this).size>=5){toast("Max 5 apps");return}
  val pm=packageManager;val bad=listOf("com.android.settings","com.android.phone","com.android.dialer","com.google.android.dialer","com.android.emergency")
  val li=pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0).map{it.loadLabel(pm).toString() to it.activityInfo.packageName}.filter{it.second!=packageName&&!bad.contains(it.second)}.distinctBy{it.second}.sortedBy{it.first.lowercase()}
  AlertDialog.Builder(this).setTitle("App chun").setItems(li.map{it.first}.toTypedArray()){_,i->Store.saveApps(this,Store.apps(this)+li[i]);render()}.show()}
 private fun audit(){
  val st=Store.tasks(this).filter{it.strict}
  if(st.isEmpty()){toast("Strict task nahi hai");return}
  if(Store.p(this).getBoolean("aud_"+Store.day(),false)){toast("Aaj ka audit ho chuka");return}
  val log=Store.log(this);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,20,40,0)}
  val ins=ArrayList<Pair<EditText,Spinner>>()
  st.forEach{t->val e=log.optJSONObject(t.id)
   val tr=if(e!=null&&e.has("s")&&e.has("e"))((e.getLong("e")-e.getLong("s"))/60000).toInt() else 0
   l.addView(tv(t.title+" (plan "+t.dur+" min)",16f,true))
   val r=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;hint="actual minutes";setText(tr.toString())}
   val q=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Poore focus se","Aadha dhyaan","Bas aise hi","Nahi kar paya"))}
   l.addView(r);l.addView(q);ins.add(r to q)}
  AlertDialog.Builder(this).setTitle("Strict audit: sach bol").setView(ScrollView(this).apply{addView(l)}).setPositiveButton("Submit"){_,_->
   val fails=st.indices.filter{i->val e=log.optJSONObject(st[i].id);e==null||e.has("x")||ins[i].second.selectedItemPosition>=2||(ins[i].first.text.toString().toIntOrNull()?:0)<st[i].dur/2}
   var msg=""
   if(fails.isEmpty())msg="Aaj tu sach me locked in tha, "+Store.name(this)+". Kal bhi yahi."
   else{msg=Store.insult(this)+"\n\nFail: "+fails.joinToString{st[it].title}
    val free=Store.apps(this).filter{Store.locks(this).optLong(it.second,0)<System.currentTimeMillis()}
    if(free.isNotEmpty()){val a=free.random();Store.lock(this,a.second);msg+="\n\nPunishment: "+a.first+" 20 ghante ke liye locked."}
    else msg+="\n\nPunishment apps add kar, warna dant adhoori rahegi."}
   Store.p(this).edit().putBoolean("aud_"+Store.day(),true).apply()
   AlertDialog.Builder(this).setTitle("Audit result").setMessage(msg).setPositiveButton("Ok"){_,_->render()}.show()}.show()}
}
