package com.magidc.ideavim.anyObject.model

data class Selection(
    val from: Int,
    val to: Int,
    val openDelimiter: String,
    val closeDelimiter: String
)
