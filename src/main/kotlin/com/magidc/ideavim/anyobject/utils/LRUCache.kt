package com.magidc.ideavim.anyobject.utils

class LRUCache<K, V>(private val maxSize: Int) :
    LinkedHashMap<K, V>(maxSize, 0.75f, /* accessOrder = */ true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }
}
