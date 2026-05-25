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
import com.example.bibliotecaunifor.databinding.ItemBookCatalogBinding
import com.example.bibliotecaunifor.databinding.TelaCatalogoBinding

class BookAdapter(
    private var entries: List<Entrada>,
    private var reservedBookIds: Set<String> = emptySet(),
    private val onBookClicked: (Entrada) -> Unit,
    private val onReserveClicked: (Entrada) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {


    class BookViewHolder(val binding: ItemBookCatalogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBookCatalogBinding.inflate(layoutInflater, parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val entry = entries[position]

        with(holder.binding) {
            tvTitle.text = entry.titulo
            tvAuthor.text = entry.autor

            val available = entry.exemplaresDisponiveis
            tvAvailability.text = "Disponibilidade: $available exemplares"

            if (entry.imageUrl.isNotEmpty()) {
                com.bumptech.glide.Glide.with(root.context)
                    .load(entry.imageUrl)
                    .placeholder(R.drawable.menu_book_24)
                    .into(ivIcon)
            } else {
                ivIcon.setImageResource(R.drawable.menu_book_24)
            }

            val jaReservado = reservedBookIds.contains(entry.id)
            if (jaReservado) {
                btnAction.text = "Reservado"
                btnAction.isEnabled = false
                btnAction.backgroundTintList = ContextCompat.getColorStateList(root.context, android.R.color.darker_gray)
            } else if (available > 0) {
                btnAction.text = "Reservar"
                btnAction.isEnabled = true
                btnAction.backgroundTintList = ContextCompat.getColorStateList(root.context, R.color.success_green)
            } else {
                btnAction.text = "Indisponível"
                btnAction.isEnabled = false
                btnAction.backgroundTintList = ContextCompat.getColorStateList(root.context, R.color.error_red)
            }

            root.setOnClickListener { onBookClicked(entry) }
            btnAction.setOnClickListener { onReserveClicked(entry) }
        }

    }

    override fun getItemCount() = entries.size
    
    fun updateData(newEntries: List<Entrada>, newReservedIds: Set<String> = emptySet()) {
        entries = newEntries
        reservedBookIds = newReservedIds
        notifyDataSetChanged()
    }
}
