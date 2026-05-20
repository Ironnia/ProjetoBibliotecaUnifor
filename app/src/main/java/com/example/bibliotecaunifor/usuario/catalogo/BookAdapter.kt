package com.example.bibliotecaunifor.usuario.catalogo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.crud.Entrada
import com.example.bibliotecaunifor.R

class BookAdapter(
    private var entries: List<Entrada>,
    private val onBookClicked: (Entrada) -> Unit,
    private val onReserveClicked: (Entrada) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvAuthor: TextView = view.findViewById(R.id.tv_author)
        val tvAvailability: TextView = view.findViewById(R.id.tv_availability)
        val btnAction: Button = view.findViewById(R.id.btn_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_catalog, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvTitle.text = entry.titulo
        holder.tvAuthor.text = entry.autor
        
        val available = entry.exemplaresDisponiveis
        holder.tvAvailability.text = "Disponibilidade: $available exemplares"
        
        if (available > 0) {
            holder.btnAction.text = "Reservar"
            holder.btnAction.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.success_green)
            holder.tvAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
        } else {
            holder.btnAction.text = "Indisponível"
            holder.btnAction.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.error_red)
            holder.tvAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.error_red))
        }

        holder.itemView.setOnClickListener { onBookClicked(entry) }
        holder.btnAction.setOnClickListener { onReserveClicked(entry) }
    }

    override fun getItemCount() = entries.size
    
    fun updateData(newEntries: List<Entrada>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
