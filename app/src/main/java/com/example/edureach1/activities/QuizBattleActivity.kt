package com.example.edureach1.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.edureach1.databinding.ActivityQuizBattleBinding
import com.example.edureach1.fragments.EXTRA_CLASS_LEVEL
import com.example.edureach1.utils.Constants.GAME_TYPE_QUIZ_BATTLE
import com.example.edureach1.viewmodels.GameState
import com.example.edureach1.viewmodels.GameViewModel
import com.google.firebase.auth.ktx.auth               // ← ADD THIS
import com.google.firebase.ktx.Firebase                 // ← ADD THIS

class QuizBattleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBattleBinding
    private val viewModel: GameViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null
    private val timerDuration = 15L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            countDownTimer?.cancel()
            finish()
        }

        val classLevel = intent.getStringExtra(EXTRA_CLASS_LEVEL) ?: "6"
        val studentUid = Firebase.auth.currentUser?.uid ?: ""   // ← ADD THIS

        observeGame()
        setupAnswerButtons()
        viewModel.loadQuestions(classLevel, GAME_TYPE_QUIZ_BATTLE, studentUid)  // ← PASS studentUid
    }

    // ── Everything below is UNCHANGED ────────────────────────────────

    private fun observeGame() {
        viewModel.gameState.observe(this) { state ->
            when (state) {
                is GameState.Loading -> {
                    binding.progressBarBattle.visibility = View.VISIBLE
                    binding.layoutBattleNoQuestions.visibility = View.GONE
                }
                is GameState.NoQuestions -> {
                    binding.progressBarBattle.visibility = View.GONE
                    binding.layoutBattleNoQuestions.visibility = View.VISIBLE
                    countDownTimer?.cancel()
                }
                is GameState.Playing -> {
                    binding.progressBarBattle.visibility = View.GONE
                    binding.layoutBattleNoQuestions.visibility = View.GONE
                    binding.tvBattleQuestion.text = "Question ${state.index} of ${state.total}"
                    binding.tvBattleQuestionText.text = state.question.questionText
                    binding.btnBattleA.text = "A.  ${state.question.optionA}"
                    binding.btnBattleB.text = "B.  ${state.question.optionB}"
                    binding.btnBattleC.text = "C.  ${state.question.optionC}"
                    binding.btnBattleD.text = "D.  ${state.question.optionD}"
                    resetButtonColors()
                    setAnswerButtonsEnabled(true)
                    binding.tvBattleFeedback.visibility = View.GONE
                    binding.btnBattleNext.visibility = View.GONE
                    startTimer()
                }
                is GameState.Finished -> {
                    countDownTimer?.cancel()
                    launchResult(state.correct, state.total, state.xpEarned, GAME_TYPE_QUIZ_BATTLE)
                }
                is GameState.Error -> { }
            }
        }

        viewModel.answerResult.observe(this) { isCorrect ->
            if (isCorrect == null) return@observe
            countDownTimer?.cancel()
            val question = viewModel.getCurrentQuestion() ?: return@observe
            setAnswerButtonsEnabled(false)
            val correctBtn = when (question.correctAnswer) {
                "A" -> binding.btnBattleA
                "B" -> binding.btnBattleB
                "C" -> binding.btnBattleC
                "D" -> binding.btnBattleD
                else -> null
            }
            correctBtn?.setBackgroundColor(Color.parseColor("#C8E6C9"))
            if (isCorrect) {
                binding.tvBattleFeedback.text = "✅ Correct! +${com.example.edureach1.utils.Constants.XP_GAME_CORRECT} XP"
                binding.tvBattleFeedback.setTextColor(Color.parseColor("#1B5E20"))
            } else {
                binding.tvBattleFeedback.text = "❌ Time's up or wrong! Correct: ${question.correctAnswer}"
                binding.tvBattleFeedback.setTextColor(Color.parseColor("#B71C1C"))
            }
            binding.tvBattleFeedback.visibility = View.VISIBLE
            binding.btnBattleNext.visibility = View.VISIBLE
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        binding.progressBattleTimer.max = timerDuration.toInt()
        binding.progressBattleTimer.progress = timerDuration.toInt()

        countDownTimer = object : CountDownTimer(timerDuration * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                binding.tvBattleTimer.text = "⏱ $secondsLeft"
                binding.progressBattleTimer.progress = secondsLeft
                if (secondsLeft <= 5) {
                    binding.tvBattleTimer.setTextColor(Color.parseColor("#B71C1C"))
                } else {
                    binding.tvBattleTimer.setTextColor(Color.parseColor("#E65100"))
                }
            }

            override fun onFinish() {
                setAnswerButtonsEnabled(false)
                viewModel.submitAnswer("")
                binding.tvBattleTimer.text = "⏱ 0"
                binding.progressBattleTimer.progress = 0
            }
        }.start()
    }

    private fun setupAnswerButtons() {
        binding.btnBattleA.setOnClickListener { viewModel.submitAnswer("A") }
        binding.btnBattleB.setOnClickListener { viewModel.submitAnswer("B") }
        binding.btnBattleC.setOnClickListener { viewModel.submitAnswer("C") }
        binding.btnBattleD.setOnClickListener { viewModel.submitAnswer("D") }
        binding.btnBattleNext.setOnClickListener { viewModel.nextQuestion() }
    }

    private fun resetButtonColors() {
        val defaultColor = Color.parseColor("#FBE9E7")
        binding.btnBattleA.setBackgroundColor(defaultColor)
        binding.btnBattleB.setBackgroundColor(defaultColor)
        binding.btnBattleC.setBackgroundColor(defaultColor)
        binding.btnBattleD.setBackgroundColor(defaultColor)
    }

    private fun setAnswerButtonsEnabled(enabled: Boolean) {
        binding.btnBattleA.isEnabled = enabled
        binding.btnBattleB.isEnabled = enabled
        binding.btnBattleC.isEnabled = enabled
        binding.btnBattleD.isEnabled = enabled
    }

    private fun launchResult(correct: Int, total: Int, xp: Int, gameType: String) {
        startActivity(Intent(this, GameResultActivity::class.java).apply {
            putExtra("correct", correct)
            putExtra("total", total)
            putExtra("xp", xp)
            putExtra("game_type", gameType)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}