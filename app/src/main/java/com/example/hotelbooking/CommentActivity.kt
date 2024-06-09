package com.example.hotelbooking

import android.os.Bundle
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelbooking.databinding.ActivityCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var hotelId: String

    data class Comment(
        val userId: String = "",
        val hotelId: String = "",
        val rating: Double = 0.0,
        val commentText: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        hotelId = intent.getStringExtra("hotelId") ?: ""

        binding.backButton.setOnClickListener {
            finish()
        }

        val ratingBar = findViewById<RatingBar>(R.id.rating_bar)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating < 0.5) ratingBar.rating = 0.5f
            else if (rating > 5.0) ratingBar.rating = 5.0f
        }

        binding.sendButton.setOnClickListener {
            val commentText = binding.name.text.toString()
            val rating = ratingBar.rating.toDouble()

            val comment = Comment(
                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                hotelId = hotelId,
                rating = rating,
                commentText = commentText
            )

            addCommentToDatabase(comment)

            Toast.makeText(this, "Feedback Successful!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun addCommentToDatabase(comment: Comment) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("comments").push()
        databaseReference.setValue(comment)
    }
}
