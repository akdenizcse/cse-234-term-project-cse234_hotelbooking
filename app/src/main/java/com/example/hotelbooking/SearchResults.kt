package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hotelbooking.databinding.ActivitySearchResultsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class SearchResults : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var database: FirebaseDatabase
    private var searchQuery: String? = null
    private var departDate: String? = null
    private var returnDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        searchQuery = intent.getStringExtra("searchQuery")?.trim()?.toLowerCase()
        departDate = intent.getStringExtra("departDate")
        returnDate = intent.getStringExtra("returnDate")

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

        loadSearchResults()
    }

    private fun loadSearchResults() {
        database.reference.child("hotels").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hotelsList = mutableListOf<Hotel>()
                for (hotelSnapshot in snapshot.children) {
                    val hotelId = hotelSnapshot.key ?: ""
                    val hotelName = hotelSnapshot.child("name").getValue(String::class.java)
                    val hotelLocation = hotelSnapshot.child("location").getValue(String::class.java)
                    val hotelImageUrl = hotelSnapshot.child("imageUrl").getValue(String::class.java)

                    if (hotelName != null && hotelLocation != null && hotelImageUrl != null) {
                        if (searchQuery == null || hotelLocation.toLowerCase().contains(searchQuery!!)) {
                            hotelsList.add(Hotel(hotelId, hotelName, hotelLocation, hotelImageUrl))
                        }
                    }
                }
                hotelsList.reverse()
                hotelsList.forEach {
                    addHotelToLayout(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun addHotelToLayout(hotel: Hotel) {
        val hotelLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            contentDescription = "Hotel named ${hotel.name} located at ${hotel.location}"
            setOnClickListener {
                val intent = Intent(this@SearchResults, HotelPage::class.java)
                intent.putExtra("hotelId", hotel.id)
                intent.putExtra("departDate", departDate)
                intent.putExtra("returnDate", returnDate)
                startActivity(intent)
            }
            setPadding(0, 20, 0, 20)
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                300, 300
            )
            Glide.with(this@SearchResults).load(hotel.imageUrl).into(this)
            contentDescription = "Image of ${hotel.name}"
        }

        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val nameView = TextView(this).apply {
            text = hotel.name
            textSize = 20f
            setPadding(16, 16, 16, 8)
        }

        val locationView = TextView(this).apply {
            text = hotel.location
            textSize = 16f
            setPadding(16, 8, 16, 8)
        }

        val cheapestRoomPriceView = TextView(this).apply {
            text = "Cheapest room: $0" // Başlangıçta fiyat bilgisi yoksa varsayılan değer
            textSize = 16f
            setPadding(16, 8, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 16)
                gravity = android.view.Gravity.END or android.view.Gravity.BOTTOM
            }
        }

        infoLayout.addView(nameView)
        infoLayout.addView(locationView)
        infoLayout.addView(cheapestRoomPriceView)

        hotelLayout.addView(imageView)
        hotelLayout.addView(infoLayout)

        binding.hotelListContainer.addView(hotelLayout)

        // Otelin en ucuz odasının fiyatını yükle
        loadCheapestRoomPrice(hotel.id, cheapestRoomPriceView)

        // Liste elemanları arasına gri ince bir çizgi ekle
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(0, 0, 0, 20)
            }
            setBackgroundColor(android.graphics.Color.GRAY)
        }

        binding.hotelListContainer.addView(divider)
    }

    private fun loadCheapestRoomPrice(hotelId: String, cheapestRoomPriceView: TextView) {
        database.reference.child("hotels").child(hotelId).child("rooms")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var cheapestPrice = Long.MAX_VALUE // Başlangıçta maksimum bir değer alır
                    for (roomSnapshot in snapshot.children) {
                        try {
                            val roomPriceLong = roomSnapshot.child("price").getValue(Long::class.java)
                            roomPriceLong?.let {
                                if (it < cheapestPrice) {
                                    cheapestPrice = it
                                }
                            }
                        } catch (e: Exception) {
                            val roomPriceString = roomSnapshot.child("price").getValue(String::class.java)
                            roomPriceString?.toLongOrNull()?.let {
                                if (it < cheapestPrice) {
                                    cheapestPrice = it
                                }
                            }
                        }
                    }
                    if (cheapestPrice != Long.MAX_VALUE) {
                        cheapestRoomPriceView.text = "${cheapestPrice.toDouble()}" // Long'u Double'a dönüştürerek göster
                    } else {
                        cheapestRoomPriceView.text = "" // Fiyat bulunamazsa "-" göster
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    data class Hotel(
        val id: String = "",
        val name: String = "",
        val location: String = "",
        val imageUrl: String = ""
    )
}
