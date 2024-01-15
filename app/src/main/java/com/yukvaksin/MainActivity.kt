package com.yukvaksin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yukvaksin.ui.home.HomeFragment
import com.yukvaksin.ui.maps.MapsFragment
import com.yukvaksin.ui.statusvaksin.StatusVaksinasiFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    openFragment(HomeFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.maps -> {
                    openFragment(MapsFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.statusvaksin -> {
                    openFragment(StatusVaksinasiFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> return@setOnNavigationItemSelectedListener false
            }
        }

        val defaultFragment = HomeFragment()
        openFragment(defaultFragment)
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}