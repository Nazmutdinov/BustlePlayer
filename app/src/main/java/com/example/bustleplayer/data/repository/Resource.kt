package com.example.bustleplayer.data.repository

sealed class Resource<T> (val data: T? = null, val message: String? = null) {
    class Success<T>(data: T): Resource<T>(data)
    class Empty<T>(data: T): Resource<T>(data)
    class Error<T>(message: String): Resource<T>(null, message)
}
