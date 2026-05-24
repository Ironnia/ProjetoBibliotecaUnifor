package com.example.bibliotecaunifor.usuario.configuracoes

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.TelaChatSuporteBinding
import com.example.bibliotecaunifor.usuario.utils.NavigationUtils

class ChatSuporteActivity : AppCompatActivity() {
    private lateinit var binding: TelaChatSuporteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TelaChatSuporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.etChatInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                val msg = binding.etChatInput.text.toString()
                if (msg.isNotEmpty()) {
                    Toast.makeText(this, "Mensagem enviada para o suporte!", Toast.LENGTH_SHORT).show()
                    binding.etChatInput.text.clear()
                }
                true
            } else {
                false
            }
        }

        NavigationUtils.navegacaoAluno(this, binding.bottomNavigation, R.id.navigation_perfil)
    }
}
