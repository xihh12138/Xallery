package com.xihh.base.util

import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor(
    private val filter: List<String>, private val headerBuilder: () -> Map<String, String>?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        /* 如果是包含的Url的时候才添加这些操作 */
        filter.forEach {
            if (it.contains(request.url.host)) {
                val builder = request.newBuilder()

                headerBuilder()?.let {
                    for ((key, value) in it) {
                        builder.addHeader(key, value)
                    }
                }

                return chain.proceed(builder.url(request.url).build())
            }
        }
        return chain.proceed(chain.request())
    }

}
