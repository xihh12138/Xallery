package com.xihh.base.model

data class Wrapper<T>(
    val base: T,
){
    operator fun invoke() = base
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return base?.hashCode() ?: 0
    }
}