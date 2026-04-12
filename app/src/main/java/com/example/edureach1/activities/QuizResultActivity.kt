package com.example.edureach1.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edureach1.databinding.ActivityQuizResultBinding

class QuizResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)
        val percent = if (total > 0) (score * 100) / total else 0

        binding.tvScore.text = "$score / $total"

        binding.tvEmoji.text = when {
            percent >= 80 -> "🎉"
            percent >= 50 -> "👍"
            else -> "📚"
        }

        binding.tvResultTitle.text = when {
            percent >= 80 -> "Excellent!"
            percent >= 50 -> "Good Job!"
            else -> "Keep Practising!"
        }

        binding.btnBackToQuizzes.setOnClickListener {
            finish()
        }
    }
}