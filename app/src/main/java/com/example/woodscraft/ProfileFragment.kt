package com.example.woodscraft

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.woodscraft.Activity.AddProductActivity
import com.example.woodscraft.Activity.YourAllProductActivity
import com.example.woodscraft.LoginActivity.LoginActivity
import com.example.woodscraft.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        val accessToken = sharedPreferences.getString("AccessToken", null)

        val userLoginSave = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userName = userLoginSave.getString("user_fullname", null)
        val admin = userLoginSave.getBoolean("user_admin", false)
        binding.nameTxt.text = userName

        println(admin)

        if (admin == true){
            _binding?.adminCard?.visibility = View.VISIBLE
        } else {
            _binding?.adminCard?.visibility = View.GONE
        }

        _binding?.logoutBtn?.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.remove("AccessToken")
            editor.apply()

            // Optional: Navigate to login screen or perform other logout actions
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        _binding?.addProduct?.setOnClickListener {
            val intent = Intent(requireContext(), AddProductActivity::class.java)
            startActivity(intent)
        }

        _binding?.yourProduct?.setOnClickListener{
            val intent = Intent(requireContext(), YourAllProductActivity::class.java)
            startActivity(intent)
        }


    }
}