package com.lockedin
import android.animation.*
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.DecelerateInterpolator

var RED=0xFFFF3B30.toInt();var EMBER=0xFFFF8A1F.toInt();var BG=0xFF0A0A0B.toInt();var CARD=0xFF141416.toInt();var PREMIUM=false
val GREEN=0xFF34C77B.toInt();val AMBER=0xFFE8A33D.toInt();val MUT=0xFF8E8E96.toInt();val TX=0xFFF4F4F5.toInt();val LINE=0xFF232326.toInt()
val PAL=arrayOf(longArrayOf(0xFFFF3B30,0xFFFF8A1F,0xFF141416,0xFF0A0A0B),longArrayOf(0xFFFF3B30,0xFFFF8F26,0xFF151517,0xFF0A0A0B),longArrayOf(0xFFFF4538,0xFFFF9A3C,0xFF16171B,0xFF090A0C),longArrayOf(0xFFF7284A,0xFFFF7A59,0xFF181417,0xFF0A0809),
 longArrayOf(0xFFE5173F,0xFFFF6B3D,0xFF191214,0xFF0B0809),longArrayOf(0xFFD81B60,0xFFFF5F6D,0xFF181218,0xFF0B0709),longArrayOf(0xFFE0A526,0xFFFFD76A,0xFF181610,0xFF0B0A07),longArrayOf(0xFFF2B01E,0xFFFFE08A,0xFF1A1710,0xFF0B0A06),
 longArrayOf(0xFFFF9E1B,0xFFFFD54F,0xFF1A1812,0xFF0B0A08),longArrayOf(0xFFB0278F,0xFFFF4081,0xFF16101A,0xFF08060B),longArrayOf(0xFFFFC53D,0xFFFF4D2D,0xFF181410,0xFF070504))
fun applyTheme(i:Int){val p=PAL[i.coerceIn(0,10)];RED=p[0].toInt();EMBER=p[1].toInt();CARD=p[2].toInt();BG=p[3].toInt();PREMIUM=i>=10}
fun lite(c:Int)=Color.rgb(minOf(255,Color.red(c)+70),minOf(255,Color.green(c)+70),minOf(255,Color.blue(c)+70))
fun Context.dp(v:Float)=v*resources.displayMetrics.density
private fun cond(sz:Float,col:Int)=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=col;textSize=sz;textAlign=Paint.Align.CENTER;typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)}
private fun anim(f:(Float)->Unit,ms:Long)=ValueAnimator.ofFloat(0f,1f).apply{duration=ms;interpolator=DecelerateInterpolator();addUpdateListener{f(it.animatedValue as Float)}}.start()

class RingView(c:Context):View(c){
 var label="TODAY";private var shown=0f;private var target=-1f
 private val bg=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;color=0xFF26262A.toInt()}
 private val fg=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND}
 private val big=cond(c.dp(40f),TX);private val sm=cond(c.dp(11f),MUT)
 fun set(p:Float){if(p==target)return;target=p;val from=shown;ValueAnimator.ofFloat(from,p).apply{duration=900;interpolator=DecelerateInterpolator();addUpdateListener{shown=it.animatedValue as Float;invalidate()}}.start()}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val s=context.dp(11f);bg.strokeWidth=s;fg.strokeWidth=s;fg.color=RED
  val r=RectF(s,s,w-s,h-s);cv.save();cv.rotate(-90f,w/2,h/2);cv.drawArc(r,0f,360f,false,bg);if(shown>0f)cv.drawArc(r,0f,360f*shown/100f,false,fg);cv.restore()
  cv.drawText(shown.toInt().toString()+"%",w/2,h/2+context.dp(8f),big);cv.drawText(label,w/2,h/2+context.dp(28f),sm)}
}

