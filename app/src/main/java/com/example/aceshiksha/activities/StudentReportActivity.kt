package com.nikhil.aceshiksha.activities

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityStudentReportBinding
import com.nikhil.aceshiksha.models.GameTypeStat
import com.nikhil.aceshiksha.models.SubjectStat
import com.nikhil.aceshiksha.repository.AuthRepository
import com.nikhil.aceshiksha.viewmodels.ReportState
import com.nikhil.aceshiksha.viewmodels.StudentReportViewModel

class StudentReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentReportBinding
    private val viewModel: StudentReportViewModel by viewModels()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // ── Teacher view: extras injected by TeacherStudentListActivity ──
        val viewMode     = intent.getStringExtra("VIEW_MODE") ?: "student"
        val targetUid    = intent.getStringExtra("TARGET_USER_ID")
        val studentName  = intent.getStringExtra("STUDENT_NAME")

        supportActionBar?.apply {
            title = if (viewMode == "teacher" && studentName != null)
                "$studentName's Report"
            else
                "My Reports"
            setDisplayHomeAsUpEnabled(true)
        }

        observeViewModel()
        loadReport(viewMode, targetUid)
    }

    // ── Only this method changed ──────────────────────────────────────────
    private fun loadReport(viewMode: String, targetUid: String?) {
        if (viewMode == "teacher" && targetUid != null) {
            // Teacher path: fetch the target student's Firestore doc directly
            authRepository.getUserData(targetUid) { user ->
                if (user != null) {
                    viewModel.loadReport(user.uid, user.classLevel)
                }else {
                    // handle error
                    Toast.makeText(this, "Failed to load student data", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // Student path: unchanged — use the logged-in user's own UID
            val uid = authRepository.getCurrentUser()?.uid ?: return
            authRepository.getUserData(uid) { user ->
                if (user != null) {
                    viewModel.loadReport(user.uid, user.classLevel)
                }else {
                    Toast.makeText(this, "Failed to load report", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // ─────────────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.reportState.observe(this) { state ->
            when (state) {
                is ReportState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.scrollContent.visibility = View.GONE
                }
                is ReportState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollContent.visibility = View.VISIBLE
                    bindReport(state.report)
                }
                is ReportState.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun bindReport(report: com.nikhil.aceshiksha.models.StudentReport) {

        binding.layoutSubjects.removeAllViews()
        binding.layoutGames.removeAllViews()

        binding.tvTotalQuizzes.text = report.totalQuizzesAttempted.toString()
        binding.tvTotalGames.text   = report.totalGamesPlayed.toString()
        binding.tvQuizAccuracy.text = "${report.quizAccuracy}%"
        binding.tvTotalXp.text      = report.totalXp.toString()

        binding.tvBestScore.text   = "${report.bestQuizScore}%"
        binding.tvAvgAccuracy.text = "${report.quizAccuracy}%"
        binding.tvStreak.text      = "🔥 ${report.streak} days"

        if (report.subjectStats.isEmpty()) {
            binding.tvNoSubjects.visibility = View.VISIBLE
        } else {
            binding.tvNoSubjects.visibility = View.GONE
            report.subjectStats.forEach { addSubjectRow(binding.layoutSubjects, it) }
        }

        if (report.gameTypeStats.isEmpty()) {
            binding.tvNoGames.visibility = View.VISIBLE
        } else {
            binding.tvNoGames.visibility = View.GONE
            report.gameTypeStats.forEach { addGameRow(binding.layoutGames, it) }
        }
    }

    private fun addSubjectRow(container: LinearLayout, stat: SubjectStat) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val tvSubject = TextView(this).apply {
            text = stat.subject
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvAccuracy = TextView(this).apply {
            text = "${stat.accuracy}%"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(com.nikhil.aceshiksha.R.color.primary, null))
        }
        header.addView(tvSubject)
        header.addView(tvAccuracy)

        val tvDetail = TextView(this).apply {
            text = "${stat.quizzesAttempted} quiz(es) · ${stat.totalScore}/${stat.totalQuestions} correct"
            textSize = 12f
            setTextColor(resources.getColor(com.nikhil.aceshiksha.R.color.gray, null))
        }
        val progressBar = android.widget.ProgressBar(
            this, null, android.R.attr.progressBarStyleHorizontal
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 8
            ).also { it.topMargin = 6 }
            max = 100
            progress = stat.accuracy
        }
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).also { it.topMargin = 12 }
            setBackgroundColor(
                resources.getColor(com.nikhil.aceshiksha.R.color.light_gray, null))
        }
        row.addView(header)
        row.addView(tvDetail)
        row.addView(progressBar)
        row.addView(divider)
        container.addView(row)
    }

    private fun addGameRow(container: LinearLayout, stat: GameTypeStat) {
        val gameLabel = when (stat.gameType) {
            "maze"       -> "🌀 Maze Runner"
            "car_race"   -> "🚗 Car Race"
            "quiz_battle"-> "⚔️ Quiz Battle"
            "timeline"   -> "📅 History Timeline"
            else         -> stat.gameType
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
        }
        val tvGame = TextView(this).apply {
            text = gameLabel
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvStats = TextView(this).apply {
            text = "${stat.gamesPlayed} played · ${stat.accuracy}% acc"
            textSize = 13f
            setTextColor(resources.getColor(com.nikhil.aceshiksha.R.color.gray, null))
        }
        row.addView(tvGame)
        row.addView(tvStats)
        container.addView(row)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}