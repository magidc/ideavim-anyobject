package com.magidc.ideavim.anyobject.utils

class LRUCache<K, V>(var maxSize: Int, private val onEvict: ((K, V) -> Unit)? = null) :
    LinkedHashMap<K, V>(maxSize, 0.75f, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        if (size > maxSize){
            eldest?.let {
                try {
                    onEvict?.invoke(it.key, it.value)
                }
                catch (_: Throwable) { }
            }
            return true
        }
        return false
    }
}
