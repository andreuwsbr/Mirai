package com.andrews.mirai.presentation.reader.state

data class ReaderPageErrorState(
    val pageIndex: Int,
    val message: String,
    val retryCount: Int = 0
)