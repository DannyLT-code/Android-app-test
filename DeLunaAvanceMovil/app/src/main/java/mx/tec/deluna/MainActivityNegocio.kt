//Axel Daniel Corona Ibarra - A01425010@tec.mx

package mx.tec.deluna

import android.R.attr.label
import android.R.attr.text
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.translation.Translator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.TranslatorOptions
import com.squareup.picasso.Picasso
import mx.tec.deluna.databinding.ActivityMainNegocioBinding
import mx.tec.deluna.ui.dashboard.DashboardFragment
import mx.tec.deluna.ui.home.HomeFragment


class MainActivityNegocio : AppCompatActivity() {
    lateinit var binding: ActivityMainNegocioBinding
    lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNegocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        val modelManager = RemoteModelManager.getInstance()
        var listaModels = mutableListOf<TranslateRemoteModel>()

        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                for (model in models) {
                    listaModels.add(model)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TRANSLATE", exception.message!!)
            }

        val spanishModel = TranslateRemoteModel.Builder(TranslateLanguage.SPANISH).build()

        if(listaModels.contains(spanishModel)){
            // Model is already downloaded.
        }else{
            var conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            modelManager.download(spanishModel, conditions)
                .addOnSuccessListener {
                    // Model downloaded.
                }
                .addOnFailureListener {
                    // Error.
                }
        }

        binding.tvTituloNeg.text = intent.getStringExtra("tituloNegocio")
        binding.tvTituloNeg.isSelected = true
        Picasso.get().load(intent.getStringExtra("imagenNegocio")).into(binding.imgFotoNegocio)
        binding.tvDescrip.text = intent.getStringExtra("descripcion")
        binding.tvNombreCategoria.text = intent.getStringExtra("categoria")
        binding.imgCatNeg.setImageResource(intent.getIntExtra("imagenCategoria", 0))
        Picasso.get().load(intent.getStringExtra("imagenReal")).into(binding.imgReal)
        binding.tvTipoNegocio.text = intent.getStringExtra("tipoNegocio")
        binding.tvHorario.text = intent.getStringExtra("horario")

        val tituloNegocio = intent.getStringExtra("tituloNegocio")
        val direccion = intent.getStringExtra("direccion")
        val insignia = intent.getBooleanExtra("insignia", false)
        val latitud = intent.getDoubleExtra("latitud", 0.0)
        val longitud = intent.getDoubleExtra("longitud", 0.0)

        if(insignia) {
            binding.imgInsignia.setImageResource(R.drawable.image22)
        } else {
            binding.imgInsignia.visibility = View.GONE
        }

        binding.imgCP.setOnClickListener{ view ->
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", direccion)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, direccion, Toast.LENGTH_SHORT).show()
        }

        binding.tvTituloNeg.setOnLongClickListener(){ view ->
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            Toast.makeText(this, tituloNegocio , Toast.LENGTH_SHORT).show()
            true
        }

       binding.imgInsignia.setOnLongClickListener(){ view ->
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            Toast.makeText(this, "Insignia de negocio verificado", Toast.LENGTH_SHORT).show()
            true
        }

        binding.imgPlay.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

            // Guardar latitud y longitud en SharedPreferences
            val sharedPreferences = getSharedPreferences("LatLang", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("latitudNegocioPlay", latitud.toString())
            editor.putString("longitudNegocioPlay", longitud.toString())
            editor.apply()

            startActivity(intent)
        }

        val descripcionOriginal = intent.getStringExtra("descripcion")
        val tipoOriginal = intent.getStringExtra("tipoNegocio")
        var flag = false
        if(binding.btnListo.text == "Listo"){
            binding.imgTranslate.visibility = View.GONE
            binding.imgTranslate.isClickable = false

        }   else{
            binding.imgTranslate.visibility = View.VISIBLE
            binding.imgTranslate.isClickable = true
            binding.imgTranslate.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                if(flag){
                    binding.tvDescrip.text = descripcionOriginal
                    binding.tvTipoNegocio.text = tipoOriginal
                    flag = false
                }
                else {
                    //translate to english the description using ml kit
                    val descripcion = binding.tvDescrip.text.toString()
                    val tipoNegocio = binding.tvTipoNegocio.text.toString()
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.SPANISH)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                    val englishTranslator =
                        com.google.mlkit.nl.translate.Translation.getClient(options)
                    englishTranslator.translate(descripcion)
                        .addOnSuccessListener { translatedText ->
                            binding.tvDescrip.text = translatedText
                        }
                        .addOnFailureListener { exception ->
                            Log.e("TRANSLATE", exception.message!!)
                        }
                    val englishTranslator2 =
                        com.google.mlkit.nl.translate.Translation.getClient(options)
                    englishTranslator2.translate(tipoNegocio)
                        .addOnSuccessListener { translatedText ->
                            binding.tvTipoNegocio.text = translatedText
                        }
                        .addOnFailureListener { exception ->
                            Log.e("TRANSLATE", exception.message!!)
                        }
                    flag = true
                }
            }
        }




        binding.btnListo.setOnClickListener {
            finish()
        }

    }
}