package com.tridya.readsphere

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.tridya.readsphere.base.BaseActivity
import com.tridya.readsphere.databinding.ActivityMainBinding
import com.tridya.readsphere.utils.visible

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