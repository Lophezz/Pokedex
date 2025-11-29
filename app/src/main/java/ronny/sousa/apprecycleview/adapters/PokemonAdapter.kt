package ronny.sousa.apprecycleview.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ronny.sousa.apprecycleview.R
import ronny.sousa.apprecycleview.models.Pokemon
import java.io.File

class PokemonAdapter(
    private val lista: ArrayList<Pokemon>,
    private val onClick: (Pokemon) -> Unit // <--- Agora aceita o clique
) : RecyclerView.Adapter<PokemonAdapter.ViewHolderPokemon>() {

    var posicaoClicada = -1

    inner class ViewHolderPokemon(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

        var txtNome: TextView = itemView.findViewById(R.id.txtNome)
        var txtTipo: TextView = itemView.findViewById(R.id.txtTelefone)
        var imgFoto: ImageView = itemView.findViewById(R.id.imgFoto)

        init {
            itemView.setOnCreateContextMenuListener(this)

            // Atualiza a posição quando segura o clique (para o menu Editar/Excluir)
            itemView.setOnLongClickListener {
                posicaoClicada = adapterPosition
                false
            }

            // Configura o clique simples (para Editar direto)
            itemView.setOnClickListener {
                val posicao = adapterPosition
                if (posicao != RecyclerView.NO_POSITION) {
                    onClick(lista[posicao])
                }
            }
        }

        fun bind(pokemon: Pokemon) {
            txtNome.text = pokemon.nome
            txtTipo.text = "${pokemon.tipo} - CP: ${pokemon.cp}"

            if (pokemon.caminhoFoto != null) {
                val imgFile = File(pokemon.caminhoFoto)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    imgFoto.setImageBitmap(bitmap)
                }
            } else {
                imgFoto.setImageResource(R.drawable.ic_baseline_person_50)
            }
        }

        override fun onCreateContextMenu(menu: android.view.ContextMenu?, v: View?, menuInfo: android.view.ContextMenu.ContextMenuInfo?) {
            posicaoClicada = adapterPosition
            android.view.MenuInflater(v?.context).inflate(R.menu.menu_contato, menu)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPokemon {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contato, parent, false)
        return ViewHolderPokemon(view)
    }

    override fun onBindViewHolder(holder: ViewHolderPokemon, position: Int) {
        val pokemon = lista[position]
        holder.bind(pokemon)
    }

    override fun getItemCount(): Int = lista.count()

    // Função necessária para o ContextMenu funcionar na MainActivity
    fun getItem(position: Int): Pokemon {
        return lista[position]
    }
}