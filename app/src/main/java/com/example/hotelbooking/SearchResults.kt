package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelbooking.databinding.ActivitySearchResultsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchResults : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, Dashboard::class.java))
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
        binding.hotel10.setOnClickListener {
            val intent = Intent(this, HotelPage::class.java)
            startActivity(intent)
        }
        binding.hotel11.setOnClickListener {
            val intent = Intent(this, HotelPage::class.java)
            startActivity(intent)
        }
        binding.hotel12.setOnClickListener {
            val intent = Intent(this, HotelPage::class.java)
            startActivity(intent)
        }
        binding.hotel13.setOnClickListener {
            val intent = Intent(this, HotelPage::class.java)
            startActivity(intent)
        }
    }
}