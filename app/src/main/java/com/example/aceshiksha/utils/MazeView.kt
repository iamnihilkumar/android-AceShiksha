package com.nikhil.aceshiksha.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

data class Cell(val row: Int, val col: Int)

class MazeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Grid size
    val cols = 5
    val rows = 5

    var playerPos = Cell(0, 0)
    var exitPos = Cell(4, 4)
    var wrongFlashCell: Cell? = null

    // Walls: each entry blocks movement between two adjacent cells
    // Format: Pair(from, to) — bidirectional
    val walls: Set<Pair<Cell, Cell>> = setOf(
        // Row 0
        Pair(Cell(0,0), Cell(0,1)),
        Pair(Cell(0,2), Cell(0,3)),
        // Row 1
        Pair(Cell(1,1), Cell(1,2)),
        Pair(Cell(1,3), Cell(1,4)),
        // Row 2
        Pair(Cell(2,0), Cell(2,1)),
        Pair(Cell(2,2), Cell(2,3)),
        // Row 3
        Pair(Cell(3,1), Cell(3,2)),
        Pair(Cell(3,3), Cell(3,4)),
        // Col walls (vertical)
        Pair(Cell(0,1), Cell(1,1)),
        Pair(Cell(1,0), Cell(2,0)),
        Pair(Cell(1,2), Cell(2,2)),
        Pair(Cell(1,4), Cell(2,4)),
        Pair(Cell(2,1), Cell(3,1)),
        Pair(Cell(2,3), Cell(3,3)),
        Pair(Cell(3,0), Cell(4,0)),
        Pair(Cell(3,2), Cell(4,2)),
        Pair(Cell(3,4), Cell(4,4)).let {
            // Don't block exit approach — remove this wall
            Pair(Cell(9,9), Cell(9,9)) // dummy, see below
        }
    ).filter { it.first != Cell(9,9) }.toSet()

    private val wallPaint = Paint().apply {
        color = Color.parseColor("#1B5E20")
        strokeWidth = 6f
        isAntiAlias = true
    }
    private val cellPaint = Paint().apply {
        color = Color.parseColor("#F1F8E9")
        isAntiAlias = true
    }
    private val exitPaint = Paint().apply {
        color = Color.parseColor("#C8E6C9")
        isAntiAlias = true
    }
    private val playerPaint = Paint().apply {
        color = Color.parseColor("#1B5E20")
        isAntiAlias = true
    }
    private val flashPaint = Paint().apply {
        color = Color.parseColor("#FFCDD2")
        isAntiAlias = true
    }
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#A5D6A7")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        textSize = 52f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val visitedPaint = Paint().apply {
        color = Color.parseColor("#DCEDC8")
        isAntiAlias = true
    }

    val visitedCells = mutableSetOf<Cell>()

    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = minOf(w, h).toFloat()
        cellSize = size / cols
        offsetX = (w - cellSize * cols) / 2f
        offsetY = (h - cellSize * rows) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cellSize == 0f) return

        // Draw cells
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = Cell(r, c)
                val left = offsetX + c * cellSize
                val top = offsetY + r * cellSize
                val rect = RectF(left + 2, top + 2, left + cellSize - 2, top + cellSize - 2)

                val paint = when {
                    cell == wrongFlashCell -> flashPaint
                    cell == exitPos -> exitPaint
                    cell in visitedCells -> visitedPaint
                    else -> cellPaint
                }
                canvas.drawRoundRect(rect, 8f, 8f, paint)
                canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
            }
        }

        // Draw walls
        for (wall in walls) {
            val a = wall.first
            val b = wall.second
            drawWall(canvas, a, b)
        }

        // Draw outer border
        val outerRect = RectF(
            offsetX, offsetY,
            offsetX + cols * cellSize,
            offsetY + rows * cellSize
        )
        wallPaint.style = Paint.Style.STROKE
        wallPaint.strokeWidth = 8f
        canvas.drawRect(outerRect, wallPaint)
        wallPaint.style = Paint.Style.FILL

        // Draw exit emoji
        val exitCenterX = offsetX + exitPos.col * cellSize + cellSize / 2f
        val exitCenterY = offsetY + exitPos.row * cellSize + cellSize / 2f + textPaint.textSize / 3f
        canvas.drawText("🚪", exitCenterX, exitCenterY, textPaint)

        // Draw player emoji
        val playerCenterX = offsetX + playerPos.col * cellSize + cellSize / 2f
        val playerCenterY = offsetY + playerPos.row * cellSize + cellSize / 2f + textPaint.textSize / 3f
        if (playerPos != exitPos) {
            canvas.drawText("🧍", playerCenterX, playerCenterY, textPaint)
        }
    }

    private fun drawWall(canvas: Canvas, a: Cell, b: Cell) {
        wallPaint.strokeWidth = 6f
        wallPaint.style = Paint.Style.STROKE

        // Horizontal wall (same col, adjacent rows) → draw horizontal line between them
        if (a.col == b.col && Math.abs(a.row - b.row) == 1) {
            val topRow = minOf(a.row, b.row)
            val wallY = offsetY + (topRow + 1) * cellSize
            val wallX1 = offsetX + a.col * cellSize + 4f
            val wallX2 = offsetX + a.col * cellSize + cellSize - 4f
            canvas.drawLine(wallX1, wallY, wallX2, wallY, wallPaint)
        }

        // Vertical wall (same row, adjacent cols) → draw vertical line between them
        if (a.row == b.row && Math.abs(a.col - b.col) == 1) {
            val leftCol = minOf(a.col, b.col)
            val wallX = offsetX + (leftCol + 1) * cellSize
            val wallY1 = offsetY + a.row * cellSize + 4f
            val wallY2 = offsetY + a.row * cellSize + cellSize - 4f
            canvas.drawLine(wallX, wallY1, wallX, wallY2, wallPaint)
        }

        wallPaint.style = Paint.Style.FILL
    }

    fun isWallBetween(from: Cell, to: Cell): Boolean {
        return walls.contains(Pair(from, to)) || walls.contains(Pair(to, from))
    }

    fun flashWrong(cell: Cell) {
        wrongFlashCell = cell
        invalidate()
        postDelayed({
            wrongFlashCell = null
            invalidate()
        }, 600)
    }

    fun movePlayer(to: Cell) {
        visitedCells.add(playerPos)
        playerPos = to
        invalidate()
    }

    // Returns valid neighbor cells (no wall, within grid)
    fun getNeighbor(from: Cell, direction: Direction): Cell? {
        val to = when (direction) {
            Direction.UP    -> Cell(from.row - 1, from.col)
            Direction.DOWN  -> Cell(from.row + 1, from.col)
            Direction.LEFT  -> Cell(from.row, from.col - 1)
            Direction.RIGHT -> Cell(from.row, from.col + 1)
        }
        if (to.row < 0 || to.row >= rows || to.col < 0 || to.col >= cols) return null
        if (isWallBetween(from, to)) return null
        return to
    }
}

enum class Direction { UP, DOWN, LEFT, RIGHT }