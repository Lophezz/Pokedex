package ronny.sousa.apprecycleview

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ronny.sousa.apprecycleview.models.Pokemon

@Dao
interface PokemonDao {

    @Insert
    suspend fun inserir(pokemon: Pokemon)

    @Update
    suspend fun atualizar(pokemon: Pokemon)

    @Delete
    suspend fun deletar(pokemon: Pokemon)

    @Query("SELECT * FROM tabela_pokemon ORDER BY nome ASC")
    suspend fun listarTodos(): List<Pokemon>
}