package com.example.woodscraft

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.woodscraft.Adapter.AdminProductAdapter
import com.example.woodscraft.Adapter.AllProductAdapter
import com.example.woodscraft.Adapter.ImageSliderAdapter
import com.example.woodscraft.Authentication.AuthInterceptor
import com.example.woodscraft.DataModels.ListOfProduct
import com.example.woodscraft.DataModels.Product
import com.example.woodscraft.Routes.ApiService
import com.example.woodscraft.URLs.RetrofitInstance
import com.example.woodscraft.databinding.FragmentHomeBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.RecursiveTask

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: AllProductAdapter
    private val productList = mutableListOf<Product>()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var currentPage = 1
    private var totalPages =1
    private var isLoading = false
    private val itemsPerPage = 5 // Number of items to load per page
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.product_recycle_view)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)



        val layoutManager = GridLayoutManager(context,2)
//        val layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        productAdapter = AllProductAdapter(productList)
        recyclerView.adapter = productAdapter



        val image = listOf(R.drawable.png5,R.drawable.png4,R.drawable.png7)
        val adapter = ImageSliderAdapter(image)
        val imageSlider = view.findViewById<ViewPager2>(R.id.imageSlider)
        imageSlider.adapter = adapter

        val dotsLayout = view.findViewById<LinearLayout>(R.id.slideDots)
        val dots = arrayOfNulls<ImageView>(image.size)


        for (i in image.indices) {
            dots[i] = ImageView(requireContext())
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive)
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(18, 0, 18, 0)
            dotsLayout.addView(dots[i], params)
        }

        dots[0]?.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dot_active)
        )

        imageSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isAdded) {
                    val context = requireContext()
                    for (i in image.indices) {
                        dots[i]?.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.dot_inactive)
                        )
                    }
                    dots[position]?.setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.dot_active)
                    )
                }
            }
        })



        runnable = object : Runnable {
            override fun run() {
                if(isAdded) {
                    val currentItem = imageSlider.currentItem
                    imageSlider.currentItem = (currentItem + 1) % image.size
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 3000)



//        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
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
//                val response = apiServiceWithInterceptor.getAllProduct(page = 1, limit = 10)
////                if (response.isSuccessful && response.body() != null) {
//                    val products = response.data.products // Assuming your ListOfProduct has a property named "products"
//                    productList.clear()
//                    if (products != null) {
//                        productList.addAll(products)
//                    } else {
//                        println("product will be null")
//                    }
////                    adapter.notifyDataSetChanged()
//                    println(productList)
//                    val Adapter = AllProductAdapter(productList)
//                    recyclerView.adapter = Adapter
////                } else {
////                    println("Getting product failed")
////                }
//            } catch (e: Exception) {
//                Log.e("Home Fragment", "Getting AllProduct failed: ${e.message}", e)
//                Toast.makeText(
//                    requireContext(),
//                    "Getting AllProduct failed due to network error",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }



        // Load initial data
        loadProducts()

        // Add scroll listener for infinite scrolling
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int){
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemsCount = layoutManager.childCount
//                println("VisibleItemCount: "+visibleItemsCount)
                val totalItemCount = layoutManager.itemCount
                println("Total Item Count: "+ totalItemCount)
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//                println("firstVisibleItemPosition: $firstVisibleItemPosition")

                if (!isLoading && currentPage <= totalPages) {
                    if (visibleItemsCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadProducts()
                    }
                }
            }
        })

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

//        apiServiceWithInterceptor.getAllProduct(page = 1, limit = 10).enqueue(object : Callback<ListOfProduct> {
//            override fun onResponse(p0: Call<ListOfProduct>, p1: Response<ListOfProduct>) {
//                if (p1.isSuccessful && p1.body() != null){
//                    val products = p1.body()?.data // Assuming your ListOfProduct has a property named "products"
//                    productList.clear()
//                    if (products != null) {
//                        productList.addAll(products)
//                    } else {
//                        println("product will be null")
//                    }
//                    adapter.notifyDataSetChanged()
//                    println(productList)
//                    val Adapter = AllProductAdapter(productList)
//                    recyclerView.adapter = Adapter
//
//                } else {
//                    println("Getting product failed")
//                }
//            }
//
//            override fun onFailure(p0: Call<ListOfProduct>, p1: Throwable) {
//                Log.e("HomeFragment", "Getting AllProduct failed: ${p1.message}", p1)
//                Toast.makeText(this@HomeFragment.requireContext(), "Getting AllProduct failed due to network error", Toast.LENGTH_SHORT).show()
//
//            }
//
//        })

    }

    private fun loadProducts() {
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
        if(isLoading) return
        isLoading = true
        lifecycleScope.launch {
            try {
                val response = apiServiceWithInterceptor.getAllProduct(currentPage, itemsPerPage                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    )
//                if (response.isSuccessful && response.body() != null) {
                val products = response.data.products // Assuming your ListOfProduct has a property named "products"
                if (response.data.totalPages != null){
                    totalPages = response.data.totalPages
                }

                println("Total Pages: "+totalPages)
                val newProduct = response.data.products
                println(newProduct)
                val startPosition = productList.size
                productList.addAll(newProduct)
//                println(productList)
                productAdapter.notifyItemRangeInserted(startPosition, newProduct.size)
                currentPage++
//                if (products != null) {
//                    productList.addAll(products)
//                    productAdapter.notifyDataSetChanged()
//                    currentPage++
//                } else {
//                    println("product will be null")
//                }
//                    adapter.notifyDataSetChanged()

//                val Adapter = AllProductAdapter(productList)
//                recyclerView.adapter = Adapter
//                } else {
//                    println("Getting product failed")
//                }
            } catch (e: Exception) {
                Log.e("Home Fragment", "Getting AllProduct failed: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Getting AllProduct failed due to network error",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

    }

    private fun refreshData() {
        currentPage = 1
        totalPages = 1
        productList.clear()
        productAdapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = true
        loadProducts()
    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable) // Stop auto-sliding when the fragment is paused
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000) // Resume auto-sliding when the fragment is resumed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(runnable)
    }

}


//// In HomeFragment.kt
//override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//    super.onViewCreated(view, savedInstanceState)
//
//    val images = listOf(R.drawable.image1, R.drawable.image2, R.drawable.image3)
//    val adapter = ImageSliderAdapter(images)
//    val imageSlider = view.findViewById<ViewPager2>(R.id.imageSlider)
//    imageSlider.adapter = adapter
//
//    // Auto-sliding
//    val handler = Handler(Looper.getMainLooper())
//    val runnable = object : Runnable {
//        override fun run() {
//            val currentItem = imageSlider.currentItem
//            imageSlider.currentItem = (currentItem + 1) % images.size
//            handler.postDelayed(this, 3000) // Slide every 3 seconds
//        }
//    }
//    handler.postDelayed(runnable, 3000)
//}