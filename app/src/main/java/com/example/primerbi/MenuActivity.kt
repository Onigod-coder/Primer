package com.example.primerbi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnNew: Button = findViewById(R.id.btnNewGame)
        val btnContinue: Button = findViewById(R.id.btnContinue)
        val btnExit: Button = findViewById(R.id.btnExit)

        btnNew.setOnClickListener {
            // Clear saved progress and start
            getSharedPreferences("progress", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnContinue.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnExit.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(R.string.exit_confirm)
                .setPositiveButton(android.R.string.ok) { _, _ -> finishAffinity() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
}



