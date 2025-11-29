package ronny.sousa.apprecycleview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.lifecycle.lifecycleScope // <--- Necessário para o Banco
import kotlinx.coroutines.launch          // <--- Necessário para o Banco
import ronny.sousa.apprecycleview.adapters.PokemonAdapter
import ronny.sousa.apprecycleview.models.Pokemon

class MainActivity : AppCompatActivity() {
    lateinit var listaRecycler: RecyclerView
    lateinit var adapter: PokemonAdapter
    lateinit var btnAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listaRecycler = findViewById(R.id.ListaContatos)
        btnAdd = findViewById(R.id.btnAdd)

        btnAdd.setOnClickListener {
            val intent = Intent(this, FormPokemonActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recarrega a lista do BANCO DE DADOS toda vez que a tela aparece
        carregarLista()
    }

    fun carregarLista() {
        // Abre uma thread paralela para buscar no banco
        lifecycleScope.launch {
            // 1. Busca todos os pokemons salvos
            val listaDoBanco = AppDatabase.getDatabase(this@MainActivity).pokemonDao().listarTodos()

            // 2. Converte para ArrayList (o Adapter gosta de ArrayList)
            val arrayListPokemon = ArrayList(listaDoBanco)

            // 3. Configura o Adapter
            // O segundo parâmetro { ... } é o que acontece no CLIQUE SIMPLES (Editar)
            adapter = PokemonAdapter(arrayListPokemon) { pokemonClicado ->
                val intent = Intent(this@MainActivity, FormPokemonActivity::class.java)
                intent.putExtra("pokemon", pokemonClicado)
                startActivity(intent)
            }

            listaRecycler.adapter = adapter
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val posicao = adapter.posicaoClicada

        // Verifica se clicou em algo válido
        if (posicao < 0) {
            return super.onContextItemSelected(item)
        }

        // Recupera o Pokémon que estava naquela posição visual
        // ATENÇÃO: Certifique-se de ter adicionado a fun getItem(pos) no Adapter como combinamos
        // Se der erro aqui, adicione "fun getItem(pos: Int) = lista[pos]" no PokemonAdapter.kt
        val pokemon = adapter.getItem(posicao)

        when(item.itemId) {
            R.id.menu_editar -> {
                val intent = Intent(this, FormPokemonActivity::class.java)
                intent.putExtra("pokemon", pokemon)
                startActivity(intent)
            }
            R.id.menu_excluir -> {
                // Deleta do BANCO DE DADOS
                lifecycleScope.launch {
                    val dao = AppDatabase.getDatabase(this@MainActivity).pokemonDao()
                    dao.deletar(pokemon)

                    // Atualiza a tela recarregando a lista do zero
                    carregarLista()

                    Toast.makeText(this@MainActivity, "Pokémon libertado!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return super.onContextItemSelected(item)
    }
}