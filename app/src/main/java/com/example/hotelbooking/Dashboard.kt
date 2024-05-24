package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelbooking.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class Dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    true
                }
                R.id.myTrips -> {
                    startActivity(Intent(this, MyTripsActivity::class.java))
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.searchButton.setOnClickListener {
            val intent = Intent(this, SearchResults::class.java)
            startActivity(intent)
        }
        binding.seeAllText.setOnClickListener {
            val intent = Intent(this, SearchResults::class.java)
            startActivity(intent)
        }
        binding.hotel1.setOnClickListener {
            val intent = Intent(this, HotelPage::class.java)
            startActivity(intent)
        }
        binding.hotel2.setOnClickListener {
            val intent = Intent(this, HotelPage::class.java)
            startActivity(intent)
        }
        binding.hotel3.setOnClickListener {
            val intent = Intent(this, HotelPage::class.java)
            startActivity(intent)
        }
    }
}