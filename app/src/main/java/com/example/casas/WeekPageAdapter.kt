import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.casas.DayFragment

class WeekPageAdapter(
    fragment: Fragment,
    private val days: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = days.size

    override fun createFragment(position: Int): Fragment {
        return DayFragment.newInstance(days[position])
    }
}
