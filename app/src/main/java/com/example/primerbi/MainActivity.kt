package com.example.primerbi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.round
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var tvExample: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvStats: TextView
    private lateinit var etAnswer: EditText
    private lateinit var btnStart: Button
    private lateinit var btnCheck: Button
    private lateinit var btnBackToMenu: Button

    private var operandA: Int? = null
    private var operandB: Int? = null
    private var operator: Char? = null
    private var correct: Int = 0
    private var wrong: Int = 0
    private var exampleBgColor: Int = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvExample = findViewById(R.id.tvExample)
        tvResult = findViewById(R.id.tvResult)
        tvStats = findViewById(R.id.tvStats)
        etAnswer = findViewById(R.id.etAnswer)
        btnStart = findViewById(R.id.btnStart)
        btnCheck = findViewById(R.id.btnCheck)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)

        btnStart.setOnClickListener {
            generateExample()
        }

        btnCheck.setOnClickListener {
            checkAnswer()
        }

        btnBackToMenu.setOnClickListener {
            saveProgress()
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Try to restore persisted progress to allow "Continue game"
            restoreFromPrefs()
            updateUiForIdle()
        }
        updateStats()
    }

    private fun generateExample() {
        val ops = charArrayOf('+', '-', '*', '/')
        val op = ops[Random.nextInt(ops.size)]

        var a: Int
        var b: Int
        when (op) {
            '+' -> {
                a = randomTwoDigit()
                b = randomTwoDigit()
            }
            '-' -> {
                a = randomTwoDigit()
                b = randomTwoDigit()
                if (b > a) {
                    val t = a; a = b; b = t
                }
            }
            '*' -> {
                a = randomTwoDigit()
                b = randomTwoDigit()
            }
            '/' -> {
                // Ensure integer division: pick divisor and quotient, derive dividend
                b = randomTwoDigitNonZero()
                val q = randomTwoDigit()
                a = b * q
                // If a goes beyond two digits, clamp by regenerating with smaller numbers
                while (a < 10 || a > 99) {
                    b = randomTwoDigitNonZero()
                    val q2 = Random.nextInt(1, 10) // keep dividend in two digits more likely
                    a = b * q2
                }
            }
            else -> {
                a = randomTwoDigit(); b = randomTwoDigit()
            }
        }

        operandA = a
        operandB = b
        operator = op
        exampleBgColor = Color.WHITE

        tvExample.text = "$a $op $b ="
        tvExample.setBackgroundColor(exampleBgColor)

        etAnswer.isEnabled = true
        etAnswer.text.clear()
        btnStart.isEnabled = false
        btnCheck.isEnabled = true
        tvResult.text = ""
    }

    private fun checkAnswer() {
        val a = operandA
        val b = operandB
        val op = operator
        if (a == null || b == null || op == null) return

        val correctAnswer = when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            else -> 0
        }

        val userAnswer = etAnswer.text.toString().toIntOrNull()
        val isRight = userAnswer != null && userAnswer == correctAnswer

        if (isRight) {
            correct++
            exampleBgColor = Color.GREEN
            tvResult.text = getString(R.string.correct)
        } else {
            wrong++
            exampleBgColor = Color.RED
            tvResult.text = getString(R.string.incorrect, correctAnswer)
        }

        tvExample.setBackgroundColor(exampleBgColor)

        etAnswer.isEnabled = false
        btnCheck.isEnabled = false
        btnStart.isEnabled = true

        updateStats()
        saveProgress()
    }

    private fun updateStats() {
        val total = correct + wrong
        val percent = if (total == 0) 0.0 else (correct.toDouble() * 100.0 / total.toDouble())
        val rounded = round(percent * 100.0) / 100.0
        tvStats.text = getString(R.string.stats_format, correct, wrong, rounded)
    }

    private fun updateUiForIdle() {
        tvExample.text = ""
        tvExample.setBackgroundColor(exampleBgColor)
        etAnswer.isEnabled = false
        btnStart.isEnabled = true
        btnCheck.isEnabled = false
        tvResult.text = ""
    }

    private fun randomTwoDigit(): Int = Random.nextInt(10, 100)
    private fun randomTwoDigitNonZero(): Int = Random.nextInt(10, 100)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("correct", correct)
        outState.putInt("wrong", wrong)
        operandA?.let { outState.putInt("a", it) }
        operandB?.let { outState.putInt("b", it) }
        operator?.let { outState.putChar("op", it) }
        outState.putInt("bg", exampleBgColor)
        outState.putString("answer", etAnswer.text.toString())
        outState.putBoolean("etEnabled", etAnswer.isEnabled)
        outState.putBoolean("startEnabled", btnStart.isEnabled)
        outState.putBoolean("checkEnabled", btnCheck.isEnabled)
        outState.putString("resultText", tvResult.text.toString())
        outState.putString("exampleText", tvExample.text.toString())
    }

    private fun restoreState(state: Bundle) {
        correct = state.getInt("correct")
        wrong = state.getInt("wrong")
        exampleBgColor = state.getInt("bg", Color.WHITE)
        operandA = if (state.containsKey("a")) state.getInt("a") else null
        operandB = if (state.containsKey("b")) state.getInt("b") else null
        operator = if (state.containsKey("op")) state.getChar("op") else null

        tvExample.text = state.getString("exampleText", "")
        tvExample.setBackgroundColor(exampleBgColor)
        tvResult.text = state.getString("resultText", "")

        etAnswer.setText(state.getString("answer", ""))
        etAnswer.isEnabled = state.getBoolean("etEnabled", false)
        btnStart.isEnabled = state.getBoolean("startEnabled", true)
        btnCheck.isEnabled = state.getBoolean("checkEnabled", false)
    }

    private fun saveProgress() {
        val prefs = getSharedPreferences("progress", MODE_PRIVATE)
        prefs.edit()
            .putInt("correct", correct)
            .putInt("wrong", wrong)
            .putInt("bg", exampleBgColor)
            .putBoolean("hasA", operandA != null)
            .putBoolean("hasB", operandB != null)
            .putBoolean("hasOp", operator != null)
            .apply()
        operandA?.let { prefs.edit().putInt("a", it).apply() }
        operandB?.let { prefs.edit().putInt("b", it).apply() }
        operator?.let { prefs.edit().putString("op", it.toString()).apply() }
    }

    private fun restoreFromPrefs() {
        val prefs = getSharedPreferences("progress", MODE_PRIVATE)
        correct = prefs.getInt("correct", correct)
        wrong = prefs.getInt("wrong", wrong)
        exampleBgColor = prefs.getInt("bg", Color.WHITE)
        if (prefs.getBoolean("hasA", false)) operandA = prefs.getInt("a", 0)
        if (prefs.getBoolean("hasB", false)) operandB = prefs.getInt("b", 0)
        if (prefs.getBoolean("hasOp", false)) operator = prefs.getString("op", null)?.first()
    }
}


