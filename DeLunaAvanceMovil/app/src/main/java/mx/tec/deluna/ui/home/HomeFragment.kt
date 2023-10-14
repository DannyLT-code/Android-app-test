//Axel Daniel Corona Ibarra - A01425010@tec.mx

package mx.tec.deluna.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.tec.deluna.FragmentBottonSheet
import mx.tec.deluna.Interfaces.DirectionsApiService
import mx.tec.deluna.R
import mx.tec.deluna.databinding.FragmentHomeBinding
import mx.tec.deluna.model.BottonSheet
import mx.tec.deluna.model.DirectionsResponse
import mx.tec.deluna.model.Elemento
import mx.tec.deluna.ui.dashboard.DashboardFragment
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

class HomeFragment : Fragment(), LocationListener {

    private var _binding: FragmentHomeBinding? = null
    private var bottomSheetDialogFragment: FragmentBottonSheet? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var map: GoogleMap
    lateinit var locationManager: LocationManager

    private lateinit var bottomView: BottonSheet

    var poly : Polyline? = null

        @SuppressLint("PotentialBehaviorOverride")
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPreferences =
            requireContext().getSharedPreferences("archivo", Context.MODE_PRIVATE)
        val elementosJson = sharedPreferences.getString("elementosJson", "")

        val sharedPreferences3 =
            requireContext().getSharedPreferences("LatLang", Context.MODE_PRIVATE)
        val latitudAnterior =
            sharedPreferences3.getString("latitudNegocioPlay", "0.0")?.toDouble() ?: 0.0
        val longitudAnterior =
            sharedPreferences3.getString("longitudNegocioPlay", "0.0")?.toDouble() ?: 0.0

        Log.d("longitudAnterior", longitudAnterior.toString())
        Log.d("latitudAnterior", latitudAnterior.toString())


        val gson = Gson()
        val tipoLista = object : TypeToken<List<Elemento>>() {}.type

        val datos = elementosJson?.let {
            try {
                gson.fromJson<List<Elemento>>(it, tipoLista)
            } catch (e: Exception) {
                // Manejar cualquier error de deserialización aquí
                emptyList()
            }
        } ?: emptyList()

        bottomView = ViewModelProvider(this).get(BottonSheet::class.java)

        //extraer latitud y longitud de cada elemento

        val latitud = datos.map { it.latitud }
        val longitud = datos.map { it.longitud }

