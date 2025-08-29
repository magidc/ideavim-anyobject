package com.magidc.ideavim.anyObject.handlers

import com.magidc.ideavim.anyObject.DelimiterHandler

interface BaseHandlers {
    fun getInnerHandler(): DelimiterHandler
    fun getOuterHandler(): DelimiterHandler
}