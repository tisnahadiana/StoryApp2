package id.tisnahadiana.storyapp.ui.detail


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import id.tisnahadiana.storyapp.data.remote.responses.StoryEntity
import id.tisnahadiana.storyapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private val binding: ActivityDetailBinding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupView()
    }

    private fun setupView() {
        val story = intent.getParcelableExtra<StoryEntity>(STORY_EXTRA)

        val name = story?.name
        val description = story?.description
        val imgUrl = story?.photoUrl

        supportActionBar?.title = name
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.tvDescription.text = description

        Glide.with(this)
            .load(imgUrl)
            .into(binding.imgStory)
    }

    companion object {
        const val STORY_EXTRA = "story_extra"
    }
}