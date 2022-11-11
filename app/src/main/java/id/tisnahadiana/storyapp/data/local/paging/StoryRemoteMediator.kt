package id.tisnahadiana.storyapp.data.local.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import id.tisnahadiana.storyapp.data.local.entity.StoryRemoteKeys
import id.tisnahadiana.storyapp.data.local.room.StoryDatabase
import id.tisnahadiana.storyapp.data.remote.responses.StoryEntity
import id.tisnahadiana.storyapp.data.remote.retrofit.ApiService
import id.tisnahadiana.storyapp.utils.wrapEspressoIdlingResource

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val token: String,
    private val apiService: ApiService,
    private val database: StoryDatabase
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextPage?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                remoteKeys?.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                remoteKeys?.prevPage
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        return try {
            wrapEspressoIdlingResource {
                val responseData = apiService.getStories(token, page, state.config.pageSize).story
                val endOfPaginationReached = responseData.isEmpty()

                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        database.storyRemoteKeysDao().deleteRemoteKeys()
                        database.storyDao().deleteAllStories()
                    }

                    val prevPage = if (page == 1) null else page - 1
                    val nextPage = if (endOfPaginationReached) null else page + 1
                    val keys = responseData.map {
                        StoryRemoteKeys(id = it.id, prevPage = prevPage, nextPage = nextPage)
                    }

                    database.storyRemoteKeysDao().addRemoteKeys(keys)
                    database.storyDao().insertStories(responseData)
                }
                MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): StoryRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.storyRemoteKeysDao().getRemoteKeys(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): StoryRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.storyRemoteKeysDao().getRemoteKeys(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): StoryRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.storyRemoteKeysDao().getRemoteKeys(id)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}