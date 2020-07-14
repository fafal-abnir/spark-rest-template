package coyote

import com.google.gson.GsonBuilder
import coyote.config.getConfigAsHocon
import coyote.exception.*
import coyote.metrics.ServerMetrics
import mu.KotlinLogging
import org.joda.time.DateTime
import spark.Request
import spark.Response
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

/**
 * Server
 *
 * This class is used for defining API and Server
 *
 * @param address address ([InetSocketAddress]) which this service is listening to.
 * @param workers size of thread pool which handling requests
 *
 */
private val logger = KotlinLogging.logger {}

class ProxyService(private val address: InetSocketAddress, private val workers: Int) {
    private val service = spark.Service.ignite().ipAddress(address.hostName).port(address.port).threadPool(workers)
    private val gson = GsonBuilder().disableHtmlEscaping().create()
    @Volatile
    private var stopIsCalled = false


    fun run() {
        register()
        service.awaitInitialization()
    }

    fun stop() {
        stopIsCalled = true
        for (i in 0..10_000) {
            if (executingRequests.get() == 0)
                break
            Thread.sleep(1)
        }
        service.stop()
    }

    private val executingRequests = AtomicInteger()
    private fun <T> watchExecution(block: () -> T): T {
        return try {
            executingRequests.incrementAndGet()
            if (stopIsCalled)
                throw ServiceStopped()
            block()
        } finally {
            executingRequests.decrementAndGet()
        }
    }


    /**
     * Register defined API layout
     *
     * Register defined API layout in service
     *
     */
    private fun register() {
        with(service) {
            put("/v1/:param") { request, response -> watchExecution { putRequest(request, response) } }
            get("/v1/:param") { request, response -> watchExecution { getRequest(request, response) } }
            get("/metrics") { request, response -> watchExecution { createMetrics(request, response) } }
            get("/config") { request, response -> getConfig(request, response) }
            get("/health") { request, response -> getHealth(request, response) }
        }
    }

    private fun handleException(response: Response, exc: Throwable): String {
        val (status, message) = when (exc) {
            is IllegalArgumentException -> Pair(400, exc.message ?: "")
            is IncompleteHeaders -> Pair(400, exc.message ?: "")
            is InvalidContentSize -> Pair(400, exc.message ?: "")
            is ServiceStopped -> Pair(503, exc.message ?: "")
            is OperationFailed -> Pair(500, exc.message)
            is BadChunkIDReceived -> Pair(400, exc.message ?: "")
            is ResourceNotAvailable -> Pair(503, "Resource Temporarily Unavailable")
            else -> { // All general exceptions
                logger.error(exc.message, exc)
                Pair(500, "Internal Error!")
            }
        }
        response.status(status)
        response.body(message)
        return message
    }

    /**
     * simple put implementation
     *
     * @param req [Request]
     * @param res [Response]
     * @return response body
     */
    private fun putRequest(req: Request, res: Response): String {
        val param1 = req.params(":param")
        req.queryParams()
        try {
            res.body("put a $param1")
            logger.info("put something successfully")
        } catch (e: Throwable) {
            logger.error("could not put.")
            handleException(res, e)
        }

        return res.body()
    }

    /**
     * simple get implementation
     *
     * @param req [Request]
     * @param res [Response]
     * @return response body
     */
    private fun getRequest(req: Request, res: Response): String {
        val bucket = req.params(":param")
        try {
            res.status(200)
            res.body("OK")
            logger.info("successfully")
        } catch (e: Throwable) {
            logger.error("could not deleteBucket.",
                    hashMapOf(Pair("bucket", bucket), Pair("exception", e.javaClass.simpleName)))
            ServerMetrics.markErrorRequest()
            handleException(res, e)
        }
        return res.body()
    }

    /**
     * Get metrics of system
     *
     * @param req [Request]
     * @param res [Response]
     * @return response body
     */
    private fun createMetrics(req: Request, res: Response): String {
        res.header("Content-Type", "application/json")
        res.body(gson.toJson(ServerMetrics.getInfo()))
        return res.body()
    }

    /**
     * Get config of system
     *
     * @param req [Request]
     * @param res [Response]
     * @return response body
     */
    private fun getConfig(req: Request, res: Response): String {
        res.header("Content-Type", "application/hocon")
        res.body(gson.toJson(getConfigAsHocon()))
        return res.body()
    }

    /**
     * Health check of system for checking upping or downing of system
     *
     * @param req [Request]
     * @param res [Response]
     * @return response body
     */
    private fun getHealth(req: Request, res: Response) =
            gson.toJson(mapOf("time" to DateTime.now().toString()))
}