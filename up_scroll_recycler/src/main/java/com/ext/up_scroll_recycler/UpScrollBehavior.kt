package com.ext.up_scroll_recycler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Main controller class for UpScrollBehavior library.
 * Manages auto-hide/show behavior for header and bottom views on RecyclerView scroll.
 *
 * Usage:
 * ```kotlin
 * val behavior = UpScrollBehavior(context)
 * behavior.setupWithRecyclerView(recyclerView, headerView, bottomView)
 * ```
 */
class UpScrollBehavior {

    // Configuration properties (with defaults)
    private var enableHeaderHide = true
    private var enableBottomHide = true
    private var enableAnimation = true
    private var enabled = true

    private var animationDuration = 300L
    private var interpolatorType = InterpolatorType.DECELERATE

    private var scrollThreshold = 20
    private var minScrollDistance = 5

    private var hideOnScrollDown = true
    private var showOnScrollUp = true
    private var autoShowAtTop = true
    private var autoShowAtBottom = false

    private var headerHeightOverride = -1
    private var bottomHeightOverride = -1
    private var snapAnimation = true

    private var respectFling = true
    private var hideImmediately = false
    private var showImmediately = false

    // View references
    private var headerView: View? = null
    private var bottomView: View? = null
    private var recyclerView: RecyclerView? = null

    // Helper instances
    private lateinit var scrollDetector: ScrollDirectionDetector
    private lateinit var animationHelper: AnimationHelper

    // Scroll listener instance
    private var scrollListener: RecyclerView.OnScrollListener? = null

    // Current scroll state
    private var currentScrollState = RecyclerView.SCROLL_STATE_IDLE

    /**
     * Constructor for programmatic usage.
     */
    constructor(context: Context) {
        initializeHelpers()
    }

    /**
     * Constructor that reads XML attributes.
     */
    constructor(context: Context, attrs: AttributeSet?) {
        attrs?.let { parseAttributes(context, it) }
        initializeHelpers()
    }

