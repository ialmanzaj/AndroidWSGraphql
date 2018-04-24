package graphql.android.ws.graphql.websocket

class WebSocketException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, t: Throwable) : super(message, t)
    companion object {
        private val serialVersionUID = 939250145018159015L
    }
}