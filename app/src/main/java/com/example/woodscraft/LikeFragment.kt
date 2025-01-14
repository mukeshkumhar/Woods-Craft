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
import com.example.woodscraft.Adapter.AllProductAdapter
import com.example.woodscraft.Adapter.LikeProductAdapter
import com.example.woodscraft.Authentication.AuthInterceptor
import com.example.woodscraft.DataModels.LikedProduct
import com.example.woodscraft.DataModels.Product
import com.example.woodscraft.DataModels.WishListResponse
import com.example.woodscraft.Routes.ApiService
import com.example.woodscraft.URLs.RetrofitInstance
import com.example.woodscraft.databinding.FragmentLikeBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LikeFragment : Fragment() {
   private lateinit var binding: FragmentLikeBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LikeProductAdapter
    private val productList = mutableListOf<LikedProduct>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.likeRecyclerView)
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

        apiServiceWithInterceptor.getWishList().enqueue(object : Callback<WishListResponse> {
            override fun onResponse(p0: Call<WishListResponse>, p1: Response<WishListResponse>) {
                if(p1.isSuccessful && p1.body() != null){
                    println(p1.body())
                    val likedProducts = p1.body()?.data
                    productList.clear()
                    if (likedProducts != null) {
                        productList.addAll(likedProducts)
                    }else {
                        println("Like product will be null")
                    }

                    val Adapter = LikeProductAdapter(productList)
                    recyclerView.adapter = Adapter
                }
            }

            override fun onFailure(p0: Call<WishListResponse>, p1: Throwable) {
                Log.e("LikeFragment", "Getting wishlist failed: ${p1.message}", p1)
                Toast.makeText(this@LikeFragment.requireContext(), "Getting wishlist failed due to network error", Toast.LENGTH_SHORT).show()
            }

        })
    }


}