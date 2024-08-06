package uk.ac.tees.mad.D3746064.services

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import uk.ac.tees.mad.D3746064.data.DriverDetails
import java.util.concurrent.TimeUnit




interface DriverApi {
    @GET("271e1351-9692-4d0b-be56-03b00880626c")
    suspend fun getDriverDetails(): DriverDetails
}

object DriverApiService {
    private const val BASE_URL = "https://run.mocky.io/v3/"

    private val logging = HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val driverApi: DriverApi = retrofit.create(DriverApi::class.java)
}