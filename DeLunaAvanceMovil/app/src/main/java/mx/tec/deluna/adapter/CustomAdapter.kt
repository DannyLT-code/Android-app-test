package mx.tec.deluna.adapter

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import mx.tec.deluna.R
import mx.tec.deluna.model.Elemento


class CustomAdapter(val context: Context, val layout: Int, val datos: List<Elemento>): BaseAdapter() {
    override fun getCount(): Int {
        return datos.size
    }

    override fun getItem(position: Int): Any {
        return datos[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(layout, parent, false)

        val linearImg = view.findViewById<LinearLayout>(R.id.linearImg)
        val isDarkMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val imgNegocio = view.findViewById<ImageView>(R.id.imgNegocio)
        val txtTituloNegocio = view.findViewById<TextView>(R.id.tvTitulo)
        txtTituloNegocio.isSelected = true
        val txtDisponible = view.findViewById<TextView>(R.id.tvTipo)
        val txtDistancia = view.findViewById<TextView>(R.id.tvHorario)
        val imgCategoria = view.findViewById<ImageView>(R.id.imgCategoria)
        val elemento = getItem(position) as Elemento

        if(isDarkMode){
            linearImg.setBackgroundResource(R.drawable.gradient_dark)
        }else{
            linearImg.setBackgroundResource(R.drawable.gradient)
        }

        Picasso.get().load(elemento.imagenNegocio).into(imgNegocio)
        txtTituloNegocio.text = elemento.tituloNegocio
        txtDisponible.text = elemento.disponible
        if(elemento.disponible == "Abierto"){
            txtDisponible.setTextColor(context.resources.getColor(R.color.green))
        }else{
            txtDisponible.setTextColor(context.resources.getColor(R.color.red))
        }
        txtDistancia.text = elemento.distancia
        imgCategoria.setImageResource(elemento.imagenCategoria)

        return view
    }
}