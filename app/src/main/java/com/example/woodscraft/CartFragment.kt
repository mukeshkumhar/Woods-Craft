package com.example.woodscraft

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.woodscraft.Adapter.CartProductAdapter
import com.example.woodscraft.Authentication.AuthInterceptor
import com.example.woodscraft.DataModels.CartProduct
import com.example.woodscraft.DataModels.CartResponse
import com.example.woodscraft.DataModels.LikedProduct
import com.example.woodscraft.Routes.ApiService
import com.example.woodscraft.URLs.RetrofitInstance
import com.example.woodscraft.databinding.FragmentCartBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var recyclerView: RecyclerView
    private val productList = mutableListOf<CartProduct>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.cardRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 1)

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
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

        apiServiceWithInterceptor.getCart().enqueue(object : Callback<CartResponse> {
            override fun onResponse(p0: Call<CartResponse>, p1: Response<CartResponse>) {
                if (p1.isSuccessful && p1.body() != null){
                    val cartProducts = p1.body()?.data
                    productList.clear()
                    if (cartProducts != null) {
                        productList.addAll(cartProducts)
                    } else {
                        println("Cart product will be null")
                    }

                    val Adapter = CartProductAdapter(productList)
                    recyclerView.adapter = Adapter
                }
            }

            override fun onFailure(p0: Call<CartResponse>, p1: Throwable) {
                Log.e("CartFragment", "Getting CartList failed: ${p1.message}", p1)
                Toast.makeText(this@CartFragment.requireContext(), "Getting CartList failed due to network error", Toast.LENGTH_SHORT).show()
            }

        })
    }
}

