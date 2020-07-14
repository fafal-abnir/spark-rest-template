package coyote.metrics


import com.codahale.metrics.*
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

open class Metrics : Closeable {
    override fun close() {
        reporter.close()
    }

    protected val metrics = MetricRegistry()
    protected val exceptions = metrics.meter("exceptions")!!
    private val exceptionMeterMap: LoadingCache<Throwable, Meter> =
            CacheBuilder.newBuilder().maximumSize(1000)
                    .build<Throwable, Meter>(CacheLoader.from { t ->
                        metrics.meter("${t!!.javaClass.simpleName}-${t.message}")
                    })

    protected var loggerName: String = Metrics::class.java.canonicalName
    val reporter = Slf4jReporter.forRegistry(metrics)
            .outputTo(LoggerFactory.getLogger(loggerName))
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()

    init {

        MetricRegistry.name("")

        reporter.start(1, TimeUnit.MINUTES)
    }

    fun <T> addGauge(name: String, supplier: Supplier<T>) {
        try {
            metrics.register(name, Gauge<T> { supplier.get() })
        } catch (e: Exception) {
            //Do nothing
        }
    }

    private fun sortMetersByCount(meters: Map<String, Meter>) =
            meters.toList().sortedBy { it.second.count }.reversed().map {
                Pair(it.first, it.second.toPojo())
            }.toMap()

    private fun sortHistosByCount(histos: Map<String, Histogram>) =
            histos.toList().sortedBy { it.second.count }.reversed().map {
                Pair(it.first, it.second.toPojo())
            }.toMap()

    /**
     * Prepares lighthouse requests statistics to be served on /metrics.
     */
    fun getInfo() = ServerInfo(metrics.gauges.mapValues { it.value.value },
            sortMetersByCount(metrics.meters),
            sortHistosByCount(metrics.histograms))

    fun mark(t: Throwable) {
        exceptions.mark()
        exceptionMeterMap[t]!!.mark()
    }

    inner class Histo(name: String) {
        private val histogram = metrics.histogram(name)

        fun update(value: Long) {
            histogram.update(value)
        }

        fun getHisto(): Histogram {
            return histogram
        }
    }

    inner class Metric(name: String) {
        private var metric = metrics.meter(name)

        fun mark(l: Long = 1) {
            metric.mark(l)
        }
    }

    inner class SuccessErrorMetric(name: String) {
        private val successMetric = metrics.meter("${name.capitalize()}Success")
        private val errorMetric = metrics.meter("${name.capitalize()}Error")
        private val totalMetric = metrics.meter(name)

        fun markSuccess(l: Long = 1) {
            successMetric.mark(l)
            totalMetric.mark(l)
        }

        fun markError(l: Long = 1) {
            errorMetric.mark(l)
            totalMetric.mark(l)
        }

        fun successRatePer1Min(): Double {
            return successMetric.oneMinuteRate
        }

        fun errorRatePer1Min(): Double {
            return errorMetric.oneMinuteRate
        }
    }

}

/**
 * Represents Lighthouse statistics DTO served by [LighthouseReporter] as the result of /metrics service.
 */
data class ServerInfo(val gauges: Map<String, Any>, val meters: Map<String, MeterPojo>,
                      val histograms: Map<String, HistogramPojo>
)

/**
 * Represents as Meters statistics data model as a part of data model for [LighthouseReporter] /health service.
 */
data class MeterPojo(val count: Long,
                     val rate: Double,
                     val oneMinuteRate: Double,
                     val fiveMinuteRate: Double,
                     val fifteenMinuteRate: Double
)

private fun Meter.toPojo() =
        MeterPojo(count, meanRate, oneMinuteRate, fiveMinuteRate, fifteenMinuteRate)

/**
 * Represents as Meters histogram statistics data model as a part of data model for [LighthouseReporter] /health service.
 */
data class HistogramPojo(val mean: Double,
                         val median: Double,
                         val max: Long,
                         val min: Long,
                         val stdDev: Double,
                         val ninetyFive: Double,
                         val ninetyNine: Double
)

private fun Histogram.toPojo() =
        HistogramPojo(snapshot.mean, snapshot.median, snapshot.max, snapshot.min,
                snapshot.stdDev, snapshot.get95thPercentile(), snapshot.get99thPercentile())
