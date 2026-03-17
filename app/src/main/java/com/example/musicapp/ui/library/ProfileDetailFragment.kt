package com.example.musicapp.ui.library

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.users.ChangePasswordRequest
import com.example.musicapp.models.users.UpdateMeRequest
import com.example.musicapp.models.users.UserResponse
import com.example.musicapp.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
class ProfileDetailFragment : Fragment() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var switchBiometric: Switch
    private lateinit var btnEditProfile: Button
    private lateinit var btnChangePassword: Button

    //Ham chuyen doi uri sang file
    private fun uriToFile(uri: Uri): File {
        val contentResolver = requireContext().contentResolver
        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
        val file = File(requireContext().cacheDir, "upload_${System.currentTimeMillis()}.$extension")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Không đọc được uri")
        return file
    }

    //Avatar Picker + Preview
    private var selectedAvatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAvatarUri = uri
            currentAvatarPreview?.setImageURI(uri)
        }
    }

    private var currentAvatarPreview: ImageView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_detail, container, false)

        // Ánh xạ view
        ivAvatar = view.findViewById(R.id.ivDetailAvatar)
        tvUsername = view.findViewById(R.id.tvDetailUsername)
        tvEmail = view.findViewById(R.id.tvDetailEmail)
        tvPassword = view.findViewById(R.id.tvDetailPassword)
        switchBiometric = view.findViewById(R.id.switchBiometric)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        // Gọi API để lấy dữ liệu user
        fetchUserProfile()

        // Xử lý khi bấm nút "Chỉnh sửa"
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        // Xử lý khi bấm nút "Đổi mật khẩu"
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }


        return view
    }
    //show pop upp chinh sua
    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)

        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val ivPreview = dialogView.findViewById<ImageView>(R.id.ivAvatarPreview)

        // Lưu tham chiếu để update khi chọn ảnh
        currentAvatarPreview = ivPreview

        // Gán dữ liệu hiện tại
        etUsername.setText(tvUsername.text.toString())
        etEmail.setText(tvEmail.text.toString())

        // Load avatar hiện tại từ server
        Glide.with(this).load((ivAvatar.drawable)).into(ivPreview)

        // Khi click vào ảnh → mở thư viện
        ivPreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa thông tin")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newUsername = etUsername.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()

                // Gửi username, email và ảnh (nếu user có chọn)
                updateUserProfile(newUsername, newEmail)
            }
            .setNegativeButton("Hủy", null)
            .show()


    }
    //show pop up doi mat khau
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val etOldPassword = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etReNewPassword = dialogView.findViewById<EditText>(R.id.etReNewPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Đổi mật khẩu")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val oldPass = etOldPassword.text.toString().trim()
                val newPass = etNewPassword.text.toString().trim()
                val reNewPass = etReNewPassword.text.toString().trim()

                if (oldPass.isEmpty() || newPass.isEmpty() || reNewPass.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPass != reNewPass) {
                    Toast.makeText(requireContext(), "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                // Gọi API
                changePassword(oldPass, newPass, reNewPass)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    //    Fetch Data
    private fun fetchUserProfile() {
        ApiClient.api.getUserProfile().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data

                    // Bind data vào UI
                    tvUsername.text = user.username
                    tvEmail.text = user.email
                    tvPassword.text = "********" // luôn ẩn password

                    Glide.with(requireContext()).load(user.avatar).into(ivAvatar)
//                    Log.d("DEBUG", "Avatar URL = ${user.avatar}")
                    // giả sử server trả về có cờ 2FA thì set checked
                    switchBiometric.isChecked = false
                } else {
                    Toast.makeText(requireContext(), "Lỗi load dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Update Data
    private fun updateUserProfile(username: String?, email: String?) {
        val usernamePart = (username ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = (email ?: "").toRequestBody("text/plain".toMediaTypeOrNull())

        var avatarPart: MultipartBody.Part? = null

        if (selectedAvatarUri != null) {
            val file = uriToFile(selectedAvatarUri!!)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            avatarPart = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
        }

        ApiClient.api.updateMe(avatarPart, usernamePart, emailPart)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()!!.data
                        Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        tvUsername.text = user.username
                        tvEmail.text = user.email
                        Glide.with(requireContext()).load(user.avatar).into(ivAvatar)
                    } else {
                        Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Change Password
    private fun changePassword(oldPass: String, newPass: String, reNewPass: String) {
        val request = ChangePasswordRequest(oldPass, newPass, reNewPass)

        ApiClient.api.changePassword(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}