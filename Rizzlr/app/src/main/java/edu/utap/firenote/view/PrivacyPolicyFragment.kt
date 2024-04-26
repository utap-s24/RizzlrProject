package edu.utap.firenote.view

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.utap.firenote.R

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.privacy, container, false)
        val textView = view.findViewById<TextView>(R.id.tvPrivacyPolicy)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml(getString(R.string.privacy_policy_content), Html.FROM_HTML_MODE_COMPACT)
        } else {
            textView.text = Html.fromHtml(getString(R.string.privacy_policy_content))
        }
        return view
    }
}
