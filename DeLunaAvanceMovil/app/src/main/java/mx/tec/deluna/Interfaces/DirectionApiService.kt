package mx.tec.deluna.Interfaces

import mx.tec.deluna.model.DirectionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("/v2/directions/driving-car")
    suspend fun getRoute(
        @Query("api_key") key: String,
        @Query("start", encoded = true) start: String,
        @Query("end", encoded = true) end: String
    ): Response<DirectionsResponse>
}
