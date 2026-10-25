package com.lockedin
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class Blocker:AccessibilityService(){
 private val safe=listOf("com.android.settings","com.android.phone","com.android.dialer","com.google.android.dialer","com.android.systemui","com.android.emergency")
 override fun onAccessibilityEvent(e:AccessibilityEvent){
  if(e.eventType!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)return
  val p=e.packageName?.toString()?:return
  if(p==packageName||safe.contains(p))return
  val r=Store.reason(this,p)?:return
  performGlobalAction(GLOBAL_ACTION_HOME)
  val m=when(r){"lock"->L.f(this,"b_lock","a" to Store.label(this,p),"g" to Store.goal(this));"focus"->L.t(this,"b_focus");else->L.t(this,"b_sun")}
  Toast.makeText(this,m,Toast.LENGTH_LONG).show()}
 override fun onInterrupt(){}
}
