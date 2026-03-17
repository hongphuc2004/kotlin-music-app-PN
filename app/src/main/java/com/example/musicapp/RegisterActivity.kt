package com.example.musicapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.SignUpRequest
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.et_full_name_register)
        val etEmail = findViewById<EditText>(R.id.et_email_login)
        val etPassword = findViewById<EditText>(R.id.et_password_register)
        val etConfirm = findViewById<EditText>(R.id.et_confirm_password_register)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin: TextView = findViewById(R.id.tv_go_to_login)

        btnRegister.setOnClickListener {
            val request = SignUpRequest(
                username = etUsername.text.toString(),
                email = etEmail.text.toString(),
                password = etPassword.text.toString(),
                rePassword = etConfirm.text.toString()
            )

            ApiClient.api.signUp(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@RegisterActivity, body?.message, Toast.LENGTH_SHORT).show()

                        // Sau khi đăng ký thành công → chuyển sang LoginActivity
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
