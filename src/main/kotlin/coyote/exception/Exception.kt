package coyote.exception

open class ResourceDoesNotExist(override var message: String) : Exception(message)
open class ResourceAlreadyExists(override var message: String) : Exception(message)
open class OperationFailed(override var message: String) : Exception(message)


class InvalidContentSize() : Exception("Content-Length = 0")
class IncompleteHeaders(header: String) : Exception("Header: $header is mandatory")
class BadChunkIDReceived() : Exception("ChunkID should be greater than 0. ")


class ServiceStopped() : Exception("Service try to stop gracefully.")

class ResourceNotAvailable(message: String) : Exception(message)



