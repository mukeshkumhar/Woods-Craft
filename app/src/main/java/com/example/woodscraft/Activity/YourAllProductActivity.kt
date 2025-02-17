package com.example.woodscraft.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.woodscraft.Adapter.AdminProductAdapter
import com.example.woodscraft.Adapter.AllProductAdapter
import com.example.woodscraft.Authentication.AuthInterceptor
import com.example.woodscraft.DataModels.Product
import com.example.woodscraft.R
import com.example.woodscraft.Routes.ApiService
import com.example.woodscraft.URLs.RetrofitInstance
import com.example.woodscraft.databinding.ActivityYourAllProductBinding
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YourAllProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYourAllProductBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter:  AdminProductAdapter
    private val productList = mutableListOf<Product>()
    private var currentPage = 1
    private var totalPages = 1
    private var isLoading = false
    private val itemsPerPage = 5  // Number of items to load per page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityYourAllProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.my_product_recycle_view)
//        recyclerView.layoutManager = GridLayoutManager(context, 1)
        val layoutManager = GridLayoutManager(this,2)
//        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.layoutManager = layoutManager
        productAdapter = AdminProductAdapter(productList)
        recyclerView.adapter = productAdapter

        binding.backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragmentToLoad", "profile")
            startActivity(intent)
            finish()
        }

//        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        val authInterceptor = AuthInterceptor(sharedPreferences)
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
//            .build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(RetrofitInstance.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
//            .build()
//
//        val apiServiceWithInterceptor = retrofit.create(ApiService::class.java)
//
//        lifecycleScope.launch {
//            try {
//                val response = apiServiceWithInterceptor.getAllProduct(currentPage, itemsPerPage)
////                if (response.isSuccessful && response.body() != null) {
//                    val products = response.data.products // Assuming your ListOfProduct has a property named "products"
//
//                    if (response.data.totalPages != null) {
//                        totalPages = response.data.totalPages
//                    }
//
//                    println("Total Pages: " + totalPages)
//                val newProduct = response.data.products
//                println(newProduct)
//                val startPosition = productList.size
//                productList.addAll(newProduct)
//                productAdapter.notifyItemRangeInserted(startPosition, newProduct.size)
//                currentPage++
////                    adapter.notifyDataSetChanged()
////                    println(productList)
////                    val Adapter = AdminProductAdapter(productList)
////                    recyclerView.adapter = Adapter
////                } else {
////                    println("Getting product failed")
////                }
//            } catch (e: Exception) {
//                Log.e("YourAllProductActivity", "Getting AllProduct failed: ${e.message}", e)
//                Toast.makeText(
//                    this@YourAllProductActivity,
//                    "Getting AllProduct failed due to network error",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//        apiServiceWithInterceptor.getAllProduct().enqueue(object : Callback<ListOfProduct> {
//            override fun onResponse(p0: Call<ListOfProduct>, p1: Response<ListOfProduct>) {
//                if (p1.isSuccessful && p1.body() != null){
//                    val products = p1.body()?.data // Assuming your ListOfProduct has a property named "products"
//                    productList.clear()
//                    if (products != null) {
//                        productList.addAll(products)
//                    } else {
//                        println("product will be null")
//                    }
////                    adapter.notifyDataSetChanged()
//                    println(productList)
//                    val Adapter = AdminProductAdapter(productList)
//                    recyclerView.adapter = Adapter
//
//                } else {
//                    println("Getting product failed")
//                }
//            }
//
//            override fun onFailure(p0: Call<ListOfProduct>, p1: Throwable) {
//                Log.e("HomeFragment", "Getting AllProduct failed: ${p1.message}", p1)
//                Toast.makeText(this@YourAllProductActivity, "Getting AllProduct failed due to network error", Toast.LENGTH_SHORT).show()
//
//            }
//
//        })
        loadProduct()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int){
             super.onScrolled(recyclerView, dx, dy)
             val visibleItemsCount = layoutManager.childCount
             val totalItemCount = layoutManager.itemCount
             val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

             if (!isLoading && currentPage <= totalPages) {
                 if (visibleItemsCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                     loadProduct()
                 }
             }
            }
        })
    }
    private fun loadProduct(){
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
        if(isLoading) return
        isLoading = true
        lifecycleScope.launch {
            try {
                val response = apiServiceWithInterceptor.getAllProduct(currentPage, itemsPerPage)
//                if (response.isSuccessful && response.body() != null) {
                val products = response.data.products // Assuming your ListOfProduct has a property named "products"

                if (response.data.totalPages != null) {
                    totalPages = response.data.totalPages
                }

                println("Total Pages: " + totalPages)
                val newProduct = response.data.products
                println(newProduct)
                val startPosition = productList.size
                productList.addAll(newProduct)
                productAdapter.notifyItemRangeInserted(startPosition, newProduct.size)
                currentPage++
//                    adapter.notifyDataSetChanged()
//                    println(productList)
//                    val Adapter = AdminProductAdapter(productList)
//                    recyclerView.adapter = Adapter
//                } else {
//                    println("Getting product failed")
//                }
            } catch (e: Exception) {
                Log.e("YourAllProductActivity", "Getting AllProduct failed: ${e.message}", e)
                Toast.makeText(
                    this@YourAllProductActivity,
                    "Getting AllProduct failed due to network error",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }
//    private fun refreshData() {
//        currentPage = 1
//        totalPages = 1
//        productList.clear()
//        productAdapter.notifyDataSetChanged()
//        swipeRefreshLayout.isRefreshing = true
//        loadProducts()
//    }
}