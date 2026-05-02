package com.example.bibliotecaunifor.usuario.catalogo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R

data class Book(
    val title: String,
    val author: String,
    val availableCopies: Int,
    val isbn: String = "978-0000000000",
    val type: String = "Livro"
)

class BookAdapter(
    private var books: List<Book>,
    private val onBookClicked: (Book) -> Unit,
    private val onReserveClicked: (Book) -> Unit
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
        val book = books[position]
        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author
        
        holder.tvAvailability.text = "Disponibilidade: ${book.availableCopies} exemplares"
        
        if (book.availableCopies > 0) {
            holder.btnAction.text = "Reservar"
            holder.btnAction.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.success_green)
            holder.tvAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
        } else {
            holder.btnAction.text = "Indisponível"
            holder.btnAction.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.error_red)
            holder.tvAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.error_red))
        }

        holder.itemView.setOnClickListener { onBookClicked(book) }
        holder.btnAction.setOnClickListener { onReserveClicked(book) }
    }

    override fun getItemCount() = books.size
    
    fun updateData(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
