package ronny.sousa.apprecycleview.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tabela_pokemon") // 1. Define o nome da tabela
data class Pokemon(
    @PrimaryKey(autoGenerate = true) // 2. O Banco vai gerar o ID sozinho (1, 2, 3...)
    val id: Long = 0,

    val nome: String,
    val tipo: String,
    val cp: String,
    val habilidade: String,
    val caminhoFoto: String?
): Serializable