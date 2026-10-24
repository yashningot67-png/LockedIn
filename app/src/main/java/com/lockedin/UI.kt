package com.lockedin
import android.animation.*
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.DecelerateInterpolator

val RED=0xFFFF2D2D.toInt();val EMBER=0xFFFF8A1F.toInt();val BG=0xFF0A0A0B.toInt();val CARD=0xFF151517.toInt();val MUT=0xFF8E8E96.toInt();val TX=0xFFF4F4F5.toInt()
fun Context.dp(v:Float)=v*resources.displayMetrics.density
private fun cond(sz:Float,col:Int)=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=col;textSize=sz;textAlign=Paint.Align.CENTER;typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)}
private fun anim(f:(Float)->Unit,ms:Long)=ValueAnimator.ofFloat(0f,1f).apply{duration=ms;interpolator=DecelerateInterpolator();addUpdateListener{f(it.animatedValue as Float)}}.start()

class RingView(c:Context):View(c){
 private var shown=0f;private var target=-1f
 private val bg=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;color=0xFF26262A.toInt()}
 private val fg=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND}
 private val big=cond(c.dp(44f),TX);private val sm=cond(c.dp(11f),MUT)
 fun set(p:Float){if(p==target)return;target=p;val from=shown;ValueAnimator.ofFloat(from,p).apply{duration=1200;interpolator=DecelerateInterpolator();addUpdateListener{shown=it.animatedValue as Float;invalidate()}}.start()}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val s=context.dp(13f);bg.strokeWidth=s;fg.strokeWidth=s
  fg.shader=SweepGradient(w/2,h/2,intArrayOf(EMBER,RED,EMBER),null)
  val r=RectF(s,s,w-s,h-s)
  cv.save();cv.rotate(-90f,w/2,h/2);cv.drawArc(r,0f,360f,false,bg);if(shown>0f)cv.drawArc(r,0f,360f*shown/100f,false,fg);cv.restore()
  cv.drawText(shown.toInt().toString()+"%",w/2,h/2+context.dp(10f),big);cv.drawText("TODAY",w/2,h/2+context.dp(32f),sm)}
}

class FlameView(c:Context):View(c){
 private var n=-1;private var shown=0;private var f=0f
 private val fl=Paint(Paint.ANTI_ALIAS_FLAG);private val inner=Paint(Paint.ANTI_ALIAS_FLAG);private val gl=Paint(Paint.ANTI_ALIAS_FLAG)
 private val tp=cond(c.dp(32f),Color.WHITE)
 private val path=Path().apply{moveTo(.5f,0f);cubicTo(.62f,.22f,.9f,.38f,.9f,.68f);cubicTo(.9f,.88f,.72f,1f,.5f,1f);cubicTo(.28f,1f,.1f,.88f,.1f,.68f);cubicTo(.1f,.5f,.25f,.42f,.3f,.28f);cubicTo(.38f,.4f,.42f,.3f,.5f,0f);close()}
 init{ValueAnimator.ofFloat(0f,1f).apply{duration=800;repeatMode=ValueAnimator.REVERSE;repeatCount=ValueAnimator.INFINITE;addUpdateListener{f=it.animatedValue as Float;invalidate()}}.start()}
 fun set(v:Int,pop:Boolean){if(v==n&&!pop)return;val from=if(pop)maxOf(0,v-1) else shown;n=v
  ValueAnimator.ofInt(from,v).apply{duration=800;addUpdateListener{shown=it.animatedValue as Int;invalidate()}}.start()
  if(pop)animate().scaleX(1.3f).scaleY(1.3f).setDuration(250).withEndAction{animate().scaleX(1f).scaleY(1f).setDuration(350).start()}.start()}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val on=n>0
  val fw=w*.8f;val fh=h*.92f
  if(on){gl.shader=RadialGradient(w/2,h*.6f,w*(.5f+.15f*f),0x66FF4A1F,0x00FF2D2D,Shader.TileMode.CLAMP);cv.drawCircle(w/2,h*.6f,w*.75f,gl)}
  fl.shader=if(on)LinearGradient(0f,0f,0f,1f,intArrayOf(0xFFFFC23A.toInt(),EMBER,RED),null,Shader.TileMode.CLAMP) else null
  fl.color=0xFF2E2E33.toInt();inner.color=if(on)0xFFFFE08A.toInt() else 0xFF3A3A40.toInt()
  val sy=if(on)1f+.07f*f else 1f;val sx=if(on)1f-.04f*f else 1f
  cv.save();cv.translate(w/2,h);cv.scale(sx,sy);cv.translate(-fw/2,-fh);cv.scale(fw,fh)
  cv.drawPath(path,fl);cv.save();cv.translate(.225f,.42f);cv.scale(.55f,.55f);cv.drawPath(path,inner);cv.restore();cv.restore()
  tp.color=if(on)Color.parseColor("#7A1200") else MUT
  cv.drawText(shown.toString(),w/2,h*.84f,tp)}
}

class BarsView(c:Context):View(c){
 private var d=IntArray(7){-1};private var lb=List(7){""};private var prog=0f;private var played=false
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val lp=cond(c.dp(11f),MUT).apply{typeface=Typeface.DEFAULT}
 fun set(v:IntArray,l:List<String>){val ch=!v.contentEquals(d);d=v;lb=l;if(ch||!played){played=true;anim({prog=it;invalidate()},900)}}
 override fun onMeasure(ws:Int,hs:Int){setMeasuredDimension(MeasureSpec.getSize(ws),context.dp(150f).toInt())}
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();val g=context.dp(10f);val bw=(w-g*6)/7;val ch=h-context.dp(22f);val rr=context.dp(7f)
  for(i in 0 until 7){val x=i*(bw+g);p.shader=null;p.color=0xFF1E1E21.toInt();cv.drawRoundRect(RectF(x,0f,x+bw,ch),rr,rr,p)
   val v=d[i];if(v>=0){val bh=maxOf(context.dp(5f),ch*v/100f*prog);p.shader=LinearGradient(0f,ch-bh,0f,ch,EMBER,RED,Shader.TileMode.CLAMP);cv.drawRoundRect(RectF(x,ch-bh,x+bw,ch),rr,rr,p)}
   cv.drawText(lb[i],x+bw/2,h-context.dp(4f),lp)}}
}

class HeatView(c:Context):View(c){
 private var d=IntArray(371){-1};private var prog=0f;private var played=false
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val ev=ArgbEvaluator()
 fun set(v:IntArray){val ch=!v.contentEquals(d);d=v;if(ch||!played){played=true;anim({prog=it;invalidate()},1400)}}
 override fun onMeasure(ws:Int,hs:Int){val w=MeasureSpec.getSize(ws);setMeasuredDimension(w,(w/53f*7).toInt())}
 override fun onDraw(cv:Canvas){val cell=width/53f;val g=cell*.18f;val r=cell*.25f
  for(i in 0 until 371){val col=i/7;val row=i%7;if(col>prog*53)continue;val v=d[i]
   p.color=if(v<0)0xFF1C1C1F.toInt() else ev.evaluate(v/100f,0xFF4A1212.toInt(),0xFFFF5A1F.toInt()) as Int
   cv.drawRoundRect(RectF(col*cell,row*cell,col*cell+cell-g,row*cell+cell-g),r,r,p)}}
}
