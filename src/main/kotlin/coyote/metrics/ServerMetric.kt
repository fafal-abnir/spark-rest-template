package coyote.metrics


import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache

/**
 * Provides some methods to mark function calls and gather statistics from them.
 */
class ServerMetricsMain : Metrics() {

    class MapWithDefault<K, V>(maxSize: Long = 256, loader: (K?) -> V?) :
            LoadingCache<K, V> by CacheBuilder
                    .newBuilder()
                    .maximumSize(maxSize)
                    .build(CacheLoader.from(loader))

    //edge
    private val requests = SuccessErrorMetric("requests")
    private val putRequest = SuccessErrorMetric("putRequest")
    private val getRequest = SuccessErrorMetric("getRequest")
    private val successLatency = Histo("successLatency")
    private val errorLatency = Histo("errorLatency")

    fun markSuccessRequest(count: Long = 1) = requests.markSuccess(count)
    fun markErrorRequest(count: Long = 1) = requests.markError(count)


    init {
        loggerName = this.javaClass.canonicalName
    }


}

val ServerMetrics = ServerMetricsMain()
