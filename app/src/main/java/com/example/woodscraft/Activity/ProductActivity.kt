package com.example.woodscraft.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.woodscraft.Authentication.AuthInterceptor
import com.example.woodscraft.DataModels.AddWishlistResponse
import com.example.woodscraft.DataModels.RemoveWishlistResponse
import com.example.woodscraft.R
import com.example.woodscraft.Routes.ApiService
import com.example.woodscraft.URLs.RetrofitInstance
import com.example.woodscraft.databinding.ActivityProductBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding

    private var isProductInWishlist = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val productId = intent.getStringExtra("productId")
        val productName = intent.getStringExtra("productName")
        val productImage = intent.getStringExtra("productImage")
        val productDes = intent.getStringExtra("productDes")
        val productSubtitle = intent.getStringExtra("productSubtitle")
        val productRating = intent.getStringExtra("productRating")
        val productPrice = intent.getStringExtra("productPrice")

        Glide.with(this)
            .load(productImage)
            .into(binding.productImg)

        binding.titleTxt.text = productName
        binding.subtitleTxt.text = productDes
        binding.ratingTxt.text = productRating
        binding.priceTxt.text = "₹ ${productPrice}"
        binding.summeryText.text = productSubtitle

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val authInterceptor = AuthInterceptor(sharedPreferences)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        val apiServiceWithInterceptor = retrofit.create(ApiService::class.java)


        binding.likeImg.setOnClickListener {
            println(productId)
            if (!isProductInWishlist) {
                if (productId != null) {
                    apiServiceWithInterceptor.addToWishlist(productId)
                        .enqueue(object : Callback<AddWishlistResponse> {
                            override fun onResponse(
                                p0: Call<AddWishlistResponse>,
                                p1: Response<AddWishlistResponse>
                            ) {
                                if (p1.isSuccessful && p1.body() != null) {
                                    println(p1.body())
                                    isProductInWishlist = true
                                    binding.likeImg.setColorFilter(
                                        ContextCompat.getColor(
                                            this@ProductActivity,
                                            R.color.red
                                        )
                                    )
                                } else {
                                    println("Adding to wishlist failed not getting response")
                                    isProductInWishlist = false
                                    binding.likeImg.setColorFilter(
                                        ContextCompat.getColor(
                                            this@ProductActivity,
                                            R.color.light_brawn1
                                        )
                                    )
                                }
                            }

                            override fun onFailure(p0: Call<AddWishlistResponse>, p1: Throwable) {
                                Log.e("ProductActivity", "Add wishlist  failed: ${p1.message}", p1)
                                Toast.makeText(this@ProductActivity, "Failed to Add wishlist", Toast.LENGTH_SHORT).show()

                            }

                        })
                }
            } else {
                if (productId != null) {
                    apiServiceWithInterceptor.removeFromWishlist(productId)
                        .enqueue(object : Callback<RemoveWishlistResponse> {
                            override fun onResponse(
                                p0: Call<RemoveWishlistResponse>,
                                p1: Response<RemoveWishlistResponse>
                            ) {
                                if (p1.isSuccessful && p1.body() != null) {
                                    println(p1.body())
                                    isProductInWishlist = false
                                    binding.likeImg.setColorFilter(
                                        ContextCompat.getColor(
                                            this@ProductActivity,
                                            R.color.light_brawn1
                                        )
                                    )
                                } else {
                                    println("Removing from wishlist failed not getting response")
                                }
                            }

                            override fun onFailure(
                                p0: Call<RemoveWishlistResponse>,
                                p1: Throwable
                            ) {
                                Log.e("ProductActivity", "Delete wishlist  failed: ${p1.message}", p1)
                                Toast.makeText(this@ProductActivity, "Failed to remove wishlist", Toast.LENGTH_SHORT).show()

                            }
                        })
                }
            }
        }
    }
}