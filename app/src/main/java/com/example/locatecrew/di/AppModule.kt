package com.example.locatecrew.di

import android.content.Context
import com.example.locatecrew.data.firebase.FirebaseRepository
import com.example.locatecrew.data.repository.GroupRepository
import com.example.locatecrew.data.repository.UserRepository
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
    fun provideFirebaseRepository(): FirebaseRepository {
        return FirebaseRepository()
    }

    @Provides
    @Singleton
    fun provideUserRepository(firebaseRepository: FirebaseRepository): UserRepository {
        return UserRepository(firebaseRepository)
    }

    @Provides
    @Singleton
    fun provideGroupRepository(firebaseRepository: FirebaseRepository): GroupRepository {
        return GroupRepository(firebaseRepository)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object ApplicationModule {

        @Provides
        @Singleton
        fun provideContext(@ApplicationContext appContext: Context): Context {
            return appContext
        }
    }
}