        val flag: Boolean = false
        val latitudMax = latitud.maxOrNull()
        val latitudMin = latitud.minOrNull()
        val longitudMax = longitud.maxOrNull()
        val longitudMin = longitud.minOrNull()
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Favor de activar los permisos de ubicación")
                    .setTitle("Permiso requerido")
                builder.setPositiveButton("Ok") { dialog, id ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10
                    )
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10
                )
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)
        }

        var mapFragment = childFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            //mapa.isMyLocationEnabled = true
            for (i in latitud.indices) {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitud[i], longitud[i]))
                        .title(datos[i].tituloNegocio)
                        .snippet("Latitud: ${latitud[i]}, Longitud: ${longitud[i]}")
                        )
            }
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true

                map.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
                    var btmSt = BottonSheet()
                    btmSt.tituloNegocio = marker.title.toString()
                    btmSt.descripcionNegocio =
                        datos.find { it.tituloNegocio == marker.title.toString() }?.descripcion.toString()
                    bottomSheetDialogFragment =
                        FragmentBottonSheet(
                            marker.title.toString(),
                            btmSt.descripcionNegocio,
                            datos.find { it.tituloNegocio == marker.title.toString() }?.imagenRealNegocio
                                ?: "",
                            datos.find { it.tituloNegocio == marker.title.toString() }?.tipoNegocio
                                ?: "",
                            datos.find { it.tituloNegocio == marker.title.toString() }?.horario
                                ?: "",
                            datos.find { it.tituloNegocio == marker.title.toString() }?.latitud
                                    ?: 0.0,
                            datos.find { it.tituloNegocio == marker.title.toString() }?.longitud
                                    ?: 0.0
                        )
                    bottomSheetDialogFragment!!.show(
                        requireActivity().supportFragmentManager,
                        bottomSheetDialogFragment!!.tag
                    )
                    true

                })
                // Latitud: 17.51, Longitud: -91.9815 palenque chiapas
                if (longitudAnterior != 0.0 && latitudAnterior != 0.0) {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                latitudAnterior,
                                longitudAnterior
                            ), 19f
                        )
                    )
                } else {
                    //get location
                    val location =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location != null) {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), 15f
                            )
                        )
                    } else {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(17.51, -91.9815),
                                15f
                            )
                        )
                    }
                }


                //crear marcadores de cada elemento
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    var ubicacionAnterior: Marker? = null

    override fun onLocationChanged(location: Location) {

        if (isAdded) {
            val context = requireContext()
            val sharedPreferences = requireContext().getSharedPreferences(
                "LatLang",
                android.content.Context.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.putString("latitudActual", location.latitude.toString()).apply()
            editor.putString("longitudActual", location.longitude.toString()).apply()
        }
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(requireContext(), "GPS desactivado", Toast.LENGTH_SHORT).show()
    }

/*
    fun setupMap(latitudNegocio: Double, longitudNegocio: Double, mapa : GoogleMap) {
        // Coordenadas de inicio (ubicación actual) y destino (ubicación de la asociación)
        //https://routes.googleapis.com/directions/v2:computeRoutes?key=AIzaSyBEsyF50kHyNkA3to2ahtMmdidhZ4y4Cts

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }


        val latitudActual =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.latitude
        val longitudActual =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.longitude

        val origin = "$latitudActual,$longitudActual"
        val destination = "$latitudNegocio,$longitudNegocio"

        val apiKey = "AIzaSyB6EnQs5qWB27XR5i3TJKgsjIkDxYr2Rm8"

        //https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248821a641292f743da937476610bd10336&start=8.681495,49.41461&end=8.687872,49.420318
        // Crear una instancia de Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crear una instancia de la interfaz de servicio de API
        val service = retrofit.create(DirectionsApiService::class.java)

        // Realizar la solicitud para obtener la respuesta JSON
        val call = service.getDirections(origin, destination, apiKey)

        call.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("eeee", response.body().toString())
                    val directionsResponse = response.body()

                    // Procesar la respuesta JSON y dibujar la ruta en el mapa
                    if (directionsResponse != null && !directionsResponse.routes.isEmpty()) {
                        val route = directionsResponse.routes[0]
                        val overviewPolyline = route.overviewPolyline
                        val points = overviewPolyline.points

                        // Decodificar los puntos de la ruta
                        val decodedPath = PolyUtil.decode(points)

                        // Dibujar la ruta en el mapa
                        val polyline = mapa.addPolyline(
                            PolylineOptions()
                                .addAll(decodedPath)
                                .width(10f)
                                .color(Color.BLUE)
                        )

                        // Ajustar la cámara para mostrar la ruta completa
                        val originLatLng = LatLng(latitudActual!!, longitudActual!!)
                        val destinationLatLng = LatLng(latitudNegocio, longitudNegocio)
                        val boundsBuilder = LatLngBounds.builder()
                        boundsBuilder.include(originLatLng) // Debes definir originLatLng
                        boundsBuilder.include(destinationLatLng) // Debes definir destinationLatLng
                        val bounds = boundsBuilder.build()
                        val padding = 100 // Margen en píxeles
                        mapa.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    }
                } else {
                   Toast.makeText(requireContext(), "Error al obtener la ruta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {


            }
        })
    }

 */



/*
    fun createRoute(start : String, end : String) {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(DirectionsApiService::class.java)
                .getRoute("5b3ce3597851110001cf6248821a641292f743da937476610bd10336", start, end)
            Log.d("eeee", call.toString())
            if (call.isSuccessful) {
                Log.d("eeee", call.body().toString())
                drawRoute(call.body())
            } else {
                Log.d("eeee", "Error al obtener la ruta")
            }
        }
    }

    fun drawRoute(routeResponse: DirectionsResponse?) {
        val polylineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polylineOptions.add(LatLng(it[1], it[0]))
        }
        //run on ui thread
        requireActivity().runOnUiThread {
            poly = map.addPolyline(polylineOptions)
        }

    }

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }




 */


        //cambios aqui
    /*
    fun obtenerUbicacionActual() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //get actual location an return lat and long
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val sharedPreferences = requireContext().getSharedPreferences(
                "LatLang",
                android.content.Context.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.putString("latitudActual", location?.latitude.toString()).apply()
            editor.putString("longitudActual", location?.longitude.toString()).apply()
        }
    }
    //aqui terminan los cambios

     */

}