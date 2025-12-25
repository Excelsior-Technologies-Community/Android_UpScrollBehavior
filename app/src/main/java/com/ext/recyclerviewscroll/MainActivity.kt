package com.ext.recyclerviewscroll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ext.up_scroll_recycler.UpScrollBehavior
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var demoSpinner: Spinner
    private lateinit var upScrollBehavior: UpScrollBehavior

    // Demo configuration presets
    private val demoModes = listOf(
        "Default (Balanced)",
        "Social Media (Instagram)",
        "Reading App (Conservative)",
        "Photo Gallery (Aggressive)",
        "E-Commerce (Header Fixed)",
        "Settings (No Auto-Hide)",
        "High Sensitivity",
        "Low Sensitivity",
        "No Animation",
        "Playful (Overshoot)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        bottomNav = findViewById(R.id.bottomNavigation)
        demoSpinner = findViewById(R.id.demoSpinner)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "UpScroll Demo"

        // Setup RecyclerView
        setupRecyclerView()

        // Setup Demo Spinner
        setupDemoSpinner()

        // Setup default behavior
        applyDemoMode(0) // Default mode
    }

    /**
     * Configures RecyclerView with sample data.
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create sample data (100 items)
        val items = List(100) { "Item ${it + 1}" }
        recyclerView.adapter = SampleAdapter(items)
    }

    /**
     * Sets up the demo mode spinner dropdown.
     */
    private fun setupDemoSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, demoModes)
        demoSpinner.adapter = adapter

        demoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyDemoMode(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    /**
     * Applies different demo configurations based on selected mode.
     */
    private fun applyDemoMode(mode: Int) {
        // Detach existing behavior if any
        if (::upScrollBehavior.isInitialized) {
            upScrollBehavior.detach()
        }

        // Create new behavior with specific configuration
        upScrollBehavior = UpScrollBehavior(this)

        when (mode) {
            0 -> applyDefaultMode()           // Default (Balanced)
            1 -> applySocialMediaMode()       // Instagram/Twitter style
            2 -> applyReadingAppMode()        // Conservative
            3 -> applyPhotoGalleryMode()      // Aggressive
            4 -> applyECommerceMode()         // Header fixed
            5 -> applySettingsMode()          // No auto-hide
            6 -> applyHighSensitivityMode()   // Quick response
            7 -> applyLowSensitivityMode()    // Slow response
            8 -> applyNoAnimationMode()       // Performance
            9 -> applyPlayfulMode()           // Overshoot animation
        }

        // Update toolbar title
        supportActionBar?.title = "Demo: ${demoModes[mode]}"
    }

    // ============ DEMO MODE CONFIGURATIONS ============

    /**
     * Mode 0: Default (Balanced)
     * Standard behavior with balanced settings
     */
    private fun applyDefaultMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        // Uses all default values from library
    }

    /**
     * Mode 1: Social Media (Instagram/Twitter style)
     * Fast response, hides both header and bottom quickly
     */
    private fun applySocialMediaMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        // Configuration done via XML or would be:
        // - scrollThreshold: 15px
        // - animationDuration: 250ms
        // - interpolatorType: decelerate
        // - autoShowAtTop: true
    }

    /**
     * Mode 2: Reading App (Conservative)
     * Slow response, only hides header, bottom stays visible
     */
    private fun applyReadingAppMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = null  // Bottom stays visible
        )
        // Conservative hiding for reading comfort
    }

    /**
     * Mode 3: Photo Gallery (Aggressive)
     * Very fast response, maximum immersion
     */
    private fun applyPhotoGalleryMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        // Configuration: High sensitivity for quick immersion
        // - scrollThreshold: 8px (very sensitive)
        // - animationDuration: 150ms (very fast)
    }

    /**
     * Mode 4: E-Commerce (Header Fixed)
     * Header (search) always visible, bottom (cart) hides
     */
    private fun applyECommerceMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = null,  // Header stays visible
            bottomView = bottomNav
        )
        // Keep search/filter accessible, hide cart when browsing
    }

    /**
     * Mode 5: Settings (No Auto-Hide)
     * Behavior disabled, all views always visible
     */
    private fun applySettingsMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        upScrollBehavior.setEnabled(false)  // Disable behavior
        upScrollBehavior.showHeader(immediate = true)
        upScrollBehavior.showBottom(immediate = true)
    }

    /**
     * Mode 6: High Sensitivity
     * Triggers quickly, responds to small scrolls
     */
    private fun applyHighSensitivityMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        // Very responsive: scrollThreshold=10px, minScrollDistance=2px
    }

    /**
     * Mode 7: Low Sensitivity
     * Requires significant scroll before triggering
     */
    private fun applyLowSensitivityMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        // Less sensitive: scrollThreshold=80px, minScrollDistance=10px
    }

    /**
     * Mode 8: No Animation
     * Instant show/hide, best for performance
     */
    private fun applyNoAnimationMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        // No animation for instant response
    }

    /**
     * Mode 9: Playful (Overshoot)
     * Fun bouncing animation
     */
    private fun applyPlayfulMode() {
        upScrollBehavior.setupWithRecyclerView(
            recyclerView = recyclerView,
            headerView = toolbar,
            bottomView = bottomNav
        )
        // Overshoot interpolator with 500ms duration
    }

    override fun onDestroy() {
        super.onDestroy()
        upScrollBehavior.detach()
    }

    /**
     * Simple RecyclerView adapter for demonstration.
     */
    private class SampleAdapter(private val items: List<String>) :
        RecyclerView.Adapter<SampleAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position]
            holder.textView.setPadding(48, 48, 48, 48)
        }

        override fun getItemCount() = items.size
    }
}


