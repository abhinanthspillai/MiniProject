package com.netraze.app.data.remote

import com.netraze.app.data.security.SecureSessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor appending Authorization: Bearer <token> from SecureSessionStore.
 */
class AuthInterceptor(
    private val sessionStore: SecureSessionStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip adding token if request already has Authorization header
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val session = runBlocking { sessionStore.getSession() }
        val token = session?.accessToken

        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
