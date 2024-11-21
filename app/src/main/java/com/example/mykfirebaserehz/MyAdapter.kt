package com.example.mykfirebaserehz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class MyAdapter(private val context: Context, private val arrayList: ArrayList<Contacto>) : BaseAdapter() {
    override fun getCount(): Int = arrayList.size

    override fun getItem(position: Int): Any = arrayList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.misfilas, parent, false)
        val txtNombre: TextView = view.findViewById(R.id.txtminombre)
        val txtAlias: TextView = view.findViewById(R.id.txtmialias)
        val imgImagen: ImageView = view.findViewById(R.id.imgimagen)

        val contacto = arrayList[position]

        txtNombre.text = contacto.nombre
        txtAlias.text = contacto.alias
        Glide.with(context).load(contacto.urlImagen).into(imgImagen)

        return view
    }
}
