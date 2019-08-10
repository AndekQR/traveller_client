package com.client.traveller.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.client.traveller.data.db.entities.User

@Database(
    entities = [User::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun UserDao(): UserDao

    companion object{
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any() // zapewnia że będzie tylko jedna instancja bazy danych

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance?:buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context)=Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java, "traveller.db"
        ).build()
    }

}