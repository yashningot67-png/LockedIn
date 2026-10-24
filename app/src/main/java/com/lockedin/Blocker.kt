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
  val u=Store.locks(this).optLong(p,0)
  if(u>System.currentTimeMillis()){performGlobalAction(GLOBAL_ACTION_HOME)
   Toast.makeText(this,Store.label(this,p)+" locked hai. "+Store.goal(this)+" pe dhyaan de.",Toast.LENGTH_LONG).show()}}
 override fun onInterrupt(){}
}
