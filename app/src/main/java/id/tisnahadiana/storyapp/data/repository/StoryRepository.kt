package id.tisnahadiana.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import id.tisnahadiana.storyapp.data.ITEMS_PER_PAGE
import id.tisnahadiana.storyapp.data.Result
import id.tisnahadiana.storyapp.data.local.paging.StoryRemoteMediator
import id.tisnahadiana.storyapp.data.local.room.StoryDatabase
import id.tisnahadiana.storyapp.data.remote.responses.AddStoryResponse
import id.tisnahadiana.storyapp.data.remote.responses.StoryEntity
import id.tisnahadiana.storyapp.data.remote.retrofit.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(
    private val apiService: ApiService,
    private val database: StoryDatabase
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getStories(token: String): LiveData<PagingData<StoryEntity>> {
        val pagingSourceFactory = { database.storyDao().getStories() }

        return Pager(
            config = PagingConfig(
                pageSize = ITEMS_PER_PAGE
            ),
            remoteMediator = StoryRemoteMediator(
                token,
                apiService,
                database
            ),
            pagingSourceFactory = pagingSourceFactory
        ).liveData
    }

    fun getStoriesWithLocation(token: String): LiveData<Result<List<StoryEntity>>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.getStories(token)
                if (response.error) {
                    emit(Result.Error(response.message))
                } else {
                    val stories = response.story
                    emit(Result.Success(stories))
                }
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun postStory(
        token: String,
        imageFile: File,
        description: String,
        lat: Float?,
        lon: Float?
    ): LiveData<Result<AddStoryResponse>> = liveData {
        emit(Result.Loading)

        val textPlainMediaType = "text/plain".toMediaType()
        val imageMediaType = "image/jpeg".toMediaTypeOrNull()

        val imageMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            imageFile.asRequestBody(imageMediaType)
        )
        val descriptionRequestBody = description.toRequestBody(textPlainMediaType)
        val latRequestBody = lat.toString().toRequestBody(textPlainMediaType)
        val lonRequestBody = lon.toString().toRequestBody(textPlainMediaType)

        try {
            val response = apiService.postStory(
                token,
                imageMultiPart,
                descriptionRequestBody,
                latRequestBody,
                lonRequestBody
            )

            if (response.error) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    companion object {
        private val TAG = StoryRepository::class.java.simpleName
        private const val POST_ERROR_MESSAGE = "Story was not posted, please try again later."

        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(
            apiService: ApiService,
            database: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, database)
            }.also { instance = it }
    }
}