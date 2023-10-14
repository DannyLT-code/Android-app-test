package mx.tec.deluna.ui.notifications

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import mx.tec.deluna.databinding.FragmentNotificationsBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        unitConverter()

        val url = "https://1ece5bz509.execute-api.us-east-1.amazonaws.com/latest"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("eeeee", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("eeeee", response.toString())
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val exchangeRates = parseExchangeRates(responseBody)
                    if (exchangeRates != null) {
                        val mxnToUsd = convertCurrency(1.0, exchangeRates, "MXN", "USD")
                        val usdToEur = convertCurrency(1.0, exchangeRates, "USD", "EUR")
                        val eurToMxn = convertCurrency(1.0, exchangeRates, "EUR", "MXN")
                        val mxnToEur = convertCurrency(1.0, exchangeRates, "MXN", "EUR")
                        val usdToMxn = convertCurrency(1.0, exchangeRates, "USD", "MXN")
                        val eurToUsd = convertCurrency(1.0, exchangeRates, "EUR", "USD")

                        activity?.runOnUiThread {
                            val listCurrency = listOf("MXN", "USD", "EUR")
                            val adapterCurrency = ArrayAdapter(
                                requireContext(),
                                R.layout.simple_list_item_1,
                                listCurrency
                            )

                            binding.spCurrencyUno.adapter = adapterCurrency
                            binding.spCurrencyDos.adapter = adapterCurrency

                            binding.btnConvertirCurrency.setOnClickListener {
                                val currencyUno = binding.spCurrencyUno.selectedItem.toString()
                                val currencyDos = binding.spCurrencyDos.selectedItem.toString()
                                val numberStr = binding.edtCurrency.text.toString()

                                if (numberStr.isNotEmpty()) {
                                    val number = numberStr.toDouble()
                                    if (currencyUno == currencyDos) {
                                        binding.tvResultCurrency.text = number.toString()
                                    } else {
                                        var result = 0.0

                                        when (currencyUno) {
                                            "MXN" -> {
                                                when (currencyDos) {
                                                    "USD" -> result = number * mxnToUsd
                                                    "EUR" -> result = number * mxnToEur
                                                }
                                            }

                                            "USD" -> {
                                                when (currencyDos) {
                                                    "MXN" -> result = number * usdToMxn
                                                    "EUR" -> result = number * usdToEur
                                                }
                                            }

                                            "EUR" -> {
                                                when (currencyDos) {
                                                    "MXN" -> result = number * eurToMxn
                                                    "USD" -> result = number * eurToUsd
                                                }
                                            }
                                        }

                                        Log.d("eeeee", result.toString())
                                        binding.tvResultCurrency.text = result.toString()
                                    }
                                } else {
                                    binding.tvResultCurrency.text = ""
                                    return@setOnClickListener
                                }
                            }
                        }
                    }
                }
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun unitConverter() {
        //Lista de unidades en español
        val listaTipoUnidades = listOf("Longitud", "Masa", "Velocidad", "Temperatura")
        val listaLongitud = listOf(
            "Kilómetros",
            "Metros",
            "Centímetros",
            "Milímetros",
            "Millas",
            "Yardas",
            "Pies",
            "Pulgadas"
        )
        val listaMasa = listOf(
            "Toneladas",
            "Kilogramos",
            "Gramos",
            "Miligramos",
            "Toneladas cortas",
            "Toneladas largas",
            "Libras",
            "Onzas"
        )
        val listaVelocidad = listOf(
            "Metros por segundo",
            "Kilómetros por hora",
            "Millas por hora",
            "Nudos",
            "Pies por segundo"
        )
        val listaTemperatura = listOf("Celsius", "Fahrenheit", "Kelvin")

        //Lista de unidades en inglés
        val listTypeUnits = listOf("Length", "Mass", "Speed", "Temperature")
        val listLength = listOf(
            "Kilometers",
            "Meters",
            "Centimeters",
            "Millimeters",
            "Miles",
            "Yards",
            "Feet",
            "Inches"
        )
        val listMass = listOf(
            "Tons",
            "Kilograms",
            "Grams",
            "Milligrams",
            "Short tons",
            "Long tons",
            "Pounds",
            "Ounces"
        )
        val listSpeed = listOf(
            "Meters per second",
            "Kilometers per hour",
            "Miles per hour",
            "Knots",
            "Feet per second"
        )
        val listTemperature = listOf("Celsius", "Fahrenheit", "Kelvin")

        val adapter: ArrayAdapter<String>
        val adapterLongitud: ArrayAdapter<String>
        val adapterMasa: ArrayAdapter<String>
        val adapterVelocidad: ArrayAdapter<String>
        val adapterTemperatura: ArrayAdapter<String>

        if (binding.tvAjustes.text != "Utilidades") {
            adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listTypeUnits)
            adapterLongitud =
                ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listLength)
            adapterMasa = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listMass)
            adapterVelocidad =
                ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listSpeed)
            adapterTemperatura =
                ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listTemperature)

        } else {
            adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listaTipoUnidades)
            adapterLongitud =
                ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listaLongitud)
            adapterMasa = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listaMasa)
            adapterVelocidad =
                ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listaVelocidad)
            adapterTemperatura =
                ArrayAdapter(requireContext(), R.layout.simple_list_item_1, listaTemperatura)
        }

        binding.spTipoUnidades.adapter = adapter

        binding.spTipoUnidades.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    when (position) {
                        0 -> {
                            binding.spUnidadUno.adapter = adapterLongitud
                            binding.spUnidadDos.adapter = adapterLongitud
                        }

                        1 -> {
                            binding.spUnidadUno.adapter = adapterMasa
                            binding.spUnidadDos.adapter = adapterMasa
                        }

                        2 -> {
                            binding.spUnidadUno.adapter = adapterVelocidad
                            binding.spUnidadDos.adapter = adapterVelocidad
                        }

                        3 -> {
                            binding.spUnidadUno.adapter = adapterTemperatura
                            binding.spUnidadDos.adapter = adapterTemperatura
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        binding.btnConvertir.setOnClickListener {

            val numeroStr = binding.edtNumber.text.toString()

            if (numeroStr.isEmpty()) {
                binding.tvRespuesta.text = ""
                return@setOnClickListener
            }

            val numero = binding.edtNumber.text.toString().toDouble()
            val unidadUno = binding.spUnidadUno.selectedItem.toString()
            val unidadDos = binding.spUnidadDos.selectedItem.toString()


            var resultado = 0.0
            if (unidadUno == unidadDos) {
                binding.tvRespuesta.text = numero.toString()
            } else if (binding.tvAjustes.text == "Utilidades") {
                when (unidadUno) {
                    "Kilómetros" -> {
                        when (unidadDos) {
                            "Metros" -> resultado = numero * 1000
                            "Centímetros" -> resultado = numero * 100000
                            "Milímetros" -> resultado = numero * 1000000
                            "Millas" -> resultado = numero * 0.621371
                            "Yardas" -> resultado = numero * 1093.61
                            "Pies" -> resultado = numero * 3280.84
                            "Pulgadas" -> resultado = numero * 39370.1
                        }
                    }

                    "Metros" -> {
                        when (unidadDos) {
                            "Kilómetros" -> resultado = numero * 0.001
                            "Centímetros" -> resultado = numero * 100
                            "Milímetros" -> resultado = numero * 1000
                            "Millas" -> resultado = numero * 0.000621371
                            "Yardas" -> resultado = numero * 1.09361
                            "Pies" -> resultado = numero * 3.28084
                            "Pulgadas" -> resultado = numero * 39.3701
                        }
                    }

                    "Centímetros" -> {
                        when (unidadDos) {
                            "Kilómetros" -> resultado = numero * 0.00001
                            "Metros" -> resultado = numero * 0.01
                            "Milímetros" -> resultado = numero * 10
                            "Millas" -> resultado = numero * 0.0000062137
                            "Yardas" -> resultado = numero * 0.0109361
                            "Pies" -> resultado = numero * 0.0328084
                            "Pulgadas" -> resultado = numero * 0.393701
                        }
                    }

                    "Milímetros" -> {
                        when (unidadDos) {
                            "Kilómetros" -> resultado = numero * 0.000001
                            "Metros" -> resultado = numero * 0.001
                            "Centímetros" -> resultado = numero * 0.1
                            "Millas" -> resultado = numero * 0.00000062137
                            "Yardas" -> resultado = numero * 0.00109361
                            "Pies" -> resultado = numero * 0.00328084
                            "Pulgadas" -> resultado = numero * 0.0393701
                        }
                    }

                    "Millas" -> {
                        when (unidadDos) {
                            "Kilómetros" -> resultado = numero * 1.60934
                            "Metros" -> resultado = numero * 1609.34
                            "Centímetros" -> resultado = numero * 160934
                            "Milímetros" -> resultado = numero * 1609340
                            "Yardas" -> resultado = numero * 1760
                            "Pies" -> resultado = numero * 5280
                            "Pulgadas" -> resultado = numero * 63360
                        }
                    }

                    "Yardas" -> {
                        when (unidadDos) {
                            "Kilómetros" -> resultado = numero * 0.0009144
                            "Metros" -> resultado = numero * 0.9144
                            "Centímetros" -> resultado = numero * 91.44
                            "Milímetros" -> resultado = numero * 914.4
                            "Millas" -> resultado = numero * 0.000568182
                            "Pies" -> resultado = numero * 3
                            "Pulgadas" -> resultado = numero * 36
                        }
                    }

                    "Pies" -> {
                        when (unidadDos) {
                            "Kilómetros" -> resultado = numero * 0.0003048
                            "Metros" -> resultado = numero * 0.3048
                            "Centímetros" -> resultado = numero * 30.48
                            "Milímetros" -> resultado = numero * 304.8
                            "Millas" -> resultado = numero * 0.000189394
                            "Yardas" -> resultado = numero * 0.333333
                            "Pulgadas" -> resultado = numero * 12
                        }
                    }

                    "Pulgadas" -> {
                        when (unidadDos) {
                            "Kilómetros" -> resultado = numero * 0.0000254
                            "Metros" -> resultado = numero * 0.0254
                            "Centímetros" -> resultado = numero * 2.54
                            "Milímetros" -> resultado = numero * 25.4
                            "Millas" -> resultado = numero * 0.0000157828
                            "Yardas" -> resultado = numero * 0.0277778
                            "Pies" -> resultado = numero * 0.0833333
                        }
                    }

                    "Toneladas" -> {
                        when (unidadDos) {
                            "Kilogramos" -> resultado = numero * 1000
                            "Gramos" -> resultado = numero * 1000000
                            "Miligramos" -> resultado = numero * 1000000000
                            "Toneladas cortas" -> resultado = numero * 1.10231
                            "Toneladas largas" -> resultado = numero * 0.984207
                            "Libras" -> resultado = numero * 2204.62
                            "Onzas" -> resultado = numero * 35274
                        }
                    }

                    "Kilogramos" -> {
                        when (unidadDos) {
                            "Toneladas" -> resultado = numero * 0.001
                            "Gramos" -> resultado = numero * 1000
                            "Miligramos" -> resultado = numero * 1000000
                            "Toneladas cortas" -> resultado = numero * 0.00110231
                            "Toneladas largas" -> resultado = numero * 0.000984207
                            "Libras" -> resultado = numero * 2.20462
                            "Onzas" -> resultado = numero * 35.274
                        }
                    }

                    "Gramos" -> {
                        when (unidadDos) {
                            "Toneladas" -> resultado = numero * 0.000001
                            "Kilogramos" -> resultado = numero * 0.001
                            "Miligramos" -> resultado = numero * 1000
                            "Toneladas cortas" -> resultado = numero * 0.0000011023
                            "Toneladas largas" -> resultado = numero * 0.00000098421
                            "Libras" -> resultado = numero * 0.00220462
                            "Onzas" -> resultado = numero * 0.035274
                        }
                    }

                    "Miligramos" -> {
                        when (unidadDos) {
                            "Toneladas" -> resultado = numero * 0.000000001
                            "Kilogramos" -> resultado = numero * 0.000001
                            "Gramos" -> resultado = numero * 0.001
                            "Toneladas cortas" -> resultado = numero * 0.0000000011023
                            "Toneladas largas" -> resultado = numero * 0.00000000098421
                            "Libras" -> resultado = numero * 0.0000022046
                            "Onzas" -> resultado = numero * 0.000035274
                        }
                    }

                    "Toneladas cortas" -> {
                        when (unidadDos) {
                            "Toneladas" -> resultado = numero * 0.907185
                            "Kilogramos" -> resultado = numero * 907.185
                            "Gramos" -> resultado = numero * 907185
                            "Miligramos" -> resultado = numero * 907185000
                            "Toneladas largas" -> resultado = numero * 0.892857
                            "Libras" -> resultado = numero * 2000
                            "Onzas" -> resultado = numero * 32000
                        }
                    }

                    "Toneladas largas" -> {
                        when (unidadDos) {
                            "Toneladas" -> resultado = numero * 1.01605
                            "Kilogramos" -> resultado = numero * 1016.05
                            "Gramos" -> resultado = numero * 1016050
                            "Miligramos" -> resultado = numero * 1016050000
                            "Toneladas cortas" -> resultado = numero * 1.12
                            "Libras" -> resultado = numero * 2240
                            "Onzas" -> resultado = numero * 35840
                        }
                    }

                    "Libras" -> {
                        when (unidadDos) {
                            "Toneladas" -> resultado = numero * 0.000453592
                            "Kilogramos" -> resultado = numero * 0.453592
                            "Gramos" -> resultado = numero * 453.592
                            "Miligramos" -> resultado = numero * 453592
                            "Toneladas cortas" -> resultado = numero * 0.0005
                            "Toneladas largas" -> resultado = numero * 0.000446429
                            "Onzas" -> resultado = numero * 16
                        }
                    }

                    "Onzas" -> {
                        when (unidadDos) {
                            "Toneladas" -> resultado = numero * 0.0000283495
                            "Kilogramos" -> resultado = numero * 0.0283495
                            "Gramos" -> resultado = numero * 28.3495
                            "Miligramos" -> resultado = numero * 28349.5
                            "Toneladas cortas" -> resultado = numero * 0.00003125
                            "Toneladas largas" -> resultado = numero * 0.0000279018
                            "Libras" -> resultado = numero * 0.0625
                        }
                    }

                    "Metros por segundo" -> {
                        when (unidadDos) {
                            "Kilómetros por hora" -> resultado = numero * 3.6
                            "Millas por hora" -> resultado = numero * 2.23694
                            "Nudos" -> resultado = numero * 1.94384
                            "Pies por segundo" -> resultado = numero * 3.28084
                        }
                    }

                    "Kilómetros por hora" -> {
                        when (unidadDos) {
                            "Metros por segundo" -> resultado = numero * 0.277778
                            "Millas por hora" -> resultado = numero * 0.621371
                            "Nudos" -> resultado = numero * 0.539957
                            "Pies por segundo" -> resultado = numero * 0.911344
                        }
                    }

                    "Millas por hora" -> {
                        when (unidadDos) {
                            "Metros por segundo" -> resultado = numero * 0.44704
                            "Kilómetros por hora" -> resultado = numero * 1.60934
                            "Nudos" -> resultado = numero * 0.868976
                            "Pies por segundo" -> resultado = numero * 1.46667
                        }
                    }

                    "Nudos" -> {
                        when (unidadDos) {
                            "Metros por segundo" -> resultado = numero * 0.514444
                            "Kilómetros por hora" -> resultado = numero * 1.852
                            "Millas por hora" -> resultado = numero * 1.15078
                            "Pies por segundo" -> resultado = numero * 1.68781
                        }
                    }

                    "Pies por segundo" -> {
                        when (unidadDos) {
                            "Metros por segundo" -> resultado = numero * 0.3048
                            "Kilómetros por hora" -> resultado = numero * 1.09728
                            "Millas por hora" -> resultado = numero * 0.681818
                            "Nudos" -> resultado = numero * 0.592484
                        }
                    }
                }
            } else {
                when (unidadUno) {
                    "Kilometers" -> {
                        when (unidadDos) {
                            "Meters" -> resultado = numero * 1000
                            "Centimeters" -> resultado = numero * 100000
                            "Millimeters" -> resultado = numero * 1000000
                            "Miles" -> resultado = numero * 0.621371
                            "Yards" -> resultado = numero * 1093.61
                            "Feet" -> resultado = numero * 3280.84
                            "Inches" -> resultado = numero * 39370.1
                        }
                    }

                    "Meters" -> {
                        when (unidadDos) {
                            "Kilometers" -> resultado = numero * 0.001
                            "Centimeters" -> resultado = numero * 100
                            "Millimeters" -> resultado = numero * 1000
                            "Miles" -> resultado = numero * 0.000621371
                            "Yards" -> resultado = numero * 1.09361
                            "Feet" -> resultado = numero * 3.28084
                            "Inches" -> resultado = numero * 39.3701
                        }
                    }

                    "Centimeters" -> {
                        when (unidadDos) {
                            "Kilometers" -> resultado = numero * 0.00001
                            "Meters" -> resultado = numero * 0.01
                            "Millimeters" -> resultado = numero * 10
                            "Miles" -> resultado = numero * 0.0000062137
                            "Yards" -> resultado = numero * 0.0109361
                            "Feet" -> resultado = numero * 0.0328084
                            "Inches" -> resultado = numero * 0.393701
                        }
                    }

                    "Millimeters" -> {
                        when (unidadDos) {
                            "Kilometers" -> resultado = numero * 0.000001
                            "Meters" -> resultado = numero * 0.001
                            "Centimeters" -> resultado = numero * 0.1
                            "Miles" -> resultado = numero * 0.00000062137
                            "Yards" -> resultado = numero * 0.00109361
                            "Feet" -> resultado = numero * 0.00328084
                            "Inches" -> resultado = numero * 0.0393701
                        }
                    }

                    "Miles" -> {
                        when (unidadDos) {
                            "Kilometers" -> resultado = numero * 1.60934
                            "Meters" -> resultado = numero * 1609.34
                            "Centimeters" -> resultado = numero * 160934
                            "Millimeters" -> resultado = numero * 1609340
                            "Yards" -> resultado = numero * 1760
                            "Feet" -> resultado = numero * 5280
                            "Inches" -> resultado = numero * 63360
                        }
                    }

                    "Yards" -> {
                        when (unidadDos) {
                            "Kilometers" -> resultado = numero * 0.0009144
                            "Meters" -> resultado = numero * 0.9144
                            "Centimeters" -> resultado = numero * 91.44
                            "Millimeters" -> resultado = numero * 914.4
                            "Miles" -> resultado = numero * 0.000568182
                            "Feet" -> resultado = numero * 3
                            "Inches" -> resultado = numero * 36
                        }
                    }

                    "Feet" -> {
                        when (unidadDos) {
                            "Kilometers" -> resultado = numero * 0.0003048
                            "Meters" -> resultado = numero * 0.3048
                            "Centimeters" -> resultado = numero * 30.48
                            "Millimeters" -> resultado = numero * 304.8
                            "Miles" -> resultado = numero * 0.000189394
                            "Yards" -> resultado = numero * 0.333333
                            "Inches" -> resultado = numero * 12
                        }
                    }

                    "Inches" -> {
                        when (unidadDos) {
                            "Kilometers" -> resultado = numero * 0.0000254
                            "Meters" -> resultado = numero * 0.0254
                            "Centimeters" -> resultado = numero * 2.54
                            "Millimeters" -> resultado = numero * 25.4
                            "Miles" -> resultado = numero * 0.0000157828
                            "Yards" -> resultado = numero * 0.0277778
                            "Feet" -> resultado = numero * 0.0833333
                        }
                    }

                    "Tons" -> {
                        when (unidadDos) {
                            "Kilograms" -> resultado = numero * 1000
                            "Grams" -> resultado = numero * 1000000
                            "Milligrams" -> resultado = numero * 1000000000
                            "Short tons" -> resultado = numero * 1.10231
                            "Long tons" -> resultado = numero * 0.984207
                            "Pounds" -> resultado = numero * 2204.62
                            "Ounces" -> resultado = numero * 35274
                        }
                    }

                    "Kilograms" -> {
                        when (unidadDos) {
                            "Tons" -> resultado = numero * 0.001
                            "Grams" -> resultado = numero * 1000
                            "Milligrams" -> resultado = numero * 1000000
                            "Short tons" -> resultado = numero * 0.00110231
                            "Long tons" -> resultado = numero * 0.000984207
                            "Pounds" -> resultado = numero * 2.20462
                            "Ounces" -> resultado = numero * 35.274
                        }
                    }

                    "Grams" -> {
                        when (unidadDos) {
                            "Tons" -> resultado = numero * 0.000001
                            "Kilograms" -> resultado = numero * 0.001
                            "Milligrams" -> resultado = numero * 1000
                            "Short tons" -> resultado = numero * 0.0000011023
                            "Long tons" -> resultado = numero * 0.00000098421
                            "Pounds" -> resultado = numero * 0.00220462
                            "Ounces" -> resultado = numero * 0.035274
                        }
                    }

                    "Milligrams" -> {
                        when (unidadDos) {
                            "Tons" -> resultado = numero * 0.000000001
                            "Kilograms" -> resultado = numero * 0.000001
                            "Grams" -> resultado = numero * 0.001
                            "Short tons" -> resultado = numero * 0.0000000011023
                            "Long tons" -> resultado = numero * 0.00000000098421
                            "Pounds" -> resultado = numero * 0.0000022046
                            "Ounces" -> resultado = numero * 0.000035274
                        }
                    }

                    "Short tons" -> {
                        when (unidadDos) {
                            "Tons" -> resultado = numero * 0.907185
                            "Kilograms" -> resultado = numero * 907.185
                            "Grams" -> resultado = numero * 907185
                            "Milligrams" -> resultado = numero * 907185000
                            "Long tons" -> resultado = numero * 0.892857
                            "Pounds" -> resultado = numero * 2000
                            "Ounces" -> resultado = numero * 32000
                        }
                    }

                    "Long tons" -> {
                        when (unidadDos) {
                            "Tons" -> resultado = numero * 1.01605
                            "Kilograms" -> resultado = numero * 1016.05
                            "Grams" -> resultado = numero * 1016050
                            "Milligrams" -> resultado = numero * 1016050000
                            "Short tons" -> resultado = numero * 1.12
                            "Pounds" -> resultado = numero * 2240
                            "Ounces" -> resultado = numero * 35840
                        }
                    }

                    "Pounds" -> {
                        when (unidadDos) {
                            "Tons" -> resultado = numero * 0.000453592
                            "Kilograms" -> resultado = numero * 0.453592
                            "Grams" -> resultado = numero * 453.592
                            "Milligrams" -> resultado = numero * 453592
                            "Short tons" -> resultado = numero * 0.0005
                            "Long tons" -> resultado = numero * 0.000446429
                            "Ounces" -> resultado = numero * 16
                        }
                    }

                    "Ounces" -> {
                        when (unidadDos) {
                            "Tons" -> resultado = numero * 0.0000283495
                            "Kilograms" -> resultado = numero * 0.0283495
                            "Grams" -> resultado = numero * 28.3495
                            "Milligrams" -> resultado = numero * 28349.5
                            "Short tons" -> resultado = numero * 0.00003125
                            "Long tons" -> resultado = numero * 0.0000279018
                            "Pounds" -> resultado = numero * 0.0625
                        }
                    }

                    "Meters per second" -> {
                        when (unidadDos) {
                            "Kilometers per hour" -> resultado = numero * 3.6
                            "Miles per hour" -> resultado = numero * 2.23694
                            "Knots" -> resultado = numero * 1.94384
                            "Feet per second" -> resultado = numero * 3.28084
                        }
                    }

                    "Kilometers per hour" -> {
                        when (unidadDos) {
                            "Meters per second" -> resultado = numero * 0.277778
                            "Miles per hour" -> resultado = numero * 0.621371
                            "Knots" -> resultado = numero * 0.539957
                            "Feet per second" -> resultado = numero * 0.911344
                        }
                    }

                    "Miles per hour" -> {
                        when (unidadDos) {
                            "Meters per second" -> resultado = numero * 0.44704
                            "Kilometers per hour" -> resultado = numero * 1.60934
                            "Knots" -> resultado = numero * 0.868976
                            "Feet per second" -> resultado = numero * 1.46667
                        }
                    }

                    "Knots" -> {
                        when (unidadDos) {
                            "Meters per second" -> resultado = numero * 0.514444
                            "Kilometers per hour" -> resultado = numero * 1.852
                            "Miles per hour" -> resultado = numero * 1.15078
                            "Feet per second" -> resultado = numero * 1.68781
                        }
                    }

                    "Feet per second" -> {
                        when (unidadDos) {
                            "Meters per second" -> resultado = numero * 0.3048
                            "Kilometers per hour" -> resultado = numero * 1.09728
                            "Miles per hour" -> resultado = numero * 0.681818
                            "Knots" -> resultado = numero * 0.592484
                        }
                    }
                }
            }
            when (unidadUno) {
                "Celsius" -> {
                    when (unidadDos) {
                        "Fahrenheit" -> resultado = numero * 1.8 + 32
                        "Kelvin" -> resultado = numero + 273.15
                    }
                }

                "Fahrenheit" -> {
                    when (unidadDos) {
                        "Celsius" -> resultado = (numero - 32) / 1.8
                        "Kelvin" -> resultado = (numero + 459.67) / 1.8
                    }
                }

                "Kelvin" -> {
                    when (unidadDos) {
                        "Celsius" -> resultado = numero - 273.15
                        "Fahrenheit" -> resultado = numero * 1.8 - 459.67
                    }
                }
            }
            binding.tvRespuesta.text = resultado.toString()
        }

    }


    private fun parseExchangeRates(json: String?): Map<String, Double>? {
        try {
            val jsonObject = JSONObject(json)
            val ratesObject = jsonObject.getJSONObject("rates")
            val ratesMap = mutableMapOf<String, Double>()

            ratesMap["MXN"] = ratesObject.getDouble("MXN")
            ratesMap["USD"] = ratesObject.getDouble("USD")
            ratesMap["EUR"] = ratesObject.getDouble("EUR")

            return ratesMap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun convertCurrency(
        amount: Double,
        exchangeRates: Map<String, Double>,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        val fromRate = exchangeRates[fromCurrency]
        val toRate = exchangeRates[toCurrency]

        if (fromRate != null && toRate != null) {
            return amount * (toRate / fromRate)
        }
        return 0.0
    }
}