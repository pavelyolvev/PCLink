package com.example.pclink

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
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
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.pclink.databinding.ActivityMainBinding
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread
import androidx.core.view.size
import androidx.core.view.get

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.PCStreamFragment || destination.id == R.id.loadingScreenFragment){
                binding.toolbar.visibility = View.GONE
            } else {
                binding.toolbar.visibility = View.VISIBLE

            }
            // Показывать FAB только на PCSelectFragment
            if (destination.id == R.id.PCSelectFragment) {
                binding.fab.show()
            } else {
                binding.fab.hide()
            }
        }

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        binding.fab.setOnClickListener { view ->
            val bundle = Bundle().apply {
                putInt("pcId", -1)
            }
            navController.navigate(R.id.action_PCSelectFragment_to_PCSettingsFragment, bundle)

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
        for (i in 0 until menu.size) {
            menu[i].icon?.setTint(ContextCompat.getColor(this, R.color.white))
        }
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
//    override fun onBackPressed() {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        val currentFragment = navController.findDestination(R.id.PCStreamFragment)
//
//        if (navController.currentDestination) {
//            // Выполнить пользовательское поведение кнопки «Назад»
//        } else {
//            super.onBackPressed()
//        }
//    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}