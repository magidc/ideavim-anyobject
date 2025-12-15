package com.magidc.ideavim.anyobject.utils

class LRUCache<K, V>(private val maxSize: Int, private val onEvict: ((K, V) -> Unit)? = null) :
    LinkedHashMap<K, V>(maxSize, 0.75f, /* accessOrder = */ true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        if (size > maxSize){
            eldest?.let {
                try {
                    onEvict?.invoke(it.key, it.value)
                }
                catch (e: Throwable) { }
            }
            return true
        }
        return false
    }
}
