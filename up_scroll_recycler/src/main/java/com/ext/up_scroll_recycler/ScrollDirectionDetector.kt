package com.ext.up_scroll_recycler

import androidx.recyclerview.widget.RecyclerView

/**
 * Detects scroll direction and distance in RecyclerView.
 * Handles edge cases like top/bottom boundaries and scroll states.
 */
class ScrollDirectionDetector(
    private val minScrollDistance: Int,
    private val scrollThreshold: Int,
    private val respectFling: Boolean
) {

    // Scroll direction constants
    enum class Direction {
        UP, DOWN, IDLE
    }

    // Current accumulated scroll distance
    private var accumulatedScroll = 0

    // Last known direction
    private var lastDirection = Direction.IDLE

    // Track if RecyclerView is at top or bottom
    private var isAtTop = true
    private var isAtBottom = false

    /**
     * Processes scroll event and determines if action should be taken.
     * @param dy Vertical scroll amount (positive = scroll down, negative = scroll up)
     * @param recyclerView The RecyclerView being scrolled
     * @param scrollState Current scroll state (IDLE, DRAGGING, SETTLING)
     * @return Pair of (shouldTrigger, direction)
     */
    fun processScroll(
        dy: Int,
        recyclerView: RecyclerView,
        scrollState: Int
    ): Pair<Boolean, Direction> {

        // Update boundary states
        updateBoundaryStates(recyclerView)

        // Respect fling setting - ignore during fling if disabled
        if (!respectFling && scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
            return Pair(false, Direction.IDLE)
        }

        // Ignore very small movements (noise)
        if (Math.abs(dy) < minScrollDistance) {
            return Pair(false, Direction.IDLE)
        }

        // Determine current direction
        val currentDirection = when {
            dy > 0 -> Direction.DOWN
            dy < 0 -> Direction.UP
            else -> Direction.IDLE
        }

        // Reset accumulator if direction changed
        if (currentDirection != lastDirection && currentDirection != Direction.IDLE) {
            accumulatedScroll = 0
            lastDirection = currentDirection
        }

        // Accumulate scroll distance
        accumulatedScroll += Math.abs(dy)

        // Check if threshold reached
        val shouldTrigger = accumulatedScroll >= scrollThreshold

        // Reset accumulator after triggering
        if (shouldTrigger) {
            accumulatedScroll = 0
        }

        return Pair(shouldTrigger, currentDirection)
    }

    /**
     * Updates the isAtTop and isAtBottom flags.
     */
    private fun updateBoundaryStates(recyclerView: RecyclerView) {
        isAtTop = !recyclerView.canScrollVertically(-1)
        isAtBottom = !recyclerView.canScrollVertically(1)
    }

    /**
     * Checks if RecyclerView is at the top.
     */
    fun isAtTop(): Boolean = isAtTop

    /**
     * Checks if RecyclerView is at the bottom.
     */
    fun isAtBottom(): Boolean = isAtBottom

    /**
     * Resets all internal state.
     */
    fun reset() {
        accumulatedScroll = 0
        lastDirection = Direction.IDLE
        isAtTop = true
        isAtBottom = false
    }
}