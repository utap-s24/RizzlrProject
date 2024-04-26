package edu.utap.firenote.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import edu.utap.firenote.R

class ChangePasswordFragment : Fragment(R.layout.changepassword) {

    private lateinit var newPasswordInput: EditText
    private lateinit var changePasswordButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.changepassword, container, false)
        newPasswordInput = view.findViewById(R.id.etNewPassword)
        changePasswordButton = view.findViewById(R.id.btnChangePassword)

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordInput.text.toString()
            if (newPassword.isNotEmpty()) {
                changePassword(newPassword)
            } else {
                Toast.makeText(context, "Please enter a new password", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun changePassword(newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                // Navigate away or clear input fields
            } else {
                Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}