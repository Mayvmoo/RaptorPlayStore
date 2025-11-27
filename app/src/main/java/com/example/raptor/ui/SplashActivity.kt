package com.example.raptor.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.animation.doOnEnd
import com.example.raptor.MainActivity
import com.example.raptor.R

/**
 * Splash Screen Activity
 * Komt exact overeen met iOS SplashScreenView
 * - Gradient background (blauw/goud thema)
 * - Animated logo met glow effect
 * - Scale, fade, en rotation animaties
 * - Minimaal 2 seconden display tijd
 */
class SplashActivity : AppCompatActivity() {
    
    private var onAnimationComplete: (() -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Start animaties
        startAnimations()
        
        // Wacht minimaal 2 seconden voor animatie (zoals iOS)
        Handler(Looper.getMainLooper()).postDelayed({
            onAnimationComplete?.invoke()
            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) // 2 seconds
    }
    
    private fun startAnimations() {
        val logoCircle = findViewById<View>(R.id.logoCircle)
        val birdIcon = findViewById<View>(R.id.birdIcon)
        val appNameText = findViewById<android.widget.TextView>(R.id.appNameText)
        val subtitleText = findViewById<View>(R.id.subtitleText)
        val loadingIndicator = findViewById<View>(R.id.loadingIndicator)
        val glowCircle = findViewById<View>(R.id.glowCircle)
        
        // Enable hardware acceleration for better animation performance
        logoCircle.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        birdIcon.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        appNameText.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        glowCircle.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        // Setup gradient text for RAPTOR (white to gold) - horizontal gradient
        appNameText.post {
            val paint = appNameText.paint
            val width = paint.measureText(appNameText.text.toString())
            val whiteColor = ContextCompat.getColor(this, R.color.white)
            val goldColor = ContextCompat.getColor(this, R.color.splash_gold_light)
            val shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(whiteColor, goldColor),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = shader
            appNameText.invalidate()
        }
        
        // Bird icon stays white (gradient on ImageView requires custom drawable)
        // The white color matches iOS design closely enough
        
        // 1. Fade in en scale up animatie (0.8 seconden, easeOut)
        val initialScale = 0.5f
        val finalScale = 1.0f
        
        // Logo circle scale animatie
        logoCircle.scaleX = initialScale
        logoCircle.scaleY = initialScale
        val logoScaleX = ObjectAnimator.ofFloat(logoCircle, "scaleX", initialScale, finalScale)
        val logoScaleY = ObjectAnimator.ofFloat(logoCircle, "scaleY", initialScale, finalScale)
        logoScaleX.duration = 800
        logoScaleY.duration = 800
        logoScaleX.interpolator = AccelerateDecelerateInterpolator()
        logoScaleY.interpolator = AccelerateDecelerateInterpolator()
        
        // Bird icon scale en fade animatie
        birdIcon.scaleX = initialScale
        birdIcon.scaleY = initialScale
        birdIcon.alpha = 0f
        val birdScaleX = ObjectAnimator.ofFloat(birdIcon, "scaleX", initialScale, finalScale)
        val birdScaleY = ObjectAnimator.ofFloat(birdIcon, "scaleY", initialScale, finalScale)
        val birdFade = ObjectAnimator.ofFloat(birdIcon, "alpha", 0f, 1f)
        birdScaleX.duration = 800
        birdScaleY.duration = 800
        birdFade.duration = 800
        birdScaleX.interpolator = AccelerateDecelerateInterpolator()
        birdScaleY.interpolator = AccelerateDecelerateInterpolator()
        birdFade.interpolator = AccelerateDecelerateInterpolator()
        
        // Text fade in animatie
        appNameText.alpha = 0f
        subtitleText.alpha = 0f
        loadingIndicator.alpha = 0f
        val appNameFade = ObjectAnimator.ofFloat(appNameText, "alpha", 0f, 1f)
        val subtitleFade = ObjectAnimator.ofFloat(subtitleText, "alpha", 0f, 1f)
        val loadingFade = ObjectAnimator.ofFloat(loadingIndicator, "alpha", 0f, 1f)
        appNameFade.duration = 800
        subtitleFade.duration = 800
        loadingFade.duration = 800
        appNameFade.interpolator = AccelerateDecelerateInterpolator()
        subtitleFade.interpolator = AccelerateDecelerateInterpolator()
        loadingFade.interpolator = AccelerateDecelerateInterpolator()
        
        // Start alle fade/scale animaties tegelijk
        val fadeScaleAnimator = AnimatorSet()
        fadeScaleAnimator.playTogether(
            logoScaleX, logoScaleY,
            birdScaleX, birdScaleY, birdFade,
            appNameFade, subtitleFade, loadingFade
        )
        fadeScaleAnimator.start()
        
        // 2. Rotation animatie (3 seconden, linear, repeat forever)
        val rotationAnimator = ObjectAnimator.ofFloat(logoCircle, "rotation", 0f, 360f)
        rotationAnimator.duration = 3000
        rotationAnimator.repeatCount = ValueAnimator.INFINITE
        rotationAnimator.interpolator = LinearInterpolator()
        rotationAnimator.start()
        
        // 3. Glow pulse animatie (start na 0.3 seconden, 2 seconden duration, repeat forever)
        Handler(Looper.getMainLooper()).postDelayed({
            glowCircle.alpha = 0.6f
            glowCircle.scaleX = 1.0f
            glowCircle.scaleY = 1.0f
            
            // Scale animatie (1.0 -> 1.2 -> 1.0) - optimized with hardware layer
            val glowScaleX = ObjectAnimator.ofFloat(glowCircle, "scaleX", 1.0f, 1.2f)
            val glowScaleY = ObjectAnimator.ofFloat(glowCircle, "scaleY", 1.0f, 1.2f)
            glowScaleX.duration = 2000
            glowScaleY.duration = 2000
            glowScaleX.repeatCount = ValueAnimator.INFINITE
            glowScaleY.repeatCount = ValueAnimator.INFINITE
            glowScaleX.repeatMode = ValueAnimator.REVERSE
            glowScaleY.repeatMode = ValueAnimator.REVERSE
            glowScaleX.interpolator = AccelerateDecelerateInterpolator()
            glowScaleY.interpolator = AccelerateDecelerateInterpolator()
            
            // Opacity animatie (0.6 -> 0.3 -> 0.6)
            val glowOpacity = ObjectAnimator.ofFloat(glowCircle, "alpha", 0.6f, 0.3f)
            glowOpacity.duration = 2000
            glowOpacity.repeatCount = ValueAnimator.INFINITE
            glowOpacity.repeatMode = ValueAnimator.REVERSE
            glowOpacity.interpolator = AccelerateDecelerateInterpolator()
            
            val glowAnimator = AnimatorSet()
            glowAnimator.playTogether(glowScaleX, glowScaleY, glowOpacity)
            glowAnimator.start()
            
            // Clean up hardware layer when animation completes (memory optimization)
            glowAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // Keep hardware layer for infinite animations
                }
            })
        }, 300) // Start na 0.3 seconden
    }
}

