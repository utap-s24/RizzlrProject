package edu.utap.firenote.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import edu.utap.firenote.R
import edu.utap.firenote.utils.ThemeUtils

class SettingsFragment : Fragment(R.layout.settings) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSpinner(view)
        setupLogoutButton(view)
        setUpSettingsButton(view)
        setUpPrivacyButton(view)
    }

    private fun setupThemeSpinner(view: View) {
        val spinnerTheme: Spinner = view.findViewById(R.id.spinner_theme)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.theme_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTheme.adapter = adapter
        }

        val currentTheme = ThemeUtils.loadThemePreference(requireContext())
        val themeIndex = when (currentTheme) {
            "AppTheme.Light" -> 0
            "AppTheme.Dark" -> 1
            "AppTheme" -> 2  // Assumes default is system responsive
            else -> 2  // Default index if no preference matches
        }
        spinnerTheme.setSelection(themeIndex, false)  // Prevent triggering listener on initial set

        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTheme = when (position) {
                    0 -> "AppTheme.Light"
                    1 -> "AppTheme.Dark"
                    else -> "AppTheme"  // Assumes default is system responsive
                }
                if (currentTheme != selectedTheme) {
                    ThemeUtils.saveThemePreference(requireContext(), selectedTheme)
                    ThemeUtils.applyTheme(selectedTheme)
                    requireActivity().recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }

    private fun setupLogoutButton(view: View) {
        val btnLogout: Button = view.findViewById(R.id.btn_logout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.settingsFragment_to_loginFragment)
        }
    }

    private fun setUpSettingsButton(view: View) {
        val btnPassword: Button? = view.findViewById(R.id.btn_change_password)
        if (btnPassword == null) {
            Log.e("SettingsFragment", "Button not found")
        } else {
            btnPassword.setOnClickListener {
                findNavController().navigate(R.id.settingsFragment_to_changePasswordFragment)
            }
        }
    }

    private fun setUpPrivacyButton(view: View) {
        val btnPrivacy: Button? = view.findViewById(R.id.btn_privacy_policy)
        if (btnPrivacy == null) {
            Log.e("SettingsFragment", "Button not found")
        } else {
            btnPrivacy.setOnClickListener {
                findNavController().navigate(R.id.settingsFragment_to_privacyFragment)
            }
        }
    }


}



