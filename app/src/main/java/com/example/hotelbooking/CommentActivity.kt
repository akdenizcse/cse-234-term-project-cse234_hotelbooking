package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelbooking.databinding.ActivityCommentBinding


class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }


        val ratingBar = findViewById<RatingBar>(R.id.rating_bar)

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating < 0.5) ratingBar.rating = 0.5f
            else if (rating > 5.0) ratingBar.rating = 5.0f
        }

        binding.sendButton.setOnClickListener {
            Toast.makeText(this, "Feedback Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MyTripsActivity::class.java)
            startActivity(intent)
        }
    }
}
