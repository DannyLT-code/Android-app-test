//Axel Daniel Corona Ibarra - A01425010@tec.mx

package mx.tec.deluna.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.tec.deluna.MainActivity
import mx.tec.deluna.MainActivityNegocio
import mx.tec.deluna.R
import mx.tec.deluna.adapter.CustomAdapter
import mx.tec.deluna.databinding.FragmentDashboardBinding
import mx.tec.deluna.model.Elemento
import mx.tec.deluna.ui.home.HomeFragment

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    lateinit var locationManager: LocationManager
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val isDarkMode = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if(isDarkMode){
            binding.imgFiltrar.setImageResource(R.drawable.vector_filtrar_dark)
        }else{
            binding.imgFiltrar.setImageResource(R.drawable.vector_filtrar)
        }
        val sharedPreferences = requireContext().getSharedPreferences("archivo", android.content.Context.MODE_PRIVATE)
        val elementosJson = sharedPreferences.getString("elementosJson", "")


        val gson = Gson()
        val tipoLista = object : TypeToken<List<Elemento>>() {}.type

        val datos = elementosJson?.let {
            try {
                gson.fromJson<List<Elemento>>(it, tipoLista)
            } catch (e: Exception) {
                // Manejar cualquier error de deserialización aquí
                emptyList()  // Si ocurre un error, devuelve una lista vacía o maneja el error según sea necesario
            }
        } ?: emptyList()

        //log para mostrar datos
        Log.d("datos", datos.toString())


        //se necesita la imagen real del negocio en 300-350 x 200-250
       /* val datos = listOf(
            Elemento(R.drawable.maskgroup, "Daniel De Luna", "Abierto", "1.5 km", R.drawable.vector, "El fascinante centro de Cuernavaca está lleno de reliquias coloniales como el fortificado Palacio de Cortés, que alberga el Museo Regional Cuauhnáhuac y sus murales de Diego Rivera. La plaza arbolada el Zócalo linda con el jardín Juárez, que cuenta con un quiosco que diseñó Gustave Eiffel.", true, "Museo", "Paseo de las lunas #123",R.drawable.imagenlugar, "Turismo Consciente", "9:00 am - 7:00 pm", 2.0, 45.0),
            Elemento(R.drawable.imagenlogoprueba2, "Axel Espin", "Abierto", "1.8 km", R.drawable.vector,"Axel espin es una gatotototototototototototototototototototototototototototototota", true, "Museo", "Paseo de los Axelines #123", R.drawable.imgreal_prueba21, "Turismo Consciente", "9:00 am - 7:00 pm",4.0, 5.7),
            Elemento(R.drawable.maskgroup, "Axel", "Cerrado", "2 km", R.drawable.vector, "Axel espin es una loca", true, "Parque", "Popas #123", R.drawable.imagenlugar, "Agricultura Regenerativa", "9:00 am - 7:00 pm", 10.2, 3.0),
            Elemento(R.drawable.maskgroup, "Pop", "Cerrado", "45 km", R.drawable.vector,"Popis es una loca", false, "Tienda", "Popis #19", R.drawable.imagenlugar, "Turismo Consciente", "9:00 am - 7:00 pm", 7.0, 4.3),
            Elemento(R.drawable.maskgroup, "Díaz", "Cerrado", "1.5 km", R.drawable.vector, "Díaz es una loca", true, "Museo", "Díaz #123", R.drawable.imgreal_prueba21, "Medicina Tradicional", "9:00 am - 7:00 pm", 1.0, 2.0),
            Elemento(R.drawable.maskgroup, "Daniel De Luna", "Cerrado", "1.5 km", R.drawable.vector, "Ola", false, "Abarrotes", "Paseo de las lunas #69", R.drawable.imgreal_prueba21, "Bioconstrucción", "9:00 am - 7:00 pm", 22.0, 11.0),
        )
*/
        var datosFiltrados = datos.toMutableList()

        var datosCercanos = datos.toMutableList()

        val adaptador = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datos)

        binding.rvListaCompleta.adapter = adaptador



        //acceder a la ubicacion del usuario actual
        val sharedPreferences2 = requireContext().getSharedPreferences("LatLang", android.content.Context.MODE_PRIVATE)
        val latitud = sharedPreferences2.getString("latitudActual", "")
        val longitud = sharedPreferences2.getString("longitudActual", "")
        Log.d("latitud", latitud.toString())
        Log.d("longitud", longitud.toString())


        //rvListaCercana deberà mostrar unicamente los negocios a 5km a la redonda tomando la distancia de cada elemento
        //no hace falta calcular distancia
        datosCercanos = datosCercanos.filter { elemento ->
            val distanciaStr = elemento.distancia
            val distanciaNumerica = distanciaStr.replace(" km", "").toDoubleOrNull() ?: 0.0
            distanciaNumerica <= 5.0
        }.toMutableList()

        if(datosCercanos.isEmpty()){
            binding.tvNoAsociacionesCercanas.visibility = View.VISIBLE
            binding.tvNoAsociacionesCercanas.setPadding(0, 200, 0, 200)

            binding.rvListaCercana.visibility = View.GONE
        } else {
            binding.tvNoAsociacionesCercanas.visibility = View.INVISIBLE
            binding.rvListaCercana.visibility = View.VISIBLE
        }
        val adaptadorCercano = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosCercanos)

        binding.rvListaCercana.adapter = adaptadorCercano

        binding.rvListaCompleta.setOnItemClickListener { parent, view, position, id ->
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val intent = Intent(requireContext(), MainActivityNegocio::class.java)
            val selectedData = datosFiltrados[position] // Obtén el elemento seleccionado de los datos filtrados
            intent.putExtra("imagenNegocio", selectedData.imagenNegocio)
            intent.putExtra("tituloNegocio", selectedData.tituloNegocio)
            intent.putExtra("disponible", selectedData.disponible)
            intent.putExtra("distancia", selectedData.distancia)
            intent.putExtra("imagenCategoria", selectedData.imagenCategoria)
            intent.putExtra("descripcion", selectedData.descripcion)
            intent.putExtra("abierto", selectedData.disponible)
            intent.putExtra("tipoNegocio", selectedData.tipoNegocio)
            intent.putExtra("direccion", selectedData.direccion)
            intent.putExtra("imagenReal", selectedData.imagenRealNegocio)
            intent.putExtra("categoria", selectedData.nombreCategoria)
            intent.putExtra("horario", selectedData.horario)
            intent.putExtra("insignia", selectedData.insignia)
            intent.putExtra("latitud", selectedData.latitud)
            intent.putExtra("longitud", selectedData.longitud)
            startActivity(intent)
        }

        binding.rvListaCercana.setOnItemClickListener { parent, view, position, id ->
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val intent = Intent(requireContext(), MainActivityNegocio::class.java)
            intent.putExtra("imagenNegocio", datosCercanos[position].imagenNegocio)
            intent.putExtra("tituloNegocio", datosCercanos[position].tituloNegocio)
            intent.putExtra("disponible", datosCercanos[position].disponible)
            intent.putExtra("distancia", datosCercanos[position].distancia)
            intent.putExtra("imagenCategoria", datosCercanos[position].imagenCategoria)
            intent.putExtra("descripcion", datosCercanos[position].descripcion)
            intent.putExtra("abierto", datosCercanos[position].disponible)
            intent.putExtra("tipoNegocio", datosCercanos[position].tipoNegocio)
            intent.putExtra("direccion", datosCercanos[position].direccion)
            intent.putExtra("imagenReal", datosCercanos[position].imagenRealNegocio)
            intent.putExtra("categoria", datosCercanos[position].nombreCategoria)
            intent.putExtra("horario", datosCercanos[position].horario)
            intent.putExtra("insignia", datosCercanos[position].insignia)
            intent.putExtra("latitud", datosCercanos[position].latitud)
            intent.putExtra("longitud", datosCercanos[position].longitud)
            startActivity(intent)
        }

        binding.imgFiltrar.setOnClickListener{view ->
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            PopupMenu(requireContext(), binding.imgFiltrar).apply {
                menuInflater.inflate(R.menu.popmenu, menu)
                setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.item_1 -> {
                            datosFiltrados.clear()
                            datosFiltrados.addAll(datos.filter { elemento ->
                                elemento.nombreCategoria == "Turismo Consciente"
                            })
                            binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                            adaptador.notifyDataSetChanged()
                            true
                        }
                        R.id.item_2 -> {
                            datosFiltrados.clear()
                            datosFiltrados.addAll(datos.filter { elemento ->
                                elemento.nombreCategoria == "Agricultura Regenerativa"
                            })
                            binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                            adaptador.notifyDataSetChanged()
                            true
                        }
                        R.id.item_3 -> {
                            datosFiltrados.clear()
                            datosFiltrados.addAll(datos.filter { elemento ->
                                elemento.nombreCategoria == "Medicina Tradicional"
                            })
                            binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                            adaptador.notifyDataSetChanged()
                            true
                        }
                        R.id.item_4 -> {
                            datosFiltrados.clear()
                            datosFiltrados.addAll(datos.filter { elemento ->
                                elemento.nombreCategoria == "Bioconstrucción"
                            })
                            binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                            adaptador.notifyDataSetChanged()
                            true
                        }
                        R.id.item_5 -> {
                            datosFiltrados.clear()
                            datosFiltrados.addAll(datos)
                            binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                            adaptador.notifyDataSetChanged()
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }



        //cambios aqui
        binding.imgRefresh.setOnClickListener{ view ->
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val sharedPreference = requireContext().getSharedPreferences("archivo", android.content.Context.MODE_PRIVATE)
            val elementosJson = sharedPreference.getString("elementosJson", "")
            val gson = Gson()
            val tipoLista = object : TypeToken<List<Elemento>>() {}.type


            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            //cambios aqui
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //get location here
                val location =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            var latitud : Double? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.latitude
            var longitud : Double? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.longitude

            //actualizar distancia por cada elemento en dato
            val datos = elementosJson?.let {
                try {
                    gson.fromJson<List<Elemento>>(it, tipoLista)
                } catch (e: Exception) {
                    // Manejar cualquier error de deserialización aquí
                    emptyList()  // Si ocurre un error, devuelve una lista vacía o maneja el error según sea necesario
                }
            } ?: emptyList()

            datos.forEach { elemento ->
                val distancia = MainActivity().calcularDistancia(latitud!!.toDouble(), longitud!!.toDouble(), elemento.latitud, elemento.longitud)
                elemento.distancia = String.format("%.2f", distancia) + " km"
            }


            var datosFiltrados = datos.toMutableList()

            var datosCercanos = datos.toMutableList()
            val adaptador = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datos)

            binding.rvListaCompleta.adapter = adaptador


            //acceder a la ubicacion del usuario actual
            /*
            val sharedPreferences2 = requireContext().getSharedPreferences("LatLang", android.content.Context.MODE_PRIVATE)
            val latitud = sharedPreferences2.getString("latitudActual", "")
            val longitud = sharedPreferences2.getString("longitudActual", "")

             */
            Log.d("latitud", latitud.toString())

            Log.d("longitud", longitud.toString())


            //rvListaCercana deberà mostrar unicamente los negocios a 5km a la redonda tomando la distancia de cada elemento
            //no hace falta calcular distancia
            datosCercanos = datosCercanos.filter { elemento ->
                val distanciaStr = elemento.distancia
                val distanciaNumerica = distanciaStr.replace(" km", "").toDoubleOrNull() ?: 0.0
                distanciaNumerica <= 5.0
            }.toMutableList()

            if(datosCercanos.isEmpty()){
                binding.tvNoAsociacionesCercanas.visibility = View.VISIBLE
                binding.tvNoAsociacionesCercanas.setPadding(0, 200, 0, 200)

                binding.rvListaCercana.visibility = View.GONE
            } else {
                binding.tvNoAsociacionesCercanas.visibility = View.INVISIBLE
                binding.rvListaCercana.visibility = View.VISIBLE
            }
            val adaptadorCercano = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosCercanos)

            binding.rvListaCercana.adapter = adaptadorCercano

            binding.rvListaCompleta.setOnItemClickListener { parent, view, position, id ->
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                val intent = Intent(requireContext(), MainActivityNegocio::class.java)
                val selectedData = datosFiltrados[position] // Obtén el elemento seleccionado de los datos filtrados
                intent.putExtra("imagenNegocio", selectedData.imagenNegocio)
                intent.putExtra("tituloNegocio", selectedData.tituloNegocio)
                intent.putExtra("disponible", selectedData.disponible)
                intent.putExtra("distancia", selectedData.distancia)
                intent.putExtra("imagenCategoria", selectedData.imagenCategoria)
                intent.putExtra("descripcion", selectedData.descripcion)
                intent.putExtra("abierto", selectedData.disponible)
                intent.putExtra("tipoNegocio", selectedData.tipoNegocio)
                intent.putExtra("direccion", selectedData.direccion)
                intent.putExtra("imagenReal", selectedData.imagenRealNegocio)
                intent.putExtra("categoria", selectedData.nombreCategoria)
                intent.putExtra("horario", selectedData.horario)
                intent.putExtra("insignia", selectedData.insignia)
                intent.putExtra("latitud", selectedData.latitud)
                intent.putExtra("longitud", selectedData.longitud)
                startActivity(intent)
            }

            binding.rvListaCercana.setOnItemClickListener { parent, view, position, id ->
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                val intent = Intent(requireContext(), MainActivityNegocio::class.java)
                intent.putExtra("imagenNegocio", datosCercanos[position].imagenNegocio)
                intent.putExtra("tituloNegocio", datosCercanos[position].tituloNegocio)
                intent.putExtra("disponible", datosCercanos[position].disponible)
                intent.putExtra("distancia", datosCercanos[position].distancia)
                intent.putExtra("imagenCategoria", datosCercanos[position].imagenCategoria)
                intent.putExtra("descripcion", datosCercanos[position].descripcion)
                intent.putExtra("abierto", datosCercanos[position].disponible)
                intent.putExtra("tipoNegocio", datosCercanos[position].tipoNegocio)
                intent.putExtra("direccion", datosCercanos[position].direccion)
                intent.putExtra("imagenReal", datosCercanos[position].imagenRealNegocio)
                intent.putExtra("categoria", datosCercanos[position].nombreCategoria)
                intent.putExtra("horario", datosCercanos[position].horario)
                intent.putExtra("insignia", datosCercanos[position].insignia)
                intent.putExtra("latitud", datosCercanos[position].latitud)
                intent.putExtra("longitud", datosCercanos[position].longitud)
                startActivity(intent)
            }

            binding.imgFiltrar.setOnClickListener{view ->
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                PopupMenu(requireContext(), binding.imgFiltrar).apply {
                    menuInflater.inflate(R.menu.popmenu, menu)
                    setOnMenuItemClickListener { item ->
                        when(item.itemId) {
                            R.id.item_1 -> {
                                datosFiltrados.clear()
                                datosFiltrados.addAll(datos.filter { elemento ->
                                    elemento.nombreCategoria == "Turismo Consciente"
                                })
                                binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                                adaptador.notifyDataSetChanged()
                                true
                            }
                            R.id.item_2 -> {
                                datosFiltrados.clear()
                                datosFiltrados.addAll(datos.filter { elemento ->
                                    elemento.nombreCategoria == "Agricultura Regenerativa"
                                })
                                binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                                adaptador.notifyDataSetChanged()
                                true
                            }
                            R.id.item_3 -> {
                                datosFiltrados.clear()
                                datosFiltrados.addAll(datos.filter { elemento ->
                                    elemento.nombreCategoria == "Medicina Tradicional"
                                })
                                binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                                adaptador.notifyDataSetChanged()
                                true
                            }
                            R.id.item_4 -> {
                                datosFiltrados.clear()
                                datosFiltrados.addAll(datos.filter { elemento ->
                                    elemento.nombreCategoria == "Bioconstrucción"
                                })
                                binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                                adaptador.notifyDataSetChanged()
                                true
                            }
                            R.id.item_5 -> {
                                datosFiltrados.clear()
                                datosFiltrados.addAll(datos)
                                binding.rvListaCompleta.adapter = CustomAdapter(requireContext(), R.layout.layout_negocio_rv, datosFiltrados)
                                adaptador.notifyDataSetChanged()
                                true
                            }
                            else -> false
                        }
                    }
                    show()
                }
            }
            Toast.makeText(requireContext(), "Actualizado", Toast.LENGTH_SHORT).show()
            Log.d("dadatos", datos.toString())
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}