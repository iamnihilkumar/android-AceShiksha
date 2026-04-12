package com.example.edureach1.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.edureach1.R
import com.example.edureach1.databinding.ActivityMazeGameBinding
import com.example.edureach1.fragments.EXTRA_CLASS_LEVEL
import com.example.edureach1.models.GameQuestion
import com.example.edureach1.utils.Cell
import com.example.edureach1.utils.Constants
import com.example.edureach1.utils.Direction
import com.example.edureach1.viewmodels.GameState
import com.example.edureach1.viewmodels.GameViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class MazeGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMazeGameBinding
    private val viewModel: GameViewModel by viewModels()

    private var pendingDirection: Direction? = null
    private var pendingTarget: Cell? = null
    private var correctCount = 0
    private var wrongCount = 0
    private var stepCount = 0
    private var classLevel = "6"
    private var activeSheet: BottomSheetDialog? = null

    // ✅ NEW: current user UID
    private val studentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMazeGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        classLevel = intent.getStringExtra(EXTRA_CLASS_LEVEL) ?: "6"

        setupDirectionButtons()
        observeGame()

        // ✅ UPDATED: pass studentUid
        viewModel.loadQuestionsForMaze(
            classLevel,
            Constants.GAME_TYPE_MAZE,
            studentUid
        )
    }

    private fun observeGame() {
        viewModel.gameState.observe(this) { state ->
            when (state) {
                is GameState.Loading -> {
                    binding.progressBarMaze.visibility = View.VISIBLE
                    binding.layoutControls.visibility = View.GONE
                    binding.tvNoQuestions.visibility = View.GONE
                }
                is GameState.NoQuestions -> {
                    binding.progressBarMaze.visibility = View.GONE
                    binding.layoutControls.visibility = View.GONE
                    binding.tvNoQuestions.visibility = View.VISIBLE
                }
                is GameState.Playing -> {
                    binding.progressBarMaze.visibility = View.GONE
                    binding.layoutControls.visibility = View.VISIBLE
                    binding.tvNoQuestions.visibility = View.GONE
                    showQuestionSheet(state.question)
                }
                is GameState.Finished -> {
                    activeSheet?.dismiss()
                    launchResult(state.correct, state.total, state.xpEarned)
                }
                is GameState.Error -> { }
            }
        }
    }

    private fun setupDirectionButtons() {
        binding.btnUp.setOnClickListener { tryMove(Direction.UP) }
        binding.btnDown.setOnClickListener { tryMove(Direction.DOWN) }
        binding.btnLeft.setOnClickListener { tryMove(Direction.LEFT) }
        binding.btnRight.setOnClickListener { tryMove(Direction.RIGHT) }
    }

    private fun tryMove(direction: Direction) {
        val current = binding.mazeView.playerPos
        val target = binding.mazeView.getNeighbor(current, direction)

        if (target == null) {
            binding.mazeView.flashWrong(current)
            return
        }

        pendingDirection = direction
        pendingTarget = target
        setDirectionButtonsEnabled(false)

        viewModel.loadNextQuestion()
    }

    private fun showQuestionSheet(question: GameQuestion) {
        activeSheet?.dismiss()
        val sheet = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this)
            .inflate(R.layout.fragment_maze_question, null)

        sheetView.findViewById<TextView>(R.id.tvSheetQuestion).text = question.questionText
        sheetView.findViewById<Button>(R.id.btnSheetA).text = "A.  ${question.optionA}"
        sheetView.findViewById<Button>(R.id.btnSheetB).text = "B.  ${question.optionB}"
        sheetView.findViewById<Button>(R.id.btnSheetC).text = "C.  ${question.optionC}"
        sheetView.findViewById<Button>(R.id.btnSheetD).text = "D.  ${question.optionD}"

        val feedbackView = sheetView.findViewById<TextView>(R.id.tvSheetFeedback)
        val buttons = listOf(
            "A" to sheetView.findViewById<Button>(R.id.btnSheetA),
            "B" to sheetView.findViewById<Button>(R.id.btnSheetB),
            "C" to sheetView.findViewById<Button>(R.id.btnSheetC),
            "D" to sheetView.findViewById<Button>(R.id.btnSheetD)
        )

        sheet.setCancelable(false)

        buttons.forEach { (answer, btn) ->
            btn.setOnClickListener {
                val isCorrect = answer == question.correctAnswer
                buttons.forEach { (_, b) -> b.isEnabled = false }

                buttons.find { it.first == question.correctAnswer }?.second
                    ?.setBackgroundColor(Color.parseColor("#C8E6C9"))

                feedbackView.visibility = View.VISIBLE

                if (isCorrect) {
                    correctCount++
                    feedbackView.text = "✅ Correct! Moving forward..."
                    feedbackView.setTextColor(Color.parseColor("#1B5E20"))

                    sheetView.postDelayed({
                        sheet.dismiss()
                        val target = pendingTarget
                        if (target != null) {
                            stepCount++
                            binding.mazeView.movePlayer(target)
                            updateStats()
                            viewModel.recordCorrect()
                            checkWin(target)
                        }
                        setDirectionButtonsEnabled(true)
                    }, 800)

                } else {
                    wrongCount++
                    btn.setBackgroundColor(Color.parseColor("#FFCDD2"))
                    feedbackView.text = "❌ Wrong! You're blocked. Try another direction."
                    feedbackView.setTextColor(Color.parseColor("#B71C1C"))

                    viewModel.recordWrong()

                    sheetView.postDelayed({
                        sheet.dismiss()
                        binding.mazeView.flashWrong(binding.mazeView.playerPos)
                        updateStats()
                        setDirectionButtonsEnabled(true)
                    }, 1200)
                }
            }
        }

        sheet.setContentView(sheetView)
        sheet.show()
        activeSheet = sheet
    }

    private fun checkWin(cell: Cell) {
        if (cell == binding.mazeView.exitPos) {
            setDirectionButtonsEnabled(false)
            binding.mazeView.postDelayed({
                val xp = (correctCount * 10) + 30
                viewModel.finishMazeGame(
                    correctCount,
                    correctCount + wrongCount
                )
                launchResult(correctCount, correctCount + wrongCount, xp)
            }, 500)
        }
    }

    private fun updateStats() {
        binding.tvMazeSteps.text = "Steps: $stepCount"
        binding.tvMazeScore.text = "✅ $correctCount  ❌ $wrongCount"
    }

    private fun setDirectionButtonsEnabled(enabled: Boolean) {
        binding.btnUp.isEnabled = enabled
        binding.btnDown.isEnabled = enabled
        binding.btnLeft.isEnabled = enabled
        binding.btnRight.isEnabled = enabled
    }

    private fun launchResult(correct: Int, total: Int, xp: Int) {
        startActivity(Intent(this, GameResultActivity::class.java).apply {
            putExtra("correct", correct)
            putExtra("total", total)
            putExtra("xp", xp)
            putExtra("game_type", Constants.GAME_TYPE_MAZE)
            putExtra("class_level", classLevel)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeSheet?.dismiss()
    }
}