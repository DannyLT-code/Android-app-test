//Axel Daniel Corona Ibarra - A01425010@tec.mx

package mx.tec.deluna

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import mx.tec.deluna.databinding.ActivityMainBinding
import mx.tec.deluna.model.Elemento
import org.json.JSONArray
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timer: Timer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var datoss: List<Elemento> = recibeDatos()

        Log.d("datitos", datoss.toString())

        /*
        val elementoSet = HashSet<String>()
        datos.forEach { elemento ->
            elementoSet.add("${elemento.imagenNegocio},${elemento.tituloNegocio},${elemento.disponible},${elemento.distancia},${elemento.imagenCategoria},${elemento.descripcion},${elemento.insignia},${elemento.tipoNegocio},${elemento.direccion},${elemento.imagenRealNegocio},${elemento.nombreCategoria},${elemento.horario},${elemento.latitud},${elemento.longitud}")
        }

        Log.d("elementos", elementoSet.toString())

        with(sharedPreference.edit()) {
            putStringSet("elementos", elementoSet)
            commit()
        }
        */
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()
    }

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371 // Radio de la Tierra en kilómetros

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return radioTierra * c // Distancia en kilómetros
    }

    private fun calcularDisponibilidad(horario: String): String {

        //9:00 a 21:30
        val horarioArray = horario.split(" - ")
        val horaInicio = horarioArray[0].split(":")
        val horaFin = horarioArray[1].split(":")
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val minutoActual = Calendar.getInstance().get(Calendar.MINUTE)
        val horaInicioInt = horaInicio[0].toInt()
        val horaFinInt = horaFin[0].toInt()
        val minutoInicioInt = horaInicio[1].toInt()
        val minutoFinInt = horaFin[1].toInt()
        val horaActualInt = horaActual.toInt()
        val minutoActualInt = minutoActual.toInt()

        if (horaActualInt >= horaInicioInt && horaActualInt <= horaFinInt) {
            if (horaActualInt == horaInicioInt && minutoActualInt < minutoInicioInt) {
                return "Cerrado"
            } else if (horaActualInt == horaFinInt && minutoActualInt > minutoFinInt) {
                return "Cerrado"
            } else {
                return "Abierto"
            }
        } else {
            return "Cerrado"
        }
    }

    override fun onResume() {
        super.onResume()
        val elementosJson = getSharedPreferences("archivo", Context.MODE_PRIVATE).getString("elementosJson", "")

        if(elementosJson != null) {
            actualizarDistanciasYDisponibilidad()

            // Si el temporizador está nulo o ha sido cancelado, crea uno nuevo
            if (timer == null) {
                timer = Timer()
                // Programa una tarea para actualizar los valores cada cierto intervalo de tiempo (por ejemplo, cada 5 minutos)
                timer?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        actualizarDistanciasYDisponibilidad()
                    }
                }, 0, 2000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Cancela el temporizador y establece su referencia como nula para evitar errores
        timer?.cancel()
        timer = null
    }

    // Función para calcular distancias y disponibilidad
     fun actualizarDistanciasYDisponibilidad() {
        val sharedPreference = getSharedPreferences("archivo", Context.MODE_PRIVATE)
        val elementosJson = sharedPreference.getString("elementosJson", "")

        if (!elementosJson.isNullOrEmpty()) {
            val gson = Gson()
            try {
                val datos = gson.fromJson(elementosJson, Array<Elemento>::class.java).toList()
                val sharedPreference2 = getSharedPreferences("LatLang", Context.MODE_PRIVATE)
                val latitud = sharedPreference2.getString("latitudActual", "0.0")
                val longitud = sharedPreference2.getString("longitudActual", "0.0")

                datos.forEach { elemento ->
                    val distancia = calcularDistancia(
                        latitud!!.toDouble(),
                        longitud!!.toDouble(),
                        elemento.latitud,
                        elemento.longitud
                    )
                    elemento.distancia = String.format("%.2f", distancia) + " km"
                }

                // Llama a la función calcularDisponibilidad
                datos.forEach { elemento ->
                    elemento.disponible = calcularDisponibilidad(elemento.horario)
                }

                Log.d("elementos", datos.toString())
                val elementosJson2 = gson.toJson(datos)
                sharedPreference.edit().putString("elementosJson", elementosJson2).apply()



            } catch (e: Exception) {
                // Manejo de excepción en caso de error de deserialización
                Log.e("Error", "Error al deserializar elementosJson: ${e.message}")
            }
        } else {
            Log.e("Error", "Error al obtener elementosJson")
        }

    }


    fun recibeDatos(): List<Elemento> {

        var queue = Volley.newRequestQueue(this)
        val url = "https://apideluna-production.up.railway.app/api/negocios"
        var datos = mutableListOf<Elemento>()

// Elemento(val id: Int, val imagenNegocio:Int, val tituloNegocio:String, var disponible:String, var distancia:String, val imagenCategoria:Int, val descripcion: String, val insignia: Boolean, val tipoNegocio: String, val direccion: String, val imagenRealNegocio: Int, val nombreCategoria: String, val horario: String, val latitud: Double, val longitud: Double)

        val listener = Response.Listener<JSONArray> { response ->
            Log.d("dodo", response.toString())
            for(i in 0 until response.length()) {
                val id = response.getJSONObject(i).getInt("id")
                val imgNegocio = response.getJSONObject(i).getString("imagenNegocio")
                val nombre = response.getJSONObject(i).getString("tituloNegocio")
                val disponible = response.getJSONObject(i).getString("disponible")
                val distancia = response.getJSONObject(i).getString("distancia")
                val descripcion = response.getJSONObject(i).getString("descripcion")
                val insigniaJson = response.getJSONObject(i).getInt("insignia")
                val tipo = response.getJSONObject(i).getString("tipoNegocio")
                val direccion = response.getJSONObject(i).getString("direccion")
                val imgReal = response.getJSONObject(i).getString("imagenRealNegocio")
                val nombreCategoria = response.getJSONObject(i).getString("nombreCategoria")
                val horario = response.getJSONObject(i).getString("horario")
                val latitudNegocio = response.getJSONObject(i).getDouble("latitud")
                val longitudNegocio = response.getJSONObject(i).getDouble("longitud")

                val insignia: Boolean
                if(insigniaJson == 1){
                    insignia = true
                }else{
                    insignia = false
                }

                val imgCategoria : Int
                when(nombreCategoria){
                    "Turismo Consciente" -> {
                        imgCategoria = R.drawable.vector
                    }
                    "Bioconstrucción" -> {
                        imgCategoria = R.drawable.vectorbiocons
                    }
                    "Agricultura Regenerativa" -> {
                        imgCategoria = R.drawable.vector_agri
                    }
                    "Medicina Tradicional" -> {
                        imgCategoria = R.drawable.vector_medi
                    }
                    else -> {
                        imgCategoria = R.drawable.vector
                    }
                }


                var elemento = Elemento(id, imgNegocio, nombre, "Abierto", "1.5 km", imgCategoria, descripcion, true, tipo , direccion, imgReal, nombreCategoria, horario, latitudNegocio,  longitudNegocio)
                datos.add(elemento)
                Log.d("dadada", datos.toString())

                val sharedPreference = getSharedPreferences("archivo", Context.MODE_PRIVATE)
                val sharedPreference2 = getSharedPreferences("LatLang", Context.MODE_PRIVATE)

                val  latitud = sharedPreference2.getString("latitudActual", "0.0")
                val  longitud = sharedPreference2.getString("longitudActual", "0.0")

                //fotos reales 360x180
                //80 x 80 logos
                //descripcion max 300 caracteres
                //hora formato 24 horas  ej. "9:00 a 21:30"
                //tipo negocio 20 max caracteres
                //nombre negocio sin limites
                //latitud y longitud de la ubicacion del negocio siempre en double, si no tiene decimal agregale un 0.00001 o algo para que sea double porque el api lo convierte a int xd
                //ningun campo vacio porque sino truena alv
                //calcular distancia y modificar el campo distancia de cada elemento
                datos.forEach { elemento ->
                    val distancia = calcularDistancia(latitud!!.toDouble(), longitud!!.toDouble(), elemento.latitud, elemento.longitud)
                    elemento.distancia = String.format("%.2f", distancia) + " km"
                }

                datos.forEach { elemento ->
                    elemento.disponible = calcularDisponibilidad(elemento.horario)
                }

                val gson = Gson()
                val elementosJson = gson.toJson(datos)
                sharedPreference.edit().putString("elementosJson", elementosJson).apply()

            }
        }

        val error = Response.ErrorListener { error ->
            Log.e("RESTLIBS", error.message!!)
        }
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null, listener, error)
        queue.add(jsonArrayRequest)

        val sharedPreference = getSharedPreferences("archivo", Context.MODE_PRIVATE)
        val sharedPreference2 = getSharedPreferences("LatLang", Context.MODE_PRIVATE)

        //cambios aqui
        /*
        val homeFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? HomeFragment
        homeFragment?.obtenerUbicacionActual()

         */
        //aqui terminan los cambios


        /*
         val datos = listOf(
            Elemento(1, R.drawable.ventana_logo, "La Ventana Palenque", "Abierto", "1.5 km", R.drawable.vector, "Somos un santuario de 50 hectáreas que sirve como centro sustentable, educativo, ecoturístico y ceremonial en el corazón de la Selva Maya Mexicana (precisamente Palenque, Chiapas). Nos enfocamos en revivir la rica cultura perdida hace mucho tiempo de las comunidades mayas y traer de vuelta el santuario cultural para \"EL BUEN VIVIR\".", true, "Santuario", "29963 Palenque, Chis., México", R.drawable.laventana_real, "Turismo Consciente", "0:00 a 23:59", 17.454557605940717,  -92.03207173344042),
            Elemento(2, R.drawable.ciennatural_logo,"100% Natural Cancún", "Abierto", "1.5 km", R.drawable.vector, "100% natural es el lugar ideal para disfrutar platillos y bebidas preparados al momento con la mejor selección de ingredientes frescos y naturales. Atendemos a nuestros clientes con cordialidad y vocación de servicio, ofreciéndoles un menú innovador, variado e incluyente para satisfacer todos los paladares a cualquier hora del día con opciones para desayunos, comidas y cenas.", true, "Restaurante", "Av Sunyaxchen Mza. 6, 77509 Cancún, Q.R.", R.drawable.ciennaturalcancun, "Turismo Consciente", "7:00 a 23:00", 21.16223961525116,  -86.82993721443279),
            Elemento(3, R.drawable.cafejade_logo,"Café Jade", "Abierto","1.5 km", R.drawable.vector,"¡Descubre el encanto de Café Jade! Nos ubicamos en un lugar privilegiado lleno de naturaleza. ¡Ven a vivir momentos inolvidables con nosotros!", false, "Restaurante", "Prolongación Av. Hidalgo Esq. 5ta Poniente Norte, #1 Zona Turística la Cañada. 29960 Palenque, Chiapas, Mexico", R.drawable.cafejade, "Turismo Consciente", "8:00 a 22:00", 17.50966062464474,  -91.9869004314338),
            Elemento(4, R.drawable.teclogo, "Tec de Monterrey Cuernavaca", "Abierto", "1 km",R.drawable.vectorbiocons, " El Tecnológico de Monterrey, es una institución de educación superior privada, con sede en Monterrey, Nuevo León, México. Cuenta con 31 campus en el país, además de sedes en China, Colombia, Costa Rica, España, Guatemala y Perú.", true, "Universidad", "Av. 24 de Marzo #210", R.drawable.tecreal, "Bioconstrucción", "9:00 a 22:40", 18.80551022,  -99.22189178),
            Elemento(5, R.drawable.bambumaya_logo, "Bambú Maya", "Abierto", "1.5 km", R.drawable.vectorbiocons, " Taller de Bioconstrucción, arquitectura sustentable, materiales renovables, arte y diseño en Bambú.", true, "Taller", "Carretera Federal Km 4.5 en Zona Arqueológica 29960 Palenque, Chiapas, Mexico", R.drawable.bambumaya, "Bioconstrucción", "7:00 a 16:00", 17.493283526536157,  -92.0208053),
            Elemento(6, R.drawable.zonaarq_logo, "Zona Arqueológica de Palenque", "Abierto", "27 km", R.drawable.vector,"La Zona Arqueológica de Palenque, tuvo un notable desarrollo cultural hasta fines del período Clásico. Es un sitio de sobresaliente belleza arquitectónica e importancia estética, lo que hace que esta excepcional ciudad, enclavada en medio de la selva, sea una de las más grandiosas creaciones de los hombres antiguos.", true, "Zona arqueológica", "Carretera a Palenque- Zona Archaeologica Km. 8, 29960 Palenque, Chis.", R.drawable.zonaarq, "Turismo Consciente", "8:30 a 16:30", 17.48532228817129, -92.04593781534382),
            Elemento(7, R.drawable.maskgroup, "De Luna", "Abierto", "1.5 km", R.drawable.vector_agri, "El fascinante centro de Cuernavaca está lleno de reliquias coloniales como el fortificado Palacio de Cortés, que alberga el Museo Regional Cuauhnáhuac y sus murales de Diego Rivera. La plaza arbolada el Zócalo linda con el jardín Juárez, que cuenta con un quiosco que diseñó Gustave Eiffel.", true, "Museo", "Paseo de las lunas #123",R.drawable.imagenlugar, "Agricultura Regenerativa", "9:00 a 21:30", 2.0, 45.0),
        )


         */


        return datos
    }
}