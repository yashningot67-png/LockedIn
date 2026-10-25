package com.lockedin
import android.content.Context
import org.json.*
import java.text.SimpleDateFormat
import java.util.*

data class Task(val id:String,val title:String,val h:Int,val m:Int,val dur:Int,val strict:Boolean){
 val mins get()=h*60+m
 val time get()="%02d:%02d".format(h,m)
}
object Store{
 fun p(c:Context)=c.getSharedPreferences("li",0)
 fun day():String=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
 fun nowMin():Int{val c=Calendar.getInstance();return c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE)}
 fun name(c:Context)=p(c).getString("name","Yash")!!
 fun goal(c:Context)=p(c).getString("goal","cybersecurity aur bulking")!!
 fun tasks(c:Context):List<Task>{val a=JSONArray(p(c).getString("tasks","[]"))
  return (0 until a.length()).map{val o=a.getJSONObject(it);Task(o.getString("id"),o.getString("t"),o.getInt("h"),o.getInt("m"),o.getInt("d"),o.getBoolean("s"))}.sortedBy{it.mins}}
 fun saveTasks(c:Context,l:List<Task>){val a=JSONArray();l.forEach{a.put(JSONObject().put("id",it.id).put("t",it.title).put("h",it.h).put("m",it.m).put("d",it.dur).put("s",it.strict))};p(c).edit().putString("tasks",a.toString()).apply()}
 fun log(c:Context)=JSONObject(p(c).getString("log_"+day(),"{}"))
 fun mark(c:Context,id:String,k:String,v:Any){val l=log(c);val o=l.optJSONObject(id)?:JSONObject();o.put(k,v);l.put(id,o);p(c).edit().putString("log_"+day(),l.toString()).apply()}
 fun start(c:Context,id:String){mark(c,id,"s",System.currentTimeMillis());val t=tasks(c).firstOrNull{it.id==id};if(t!=null&&t.strict)p(c).edit().putLong("focus",System.currentTimeMillis()+t.dur*60000L).apply()}
 fun finish(c:Context,id:String){val l=log(c).optJSONObject(id);mark(c,id,"e",System.currentTimeMillis());p(c).edit().putLong("focus",0).apply()
  val mins=if(l!=null&&l.has("s"))((System.currentTimeMillis()-l.getLong("s"))/60000).toInt() else 0
  val k="fm_"+day();val used=p(c).getInt(k,0);val add=minOf(maxOf(0,120-used),maxOf(0,mins));p(c).edit().putInt(k,used+add).apply()
  addXp(c,20+add/5)}
 fun skip(c:Context,id:String){mark(c,id,"x",true);p(c).edit().putLong("focus",0).apply();addXp(c,-15)}
 fun pending(c:Context):Task?{val l=log(c);return tasks(c).firstOrNull{it.strict&&it.mins<=nowMin()&&!l.has(it.id)}}
 fun apps(c:Context):List<Pair<String,String>>{val a=JSONArray(p(c).getString("apps","[]"));return (0 until a.length()).map{val s=a.getString(it).split("|");s[0] to s[1]}}
 fun saveApps(c:Context,l:List<Pair<String,String>>){val a=JSONArray();l.forEach{a.put(it.first+"|"+it.second)};p(c).edit().putString("apps",a.toString()).apply()}
 fun locks(c:Context)=JSONObject(p(c).getString("locks","{}"))
 fun lock(c:Context,pkg:String,hours:Int=20){p(c).edit().putString("locks",locks(c).put(pkg,System.currentTimeMillis()+hours*3600000L).toString()).apply()}
 fun label(c:Context,pkg:String)=apps(c).firstOrNull{it.second==pkg}?.first?:pkg
 private fun f(c:Context,s:String)=s.replace("{n}",name(c)).replace("{g}",goal(c))
 private val Q=listOf("Bhai {n}, mood nahi, system chalta hai. {g} ke liye bas start kar.","Jo aaj {g} ke liye pasina bahayega, wahi kal results pe hasega.","Discipline boring lagta hai, par regret usse zyada boring hota hai.","5 minute shuru kar de {n}, baaki momentum sambhal lega.","Tera competition koi aur nahi, kal wala tu hai.","Lock-in ho ja {n}. Tareef baad me milegi, pehle kaam.")
 private val I=listOf("{n}, tune aaj apne hi {g} ke saath dhoka kiya. Jo plan khud banaya tha, wahi nibha nahi paaya.","Comfort zone ne aaj tujhe jeet liya, {n}. {g} wale log bahane nahi dete, results dete hain.","Kal se pakka, yahi bolega na? {g} ko kal nahi, aaj chahiye tha.","Respect ke saath bol raha hu {n}: tu isse behtar hai, par aaj tune dikhaya nahi.","Jab tu ruka, tab koi aur {g} ki taraf ek kadam aage badh gaya.")
 private val G=listOf("{n}, time ho gaya. {g} apne aap nahi hoga, start kar.","Abhi nahi to kab? {g} tera intezaar nahi karega.","Phone rakh, kaam pakad. Ye reminder tab tak aayega jab tak tu shuru nahi karta.")
 fun quote(c:Context)=f(c,Q[(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)+Calendar.getInstance().get(Calendar.HOUR_OF_DAY))%Q.size])
 fun insult(c:Context)=f(c,I.random())
 fun nag(c:Context)=f(c,G.random())

 fun key(ago:Int):String{val cl=Calendar.getInstance();cl.add(Calendar.DAY_OF_YEAR,-ago);return SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cl.time)}
 fun liveProgress(c:Context):Pair<Int,Int>{val l=log(c);val st=tasks(c).filter{it.strict};return st.count{l.optJSONObject(it.id)?.has("e")==true} to st.size}
 fun pctAt(c:Context,ago:Int):Int{val h=p(c).getInt("h_"+key(ago),-1);if(h>=0||ago!=0)return h;val(d,t)=liveProgress(c);return if(t==0)-1 else d*100/t}
 fun setHist(c:Context,pct:Int,ok:Boolean){p(c).edit().putInt("h_"+day(),pct).putBoolean("ok_"+day(),ok).apply();val s=streak(c);if(s>p(c).getInt("best",0))p(c).edit().putInt("best",s).apply()}
 fun streak(c:Context):Int{var i=if(p(c).getBoolean("ok_"+day(),false))0 else 1;var n=0;while(i<400&&p(c).getBoolean("ok_"+key(i),false)){n++;i++};return n}
 fun best(c:Context)=maxOf(p(c).getInt("best",0),streak(c))

 fun focusOn(c:Context)=p(c).getBoolean("dw",true)&&p(c).getLong("focus",0)>System.currentTimeMillis()
 fun sunsetOn(c:Context):Boolean{if(!p(c).getBoolean("ss",false))return false;val h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);val a=p(c).getInt("sh",23);val b=p(c).getInt("eh",6);return if(a>b)(h>=a||h<b) else (h>=a&&h<b)}
 fun cheatOn(c:Context)=p(c).getLong("cheat_until",0)>System.currentTimeMillis()
 fun useCheat(c:Context){val n=p(c).getInt("cheatn",0);if(n>0)p(c).edit().putInt("cheatn",n-1).putLong("cheat_until",System.currentTimeMillis()+3600000L).apply()}
 fun reason(c:Context,pkg:String):String?{if(cheatOn(c))return null
  if(locks(c).optLong(pkg,0)>System.currentTimeMillis())return "lock"
  if(apps(c).any{it.second==pkg}){if(focusOn(c))return "focus";if(sunsetOn(c))return "sunset"}
  return null}
 val RANKS=listOf("FREEBIE" to 0,"ROOKIE" to 100,"WARRIOR" to 300,"ENFORCER" to 650,"DOMINATOR" to 1100,"PREDATOR" to 1800,"ELITE" to 2800,"OVERLORD" to 4200,"APEX" to 6000,"VENGEANCE" to 8500,"CONQUEROR" to 10000)
 fun xp(c:Context)=p(c).getInt("xp",0)
 fun addXp(c:Context,n:Int){val i=rankIdx(c);val floor=if(i>0)RANKS[i-1].second else 0;val v=if(n<0)maxOf(floor,xp(c)+n) else xp(c)+n;p(c).edit().putInt("xp",maxOf(0,v)).apply()}
 fun rankIdx(c:Context):Int{val x=xp(c);return RANKS.indexOfLast{x>=it.second}}
 fun rankName(c:Context)=RANKS[rankIdx(c)].first
 fun rankPct(c:Context):Int{val i=rankIdx(c);if(i==RANKS.lastIndex)return 100;val a=RANKS[i].second;val b=RANKS[i+1].second;return (xp(c)-a)*100/(b-a)}
 class AuditResult(val pass:Boolean,val shieldUsed:Boolean,val lockHours:Int,val lockCount:Int,val reward:String)
 fun applyAudit(c:Context,failCount:Int,total:Int):AuditResult{
  val pr=p(c);val pct=(total-failCount)*100/total;var reward=""
  if(failCount==0){
   pr.edit().putInt("miss",0).apply();addXp(c,50)
   val cb=pr.getInt("cb",0)
   if(cb>0){pr.edit().putInt("cb",cb-1).apply();if(cb==1){pr.edit().putInt("shield",minOf(2,pr.getInt("shield",0)+1)).apply();reward+="Comeback complete: shield mila. "}}
   setHist(c,pct,true)
   val s=streak(c)
   if(s>0&&s%7==0){pr.edit().putInt("shield",minOf(2,pr.getInt("shield",0)+1)).putInt("cheatn",pr.getInt("cheatn",0)+1).apply();addXp(c,30);reward+="Perfect week: shield aur cheat hour mila."}
   return AuditResult(true,false,0,0,reward.trim())}
  val prev=streak(c);val miss=pr.getInt("miss",0)+1
  pr.edit().putInt("miss",miss).apply();addXp(c,-30)
  val sh=pr.getInt("shield",0);var used=false
  if(sh>0){pr.edit().putInt("shield",sh-1).apply();used=true}else if(prev>=3)pr.edit().putInt("cb",3).apply()
  setHist(c,pct,used)
  return AuditResult(false,used,minOf(48,20+8*(miss-1)),1+(miss-1)/2,"")}
 private fun tail(a:JSONArray,n:Int):JSONArray{if(a.length()<=n)return a;val r=JSONArray();for(i in a.length()-n until a.length())r.put(a.get(i));return r}
 fun addExcuse(c:Context,title:String,reason:String){val a=JSONArray(p(c).getString("exc","[]"));a.put(JSONObject().put("t",title).put("r",reason).put("d",day()));p(c).edit().putString("exc",tail(a,100).toString()).apply()}
 fun addEvent(c:Context,title:String,ok:Boolean){val a=JSONArray(p(c).getString("ev","[]"));a.put(JSONObject().put("t",title).put("w",Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).put("ok",ok));p(c).edit().putString("ev",tail(a,600).toString()).apply()}
 fun undone(c:Context):Int{val l=log(c);return tasks(c).count{it.strict&&l.optJSONObject(it.id)?.has("e")!=true}}
 fun insights(c:Context):List<String>{
  val a=JSONArray(p(c).getString("ev","[]"));val ex=JSONArray(p(c).getString("exc","[]"))
  if(a.length()<4&&ex.length()==0)return listOf(L.t(c,"i_none"))
  val byT=HashMap<String,IntArray>();val byD=HashMap<Int,IntArray>()
  for(i in 0 until a.length()){val o=a.getJSONObject(i);val t=byT.getOrPut(o.getString("t")){IntArray(2)};val d=byD.getOrPut(o.getInt("w")){IntArray(2)};t[0]++;d[0]++;if(!o.getBoolean("ok")){t[1]++;d[1]++}}
  val out=ArrayList<String>()
  byT.filter{it.value[0]>=2}.maxByOrNull{it.value[1]*100/it.value[0]}?.let{if(it.value[1]>0)out.add(L.f(c,"i_task","x" to it.key,"p" to (it.value[1]*100/it.value[0]).toString()))}
  val dn=listOf("","Sun","Mon","Tue","Wed","Thu","Fri","Sat")
  byD.filter{it.value[0]>=2}.maxByOrNull{it.value[1]*100/it.value[0]}?.let{if(it.value[1]>0)out.add(L.f(c,"i_day","x" to dn[it.key],"p" to (it.value[1]*100/it.value[0]).toString()))}
  if(ex.length()>0)out.add(L.f(c,"i_skips","n" to ex.length().toString(),"r" to ex.getJSONObject(ex.length()-1).getString("r")))
  if(out.isEmpty())out.add(L.t(c,"i_ok"))
  return out}
 fun backup(c:Context):String{val o=JSONObject()
  for((k,v) in p(c).all){if(k=="crash")continue;val j=JSONObject()
   when(v){is Int->j.put("t","i").put("v",v);is Long->j.put("t","l").put("v",v);is Boolean->j.put("t","b").put("v",v);is String->j.put("t","s").put("v",v);else->continue}
   o.put(k,j)}
  return "LOCKEDIN-BACKUP:"+o.toString()}
 fun restore(c:Context,txt:String):Boolean{
  try{val t=txt.trim();if(!t.startsWith("LOCKEDIN-BACKUP:"))return false
   val o=JSONObject(t.removePrefix("LOCKEDIN-BACKUP:"));val e=p(c).edit().clear()
   for(k in o.keys()){val j=o.getJSONObject(k)
    when(j.getString("t")){"i"->e.putInt(k,j.getInt("v"));"l"->e.putLong(k,j.getLong("v"));"b"->e.putBoolean(k,j.getBoolean("v"));else->e.putString(k,j.getString("v"))}}
   e.apply();return true}catch(x:Exception){return false}}
}