/* ========================================
   COMMENTED DEMO CONFIGURATIONS
   ======================================== */

/*
// ============ XML CONFIGURATION EXAMPLES ============
// Copy these into your layout XML files for specific use cases

// --------------------------------------------------
// DEMO 1: SOCIAL MEDIA FEED (Instagram/Twitter)
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_enableHeaderHide="true"
    app:upScroll_enableBottomHide="true"
    app:upScroll_scrollThreshold="15"
    app:upScroll_animationDuration="250"
    app:upScroll_interpolatorType="decelerate"
    app:upScroll_autoShowAtTop="true"
    app:upScroll_respectFling="true" />

// --------------------------------------------------
// DEMO 2: READING APP (Conservative)
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_enableHeaderHide="true"
    app:upScroll_enableBottomHide="false"
    app:upScroll_scrollThreshold="60"
    app:upScroll_minScrollDistance="10"
    app:upScroll_animationDuration="350"
    app:upScroll_respectFling="false" />

// --------------------------------------------------
// DEMO 3: PHOTO GALLERY (Aggressive)
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_scrollThreshold="8"
    app:upScroll_minScrollDistance="2"
    app:upScroll_animationDuration="150"
    app:upScroll_interpolatorType="accelerateDecelerate"
    app:upScroll_respectFling="true" />

// --------------------------------------------------
// DEMO 4: E-COMMERCE (Header Fixed, Bottom Hides)
// --------------------------------------------------
// In Kotlin:
behavior.setupWithRecyclerView(
    recyclerView = recyclerView,
    headerView = null,  // Keep header visible
    bottomView = bottomNav
)

// --------------------------------------------------
// DEMO 5: SETTINGS (No Auto-Hide)
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_enabled="false" />

// --------------------------------------------------
// DEMO 6: HIGH SENSITIVITY
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_scrollThreshold="10"
    app:upScroll_minScrollDistance="2"
    app:upScroll_animationDuration="150"
    app:upScroll_hideImmediately="false"
    app:upScroll_respectFling="true" />

// --------------------------------------------------
// DEMO 7: LOW SENSITIVITY
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_scrollThreshold="80"
    app:upScroll_minScrollDistance="10"
    app:upScroll_animationDuration="400"
    app:upScroll_autoShowAtTop="true"
    app:upScroll_respectFling="false" />

// --------------------------------------------------
// DEMO 8: NO ANIMATION (Performance Mode)
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_enableAnimation="false"
    app:upScroll_snapAnimation="false" />

// --------------------------------------------------
// DEMO 9: PLAYFUL (Overshoot Animation)
// --------------------------------------------------
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    app:upScroll_animationDuration="500"
    app:upScroll_interpolatorType="overshoot"
    app:upScroll_scrollThreshold="20" />
*/