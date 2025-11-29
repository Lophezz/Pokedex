package ronny.sousa.apprecycleview

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ronny.sousa.apprecycleview.models.Pokemon
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FormPokemonActivity : AppCompatActivity() {

    lateinit var txtNome: EditText
    lateinit var txtTipo: EditText
    lateinit var txtHabilidade: EditText
    lateinit var txtCp: EditText
    lateinit var imgFoto: ImageView
    lateinit var btnSalvar: Button

    var pokemonAtual: Pokemon? = null
    private var photoFile: File? = null
    private var caminhoFotoSalva: String? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val CODIGO_PERMISSAO_CAMERA = 100 // Código para identificar o pedido

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_contato)

        // Vincula os IDs do XML
        txtNome = findViewById(R.id.txtNomeForm)
        txtTipo = findViewById(R.id.txtTelefoneForm)
        txtCp = findViewById(R.id.txtEmailForm)
        txtHabilidade = findViewById(R.id.txtEnderecoForm)
        imgFoto = findViewById(R.id.imgFotoForm)
        btnSalvar = findViewById(R.id.btnSalvar)

        // Configura placeholders
        txtTipo.hint = "Tipo (Ex: Fogo)"
        txtCp.hint = "CP (Combat Power)"
        txtHabilidade.hint = "Habilidade"

        // Verifica se é edição
        if (intent.hasExtra("pokemon")) {
            pokemonAtual = intent.getSerializableExtra("pokemon") as Pokemon
            preencherCampos(pokemonAtual!!)
        }

        btnSalvar.setOnClickListener {
            salvarPokemon()
        }

        // --- MUDANÇA 1: Verificação de Permissão ao Clicar ---
        imgFoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Se não tem permissão, pede agora
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CODIGO_PERMISSAO_CAMERA)
            } else {
                // Se já tem, abre a câmera
                tirarFoto()
            }
        }
    }

    // --- MUDANÇA 2: Ouvir se o usuário aceitou a permissão ---
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODIGO_PERMISSAO_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            tirarFoto()
        } else {
            Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun preencherCampos(p: Pokemon) {
        txtNome.setText(p.nome)
        txtTipo.setText(p.tipo)
        txtCp.setText(p.cp)
        txtHabilidade.setText(p.habilidade)
        caminhoFotoSalva = p.caminhoFoto

        if (caminhoFotoSalva != null) {
            val file = File(caminhoFotoSalva!!)
            if(file.exists()){
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imgFoto.setImageBitmap(bitmap)
            }
        }
    }

    private fun salvarPokemon() {
        val nome = txtNome.text.toString()
        val tipo = txtTipo.text.toString()
        val cp = txtCp.text.toString()
        val hab = txtHabilidade.text.toString()

        val dao = AppDatabase.getDatabase(this).pokemonDao()

        lifecycleScope.launch {
            if (pokemonAtual == null) {
                // Cadastro Novo
                val novoPokemon = Pokemon(
                    id = 0,
                    nome = nome,
                    tipo = tipo,
                    cp = cp,
                    habilidade = hab,
                    caminhoFoto = caminhoFotoSalva
                )
                dao.inserir(novoPokemon)
                Toast.makeText(this@FormPokemonActivity, "Pokémon Capturado!", Toast.LENGTH_SHORT).show()
            } else {
                // Edição
                val pokemonEditado = Pokemon(
                    id = pokemonAtual!!.id,
                    nome = nome,
                    tipo = tipo,
                    cp = cp,
                    habilidade = hab,
                    caminhoFoto = caminhoFotoSalva
                )
                dao.atualizar(pokemonEditado)
                Toast.makeText(this@FormPokemonActivity, "Dados Atualizados!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    // --- MUDANÇA 3: Correção para Android 11+ (resolveActivity removido) ---
    private fun tirarFoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            photoFile = criarArquivoFoto()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile!!
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (ex: Exception) {
            Toast.makeText(this, "Erro ao abrir câmera: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun criarArquivoFoto(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("POKEMON_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            caminhoFotoSalva = photoFile?.absolutePath
            val bitmap = BitmapFactory.decodeFile(caminhoFotoSalva)
            imgFoto.setImageBitmap(bitmap)
        }
    }
}