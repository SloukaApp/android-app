package com.slouka.app.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.slouka.app.BuildConfig
import com.slouka.app.network.api.AccountApi
import com.slouka.app.network.api.AuthenticationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.EventListener
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    companion object {
        private val mNetworkJson = Json { ignoreUnknownKeys = true }

        private val contentType = "application/json".toMediaType()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        authenticationInterceptor: AuthenticationInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(authenticationInterceptor)
            .authenticator(tokenAuthenticator)
        if (BuildConfig.AUTH_DIAGNOSTICS) {
            client.eventListenerFactory { call ->
                if (call.request().url.encodedPath.endsWith("/auth/logout")) AuthenticationDiagnostics()
                else EventListener.NONE
            }
        }
        return this.buildRetrofit(client.build())
    }

    @Provides
    @Singleton
    fun provideAuthenticationApi(): AuthenticationApi {
        val client = OkHttpClient.Builder()
        if (BuildConfig.AUTH_DIAGNOSTICS) {
            client.eventListenerFactory { AuthenticationDiagnostics() }
        }
        return this.buildRetrofit(client.build()).create(AuthenticationApi::class.java)
    }

    @Provides
    @Singleton
    internal fun provideAccountApi(retrofit: Retrofit): AccountApi {
        return retrofit.create(AccountApi::class.java)
    }

    private fun buildRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.slouka.csmmark.me/api/v1/")
            .client(client)
            .addConverterFactory(mNetworkJson.asConverterFactory(contentType))
            .build()
    }
}
