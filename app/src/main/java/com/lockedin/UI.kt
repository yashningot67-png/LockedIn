package com.lockedin
import android.animation.*
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.DecelerateInterpolator

var RED=0xFFFF3B30.toInt();var EMBER=0xFFFF8A1F.toInt();var BG=0xFF0A0A0B.toInt();var CARD=0xFF141416.toInt();var PREMIUM=false;var REDUCE=false
val GREEN=0xFF34C77B.toInt();val AMBER=0xFFE8A33D.toInt();var MUT=0xFF8E8E96.toInt();var TX=0xFFF4F4F5.toInt();var LINE=0xFF232326.toInt()
var TRACK=0xFF26262A.toInt();var OUT=0xFF2C2C30.toInt();var MISS=0xFF3A3A40.toInt();var EMPTY=0xFF1A1A1D.toInt();var LIGHT=false
val PAL=arrayOf(longArrayOf(0xFFFF3B30,0xFFFF8A1F,0xFF141416,0xFF0A0A0B),longArrayOf(0xFFFF3B30,0xFFFF8F26,0xFF151517,0xFF0A0A0B),longArrayOf(0xFFFF4538,0xFFFF9A3C,0xFF16171B,0xFF090A0C),longArrayOf(0xFFF7284A,0xFFFF7A59,0xFF181417,0xFF0A0809),
 longArrayOf(0xFFE5173F,0xFFFF6B3D,0xFF191214,0xFF0B0809),longArrayOf(0xFFD81B60,0xFFFF5F6D,0xFF181218,0xFF0B0709),longArrayOf(0xFFE0A526,0xFFFFD76A,0xFF181610,0xFF0B0A07),longArrayOf(0xFFF2B01E,0xFFFFE08A,0xFF1A1710,0xFF0B0A06),
 longArrayOf(0xFFFF9E1B,0xFFFFD54F,0xFF1A1812,0xFF0B0A08),longArrayOf(0xFFB0278F,0xFFFF4081,0xFF16101A,0xFF08060B),longArrayOf(0xFFFFC53D,0xFFFF4D2D,0xFF181410,0xFF070504))
fun applyTheme(i:Int,light:Boolean=false){LIGHT=light
 if(light){RED=0xFFD92D20.toInt();EMBER=0xFFF79009.toInt();CARD=0xFFFFFFFF.toInt();BG=0xFFF4F5F7.toInt();TX=0xFF111214.toInt();MUT=0xFF6B6F76.toInt();LINE=0xFFE3E5E9.toInt();TRACK=0xFFE4E6EA.toInt();OUT=0xFFD5D8DD.toInt();MISS=0xFFC9CDD3.toInt();EMPTY=0xFFECEEF1.toInt();PREMIUM=false;return}
 val p=PAL[i.coerceIn(0,10)];RED=p[0].toInt();EMBER=p[1].toInt();CARD=p[2].toInt();BG=p[3].toInt();PREMIUM=i>=10
 TX=0xFFF4F4F5.toInt();MUT=0xFF8E8E96.toInt();LINE=0xFF232326.toInt();TRACK=0xFF26262A.toInt();OUT=0xFF2C2C30.toInt();MISS=0xFF3A3A40.toInt();EMPTY=0xFF1A1A1D.toInt()}
fun lite(c:Int)=Color.rgb(minOf(255,Color.red(c)+70),minOf(255,Color.green(c)+70),minOf(255,Color.blue(c)+70))
fun Context.dp(v:Float)=v*resources.displayMetrics.density
private fun cond(sz:Float,col:Int)=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=col;textSize=sz;textAlign=Paint.Align.CENTER;typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)}
private fun anim(f:(Float)->Unit,ms:Long)=ValueAnimator.ofFloat(0f,1f).apply{duration=ms;interpolator=DecelerateInterpolator();addUpdateListener{f(it.animatedValue as Float)}}.start()

class RingView(c:Context):View(c){
 var label="TODAY";private var shown=0f;private var target=-1f
 private val bg=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;color=0xFF26262A.toInt()}
 private val fg=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND}
 private val big=cond(c.dp(30f),TX);private val sm=cond(c.dp(11f),MUT)
 fun set(p:Float){if(p==target)return;target=p;val from=shown;ValueAnimator.ofFloat(from,p).apply{duration=900;interpolator=DecelerateInterpolator();addUpdateListener{shown=it.animatedValue as Float;invalidate()}}.start()}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val s=context.dp(11f);bg.strokeWidth=s;fg.strokeWidth=s;fg.color=RED;bg.color=TRACK;big.color=TX;sm.color=MUT
  val r=RectF(s,s,w-s,h-s);cv.save();cv.rotate(-90f,w/2,h/2);cv.drawArc(r,0f,360f,false,bg);if(shown>0f)cv.drawArc(r,0f,360f*shown/100f,false,fg);cv.restore()
  cv.drawText(shown.toInt().toString()+"%",w/2,h/2+context.dp(8f),big);cv.drawText(label,w/2,h/2+context.dp(28f),sm)}
}

