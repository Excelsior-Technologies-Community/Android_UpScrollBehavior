package com.ext.up_scroll_recycler

import android.view.animation.*

/**
 * Enum representing different interpolator types for animations.
 * Maps XML attribute values to Android Interpolator instances.
 */
enum class InterpolatorType(val value: Int) {
    LINEAR(0),
    ACCELERATE(1),
    DECELERATE(2),
    ACCELERATE_DECELERATE(3),
    ANTICIPATE(4),
    OVERSHOOT(5),
    BOUNCE(6);

    companion object {
        /**
         * Converts enum value to actual Android Interpolator instance.
         * @param type The InterpolatorType enum value
         * @return Corresponding Interpolator instance
         */
        fun getInterpolator(type: InterpolatorType): Interpolator {
            return when (type) {
                LINEAR -> LinearInterpolator()
                ACCELERATE -> AccelerateInterpolator()
                DECELERATE -> DecelerateInterpolator()
                ACCELERATE_DECELERATE -> AccelerateDecelerateInterpolator()
                ANTICIPATE -> AnticipateInterpolator()
                OVERSHOOT -> OvershootInterpolator()
                BOUNCE -> BounceInterpolator()
            }
        }

        /**
         * Finds enum value from integer attribute value.
         * @param value Integer from XML attribute
         * @return Corresponding InterpolatorType, defaults to DECELERATE
         */
        fun fromValue(value: Int): InterpolatorType {
            return values().find { it.value == value } ?: DECELERATE
        }
    }
}