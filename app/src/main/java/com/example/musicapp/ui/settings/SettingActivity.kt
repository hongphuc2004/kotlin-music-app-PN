package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.users.UserResponse
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.auth.LoginActivity
import com.example.musicapp.ui.library.ProfileDetailFragment
import com.example.musicapp.utils.PreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingActivity : AppCompatActivity() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var btnEditProfile: TextView
    private lateinit var btnAuth: Button
    private lateinit var switchDarkMode: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // ánh xạ view
        ivAvatar = findViewById(R.id.ivAvatar)
        tvUsername = findViewById(R.id.tvUsername)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnAuth = findViewById(R.id.btnLogout)
        switchDarkMode = findViewById(R.id.switchDarkMode)

        // --- Dark Mode setup ---
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)

        // Gán trạng thái ban đầu
        switchDarkMode.isChecked = PreferenceHelper.isDarkMode(this)

        // Lắng nghe khi người dùng bật/tắt
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            PreferenceHelper.setDarkMode(this, isChecked)
            PreferenceHelper.applyTheme(this)
            window.setWindowAnimations(android.R.style.Animation_Dialog)
            recreate() // cập nhật lại Activity để thấy thay đổi ngay
        }

        updateUI()
    }

    private fun applyDarkMode(enabled: Boolean) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    private fun updateUI() {
        val isLoggedIn = ApiClient.cookieManager?.getCookie() != null
        btnAuth.visibility = View.VISIBLE

        if (isLoggedIn) {
            // --- TRẠNG THÁI ĐÃ ĐĂNG NHẬP ---
            fetchUserData()

            // Hiển thị thông tin người dùng
            tvUsername.visibility = View.VISIBLE
            ivAvatar.visibility = View.VISIBLE
            btnEditProfile.visibility = View.VISIBLE

            // Cấu hình nút Đăng xuất
            btnAuth.text = "Đăng xuất"
            btnAuth.setOnClickListener {
                logout()
            }

            // Cấu hình nút Chi tiết thông tin
            btnEditProfile.setOnClickListener {
                val fragment = ProfileDetailFragment()
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("PROFILE_DETAIL")
                    .commit()
            }

        } else {
            // --- TRẠNG THÁI CHƯA ĐĂNG NHẬP ---

            // Ẩn thông tin người dùng và nút chi tiết
            tvUsername.visibility = View.GONE
            ivAvatar.visibility = View.GONE
            btnEditProfile.visibility = View.GONE

            // Cấu hình nút Đăng nhập
            btnAuth.text = "Đăng nhập"
            btnAuth.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            // Đặt avatar mặc định (để tránh hiển thị ảnh cũ nếu Glide chưa kịp chạy)
            ivAvatar.setImageResource(R.drawable.ic_user)
            tvUsername.text = ""
        }
    }

    private fun fetchUserData() {
        ApiClient.api.getUserProfile().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data

                    tvUsername.text = user.username

                    if (!this@SettingActivity.isDestroyed && !this@SettingActivity.isFinishing) {
                        Glide.with(this@SettingActivity)
                            .load(user.avatar)
                            .placeholder(R.drawable.ic_user)
                            .circleCrop()
                            .into(ivAvatar)
                    }
                } else {
                    Toast.makeText(this@SettingActivity, "Lỗi load dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@SettingActivity, "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logout() {
        ApiClient.api.logout().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SettingActivity, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
                    ApiClient.cookieManager?.clearCookie()
                    PreferenceHelper.clearUserInfo(this@SettingActivity)

                    val intent = Intent(this@SettingActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@SettingActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
