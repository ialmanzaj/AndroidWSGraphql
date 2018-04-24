package graphql.android.ws.graphql.websocket


interface WebSocketListener {
    fun onConnect()
    fun onClose()

    fun onMessage(message: String?)
    fun onMessage(message: ByteArray?)

    fun onPing()
    fun onPing(data: ByteArray)

    fun onPong()
    fun onPong(data: ByteArray)
}