package id.tisnahadiana.storyapp.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.tisnahadiana.storyapp.data.local.entity.StoryRemoteKeys
import id.tisnahadiana.storyapp.data.remote.responses.StoryEntity

@Database(entities = [StoryEntity::class, StoryRemoteKeys::class], version = 1)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun storyRemoteKeysDao(): StoryRemoteKeysDao

    companion object {
        @Volatile
        private var instance: StoryDatabase? = null

        fun getInstance(context: Context): StoryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, StoryDatabase::class.java, "stories.db")
                    .build()
            }.also { instance = it }
    }
}