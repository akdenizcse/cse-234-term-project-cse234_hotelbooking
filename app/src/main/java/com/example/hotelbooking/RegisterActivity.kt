package com.example.hotelbooking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.hotelbooking.databinding.ActivityRegisterBinding
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    lateinit var name : EditText
    lateinit var email : EditText
    lateinit var password: EditText
    lateinit var confirmPassword: EditText
    lateinit var registerButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.registerButton.setOnClickListener(View.OnClickListener {
            if (binding.name.toString() == "Emre" && binding.email.text.toString() == "user@hotmail.com" &&
                binding.password.text.toString() == "1234" && binding.confirmPassword.text.toString() == "1234"
                ){
                Toast.makeText(this, "Register Successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Register Failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}