class FlameView(c:Context):View(c){
 var tint=0;private var n=-1;private var shown=0
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val tp=cond(c.dp(22f),Color.WHITE);private val sp=Paint(Paint.ANTI_ALIAS_FLAG)
 init{setLayerType(LAYER_TYPE_SOFTWARE,null)}
 private fun tg(w:Float,h:Float,cx:Float,wd:Float,ht:Float,t:Float,ph:Float,lean:Float):Path{
  val a=Math.sin(t*2.9+ph).toFloat();val b=Math.sin(t*4.6+ph*1.7).toFloat();val f=Math.sin(t*7.1+ph).toFloat()
  val tx=cx+wd*(.16f*a+lean);val ty=1f-ht*(.92f+.08f*f);val pa=Path()
  pa.moveTo((cx-wd/2)*w,h);pa.cubicTo((cx-wd*.66f)*w,(1f-ht*.42f)*h,(cx-wd*.22f+.04f*b*wd)*w,(1f-ht*.64f)*h,tx*w,ty*h)
  pa.cubicTo((cx+wd*.24f+.04f*b*wd)*w,(1f-ht*.6f)*h,(cx+wd*.66f)*w,(1f-ht*.40f)*h,(cx+wd/2)*w,h);pa.close();return pa}
 fun set(v:Int,pop:Boolean){if(v==n&&!pop)return;val from=if(pop)maxOf(0,v-1) else shown;n=v
  ValueAnimator.ofInt(from,v).apply{duration=700;addUpdateListener{shown=it.animatedValue as Int;invalidate()}}.start()
  if(pop)animate().scaleX(1.25f).scaleY(1.25f).setDuration(220).withEndAction{animate().scaleX(1f).scaleY(1f).setDuration(300).start()}.start()}
 private fun paint(h:Float,top:Int,bot:Int,blur:Float,alpha:Int){p.shader=LinearGradient(0f,0f,0f,h,top,bot,Shader.TileMode.CLAMP);p.alpha=alpha;p.maskFilter=if(blur>0f)BlurMaskFilter(blur,BlurMaskFilter.Blur.NORMAL) else null}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val on=n>0
  val t=if(REDUCE)0f else (System.nanoTime()/1e9f)*(if(on)1f else .35f)
  p.style=Paint.Style.FILL
  if(on){paint(h,EMBER,RED,w*.03f,235);cv.drawPath(tg(w,h,.27f,.42f,.58f,t,1.3f,-.1f),p);cv.drawPath(tg(w,h,.74f,.40f,.50f,t,2.6f,.1f),p)
   paint(h,RED,EMBER,w*.025f,255);cv.drawPath(tg(w,h,.5f,.80f,.98f,t,0f,0f),p)
   paint(h,EMBER,0xFFFFC23A.toInt(),w*.018f,255);cv.drawPath(tg(w,h,.5f,.58f,.72f,t,.8f,0f),p)
   paint(h,0xFFFFF0B0.toInt(),0xFFFFFFFF.toInt(),w*.012f,255);cv.drawPath(tg(w,h,.5f,.34f,.46f,t,1.6f,0f),p)
   p.shader=null;p.maskFilter=BlurMaskFilter(w*.04f,BlurMaskFilter.Blur.NORMAL);p.color=0x884FC3F7.toInt();cv.drawOval(RectF(w*.36f,h*.86f,w*.64f,h*.99f),p)
   if(!REDUCE){p.maskFilter=null;val cnt=2+(tint/2);for(i in 0 until cnt){val ph=((t*.55f+i*.41f)%1f);sp.color=Color.argb(((1f-ph)*230).toInt(),255,(190-ph*80).toInt(),70);cv.drawCircle(w*(.5f+(Math.sin(i*2.3+t*2.0)*.26*ph).toFloat()),h*(.82f-ph*.85f),w*.022f*(1f-ph*.5f),sp)}}
  }else{paint(h,0xFF8A5A3A.toInt(),0xFF5A3822.toInt(),w*.02f,170);cv.drawPath(tg(w,h,.5f,.80f,.98f,t,0f,0f),p);paint(h,0xFFB07A4A.toInt(),0xFF8A5A3A.toInt(),w*.015f,190);cv.drawPath(tg(w,h,.5f,.58f,.70f,t,.8f,0f),p)
   paint(h,0xFFD9B98A.toInt(),0xFFB89466.toInt(),w*.01f,210);cv.drawPath(tg(w,h,.5f,.32f,.44f,t,1.6f,0f),p)}
  p.shader=null;p.maskFilter=null;p.alpha=255
  tp.color=if(on)0xFF5A1A00.toInt() else 0xFFFFE9CC.toInt();cv.drawText(shown.toString(),w/2,h*.88f,tp)
  if(!REDUCE)postInvalidateOnAnimation()}
}

