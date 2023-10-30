package com.tridya.ebookhaven

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.tridya.ebookhaven.base.BaseActivity
import com.tridya.ebookhaven.databinding.ActivityMainBinding
import com.tridya.ebookhaven.models.book.BookInfo
import com.tridya.ebookhaven.utils.FindTitle
import com.tridya.ebookhaven.utils.visible
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : BaseActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

        navController = navHostFragment.navController
//        navController = findNavController(R.id.navHostFragment)
        navController.addOnDestinationChangedListener(this)
        binding.bottomTabBar.setSelectedWithId(R.id.home, false)

        binding.bottomTabBar.addBubbleListener { id ->
            when (id) {
                R.id.home -> {
                    navController.popBackStack()
                    navController.navigate(R.id.fragmentHome)
                }

                R.id.library -> {
                    navController.popBackStack()
                    navController.navigate(R.id.fragmentLibrary)
                }

                R.id.settings -> {
                    navController.popBackStack()
                    navController.navigate(R.id.fragmentSettings)
                }
            }
        }
    }

    private fun showBottomTab() {
        binding.bottomTabBar.visible()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        when (navController.currentDestination?.id) {
            R.id.fragmentHome -> {
                showBottomTab()
                binding.bottomTabBar.setSelectedWithId(R.id.home, false)
            }

            R.id.fragmentLibrary -> {
                showBottomTab()
                binding.bottomTabBar.setSelectedWithId(R.id.library, false)
            }

            R.id.fragmentSettings -> {
                showBottomTab()
                binding.bottomTabBar.setSelectedWithId(R.id.settings, false)
            }
        }
    }
}