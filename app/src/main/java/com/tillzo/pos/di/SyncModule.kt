package com.tillzo.pos.di

import com.tillzo.pos.data.sync.RestApiSyncImpl
import com.tillzo.pos.domain.sync.DataSyncInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * SyncModule — v3 Blueprint M1.4.
 *
 * Single backend only: RestApiSyncImpl (Google Sheets REST API + OAuth 2.0).
 *
 * Note: SheetsApiClient, SheetsRemoteDataSource, SheetsRepository are all
 * @Singleton + @Inject constructor — Hilt auto-provides them without explicit
 * @Provides methods (adding a redundant @Provides causes a self-reference cycle).
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideDataSyncInterface(
        restApiImpl: RestApiSyncImpl
    ): DataSyncInterface = restApiImpl
}

