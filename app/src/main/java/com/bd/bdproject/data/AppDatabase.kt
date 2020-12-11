package com.bd.bdproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bd.bdproject.data.dao.LightDao
import com.bd.bdproject.data.dao.LightTagRelationDao
import com.bd.bdproject.data.dao.TagDao
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightTagRelation
import com.bd.bdproject.data.model.Tag

@Database(entities = [Light::class, Tag::class, LightTagRelation::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun lightDao(): LightDao
    abstract fun tagDao(): TagDao
    abstract fun lightTagRelation(): LightTagRelationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "BITDAM_DB").build()
        }
    }
}