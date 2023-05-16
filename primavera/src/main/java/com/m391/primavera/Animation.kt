package com.m391.primavera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.transition.ChangeBounds
import android.transition.TransitionListenerAdapter
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.doOnLayout

object Animation {

    fun animateImageChange(imageButton: ImageButton, targetImageResource: Int) {
        val rotationOut = ObjectAnimator.ofFloat(imageButton, View.ROTATION, 0f, 360f)
        val rotationIn = ObjectAnimator.ofFloat(imageButton, View.ROTATION, 360f, 0f)

        val animatorSet = AnimatorSet().apply {
            play(rotationOut).before(rotationIn)
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    imageButton.setImageResource(targetImageResource)
                }

            })
        }

        animatorSet.start()
    }

    fun showButtonWithAnimation(button: ImageButton) {
        val startX = button.width.toFloat() // Starting X position (off-screen to the left)
        val endX = 0f
        button.visibility = View.VISIBLE
        button.translationX = startX

        button.doOnLayout {
            button.animate()
                .translationX(endX)
                .setDuration(500)
                .start()
        }
    }

    fun showTextViewWithAnimation(button: TextView) {
        val startX = -button.width.toFloat() // Starting X position (off-screen to the left)
        val endX = 0f
        button.visibility = View.VISIBLE
        button.translationX = startX

        button.doOnLayout {
            button.animate()
                .translationX(endX)
                .setDuration(500)
                .start()
        }
    }

    fun hideTextViewWithAnimation(button: TextView) {
        button.post {
            val slideOutAnimation =  TranslateAnimation(0f, -button.width.toFloat(), 0f, 0f)
            slideOutAnimation.duration = 500
            slideOutAnimation.interpolator = AccelerateDecelerateInterpolator()
            slideOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    button.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            button.startAnimation(slideOutAnimation)
        }
    }

    fun hideButtonWithAnimation(button: ImageButton) {
        button.post {
            val slideOutAnimation = TranslateAnimation(0f, button.width.toFloat(), 0f, 0f)
            slideOutAnimation.duration = 500
            slideOutAnimation.interpolator = AccelerateDecelerateInterpolator()
            slideOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    button.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            button.startAnimation(slideOutAnimation)
        }
    }
}