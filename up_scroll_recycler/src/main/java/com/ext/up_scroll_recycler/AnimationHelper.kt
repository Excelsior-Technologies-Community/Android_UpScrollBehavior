package com.ext.up_scroll_recycler

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Interpolator

/**
 * Helper class for managing view animations (hide/show).
 * Handles smooth transitions with customizable interpolators and durations.
 */
class AnimationHelper(
    private val animationDuration: Long,
    private val interpolator: Interpolator,
    private val snapAnimation: Boolean
) {

    // Keep track of running animators to prevent conflicts
    private val activeAnimators = mutableMapOf<View, ValueAnimator>()

    /**
     * Hides a view by translating it out of screen bounds.
     * @param view The view to hide
     * @param isHeader True if this is a header (hide upward), false for bottom (hide downward)
     * @param immediate If true, hides without animation
     * @param onComplete Callback when animation completes
     */
    fun hideView(
        view: View?,
        isHeader: Boolean,
        immediate: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        view ?: return

        // Cancel any existing animation on this view
        cancelAnimation(view)

        val targetTranslation = if (isHeader) -view.height.toFloat() else view.height.toFloat()

        // Check if already hidden
        if (view.translationY == targetTranslation) {
            onComplete?.invoke()
            return
        }

        if (immediate || !snapAnimation) {
            view.translationY = targetTranslation
            onComplete?.invoke()
            return
        }

        // Animate the hide
        animateTranslation(view, view.translationY, targetTranslation, onComplete)
    }

    /**
     * Shows a view by translating it back to original position.
     * @param view The view to show
     * @param immediate If true, shows without animation
     * @param onComplete Callback when animation completes
     */
    fun showView(
        view: View?,
        immediate: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        view ?: return

        // Cancel any existing animation on this view
        cancelAnimation(view)

        val targetTranslation = 0f

        // Check if already shown
        if (view.translationY == targetTranslation) {
            onComplete?.invoke()
            return
        }

        if (immediate || !snapAnimation) {
            view.translationY = targetTranslation
            onComplete?.invoke()
            return
        }

        // Animate the show
        animateTranslation(view, view.translationY, targetTranslation, onComplete)
    }

    /**
     * Performs the actual translation animation.
     */
    private fun animateTranslation(
        view: View,
        fromY: Float,
        toY: Float,
        onComplete: (() -> Unit)?
    ) {
        val animator = ValueAnimator.ofFloat(fromY, toY).apply {
            duration = animationDuration
            interpolator = this@AnimationHelper.interpolator

            addUpdateListener { animation ->
                view.translationY = animation.animatedValue as Float
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    activeAnimators.remove(view)
                    onComplete?.invoke()
                }

                override fun onAnimationCancel(animation: Animator) {
                    activeAnimators.remove(view)
                }
            })
        }

        activeAnimators[view] = animator
        animator.start()
    }

    /**
     * Cancels any running animation on the specified view.
     */
    private fun cancelAnimation(view: View) {
        activeAnimators[view]?.cancel()
        activeAnimators.remove(view)
    }

    /**
     * Checks if a view is currently visible (translationY == 0).
     */
    fun isViewVisible(view: View?): Boolean {
        return view?.translationY == 0f
    }

    /**
     * Checks if a view is currently hidden (translationY != 0).
     */
    fun isViewHidden(view: View?): Boolean {
        return view?.translationY != 0f
    }

    /**
     * Cancels all active animations.
     */
    fun cancelAllAnimations() {
        activeAnimators.values.forEach { it.cancel() }
        activeAnimators.clear()
    }
}