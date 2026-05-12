package com.nikhil.aceshiksha.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.nikhil.aceshiksha.databinding.ActivityMemoryMatchBinding
import com.nikhil.aceshiksha.fragments.EXTRA_CLASS_LEVEL
import com.nikhil.aceshiksha.models.GameQuestion
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_MEMORY_MATCH
import com.nikhil.aceshiksha.viewmodels.GameState
import com.nikhil.aceshiksha.viewmodels.GameViewModel
import com.google.firebase.auth.FirebaseAuth

data class MemoryCard(
    val pairId: Int,
    val text: String
)

class MemoryMatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemoryMatchBinding
    private val viewModel: GameViewModel by viewModels()
    private val studentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val cards = mutableListOf<MemoryCard>()
    private val cardViews = mutableListOf<CardView>()
    private val textViews = mutableListOf<TextView>()

    private val flippedIndices = mutableListOf<Int>()
    private val matchedPairIds = mutableSetOf<Int>()
    private var isChecking = false
    private var moves = 0
    private var totalPairs = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val classLevel = intent.getStringExtra(EXTRA_CLASS_LEVEL) ?: "6"

        observeViewModel()
        viewModel.loadQuestions(classLevel, GAME_TYPE_MEMORY_MATCH, studentUid)
    }

    private fun observeViewModel() {
        viewModel.gameState.observe(this) { state ->
            when (state) {
                is GameState.Loading -> {
                    binding.progressBarMemory.visibility = View.VISIBLE
                    binding.layoutMemoryNoQuestions.visibility = View.GONE
                    binding.layoutMemoryGame.visibility = View.GONE
                }
                is GameState.NoQuestions -> {
                    binding.progressBarMemory.visibility = View.GONE
                    binding.layoutMemoryNoQuestions.visibility = View.VISIBLE
                }
                is GameState.Finished -> {
                    launchResult(state.correct, state.total, state.xpEarned)
                }
                else -> {}
            }
        }

        // Observe raw question list to build the board
        viewModel.questionsLoaded.observe(this) { questions ->
            if (questions.isNotEmpty()) {
                binding.progressBarMemory.visibility = View.GONE
                binding.layoutMemoryGame.visibility = View.VISIBLE
                setupBoard(questions)
            }
        }
    }

    private fun setupBoard(questions: List<GameQuestion>) {
        // Use up to 6 questions → 12 cards (6 pairs)
        val selected = questions.shuffled().take(6)
        totalPairs = selected.size

        cards.clear()
        selected.forEachIndexed { pairIndex, q ->
            // Term card  (questionText written by teacher, e.g. "Photosynthesis")
            cards.add(MemoryCard(pairId = pairIndex, text = q.questionText))
            // Definition card (optionA written by teacher, e.g. "Process plants use to make food")
            cards.add(MemoryCard(pairId = pairIndex, text = q.optionA))
        }
        cards.shuffle()

        moves = 0
        binding.tvMemoryMoves.text = "Moves: 0"
        binding.tvMemoryPairs.text = "Pairs: 0 / $totalPairs"

        buildGrid()
    }

    private fun buildGrid() {
        binding.gridMemory.removeAllViews()
        cardViews.clear()
        textViews.clear()

        val density = resources.displayMetrics.density
        val screenWidth = resources.displayMetrics.widthPixels
        val sidePadding = (16 * density).toInt()
        val gap = (8 * density).toInt()
        val cols = 3
        val cardSize = (screenWidth - sidePadding * 2 - gap * (cols - 1)) / cols
        val rows = Math.ceil(cards.size.toDouble() / cols).toInt()

        for (row in 0 until rows) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = gap }
            }

            for (col in 0 until cols) {
                val index = row * cols + col
                if (index >= cards.size) break

                val tv = TextView(this).apply {
                    text = "?"
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    setPadding(6, 6, 6, 6)
                }

                val cv = CardView(this).apply {
                    radius = 12f * density
                    cardElevation = 4f * density
                    setCardBackgroundColor(Color.parseColor("#1976D2"))
                    layoutParams = LinearLayout.LayoutParams(cardSize, cardSize).also { lp ->
                        if (col < cols - 1) lp.rightMargin = gap
                    }
                    addView(tv)
                    setOnClickListener { onCardClicked(index) }
                }

                cardViews.add(cv)
                textViews.add(tv)
                rowLayout.addView(cv)
            }
            binding.gridMemory.addView(rowLayout)
        }
    }

    private fun onCardClicked(index: Int) {
        if (isChecking) return
        if (matchedPairIds.contains(cards[index].pairId)) return
        if (flippedIndices.contains(index)) return
        if (flippedIndices.size >= 2) return

        revealCard(index)
        flippedIndices.add(index)

        if (flippedIndices.size == 2) {
            moves++
            binding.tvMemoryMoves.text = "Moves: $moves"
            checkMatch()
        }
    }

    private fun checkMatch() {
        isChecking = true
        val i1 = flippedIndices[0]
        val i2 = flippedIndices[1]
        val matched = cards[i1].pairId == cards[i2].pairId

        Handler(Looper.getMainLooper()).postDelayed({
            if (matched) {
                markMatched(i1)
                markMatched(i2)
                matchedPairIds.add(cards[i1].pairId)
                binding.tvMemoryPairs.text = "Pairs: ${matchedPairIds.size} / $totalPairs"

                if (matchedPairIds.size == totalPairs) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.finishMazeGame(totalPairs, totalPairs)
                    }, 600)
                }
            } else {
                hideCard(i1)
                hideCard(i2)
            }
            flippedIndices.clear()
            isChecking = false
        }, 900)
    }

    private fun revealCard(index: Int) {
        cardViews[index].setCardBackgroundColor(Color.parseColor("#FFF9C4"))
        textViews[index].setTextColor(Color.parseColor("#212121"))
        textViews[index].text = cards[index].text
    }

    private fun hideCard(index: Int) {
        cardViews[index].setCardBackgroundColor(Color.parseColor("#1976D2"))
        textViews[index].setTextColor(Color.WHITE)
        textViews[index].text = "?"
    }

    private fun markMatched(index: Int) {
        cardViews[index].setCardBackgroundColor(Color.parseColor("#388E3C"))
        textViews[index].setTextColor(Color.WHITE)
        cardViews[index].setOnClickListener(null)
    }

    private fun launchResult(correct: Int, total: Int, xp: Int) {
        startActivity(Intent(this, GameResultActivity::class.java).apply {
            putExtra("correct", correct)
            putExtra("total", total)
            putExtra("xp", xp)
            putExtra("game_type", GAME_TYPE_MEMORY_MATCH)
            putExtra("class_level", intent.getStringExtra(EXTRA_CLASS_LEVEL) ?: "6")
        })
        finish()
    }
}
