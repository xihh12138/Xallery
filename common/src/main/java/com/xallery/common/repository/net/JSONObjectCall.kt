package com.xallery.common.repository.net

import com.xihh.base.util.logf
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class JSONObjectCall(val proxy: Call<JSONObject>) : Call<JSONObject> by proxy {

    override fun execute(): Response<JSONObject> = try {
        proxy.execute().check(proxy)
    } catch (e: Exception) {
        Response.success(e.toResponse())
    }

    override fun enqueue(callback: Callback<JSONObject>) {
        proxy.enqueue(object : Callback<JSONObject> {
            override fun onResponse(
                call: Call<JSONObject>,
                JSONObject: Response<JSONObject>,
            ) {
                try {
                    callback.onResponse(this@JSONObjectCall, JSONObject.check(call))
                } catch (e: Exception) {
                    callback.onResponse(
                        this@JSONObjectCall, Response.success(e.toResponse())
                    )
                }
            }

            override fun onFailure(call: Call<JSONObject>, t: Throwable) {
                logf { "JSONObjectCall: onFailure   t=${t.stackTraceToString()}" }
                callback.onResponse(this@JSONObjectCall, Response.success(t.toResponse()))
            }
        })
    }

    override fun clone(): Call<JSONObject> = JSONObjectCall(proxy.clone())

    private fun Response<JSONObject>.check(call: Call<JSONObject>): Response<JSONObject> {
        val responseBody = this.body()      // 因为这里可能是返回body，也有可能返回errorBody
        return if (responseBody == null) {
            Response.success(IOException("url=${call.request().url} responseBody is null").toResponse())
        } else {
            this
        }
    }
}

class JSONObjectCallAdapterFactory : CallAdapter.Factory() {

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
        if (getRawType(apiType) == JSONObject::class.java) {
            return object : CallAdapter<JSONObject, Call<JSONObject>> {
                override fun responseType() = apiType

                override fun adapt(call: Call<JSONObject>): Call<JSONObject> = JSONObjectCall(call)

            }
        }
        return null
    }

}

class JSONObjectConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, JSONObject>? {
        return if (getRawType(type) == JSONObject::class.java) {
            Converter { value: ResponseBody ->
                JSONObject(value.string())
            }
        } else {
            null
        }
    }
}

/**
 * 异常转Api
 */
private fun Throwable.toResponse(): JSONObject {
    return JSONObject(mapOf("success" to "false"))
}