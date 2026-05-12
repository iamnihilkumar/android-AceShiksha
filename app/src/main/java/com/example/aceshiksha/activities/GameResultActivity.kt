package com.nikhil.aceshiksha.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityGameResultBinding

class GameResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val correct = intent.getIntExtra("correct", 0)
        val total = intent.getIntExtra("total", 10)
        val xp = intent.getIntExtra("xp", 0)
        val gameType = intent.getStringExtra("game_type") ?: ""

        val percentage = if (total > 0) (correct * 100) / total else 0

        binding.tvResultEmoji.text = when {
            percentage >= 80 -> "🏆"
            percentage >= 50 -> "⭐"
            else -> "💪"
        }
        binding.tvResultTitle.text = when {
            percentage >= 80 -> "Excellent!"
            percentage >= 50 -> "Good Job!"
            else -> "Keep Practicing!"
        }
        binding.tvResultScore.text = "You got $correct out of $total correct ($percentage%)"
        binding.tvResultXp.text = "+$xp XP Earned"

        binding.btnPlayAgain.setOnClickListener {
            val activityClass = when (gameType) {
                "maze" -> MazeGameActivity::class.java
                "car_race" -> CarRaceActivity::class.java
                "quiz_battle" -> QuizBattleActivity::class.java
                "memory_match"  -> MemoryMatchActivity::class.java   // ✅ ADD
                "true_or_false" -> TrueOrFalseActivity::class.java   // ✅ ADD
                else -> null
            }
            activityClass?.let {
                startActivity(Intent(this, it).apply {
                    putExtra("game_type", gameType)
                    putExtra("class_level", intent.getStringExtra("class_level") ?: "6")
                })
                finish()
            }
        }

        binding.btnBackToGames.setOnClickListener {
            finish()
        }
    }
}