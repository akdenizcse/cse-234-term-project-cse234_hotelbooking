package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hotelbooking.databinding.ActivityHotelPageBinding
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HotelPage : AppCompatActivity() {
    private lateinit var binding: ActivityHotelPageBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var hotelId: String
    private var rooms: List<DataSnapshot> = listOf()
    private var roomListDialog: AlertDialog? = null
    private lateinit var departDate: String
    private lateinit var returnDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotelPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        hotelId = intent.getStringExtra("hotelId") ?: ""
        departDate = intent.getStringExtra("departDate") ?: ""
        returnDate = intent.getStringExtra("returnDate") ?: ""

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.roomListButton.setOnClickListener {
            showRoomsDialog()
        }

        binding.bookingNowButton.setOnClickListener {
            showRoomsDialog()
        }

        loadHotelData()
        loadComments()
        loadAverageRating()

        // departDate ve returnDate boş değilse, onları tarih formatına dönüştür
        if (departDate.isNotEmpty() && returnDate.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.parse(departDate)
                sdf.parse(returnDate)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }


    private fun isFullyBooked(departDate: String, returnDate: String, callback: (Boolean) -> Unit) {
        // Tüm booking yapılmış odaların ID'lerini alıyoruz
        val bookingsRef = database.reference.child("bookings").orderByChild("hotelId").equalTo(hotelId)

        // Rezervasyonların tarihlerini kontrol etmek için geçerli departman ve dönüş tarihlerini alıyoruz
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val depart = sdf.parse(departDate) ?: return
        val returnD = sdf.parse(returnDate) ?: return

        bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(bookingsSnapshot: DataSnapshot) {
                bookingsSnapshot.children.forEach { bookingSnapshot ->
                    val bookingRoomId = bookingSnapshot.child("roomId").getValue(String::class.java)
                    val bookingDepartDate = bookingSnapshot.child("departDate").getValue(String::class.java)
                    val bookingReturnDate = bookingSnapshot.child("returnDate").getValue(String::class.java)

                    if (bookingRoomId != null && bookingDepartDate != null && bookingReturnDate != null) {
                        // Kontrol edilen tarih aralığında ve aynı oda için booking varsa, oda tamamen doludur
                        if (isOverlap(depart, returnD, sdf.parse(bookingDepartDate)!!, sdf.parse(bookingReturnDate)!!)) {
                            callback(true)
                            return
                        }
                    }
                }
                callback(false)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(false)
            }
        })
    }

    private fun loadHotelData() {
        val hotelRef = database.reference.child("hotels").child(hotelId)
        val bookingsRef = database.reference.child("bookings").orderByChild("hotelId").equalTo(hotelId)

        hotelRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hotelName = snapshot.child("name").getValue(String::class.java) ?: ""
                val hotelImageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""

                binding.titleTxt.text = hotelName
                Glide.with(this@HotelPage).load(hotelImageUrl).into(binding.picDetail)

                rooms = snapshot.child("rooms").children.toList()

                // Tüm booking yapılmış odaların ID'lerini alıyoruz
                bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(bookingsSnapshot: DataSnapshot) {
                        val fullyBookedRoomIds = mutableListOf<String>()

                        bookingsSnapshot.children.forEach { bookingSnapshot ->
                            val bookingRoomId =
                                bookingSnapshot.child("roomId").getValue(String::class.java)
                            val bookingDepartDate =
                                bookingSnapshot.child("departDate").getValue(String::class.java)
                            val bookingReturnDate =
                                bookingSnapshot.child("returnDate").getValue(String::class.java)

                            // Tüm rezervasyonlar üzerinde kontrol yap
                            if (bookingRoomId != null && bookingDepartDate != null && bookingReturnDate != null) {
                                isFullyBooked(bookingDepartDate, bookingReturnDate) { fullyBooked ->
                                    if (fullyBooked) {
                                        val roomId = bookingSnapshot.child("roomId").getValue(String::class.java)
                                        if (roomId != null) {
                                            fullyBookedRoomIds.add(roomId)
                                        }
                                    }
                                }
                            }
                        }

                        // Booking yapılmamış ve dolu olmayan odaları bul
                        val availableRooms = rooms.filter { room ->
                            val roomId = room.key
                            roomId != null && roomId !in fullyBookedRoomIds
                        }

                        if (availableRooms.isNotEmpty()) {
                            showRoom(availableRooms[0])
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    private fun isOverlap(start1: Date, end1: Date, start2: Date, end2: Date): Boolean {
        return start1.before(end2) && start2.before(end1)
    }




    private fun showRoom(roomSnapshot: DataSnapshot) {
        val roomName = roomSnapshot.child("name").getValue(String::class.java) ?: ""
        val bathroomCount = roomSnapshot.child("bathroomCount").getValue(Int::class.java) ?: 0
        val bedCount = roomSnapshot.child("bedCount").getValue(Int::class.java) ?: 0
        val wifiAvailable =
            roomSnapshot.child("wifiAvailable").getValue(Boolean::class.java) ?: false
        val price = roomSnapshot.child("price").getValue(Double::class.java) ?: 0.0

        binding.roomNameTextView.text = roomName
        binding.bathroomCountTextView.text = "Bathroom Count: $bathroomCount"
        binding.bedCountTextView.text = "Bed Count: $bedCount"
        binding.wifiAvailabilityTextView.text =
            "WiFi Available: ${if (wifiAvailable) "Yes" else "No"}"
        binding.priceTextView.text = "Price: $$price"
    }

    private fun showRoomsDialog() {
        val sortedRooms = rooms.sortedBy { it.child("price").getValue(Double::class.java) ?: 0.0 }
        val roomIds = sortedRooms.map { it.key } // Odaların ID'lerini al

        val roomNamesAndPrices = sortedRooms.map {
            val roomName = it.child("name").getValue(String::class.java) ?: "Unknown"
            val price = it.child("price").getValue(Double::class.java) ?: 0.0
            "$roomName - $$price"
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select a Room")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roomNamesAndPrices)
        val listView = ListView(this)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRoomId = roomIds[position] // Seçilen oda ID'sini al
            showRoom(sortedRooms[position])
            roomListDialog?.dismiss() // Diyalog penceresini kapat

            // Booking Now butonunun tıklama dinleyicisini güncelle
            binding.bookingNowButton.setOnClickListener {
                isFullyBooked(departDate, returnDate) { fullyBooked ->
                    if (fullyBooked) {
                        // Odanın dolu olduğuna dair bir uyarı mesajı göster
                        AlertDialog.Builder(this)
                            .setTitle("Room Fully Booked")
                            .setMessage("The selected room is fully booked for the selected dates.")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        // Rezervasyon oluşturulacak Intent'e oda ID'sini ekle
                        val intent = Intent(this, PaymentActivity::class.java)
                        intent.putExtra("hotelId", hotelId)
                        intent.putExtra("roomId", selectedRoomId)

                        // Tarihleri intent'e ekliyoruz
                        intent.putExtra("departDate", departDate.toString())
                        intent.putExtra("returnDate", returnDate.toString())

                        startActivity(intent)
                    }
                }
            }

        }

        builder.setView(listView)
        roomListDialog = builder.create()
        roomListDialog?.show()
    }

    private fun loadComments() {
        val commentsRef =
            database.reference.child("comments").orderByChild("hotelId").equalTo(hotelId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments =  mutableListOf<String>()

                // Her bir yorumu işle
                snapshot.children.forEach { commentSnapshot ->
                    val commentText =
                        commentSnapshot.child("commentText").getValue(String::class.java) ?: ""
                    val rating = commentSnapshot.child("rating").getValue(Double::class.java) ?: 0.0
                    val userId =
                        commentSnapshot.child("userId").getValue(String::class.java) ?: "Anonymous"
                    // Kullanıcı adını al
                    val userRef = database.reference.child("users").child(userId)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userName = userSnapshot.child("name").getValue(String::class.java)
                                ?: "Anonymous"
                            val formattedComment = "$userName - Rating: $rating\n$commentText"
                            comments.add(formattedComment)

                            // Tüm yorumlar işlendikten sonra ListView'a set et
                            val adapter = ArrayAdapter(
                                this@HotelPage,
                                android.R.layout.simple_list_item_1,
                                comments
                            )
                            binding.commentsListView.adapter = adapter
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun loadAverageRating() {
        val commentsRef =
            database.reference.child("comments").orderByChild("hotelId").equalTo(hotelId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRating = 0.0
                var ratingCount = 0

                for (commentSnapshot in snapshot.children) {
                    val rating = commentSnapshot.child("rating").getValue(Double::class.java) ?: 0.0
                    totalRating += rating
                    ratingCount++
                }

                if (ratingCount > 0) {
                    val averageRating = totalRating / ratingCount
                    binding.averageRatingTextView.text =
                        "Average Rating: %.1f".format(averageRating)
                } else {
                    binding.averageRatingTextView.text = "Average Rating: N/A"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}

