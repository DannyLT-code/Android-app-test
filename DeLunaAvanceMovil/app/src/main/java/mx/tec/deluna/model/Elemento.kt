package mx.tec.deluna.model


data class Elemento(val id: Int, val imagenNegocio: String, val tituloNegocio:String, var disponible:String, var distancia:String, val imagenCategoria: Int, val descripcion: String, val insignia: Boolean, val tipoNegocio: String, val direccion: String, val imagenRealNegocio: String, val nombreCategoria: String, val horario: String, val latitud: Double, val longitud: Double)