package com.guddu.aicallassistant

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }

        val title = TextView(this).apply {
            text = "Guddu AI Call Assistant"
            textSize = 22f
            setPadding(0, 0, 0, 40)
        }

        val btnPerm = Button(this).apply {
            text = "1. Permissions Allow Karein"
            setOnClickListener {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ANSWER_PHONE_CALLS,
                        Manifest.permission.CALL_PHONE
                    ),
                    101
                )
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            }
        }

        val btnTest = Button(this).apply {
            text = "2. Floating AI Button Test Karein"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startService(Intent(this@MainActivity, CallOverlayService::class.java))
                    Toast.makeText(this@MainActivity, "Screen par AI Button aa gaya!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Pehle Overlay Permission on karein", Toast.LENGTH_SHORT).show()
                }
            }
        }

        layout.addView(title)
        layout.addView(btnPerm)
        layout.addView(btnTest)
        setContentView(layout)
    }
}

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                context.startService(Intent(context, CallOverlayService::class.java))
            } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                context.stopService(Intent(context, CallOverlayService::class.java))
            }
        }
    }
}

class CallOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var aiButton: Button

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        aiButton = Button(this).apply {
            text = "🤖 AI Assistant ko dein"
            setBackgroundColor(0xFF00C853.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(45, 30, 45, 30)
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val tm = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                    if (ActivityCompat.checkSelfPermission(this@CallOverlayService, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                        tm.endCall()
                    }
                }
                stopSelf()
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            x = 20
        }

        windowManager.addView(aiButton, params)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::aiButton.isInitialized) windowManager.removeView(aiButton)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
