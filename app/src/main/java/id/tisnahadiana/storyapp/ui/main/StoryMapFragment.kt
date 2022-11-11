package id.tisnahadiana.storyapp.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import id.tisnahadiana.storyapp.R
import id.tisnahadiana.storyapp.adapter.StoryInfoWindowAdapter
import id.tisnahadiana.storyapp.data.Result
import id.tisnahadiana.storyapp.data.remote.responses.StoryEntity
import id.tisnahadiana.storyapp.databinding.FragmentStoryMapBinding
import id.tisnahadiana.storyapp.ui.detail.DetailActivity

class StoryMapFragment : Fragment() {

    private var _binding: FragmentStoryMapBinding? = null
    private val binding get() = _binding!!

    private val storyMapViewModel: StoryMapViewModel by viewModels {
        StoryMapViewModel.StoryMapViewModelFactory.getInstance(requireContext())
    }

    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setInfoWindowAdapter(StoryInfoWindowAdapter(requireActivity()))
        googleMap.setOnInfoWindowClickListener {
            val story = it.tag as StoryEntity
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra(DetailActivity.STORY_EXTRA, story)
            startActivity(intent)
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-5.0, 105.0), 5f))
        setMapStyle(googleMap)

        loadStoriesIntoMap(googleMap)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun loadStoriesIntoMap(googleMap: GoogleMap) {
        storyMapViewModel.getToken().observe(this) { token ->
            if (token != "null") {
                val myToken = "Bearer $token"
                storyMapViewModel.getStories(myToken).observe(this) {
                    when (it) {
                        is Result.Loading -> {}
                        is Result.Error -> {}
                        is Result.Success -> {
                            setupMarkers(googleMap, it.data)
                        }
                    }
                }
            }
        }
    }

    private fun setupMarkers(googleMap: GoogleMap, stories: List<StoryEntity>) {
        stories.forEach { story ->
            val marker = MarkerOptions()
            marker.position(LatLng(story.lat, story.lon))
                .title(story.name)
                .snippet(story.description)

            val markerTag = googleMap.addMarker(marker)
            markerTag?.tag = story
        }
    }

    private fun setMapStyle(mMap: GoogleMap) {
        try {
            val success =
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.failed_load_map),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (exception: Resources.NotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.failed_load_map),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}