    /**
     * Parses custom XML attributes.
     */
    private fun parseAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UpScrollBehavior)

        try {
            // Enable/Disable
            enabled = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_enabled, true)
            enableHeaderHide = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_enableHeaderHide, true)
            enableBottomHide = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_enableBottomHide, true)
            enableAnimation = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_enableAnimation, true)

            // Animation
            animationDuration = typedArray.getInt(R.styleable.UpScrollBehavior_upScroll_animationDuration, 300).toLong()
            val interpolatorValue = typedArray.getInt(R.styleable.UpScrollBehavior_upScroll_interpolatorType, 2)
            interpolatorType = InterpolatorType.fromValue(interpolatorValue)

            // Sensitivity
            scrollThreshold = typedArray.getInt(R.styleable.UpScrollBehavior_upScroll_scrollThreshold, 20)
            minScrollDistance = typedArray.getInt(R.styleable.UpScrollBehavior_upScroll_minScrollDistance, 5)

            // Behavior
            hideOnScrollDown = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_hideOnScrollDown, true)
            showOnScrollUp = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_showOnScrollUp, true)
            autoShowAtTop = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_autoShowAtTop, true)
            autoShowAtBottom = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_autoShowAtBottom, false)

            // Layout
            headerHeightOverride = typedArray.getDimensionPixelSize(R.styleable.UpScrollBehavior_upScroll_headerHeightOverride, -1)
            bottomHeightOverride = typedArray.getDimensionPixelSize(R.styleable.UpScrollBehavior_upScroll_bottomHeightOverride, -1)
            snapAnimation = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_snapAnimation, true)

            // Advanced
            respectFling = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_respectFling, true)
            hideImmediately = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_hideImmediately, false)
            showImmediately = typedArray.getBoolean(R.styleable.UpScrollBehavior_upScroll_showImmediately, false)

        } finally {
            typedArray.recycle()
        }
    }

    /**
     * Initializes helper classes with current configuration.
     */
    private fun initializeHelpers() {
        scrollDetector = ScrollDirectionDetector(
            minScrollDistance = minScrollDistance,
            scrollThreshold = scrollThreshold,
            respectFling = respectFling
        )

        animationHelper = AnimationHelper(
            animationDuration = if (enableAnimation) animationDuration else 0L,
            interpolator = InterpolatorType.getInterpolator(interpolatorType),
            snapAnimation = snapAnimation
        )
    }

    /**
     * Sets up the behavior with RecyclerView and views.
     * @param recyclerView The RecyclerView to attach to
     * @param headerView Optional header view to control
     * @param bottomView Optional bottom view to control
     */
    fun setupWithRecyclerView(
        recyclerView: RecyclerView,
        headerView: View? = null,
        bottomView: View? = null
    ) {
        // Remove previous listener if exists
        scrollListener?.let { this.recyclerView?.removeOnScrollListener(it) }

        this.recyclerView = recyclerView
        this.headerView = headerView
        this.bottomView = bottomView

        // Apply height overrides if specified
        applyHeightOverrides()

        // Create and attach scroll listener
        scrollListener = createScrollListener()
        recyclerView.addOnScrollListener(scrollListener!!)

        // Initial state - show all views
        showHeader(immediate = true)
        showBottom(immediate = true)
    }

    /**
     * Applies custom height overrides to views if specified in XML.
     */
    private fun applyHeightOverrides() {
        if (headerHeightOverride > 0) {
            headerView?.layoutParams?.height = headerHeightOverride
        }
        if (bottomHeightOverride > 0) {
            bottomView?.layoutParams?.height = bottomHeightOverride
        }
    }

    /**
     * Creates the scroll listener that handles hide/show logic.
     */
    private fun createScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                currentScrollState = newState

                // Handle auto-show at boundaries
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    handleBoundaryAutoShow()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!enabled) return

                // Process scroll and get action decision
                val (shouldTrigger, direction) = scrollDetector.processScroll(
                    dy, recyclerView, currentScrollState
                )

                if (!shouldTrigger) return

                // Execute hide/show based on direction and configuration
                when (direction) {
                    ScrollDirectionDetector.Direction.DOWN -> {
                        if (hideOnScrollDown) {
                            hideViews()
                        }
                    }
                    ScrollDirectionDetector.Direction.UP -> {
                        if (showOnScrollUp) {
                            showViews()
                        }
                    }
                    else -> { /* IDLE - do nothing */ }
                }
            }
        }
    }

    /**
     * Handles auto-show behavior when at top or bottom boundaries.
     */
    private fun handleBoundaryAutoShow() {
        if (autoShowAtTop && scrollDetector.isAtTop()) {
            showViews()
        }
        if (autoShowAtBottom && scrollDetector.isAtBottom()) {
            showViews()
        }
    }

    /**
     * Hides enabled views.
     */
    private fun hideViews() {
        if (enableHeaderHide) {
            hideHeader(immediate = hideImmediately)
        }
        if (enableBottomHide) {
            hideBottom(immediate = hideImmediately)
        }
    }

    /**
     * Shows enabled views.
     */
    private fun showViews() {
        if (enableHeaderHide) {
            showHeader(immediate = showImmediately)
        }
        if (enableBottomHide) {
            showBottom(immediate = showImmediately)
        }
    }

    // ============ PUBLIC API ============

    /**
     * Manually hide the header view.
     * @param immediate If true, hides without animation
     */
    fun hideHeader(immediate: Boolean = false) {
        animationHelper.hideView(headerView, isHeader = true, immediate = immediate)
    }

    /**
     * Manually show the header view.
     * @param immediate If true, shows without animation
     */
    fun showHeader(immediate: Boolean = false) {
        animationHelper.showView(headerView, immediate = immediate)
    }

    /**
     * Manually hide the bottom view.
     * @param immediate If true, hides without animation
     */
    fun hideBottom(immediate: Boolean = false) {
        animationHelper.hideView(bottomView, isHeader = false, immediate = immediate)
    }

    /**
     * Manually show the bottom view.
     * @param immediate If true, shows without animation
     */
    fun showBottom(immediate: Boolean = false) {
        animationHelper.showView(bottomView, immediate = immediate)
    }

    /**
     * Resets the behavior to initial state (all views shown).
     */
    fun resetState() {
        scrollDetector.reset()
        animationHelper.cancelAllAnimations()
        showHeader(immediate = true)
        showBottom(immediate = true)
    }

    /**
     * Enables or disables the entire behavior.
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (enabled) {
            showViews()
        }
    }

    /**
     * Checks if behavior is currently enabled.
     */
    fun isEnabled(): Boolean = enabled

    /**
     * Checks if header is currently visible.
     */
    fun isHeaderVisible(): Boolean = animationHelper.isViewVisible(headerView)

    /**
     * Checks if bottom is currently visible.
     */
    fun isBottomVisible(): Boolean = animationHelper.isViewVisible(bottomView)

    /**
     * Detaches the behavior from RecyclerView and cleans up.
     */
    fun detach() {
        scrollListener?.let { recyclerView?.removeOnScrollListener(it) }
        animationHelper.cancelAllAnimations()
        scrollListener = null
        recyclerView = null
        headerView = null
        bottomView = null
    }
}