class BarsView(c:Context):View(c){
 private var d=IntArray(7){-1};private var lb=List(7){""};private var prog=0f;private var played=false
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val lp=cond(c.dp(11f),MUT).apply{typeface=Typeface.DEFAULT};private val vp=cond(c.dp(10f),TX).apply{typeface=Typeface.DEFAULT}
 fun set(v:IntArray,l:List<String>){val ch=!v.contentEquals(d);d=v;lb=l;if(ch||!played){played=true;anim({prog=it;invalidate()},700)}}
 override fun onMeasure(ws:Int,hs:Int){setMeasuredDimension(MeasureSpec.getSize(ws),context.dp(150f).toInt())}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val g=context.dp(10f);val bw=(w-g*6)/7;val ch=h-context.dp(22f);val top=context.dp(16f);val rr=context.dp(6f)
  for(i in 0 until 7){val x=i*(bw+g);p.color=EMPTY;vp.color=TX;lp.color=MUT;cv.drawRoundRect(RectF(x,top,x+bw,ch),rr,rr,p)
   val v=d[i];if(v>=0){val bh=maxOf(context.dp(4f),(ch-top)*v/100f*prog);p.color=if(v>=80)GREEN else if(v>=50)AMBER else RED;cv.drawRoundRect(RectF(x,ch-bh,x+bw,ch),rr,rr,p);cv.drawText(v.toString()+"%",x+bw/2,ch-bh-context.dp(4f),vp)}
   cv.drawText(lb[i],x+bw/2,h-context.dp(4f),lp)}}
}

class HeatView(c:Context):View(c){
 private var d=IntArray(371){-1};private var prog=0f;private var played=false;private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 fun set(v:IntArray){val ch=!v.contentEquals(d);d=v;if(ch||!played){played=true;anim({prog=it;invalidate()},1000)}}
 override fun onMeasure(ws:Int,hs:Int){val w=MeasureSpec.getSize(ws);setMeasuredDimension(w,(w/53f*7).toInt())}
 override fun onDraw(cv:Canvas){val cell=width/53f;val g=cell*.18f;val r=cell*.22f
  for(i in 0 until 371){val col=i/7;val row=i%7;if(col>prog*53)continue;val v=d[i]
   p.color=if(v<0)EMPTY else if(v>=80)GREEN else if(v>=40)AMBER else MISS
   cv.drawRoundRect(RectF(col*cell,row*cell,col*cell+cell-g,row*cell+cell-g),r,r,p)}}
}