class FlameView(c:Context):View(c){
 var tint=0;private var n=-1;private var shown=0;private var f=0f
 private val fl=Paint(Paint.ANTI_ALIAS_FLAG);private val inner=Paint(Paint.ANTI_ALIAS_FLAG);private val tp=cond(c.dp(22f),Color.WHITE)
 private val path=Path().apply{moveTo(.5f,0f);cubicTo(.62f,.22f,.9f,.38f,.9f,.68f);cubicTo(.9f,.88f,.72f,1f,.5f,1f);cubicTo(.28f,1f,.1f,.88f,.1f,.68f);cubicTo(.1f,.5f,.25f,.42f,.3f,.28f);cubicTo(.38f,.4f,.42f,.3f,.5f,0f);close()}
 init{ValueAnimator.ofFloat(0f,1f).apply{duration=900;repeatMode=ValueAnimator.REVERSE;repeatCount=ValueAnimator.INFINITE;addUpdateListener{f=it.animatedValue as Float;invalidate()}}.start()}
 fun set(v:Int,pop:Boolean){if(v==n&&!pop)return;val from=if(pop)maxOf(0,v-1) else shown;n=v
  ValueAnimator.ofInt(from,v).apply{duration=700;addUpdateListener{shown=it.animatedValue as Int;invalidate()}}.start()
  if(pop)animate().scaleX(1.25f).scaleY(1.25f).setDuration(220).withEndAction{animate().scaleX(1f).scaleY(1f).setDuration(300).start()}.start()}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val on=n>0;val fw=w*.8f;val fh=h*.92f
  fl.shader=if(on)LinearGradient(0f,0f,0f,1f,intArrayOf(lite(EMBER),EMBER,RED),null,Shader.TileMode.CLAMP) else null
  fl.color=0xFF2E2E33.toInt();inner.color=if(on)0xFFFFE08A.toInt() else 0xFF3A3A40.toInt()
  val sy=if(on)1f+.05f*f else 1f;val sx=if(on)1f-.03f*f else 1f
  cv.save();cv.translate(w/2,h);cv.scale(sx,sy);cv.translate(-fw/2,-fh);cv.scale(fw,fh)
  cv.drawPath(path,fl);cv.save();cv.translate(.225f,.42f);cv.scale(.55f,.55f);cv.drawPath(path,inner);cv.restore();cv.restore()}
}

class BarsView(c:Context):View(c){
 private var d=IntArray(7){-1};private var lb=List(7){""};private var prog=0f;private var played=false
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val lp=cond(c.dp(11f),MUT).apply{typeface=Typeface.DEFAULT};private val vp=cond(c.dp(10f),TX).apply{typeface=Typeface.DEFAULT}
 fun set(v:IntArray,l:List<String>){val ch=!v.contentEquals(d);d=v;lb=l;if(ch||!played){played=true;anim({prog=it;invalidate()},700)}}
 override fun onMeasure(ws:Int,hs:Int){setMeasuredDimension(MeasureSpec.getSize(ws),context.dp(150f).toInt())}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val g=context.dp(10f);val bw=(w-g*6)/7;val ch=h-context.dp(22f);val top=context.dp(16f);val rr=context.dp(6f)
  for(i in 0 until 7){val x=i*(bw+g);p.color=0xFF1E1E21.toInt();cv.drawRoundRect(RectF(x,top,x+bw,ch),rr,rr,p)
   val v=d[i];if(v>=0){val bh=maxOf(context.dp(4f),(ch-top)*v/100f*prog);p.color=if(v>=80)GREEN else if(v>=50)AMBER else RED;cv.drawRoundRect(RectF(x,ch-bh,x+bw,ch),rr,rr,p);cv.drawText(v.toString()+"%",x+bw/2,ch-bh-context.dp(4f),vp)}
   cv.drawText(lb[i],x+bw/2,h-context.dp(4f),lp)}}
}

class HeatView(c:Context):View(c){
 private var d=IntArray(371){-1};private var prog=0f;private var played=false;private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 fun set(v:IntArray){val ch=!v.contentEquals(d);d=v;if(ch||!played){played=true;anim({prog=it;invalidate()},1000)}}
 override fun onMeasure(ws:Int,hs:Int){val w=MeasureSpec.getSize(ws);setMeasuredDimension(w,(w/53f*7).toInt())}
 override fun onDraw(cv:Canvas){val cell=width/53f;val g=cell*.18f;val r=cell*.22f
  for(i in 0 until 371){val col=i/7;val row=i%7;if(col>prog*53)continue;val v=d[i]
   p.color=if(v<0)0xFF1A1A1D.toInt() else if(v>=80)GREEN else if(v>=40)AMBER else 0xFF3A3A40.toInt()
   cv.drawRoundRect(RectF(col*cell,row*cell,col*cell+cell-g,row*cell+cell-g),r,r,p)}}
}

class RankEmblem(c:Context,private val num:Int,private val on:Boolean):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val t=cond(c.dp(13f),TX)
 override fun onDraw(cv:Canvas){val w=width/2f;val h=height/2f;val r=minOf(w,h)-context.dp(2f);val path=Path()
  for(k in 0 until 6){val a=Math.toRadians(60.0*k-90);val x=(w+r*Math.cos(a)).toFloat();val y=(h+r*Math.sin(a)).toFloat();if(k==0)path.moveTo(x,y) else path.lineTo(x,y)};path.close()
  p.style=Paint.Style.FILL;p.color=0xFF1C1C1F.toInt();cv.drawPath(path,p);p.style=Paint.Style.STROKE;p.strokeWidth=context.dp(1.5f);p.color=if(on)RED else 0xFF3A3A40.toInt();cv.drawPath(path,p)
  t.color=if(on)TX else MUT;cv.drawText("%02d".format(num),w,h+context.dp(4.5f),t)}
}
