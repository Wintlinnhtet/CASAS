package com.example.casas

import TimetableFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class activity_dashboard : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        replaceFragment(HomeFragment()) // Show HomeFragment by default


        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.timeTable-> {
                    replaceFragment(TimetableFragment())
                    true
                }
                R.id.courses -> {
                    replaceFragment(CoursesFragment())
                    true
                }
                else -> false
            }
        }



    }
    private fun replaceFragment(fragment: androidx.fragment.app.Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()


    }
}