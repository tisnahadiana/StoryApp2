package id.tisnahadiana.storyapp.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.tisnahadiana.storyapp.ui.main.StoryListFragment
import id.tisnahadiana.storyapp.ui.main.StoryMapFragment

class SectionPageAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = StoryListFragment()
            1 -> fragment = StoryMapFragment()
        }
        return fragment as Fragment
    }
}