class RankEmblem(c:Context,private val idx:Int,private val state:Int):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 private fun poly(cv:Canvas,vararg pts:Float){val pa=Path();for(i in pts.indices step 2){if(i==0)pa.moveTo(pts[0],pts[1]) else pa.lineTo(pts[i],pts[i+1])};pa.close();cv.drawPath(pa,p)}
 override fun onDraw(cv:Canvas){val w=width/2f;val h=height/2f;val r=minOf(w,h)-context.dp(2f);val hx=Path()
  for(k in 0 until 6){val a=Math.toRadians(60.0*k-90);val x=(w+r*Math.cos(a)).toFloat();val y=(h+r*Math.sin(a)).toFloat();if(k==0)hx.moveTo(x,y) else hx.lineTo(x,y)};hx.close()
  p.style=Paint.Style.FILL;p.shader=null;p.color=CARD;cv.drawPath(hx,p);p.style=Paint.Style.STROKE;p.strokeWidth=context.dp(1.5f);p.color=if(state==2)RED else OUT;cv.drawPath(hx,p)
  val col=when(state){2->RED;1->(if(LIGHT)0xFF7A808A.toInt() else 0xFFC9CDD6.toInt());else->MISS};p.color=col;p.strokeWidth=.07f;p.strokeCap=Paint.Cap.ROUND
  cv.save();cv.translate(w,h);val u=r*1.05f;cv.scale(u,u)
  p.style=Paint.Style.FILL
  when(idx){0->{cv.drawOval(RectF(-.34f,-.05f,-.1f,.4f),p);cv.drawOval(RectF(.08f,-.4f,.32f,.05f),p)}
   1->{val f=Path();f.moveTo(0f,-.5f);f.cubicTo(.12f,-.28f,.4f,-.12f,.4f,.18f);f.cubicTo(.4f,.38f,.22f,.5f,0f,.5f);f.cubicTo(-.22f,.5f,-.4f,.38f,-.4f,.18f);f.cubicTo(-.4f,0f,-.25f,-.08f,-.2f,-.22f);f.cubicTo(-.12f,-.1f,-.06f,-.2f,0f,-.5f);f.close();cv.drawPath(f,p)}
   2->poly(cv,.12f,-.5f,-.26f,.06f,-.02f,.06f,-.12f,.5f,.28f,-.12f,.03f,-.12f)
   3->{val s=Path();s.moveTo(0f,-.5f);s.lineTo(.38f,-.36f);s.lineTo(.38f,.04f);s.quadTo(.36f,.34f,0f,.5f);s.quadTo(-.36f,.34f,-.38f,.04f);s.lineTo(-.38f,-.36f);s.close();p.style=Paint.Style.STROKE;cv.drawPath(s,p);p.style=Paint.Style.FILL;cv.drawRect(-.04f,-.3f,.04f,.3f,p)}
   4->{p.style=Paint.Style.STROKE;cv.drawLine(-.38f,-.38f,.34f,.34f,p);cv.drawLine(.38f,-.38f,-.34f,.34f,p);cv.drawLine(-.3f,.16f,-.16f,.3f,p);cv.drawLine(.3f,.16f,.16f,.3f,p)}
   5->{p.style=Paint.Style.STROKE;cv.drawCircle(0f,0f,.4f,p);cv.drawCircle(0f,0f,.22f,p);p.style=Paint.Style.FILL;cv.drawCircle(0f,0f,.07f,p);p.style=Paint.Style.STROKE;cv.drawLine(0f,0f,.46f,-.46f,p)}
   6->poly(cv,-.44f,.32f,-.44f,-.24f,-.2f,0f,0f,-.42f,.2f,0f,.44f,-.24f,.44f,.32f)
   7->{val st=Path();for(k in 0 until 16){val a=Math.toRadians(22.5*k-90);val rr=if(k%4==0).5 else if(k%2==0).3 else .1;val x=(rr*Math.cos(a)).toFloat();val y=(rr*Math.sin(a)).toFloat();if(k==0)st.moveTo(x,y) else st.lineTo(x,y)};st.close();cv.drawPath(st,p)}
   8->{poly(cv,-.5f,.4f,-.16f,-.18f,.04f,.12f,.2f,-.36f,.5f,.4f)}
   9->{poly(cv,0f,-.5f,.1f,.1f,-.1f,.1f);cv.drawRect(-.26f,.1f,.26f,.18f,p);cv.drawRect(-.04f,.18f,.04f,.38f,p);cv.drawCircle(0f,.43f,.06f,p)}
   else->{val st=Path();for(k in 0 until 10){val a=Math.toRadians(36.0*k-90);val rr=if(k%2==0).46 else .19;val x=(rr*Math.cos(a)).toFloat();val y=(rr*Math.sin(a)).toFloat();if(k==0)st.moveTo(x,y) else st.lineTo(x,y)};st.close();cv.drawPath(st,p)}}
  cv.restore()}
}

class TargetIcon(c:Context):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(cv:Canvas){val w=width/2f;val h=height/2f;val r=minOf(w,h)-context.dp(2f);p.style=Paint.Style.STROKE;p.strokeWidth=context.dp(1.5f);p.color=RED;cv.drawCircle(w,h,r,p)
  p.style=Paint.Style.FILL;cv.drawCircle(w,h,r*.5f,p);p.color=lite(RED);cv.drawCircle(w,h,r*.22f,p);p.style=Paint.Style.STROKE;p.strokeWidth=context.dp(2f);p.color=Color.WHITE;cv.drawLine(w,h,w+r*.8f,h-r*.8f,p)}
}
