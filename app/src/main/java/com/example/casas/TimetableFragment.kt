import WeekPageAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.casas.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.days
class TimetableFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_timetable, container, false)
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

        val dateTimeTextView = view.findViewById<TextView>(R.id.dateTimeTextView)

        val current = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d, MMM, yyy")
        val formatted = current.format(formatter)

        dateTimeTextView.text = formatted

        tabLayout = view.findViewById(R.id.weekTabLayout)
        viewPager = view.findViewById(R.id.weekViewPager)

        val adapter = WeekPageAdapter(this, days = days)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = days[position]
        }.attach()

        return view
    }
}

