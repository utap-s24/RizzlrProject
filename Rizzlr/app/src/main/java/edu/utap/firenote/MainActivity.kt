package edu.utap.firenote

import android.content.res.Configuration
import edu.utap.firenote.utils.ThemeUtils

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import edu.utap.firenote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val modernViewModel : ModernViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // See: https://developer.android.com/training/basics/intents/result
    // Only an activity can create a registerForActivityResult object, so
    // allocate it here, and pass to the AuthWrap object
    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
            // AuthWrap will handle it
        }
    // TODO: expand all/collapse all in menu
    private fun initMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_image_roll -> {
                        val auth = FirebaseAuth.getInstance()
                        auth.signOut()
                        true
                    }
                    else -> false
                }
            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme right before super.onCreate to ensure it's applied to the activity's context before any UI is created
        //edu.utap.firenote.ThemeUtils.applyTheme(edu.utap.firenote.ThemeUtils.loadThemePreference(this))

        super.onCreate(savedInstanceState)

        //ThemeUtils.applyCurrentTheme(this)

        setTheme(ThemeUtils.getThemeResId(ThemeUtils.loadThemePreference(this)))
        //setTheme(R.style.AppTheme)

        // Set the layout for the layout we created
        val binding = ActivityMainBinding.inflate(layoutInflater)

        val theme = ThemeUtils.loadThemePreference(this)  // assuming this method still requires context

        setContentView(binding.root)

        //setSupportActionBar(binding.toolbar)  // Make sure to set the toolbar if using custom toolbar

        setupNavigation(binding)
        setupFirebaseAuth()
        binding.indeterminateBar.visibility = View.GONE
        observeViewModel()
        initMenu()

    }

    private fun setupNavigation(binding: ActivityMainBinding) {
        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupFirebaseAuth() {
        AuthWrap.signInLauncher = signInLauncher
        AuthWrap.login()
        AuthWrap.observeUser().observe(this) {
            if(it.uid != "User logged out") {
                Log.d("MY NOTE", it.name)
                if(it.name != "User logged out") {
                    modernViewModel.login(it.uid, it.name)
                } else {
                    modernViewModel.login(it.uid, "New User!")
                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
                }
            } else {
                Log.d("MY NOTE", "LOGIN ERROR")
            }
        }
    }

    private fun observeViewModel() {
        modernViewModel.observeProfile().observe(this) {
            modernViewModel.updateListeners(it.queueStatus)
        }
    }


    // Debug Method
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_K) {
            // Debug statement
            Log.d("KeyPress", "Prof: " + modernViewModel.getProfile()+
                    " \n Auth: " + AuthWrap.getCurrentUser())
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            setTheme(R.style.AppTheme_Dark) // Set to your dark theme
        } else {
            setTheme(R.style.AppTheme_Light) // Set to your light theme
        }
        setContentView(R.layout.activity_main) // Re-set the content view to apply the new theme
        recreate() // You might need to recreate the activity to apply theme changes fully
    }
}
