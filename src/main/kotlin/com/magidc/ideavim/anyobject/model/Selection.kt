package com.magidc.ideavim.anyobject.model

data class Selection(
    val from: Int,
    val to: Int,
    val leftDelimiter: String,
    val rightDelimiter: String
)
