package com.xallery.common.util

import com.xihh.base.util.BaseInterceptor
import com.xihh.base.util.IntentInterceptChain
import com.xihh.base.util.Interceptor

class LoginChain(interceptors: MutableList<Interceptor>): IntentInterceptChain(interceptors) {
}

class LoginInterceptor : BaseInterceptor() {

    override fun intercept(chain: IntentInterceptChain) {
        super.intercept(chain)

//        if (LoginManager.isLogin()) {
//            //如果已经登录 -> 放行, 转交给下一个拦截器
//            chain.process()
//        } else {
//            //如果未登录 -> 去登录页面
//            LoginDemoActivity.startInstance()
//        }
    }


    fun loginFinished() {
        //如果登录完成，调用方法放行到下一个拦截器
        chain?.process()
    }
}