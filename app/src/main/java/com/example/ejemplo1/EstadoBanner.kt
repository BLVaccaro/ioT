package com.example.ejemplo1

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


object EstadoBanner : Application.ActivityLifecycleCallbacks {

    private const val TAG = "banner_estado"
    private val handler = Handler(Looper.getMainLooper())
    private val actualizadores = mutableMapOf<Activity, Runnable>()

    override fun onActivityResumed(a: Activity) {
        if (a.javaClass.simpleName == "Splash") return
        val banner = obtenerBanner(a)
        val r = object : Runnable {
            override fun run() {
                val estado = SyncManager.estado(a.applicationContext)
                if (estado == null) {
                    banner.visibility = View.GONE
                } else {
                    banner.text = estado.first
                    banner.setBackgroundColor(estado.second)
                    banner.visibility = View.VISIBLE
                }
                handler.postDelayed(this, 1500)
            }
        }
        actualizadores.remove(a)?.let { handler.removeCallbacks(it) }
        actualizadores[a] = r
        handler.post(r)
    }

    override fun onActivityPaused(a: Activity) {
        actualizadores.remove(a)?.let { handler.removeCallbacks(it) }
    }

    private fun obtenerBanner(a: Activity): TextView {
        val raiz = a.findViewById<ViewGroup>(android.R.id.content)
        (raiz.findViewWithTag<View>(TAG) as? TextView)?.let { return it }

        val tv = TextView(a).apply {
            tag = TAG
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(24, 10, 24, 10)
            elevation = 20f
            visibility = View.GONE
        }
        a.addContentView(tv, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))

        ViewCompat.setOnApplyWindowInsetsListener(tv) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val p = v.layoutParams as FrameLayout.LayoutParams
            if (p.bottomMargin != sb.bottom) {
                p.bottomMargin = sb.bottom
                v.layoutParams = p
            }
            insets
        }
        ViewCompat.requestApplyInsets(tv)
        return tv
    }

    override fun onActivityCreated(a: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(a: Activity) {}
    override fun onActivityStopped(a: Activity) {}
    override fun onActivitySaveInstanceState(a: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(a: Activity) {}
}