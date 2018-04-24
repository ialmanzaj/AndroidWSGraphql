package graphql.android.ws.graphql.websocket


interface MyWebSocket {

    @Throws(WebSocketException::class)
    fun connect()

    @Throws(WebSocketException::class)
    fun send(data: String)

    @Throws(WebSocketException::class)
    fun send(data: ByteArray)

    @Throws(WebSocketException::class)
    fun stream(data: String, isFinalChunk: Boolean)

    @Throws(WebSocketException::class)
    fun stream(data: ByteArray, isFinalChunk: Boolean)

    @Throws(WebSocketException::class)
    fun ping()

    @Throws(WebSocketException::class)
    fun ping(data: String)

    @Throws(WebSocketException::class)
    fun ping(data: ByteArray)

    @Throws(WebSocketException::class)
    fun pong()

    @Throws(WebSocketException::class)
    fun pong(data: String)

    @Throws(WebSocketException::class)
    fun pong(data: ByteArray)

    @Throws(WebSocketException::class)
    fun close()

    @Throws(WebSocketException::class)
    fun close(reason: String)

}