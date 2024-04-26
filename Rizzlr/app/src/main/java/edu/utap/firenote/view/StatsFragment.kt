package edu.utap.firenote.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import edu.utap.firenote.ModernViewModel
import edu.utap.firenote.R

class StatsFragment : Fragment() {

    private val viewModel: ModernViewModel by activityViewModels()
    private val args: StatsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart(view)

        updatePieChart(args.messagesSent, args.messagesReceived)

    }

    private fun setupChart(view: View) {
        val anyChartView = view.findViewById<com.anychart.AnyChartView>(R.id.any_chart_view)
        val pie = AnyChart.pie()
        val data = listOf(
            ValueDataEntry("Messages Sent", 0),
            ValueDataEntry("Messages Received", 0)
        )
        pie.data(data)

        // Configure the rest of your pie chart settings
        pie.title("Message Distribution: ")

        // Assuming 'anyChartView' is your chart component
        anyChartView.setChart(pie)

    }

    private fun fetchActivityTimes(callback: (Int, Int) -> Unit) {
        // Simulating asynchronous data fetching
        callback(120, 180) // Example values for texting and listening times
    }



    private fun updatePieChart(i: Int, countReceived: Int) {

        val anyChartView = requireView().findViewById<com.anychart.AnyChartView>(R.id.any_chart_view)

        val pie = AnyChart.pie()
        val data = listOf(
            ValueDataEntry("Messages Sent", i),
            ValueDataEntry("Messages Received", countReceived)
        )
        pie.data(data)

        // Configure the rest of your pie chart settings
        pie.title("Message Distribution")

        // Assuming 'anyChartView' is your chart component
        anyChartView.setChart(pie)

    }


}

