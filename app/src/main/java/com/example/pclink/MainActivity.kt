package com.example.pclink

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.example.pclink.databinding.ActivityMainBinding
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    private var lastX = 0f
    private var lastY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        binding.fab.setOnClickListener { view ->
            // Показ Snackbar'а
            Snackbar.make(view, "Команда отправляется...", Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.fab)
                .show()

            // Сетевые действия в отдельном потоке
            // Подключаемся к серверу
            thread {
                try {
                    socket = Socket("192.168.31.49", 12312)
                    writer = PrintWriter(socket!!.getOutputStream(), true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

//            val touchPad = findViewById<View>(R.id.touchPad)
//            Log.d("TouchPadDebug", "touchPad = $touchPad")
//
//            touchPad.setOnTouchListener { _, event ->
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        lastX = event.x
//                        lastY = event.y
//                    }
//
//                    MotionEvent.ACTION_MOVE -> {
//                        val dx = event.x - lastX
//                        val dy = event.y - lastY
//                        lastX = event.x
//                        lastY = event.y
//
//                        // Отправка команды MOVE
//                        sendCommand("MOVE:${dx.toInt()},${dy.toInt()}")
//                    }
//
//                    MotionEvent.ACTION_UP -> {
//                        // Отправка команды CLICK
//                        sendCommand("CLICK")
//                    }
//                }
//                true
//            }
        }
    }

        private fun sendCommand(command: String) {
            thread {
                try {
                    writer?.println(command)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            try {
                writer?.close()
                socket?.close()
            } catch (_: Exception) { }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}