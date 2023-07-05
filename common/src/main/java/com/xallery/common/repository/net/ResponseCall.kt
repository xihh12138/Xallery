package com.xallery.common.repository.net

import com.xallery.common.repository.net.bean.Response
import com.xihh.base.util.logf
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


private class ResponseCall(val proxy: Call<Response<*>>) : Call<Response<*>> by proxy {

    override fun execute(): retrofit2.Response<Response<*>> = try {
        proxy.execute().check(proxy)
    } catch (e: Exception) {
        retrofit2.Response.success(e.toResponse())
    }

    override fun enqueue(callback: Callback<Response<*>>) {
        proxy.enqueue(object : Callback<Response<*>> {
            override fun onResponse(
                call: Call<Response<*>>,
                Response: retrofit2.Response<Response<*>>,
            ) {
                try {
                    callback.onResponse(this@ResponseCall, Response.check(call))
                } catch (e: Exception) {
                    callback.onResponse(
                        this@ResponseCall, retrofit2.Response.success(e.toResponse())
                    )
                }
            }

            override fun onFailure(call: Call<Response<*>>, t: Throwable) {
                logf { "AIResponseCall: onFailure   t=${t.stackTraceToString()}" }
                callback.onResponse(this@ResponseCall, retrofit2.Response.success(t.toResponse()))
            }
        })
    }

    override fun clone(): Call<Response<*>> = ResponseCall(proxy.clone())

    private fun retrofit2.Response<Response<*>>.check(call: Call<Response<*>>): retrofit2.Response<Response<*>> {
        val responseBody = this.body()      // 因为这里可能是返回body，也有可能返回errorBody
        return if (responseBody == null) {
            retrofit2.Response.success(IOException("url=${call.request().url} responseBody is null").toResponse())
        } else {
            this
        }
    }
}

class ResponseCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        /* 获取异常处理 */
        val rawType = getRawType(returnType)
        if (rawType != Call::class.java || returnType !is ParameterizedType) {
            return null
        }

        val apiType = getParameterUpperBound(0, returnType)
        // 如果一级泛型为Api && 是泛型类型
        if (getRawType(apiType) == Response::class.java) {
            return object : CallAdapter<Response<*>, Call<Response<*>>> {
                override fun responseType() = apiType

                override fun adapt(call: Call<Response<*>>): Call<Response<*>> =
                    ResponseCall(call)

            }
        }
        return null
    }

}

/**
 * 异常转Api
 */
private fun Throwable.toResponse(): Response<*> {
    return Response(Response.CODE_EXCEPTION_EMPTY, this.message ?: "", null).also {
        it.originThrowable = this
    }
}