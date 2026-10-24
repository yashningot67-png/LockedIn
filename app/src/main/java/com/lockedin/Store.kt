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
 fun start(c:Context,id:String)=mark(c,id,"s",System.currentTimeMillis())
 fun finish(c:Context,id:String)=mark(c,id,"e",System.currentTimeMillis())
 fun skip(c:Context,id:String)=mark(c,id,"x",true)
 fun pending(c:Context):Task?{val l=log(c);return tasks(c).firstOrNull{it.strict&&it.mins<=nowMin()&&!l.has(it.id)}}
 fun apps(c:Context):List<Pair<String,String>>{val a=JSONArray(p(c).getString("apps","[]"));return (0 until a.length()).map{val s=a.getString(it).split("|");s[0] to s[1]}}
 fun saveApps(c:Context,l:List<Pair<String,String>>){val a=JSONArray();l.forEach{a.put(it.first+"|"+it.second)};p(c).edit().putString("apps",a.toString()).apply()}
 fun locks(c:Context)=JSONObject(p(c).getString("locks","{}"))
 fun lock(c:Context,pkg:String){p(c).edit().putString("locks",locks(c).put(pkg,System.currentTimeMillis()+20*3600000L).toString()).apply()}
 fun label(c:Context,pkg:String)=apps(c).firstOrNull{it.second==pkg}?.first?:pkg
 private fun f(c:Context,s:String)=s.replace("{n}",name(c)).replace("{g}",goal(c))
 private val Q=listOf("Bhai {n}, mood nahi, system chalta hai. {g} ke liye bas start kar.","Jo aaj {g} ke liye pasina bahayega, wahi kal results pe hasega.","Discipline boring lagta hai, par regret usse zyada boring hota hai.","5 minute shuru kar de {n}, baaki momentum sambhal lega.","Tera competition koi aur nahi, kal wala tu hai.","Lock-in ho ja {n}. Tareef baad me milegi, pehle kaam.")
 private val I=listOf("{n}, tune aaj apne hi {g} ke saath dhoka kiya. Jo plan khud banaya tha, wahi nibha nahi paaya.","Comfort zone ne aaj tujhe jeet liya, {n}. {g} wale log bahane nahi dete, results dete hain.","Kal se pakka, yahi bolega na? {g} ko kal nahi, aaj chahiye tha.","Respect ke saath bol raha hu {n}: tu isse behtar hai, par aaj tune dikhaya nahi.","Jab tu ruka, tab koi aur {g} ki taraf ek kadam aage badh gaya.")
 private val G=listOf("{n}, time ho gaya. {g} apne aap nahi hoga, start kar.","Abhi nahi to kab? {g} tera intezaar nahi karega.","Phone rakh, kaam pakad. Ye reminder tab tak aayega jab tak tu shuru nahi karta.")
 fun quote(c:Context)=f(c,Q[(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)+Calendar.getInstance().get(Calendar.HOUR_OF_DAY))%Q.size])
 fun insult(c:Context)=f(c,I.random())
 fun nag(c:Context)=f(c,G.random())
}
