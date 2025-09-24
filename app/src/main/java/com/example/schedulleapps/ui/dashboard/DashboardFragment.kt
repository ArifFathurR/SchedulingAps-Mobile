package com.example.schedulleapps.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.FragmentDashboardBinding
import com.example.schedulleapps.model.ProfileResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private var userId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        loadProfile()

        binding.btnChoosePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        binding.btnSaveProfile.setOnClickListener {
            updateProfile()
        }

        return binding.root
    }

    private fun loadProfile() {
        val token = requireActivity().getSharedPreferences("APP", 0)
            .getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ada", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getProfile("Bearer $token")
            .enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val profile = response.body()!!.data!!
                        userId = profile.id

                        binding.etNama.setText(profile.nama)
                        binding.etAlamat.setText(profile.alamat)
                        binding.etNoHp.setText(profile.no_hp)
                        binding.etEmail.setText(profile.email)

                        Glide.with(requireContext())
                            .load(profile.photo)
                            .placeholder(android.R.drawable.ic_menu_camera)
                            .into(binding.imgProfile)
                    } else {
                        Toast.makeText(requireContext(), "Gagal ambil profil", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateProfile() {
        val token = requireActivity().getSharedPreferences("APP", 0)
            .getString("TOKEN", null) ?: return

        val nama = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.etNama.text.toString())
        val alamat = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.etAlamat.text.toString())
        val noHp = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.etNoHp.text.toString())

        var photoPart: MultipartBody.Part? = null
        imageUri?.let { uri ->
            val file = createFileFromUri(uri)
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)
            }
        }

        ApiClient.instance.updateProfile(userId, "Bearer $token", nama, alamat, noHp, photoPart)
            .enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
                        loadProfile()
                    } else {
                        Toast.makeText(requireContext(), "Gagal update profil", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.imgProfile.setImageURI(imageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
