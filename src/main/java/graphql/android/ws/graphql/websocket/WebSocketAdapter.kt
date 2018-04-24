package graphql.android.ws.graphql.websocket

import com.neovisionaries.ws.client.*
import com.neovisionaries.ws.client.WebSocketException
import java.nio.charset.Charset
import java.util.logging.Logger

class MyWebSocketAdapter(private val webSocketListener: WebSocketListener): WebSocketAdapter() {

    companion object {
        val Log: Logger = Logger.getLogger(WebSocketAdapter::class.java.canonicalName)
    }

    override fun onConnected(websocket: WebSocket, headers: Map<String, List<String>>) {
        super.onConnected(websocket, headers)
        Log.info( "onConnected $websocket headers:${ headers.map {it.value}}")
        webSocketListener.onConnect()
    }

    override fun onStateChanged(websocket: WebSocket, newState: WebSocketState) {
        super.onStateChanged(websocket, newState)
        Log.info( "onStateChanged ---> $newState")
    }

    override fun onPingFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        super.onPingFrame(websocket, frame)
        val message = frame?.payload?.let { String(it, Charset.forName("UTF-8"))}
        Log.info("onPingFrame ---> $websocket payload: $message")
    }

    override fun onCloseFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        super.onCloseFrame(websocket, frame)
        val message = frame?.payload?.let { String(it, Charset.forName("UTF-8"))}
        Log.info( "onCloseFrame ---> $message")
    }

    override fun onSendError(websocket: WebSocket?, cause: WebSocketException?, frame: WebSocketFrame?) {
        super.onSendError(websocket, cause, frame)
        Log.throwing( "onSendError ---> ${cause?.error}," +
                "payload: ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}", "onSendError",cause)
    }

    override fun onTextMessage(websocket: WebSocket, message: String) {
        super.onTextMessage(websocket, message)
        Log.info( "Message ---> $message")
        webSocketListener.onMessage(message)
    }


    override fun onPongFrame(websocket: WebSocket, frame: WebSocketFrame) {
        super.onPongFrame(websocket, frame)
        Log.info( "onPongFrame payload: ${String(frame.payload, Charset.forName("UTF-8"))} ")
    }

    override fun onTextMessageError(websocket: WebSocket, cause: WebSocketException, data: ByteArray) {
        super.onTextMessageError(websocket, cause, data)
        Log.throwing( "onTextMessageError --> ${cause.error} $data", "onTextMessageError", cause)

    }

    override fun onConnectError(websocket: WebSocket, exception: WebSocketException) {
        super.onConnectError(websocket, exception)
        Log.throwing( "onConnectError --> " + exception.error, "onConnectError", exception)
    }

    override fun onError(websocket: WebSocket, cause: WebSocketException) {
        super.onError(websocket, cause)
        Log.throwing( "Error -->" + cause.message, "onError", cause)
    }

    override fun onBinaryFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        super.onBinaryFrame(websocket, frame)
        val message = frame?.payload?.let { String(it, Charset.forName("UTF-8"))}
        Log.info("onBinaryFrame payload: $message}")
    }

    override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
        super.onBinaryMessage(websocket, binary)
        Log.info( "onBinaryMessage " +
                "payload: ${binary?.let { String(it, Charset.forName("UTF-8")) }} ")
    }

    override fun onFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        super.onFrame(websocket, frame)
        val message = frame?.payload?.let { String(it, Charset.forName("UTF-8"))}
        Log.info("onFrame payload: $message}")
    }

    override fun onFrameError(websocket: WebSocket?, cause: WebSocketException?, frame: WebSocketFrame?) {
        super.onFrameError(websocket, cause, frame)
        Log.info( "onFrameError cause ${cause?.message}")
    }

    override fun onFrameSent(websocket: WebSocket?, frame: WebSocketFrame?) {
        super.onFrameSent(websocket, frame)
        val message = frame?.payload?.let { String(it, Charset.forName("UTF-8"))}
        Log.info("onFrameSent-payload: $message")
    }

    override fun onMessageError(websocket: WebSocket?, cause: WebSocketException?, frames: MutableList<WebSocketFrame>?) {
        super.onMessageError(websocket, cause, frames)
        Log.throwing( "Error --> ${cause?.message} frame ${frames?.map { it.payload }}", "onMessageError", cause)
    }

    override fun onTextFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
        super.onTextFrame(websocket, frame)
        val message = frame?.payload?.let { String(it, Charset.forName("UTF-8"))}
        Log.info("onTextFrame - payload: $message")
    }

    override fun onSendingHandshake(websocket: WebSocket?, requestLine: String?, headers: MutableList<Array<String>>?) {
        super.onSendingHandshake(websocket, requestLine, headers)
        Log.info( "onSendingHandshake $requestLine ${ headers?.map { it } }")
    }

    override fun onFrameUnsent(websocket: WebSocket?, frame: WebSocketFrame?) {
        super.onFrameUnsent(websocket, frame)
        Log.info( "onFrameUnsent frame ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
    }

    override fun onDisconnected(websocket: WebSocket,
                                serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame, closedByServer: Boolean) {
        Log.info( "onDisconnected " +
                "serverCloseFrame ${serverCloseFrame.payload?.let { String(it, Charset.forName("UTF-8"))}} " +
                "clientCloseFrame: ${clientCloseFrame.payload?.let { String(it, Charset.forName("UTF-8"))}} " +
                "closedByServer: $closedByServer")
        if (closedByServer) {
            //reconnect()
        }
        webSocketListener.onClose()
    }

    override fun onUnexpectedError(websocket: WebSocket, cause: WebSocketException) {
        Log.throwing( "Error -->" + cause.message, "onUnexpectedError", cause)

    }
}