package graphql.android.ws.graphql.utils

open class SingleSingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun get(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }
        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}

open class SingletonHolder<out T, in A, in B>(creator: (A, B) -> T) {
    private var creator: ((A, B) -> T)? = creator
    @Volatile private var instance: T? = null

    fun get(arg: A, arg1: B): T {
        val i = instance
        if (i != null) {
            return i
        }
        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg, arg1)
                instance = created
                creator = null
                created
            }
        }
    }
}