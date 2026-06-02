package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWeatherDao(database: WeatherDatabase) = database.weatherDao()


}
