package com.lockedin
import android.content.Context
import java.util.Calendar

object L{
 fun lang(c:Context)=Store.p(c).getInt("lang",1)
 fun inten(c:Context)=Store.p(c).getInt("int",1)
 private fun pick(s:String,i:Int):String{val p=s.split("|");val v=p.getOrNull(i)?:"";return if(v.isBlank())p[0] else v}
 fun t(c:Context,k:String)=pick(S[k]?:k,lang(c))
 fun f(c:Context,k:String,vararg kv:Pair<String,String>):String{var s=t(c,k);for((a,b) in kv)s=s.replace("{"+a+"}",b);return s}
 private fun fill(c:Context,s:String)=s.replace("{n}",Store.name(c)).replace("{g}",Store.goal(c))
 fun line(c:Context,pool:String,seed:Int=-1):String{val l=P[pool]?:return pool;return fill(c,pick(if(seed>=0)l[seed%l.size] else l.random(),lang(c)))}
 fun seed():Int{val c=Calendar.getInstance();return c.get(Calendar.DAY_OF_YEAR)*24+c.get(Calendar.HOUR_OF_DAY)}
 val P=mapOf(
  "q" to listOf("{n}, mood doesn't run you, the system does. Start for {g}.|Bhai {n}, mood nahi, system chalta hai. {g} ke liye bas start kar.|{n}, मूड नहीं, सिस्टम चलता है। {g} के लिए बस शुरू करो।",
   "Whoever sweats for {g} today laughs at the results tomorrow.|Jo aaj {g} ke liye pasina bahayega, wahi kal results pe hasega.|जो आज {g} के लिए पसीना बहाएगा, वही कल नतीजों पर हँसेगा।",
   "Discipline feels boring. Regret is more boring.|Discipline boring lagta hai, par regret usse zyada boring hota hai.|अनुशासन उबाऊ लगता है, पर पछतावा उससे ज़्यादा उबाऊ होता है।",
   "Start for 5 minutes, {n}. Momentum does the rest.|5 minute shuru kar de {n}, baaki momentum sambhal lega.|{n}, बस 5 मिनट शुरू करो, बाकी मोमेंटम संभाल लेगा।",
   "Your only competition is yesterday's you.|Tera competition koi aur nahi, kal wala tu hai.|तुम्हारा मुक़ाबला सिर्फ़ कल वाले तुम से है।"),
  "ins0" to listOf("{n}, today slipped away from {g}. Tomorrow is a fresh try, start small.|{n}, aaj {g} thoda peeche reh gaya. Kal naya mauka hai, chhote se shuru kar.|{n}, आज {g} थोड़ा पीछे रह गया। कल नया मौक़ा है, छोटी शुरुआत करो।",
   "One bad day isn't the end, {n}. Just don't make it a habit.|Ek kharab din end nahi hai {n}, par ise aadat mat bana.|एक ख़राब दिन अंत नहीं है {n}, पर इसे आदत मत बनाओ।"),
  "ins1" to listOf("{n}, you broke your own promise for {g}. The plan was yours.|{n}, tune apne hi {g} ke saath dhoka kiya. Jo plan khud banaya, wahi nibha nahi paaya.|{n}, तुमने अपने ही {g} से वादाख़िलाफ़ी की। प्लान तुम्हारा था, निभाया नहीं।",
   "Comfort won today, {n}. People chasing {g} don't make excuses, they deliver.|Comfort zone ne aaj tujhe jeet liya, {n}. {g} wale log bahane nahi dete, results dete hain.|आज आराम जीत गया, {n}। {g} वाले बहाने नहीं देते, नतीजे देते हैं।"),
  "ins2" to listOf("Tomorrow, tomorrow, how many times? {g} needed you today, not tomorrow.|Kal se pakka, yahi bolega na? Kitni baar? {g} ko kal nahi, aaj chahiye tha.|कल से पक्का, यही कहोगे? कितनी बार? {g} को आज चाहिए था, कल नहीं।",
   "While you stopped, someone else took a step toward {g}. Wake up, {n}.|Jab tu ruka, tab koi aur {g} ki taraf ek kadam aage badh gaya. Jaag ja {n}.|जब तुम रुके, किसी और ने {g} की ओर एक कदम बढ़ा लिया। जागो {n}।"),
  "nag0" to listOf("{n}, it's time. A small start for {g} is enough.|{n}, time ho gaya. {g} ke liye chhota sa start kaafi hai.|{n}, समय हो गया। {g} के लिए छोटी शुरुआत काफ़ी है।"),
  "nag1" to listOf("{n}, time's up. {g} won't happen by itself, start.|{n}, time ho gaya. {g} apne aap nahi hoga, start kar.|{n}, समय हो गया। {g} अपने आप नहीं होगा, शुरू करो।"),
  "nag2" to listOf("Phone down. Start now. {g} is not waiting for your mood.|Phone rakh, kaam pakad. {g} tere mood ka intezaar nahi karega.|फ़ोन रखो, काम पकड़ो। {g} तुम्हारे मूड का इंतज़ार नहीं करेगा।"),
  "eve0" to listOf("{n}, there's still time today. Finish your tasks and keep your streak.|{n}, aaj abhi time hai. Tasks khatam kar aur streak bacha.|{n}, आज अभी समय है। टास्क पूरे करो और स्ट्रीक बचाओ।"),
  "eve1" to listOf("{n}, your streak is at risk. Tasks are still pending. Move.|{n}, teri streak khatre me hai. Tasks baaki hain. Utth.|{n}, तुम्हारी स्ट्रीक ख़तरे में है। टास्क बाकी हैं। उठो।"),
  "eve2" to listOf("Still not done? Your streak is bleeding. Back to work, now.|Abhi tak nahi hua? Streak mar rahi hai. Wapas kaam par, abhi.|अभी तक नहीं हुआ? स्ट्रीक मर रही है। वापस काम पर, अभी।"),
  "eveok" to listOf("Streak safe today, {n}. Keep going.|Aaj streak safe hai, {n}. Aise hi chal.|आज स्ट्रीक सुरक्षित है, {n}। ऐसे ही चलो।"))
 val S=mapOf(
  "tab_today" to "Today|Aaj|आज","tab_stats" to "Stats||आँकड़े","tab_rank" to "Rank||रैंक","tab_set" to "Settings||सेटिंग्स",
  "dose" to "TODAY'S DOSE|AAJ KA DOSE|आज की खुराक","today" to "TODAY|AAJ|आज","streak" to "DAY STREAK||दिन की स्ट्रीक","best" to "BEST||सर्वश्रेष्ठ",
  "mission" to "TODAY'S MISSION (long press = delete)|AAJ KA MISSION (delete: long press)|आज का मिशन (हटाने को देर तक दबाओ)",
  "start" to "START||शुरू","complete" to "COMPLETE||पूरा","done" to "DONE|DONE|हो गया","skipped" to "SKIPPED|SKIP kiya|छोड़ा","upcoming" to "UPCOMING||आने वाला","mini" to "MINI||मिनी",
  "strictmin" to "min, strict||मिनट, सख़्त","minirem" to "mini reminder||मिनी रिमाइंडर","addtask" to "+ ADD TASK|+ TASK ADD KAR|+ टास्क जोड़ो","audit" to "DAY AUDIT||दिन का ऑडिट",
  "cheatbtn" to "USE CHEAT HOUR (1 hour, all unlocked)|CHEAT HOUR use kar (1 ghanta sab unlock)|चीट आवर लो (1 घंटा सब खुला)",
  "dwon" to "DEEP WORK ON||डीप वर्क चालू","dwtxt" to "Punishment apps locked until|Punishment apps locked, tak:|पनिशमेंट ऐप्स बंद, तब तक:","cheaton" to "CHEAT HOUR RUNNING|CHEAT HOUR CHAL RAHA HAI|चीट आवर चालू है",
  "lockedfor" to "LOCKED||बंद","weekly" to "WEEKLY PERFORMANCE||साप्ताहिक प्रदर्शन","yearly" to "YEARLY CONSISTENCY||सालाना निरंतरता","pattern" to "YOUR PATTERN|TERA PATTERN|तुम्हारा पैटर्न",
  "rank" to "RANK||रैंक","ladder" to "RANK LADDER||रैंक सीढ़ी","next" to "NEXT|AGLA|अगला","maxrank" to "MAX RANK REACHED|MAX RANK mil gaya|सर्वोच्च रैंक मिल गया","rankup" to "RANK UP!||रैंक अप!",
  "punish" to "PUNISHMENT APPS (max 5, tap = remove)|PUNISHMENT APPS (max 5, tap = hata)|पनिशमेंट ऐप्स (अधिकतम 5, दबाओ = हटाओ)","addapp" to "+ ADD APP|+ APP ADD KAR|+ ऐप जोड़ो",
  "disc" to "DISCIPLINE SETTINGS||अनुशासन सेटिंग्स","dwset" to "Deep Work Lock||डीप वर्क लॉक","sunset" to "Digital sunset||डिजिटल सनसेट","sunfrom" to "Sunset starts (hour)|Sunset shuru (ghanta)|सनसेट शुरू (घंटा)","sunto" to "Sunset ends (hour)|Sunset khatam (ghanta)|सनसेट खत्म (घंटा)",
  "eveset" to "Evening reminder|Shaam ka reminder|शाम का रिमाइंडर","evetime" to "Reminder time||रिमाइंडर समय","tonesys" to "Alarm tone: phone sounds|Alarm tone: phone ki sounds|अलार्म टोन: फ़ोन की आवाज़ें","tonefile" to "Alarm tone: my audio file|Alarm tone: apni audio file|अलार्म टोन: अपनी ऑडियो फ़ाइल","toneset" to "Alarm tone saved|Alarm tone save ho gayi|अलार्म टोन सेव हो गई",
  "audtime" to "Audit time||ऑडिट समय","backup" to "Backup (send to myself)|Backup (data bhej, khud ko)|बैकअप (खुद को भेजो)","restore" to "Restore (copy backup text first)|Restore (backup text copy karke)|रिस्टोर (पहले बैकअप टेक्स्ट कॉपी करो)",
  "restok" to "Restored|Restore ho gaya|रिस्टोर हो गया","restno" to "No backup text in clipboard. Copy it first.|Clipboard me backup text nahi mila. Pehle copy kar.|क्लिपबोर्ड में बैकअप टेक्स्ट नहीं मिला। पहले कॉपी करो।",
  "setup" to "ONE-TIME SETUP|EK BAAR SETUP|एक बार का सेटअप","s1" to "1. Allow exact alarms|1. Exact alarms allow|1. एग्ज़ैक्ट अलार्म की अनुमति दो","s2" to "2. Allow full-screen alerts|2. Full-screen alerts allow|2. फ़ुल-स्क्रीन अलर्ट की अनुमति दो","s3" to "3. Battery unrestricted||3. बैटरी अनरिस्ट्रिक्टेड","s4" to "4. Turn on app blocker (Accessibility > Locked In)|4. App blocker on (Accessibility > Locked In)|4. ऐप ब्लॉकर चालू करो (Accessibility > Locked In)",
  "on" to "ON||चालू","off" to "OFF||बंद","profile" to "PROFILE||प्रोफ़ाइल","lang" to "LANGUAGE||भाषा","name" to "Name|Naam|नाम","goal" to "Your goal|Tera goal|तुम्हारा लक्ष्य","inten" to "INSULT INTENSITY||तीखापन",
  "gentle" to "Gentle||नरम","strictl" to "Strict||सख़्त","brutal" to "Brutal||बेरहम","save" to "SAVE||सेव करो","saved" to "Saved|Save ho gaya|सेव हो गया","back" to "< BACK||< वापस","welcome" to "WELCOME TO LOCKED IN|LOCKED IN me swagat|LOCKED IN में स्वागत है","begin" to "START MY JOURNEY|SHURU KAR|सफ़र शुरू करो",
  "newtask" to "New task|Naya task|नया टास्क","taskph" to "Task (Workout, Skill block...)||टास्क (वर्कआउट, स्किल ब्लॉक...)","minutes" to "Minutes||मिनट","strictcb" to "Strict (alarm until I start)|Strict (alarm jab tak start na karu)|सख़्त (शुरू करने तक अलार्म)","picktime" to "Pick time|Time chun|समय चुनो","pickapp" to "Pick app|App chun|ऐप चुनो","max5" to "Max 5 apps||अधिकतम 5 ऐप","needtask" to "Enter task and time|Task aur time daal|टास्क और समय डालो",
  "audtitle" to "Strict audit: tell the truth|Strict audit: sach bol|सख़्त ऑडिट: सच बोलो","actual" to "actual minutes||असली मिनट","q1" to "Full focus|Poore focus se|पूरे फ़ोकस से","q2" to "Half attention|Aadha dhyaan|आधा ध्यान","q3" to "Just going through motions|Bas aise hi|बस यूँ ही","q4" to "Couldn't do it|Nahi kar paya|नहीं कर पाया",
  "submit" to "Submit||जमा करो","ok" to "Ok||ठीक है","nostrict" to "No strict task|Strict task nahi hai|कोई सख़्त टास्क नहीं","auddone" to "Today's audit is done|Aaj ka audit ho chuka|आज का ऑडिट हो चुका","audres" to "Audit result|Audit result|ऑडिट परिणाम",
  "passmsg" to "You were truly locked in today, {n}. Streak moves on.|Aaj tu sach me locked in tha, {n}. Streak aage badh gayi.|आज तुम सच में लॉक्ड इन थे, {n}। स्ट्रीक आगे बढ़ी।","failed" to "Failed|Fail|फ़ेल","shield" to "Shield used: streak saved, but the punishment applies.|Shield use hua: streak bach gayi, par saza lagegi.|शील्ड लगी: स्ट्रीक बची, पर सज़ा लगेगी।",
  "punline" to "Punishment: {a} locked for {h} hours.|Punishment: {a} {h} ghante ke liye locked.|सज़ा: {a} {h} घंटे के लिए बंद।","punhint" to "Add punishment apps, or the punishment stays half-done.|Punishment apps add kar, warna dant adhoori rahegi.|पनिशमेंट ऐप्स जोड़ो, वरना सज़ा अधूरी रहेगी।",
  "skiptitle" to "Skip this?|Skip karna hai?|छोड़ना है?","skipbody" to "Goal: {g}\n\nDo you really want to skip {t}? Write the real reason (6+ letters). Skip unlocks after 30 seconds.|Goal: {g}\n\nKya sach me {t} chhodna hai? Asli kaaran likh (6+ akshar). Skip 30 second baad khulega.|लक्ष्य: {g}\n\nक्या सच में {t} छोड़ना है? असली कारण लिखो (6+ अक्षर)। स्किप 30 सेकंड बाद खुलेगा।",
  "reason" to "Reason|Kaaran|कारण","nokeep" to "No, I'll do it|Nahi, kar raha hu|नहीं, मैं करूँगा","skipbtn" to "Skip||छोड़ो","needreason" to "Write a reason (6+ letters)|Kaaran likh (6+ akshar)|कारण लिखो (6+ अक्षर)",
  "start_a" to "Start||शुरू","skip_a" to "Not today|Aaj nahi|आज नहीं","audnote" to "Time for your day audit|Din ke audit ka time|दिन के ऑडिट का समय","audnote2" to "Tell the truth: what did you do today? Tap.|Sach bata, aaj kya kiya? Tap kar.|सच बताओ, आज क्या किया? दबाओ।",
  "i_none" to "Not enough data yet. Do a few audits and your pattern shows here.|Data bachha hai. Kuch din audit kar, phir tera pattern yahan dikhega.|अभी डेटा कम है। कुछ दिन ऑडिट करो, फिर पैटर्न यहाँ दिखेगा।","i_task" to "Weakest task: {x} ({p}% fail)|Sabse kamzor task: {x} ({p}% fail)|सबसे कमज़ोर टास्क: {x} ({p}% फ़ेल)","i_day" to "Weakest day: {x} ({p}% fail)|Sabse kamzor din: {x} ({p}% fail)|सबसे कमज़ोर दिन: {x} ({p}% फ़ेल)","i_skips" to "Skips: {n}. Last excuse: {r}|Skips: {n}. Last bahana: {r}|स्किप: {n}। आख़िरी बहाना: {r}","i_ok" to "No weak pattern yet. Keep going.|Abhi koi kamzor pattern nahi dikha. Aise hi chalta reh.|अभी कोई कमज़ोर पैटर्न नहीं। ऐसे ही चलो।",
  "b_lock" to "{a} is locked. Focus on {g}.|{a} locked hai. {g} pe dhyaan de.|{a} बंद है। {g} पर ध्यान दो।","b_focus" to "Deep work is on. Finish the task first.|Deep work chal raha hai. Pehle task khatam kar.|डीप वर्क चालू है। पहले टास्क पूरा करो।","b_sun" to "Digital sunset: sleep now, tomorrow awaits.|Digital sunset: ab so ja, kal ke liye.|डिजिटल सनसेट: अब सो जाओ, कल के लिए।")
}
