package graphql.android.ws.graphql

import android.util.Log
import com.neovisionaries.ws.client.*
import java.io.IOException
import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException

class ClientWebSocket(private var listener: SocketListenerCallback, private var  host: String) {

    companion object {
        val TAG: String = ClientWebSocket::class.java.canonicalName
        const val TIMEOUT: Int = 5000
    }

    private var ws: WebSocket? = null

    fun connect() {
        Thread {
            if (ws != null) {
                reconnect()
            } else {
                try {

                    val factory = WebSocketFactory().setConnectionTimeout(TIMEOUT)
                    //val context = NaiveSSLContext.getInstance("TLS")
                    //factory.sslContext = context
                    ws = factory.createSocket(host)
                    ws?.addProtocol(GRAPHQL_WS)
                    ws?.addListener(SocketAdapter())
                    ws?.connect()

                    sendPing()

                    Log.d(TAG,"creating web-socket $ws")

                } catch (e:OpeningHandshakeException) {
                    Log.e(TAG,"OpeningHandshakeException $e")
                    // A violation against the WebSocket protocol was detected
                    // during the opening handshake.
                    // Status line.
                    val sl = e.statusLine
                    println("=== Status Line ===")
                    System.out.format("HTTP Version  = %s\n", sl.httpVersion)
                    System.out.format("Status Code   = %d\n", sl.statusCode)
                    System.out.format("Reason Phrase = %s\n", sl.reasonPhrase)

                    // HTTP headers.
                    val headers = e.headers
                    println("=== HTTP Headers ===")
                    for ((name, values) in headers) {
                        // Header name.

                        // Values of the header.

                        if (values == null || values.size == 0) {
                            // Print the name only.
                            println(name)
                            continue
                        }

                        for (value in values) {
                            // Print the name and the value.
                            System.out.format("%s: %s\n", name, value)
                        }
                    }
                } catch (e:HostnameUnverifiedException) {
                    Log.e(TAG,"HostnameUnverifiedException $e")
                    // The certificate of the peer does not match the expected hostname.
                }catch (e:NoSuchAlgorithmException) {
                    Log.e(TAG,"NoSuchAlgorithmException $e")
                } catch (e: WebSocketException) {
                    Log.e(TAG,"WebSocketException $e")
                } catch (e:IOException) {
                    Log.e(TAG,"IOException $e")
                }
            }
        }.start()
    }

    private fun reconnect() {
        try {
            ws = ws?.recreate()?.connect()
            Log.i(TAG, "reconnecting")
        } catch (e: WebSocketException) {
            Log.e(TAG,"WebSocketException $e")
        } catch (e: IOException) {
            Log.e(TAG,"IOException $e")
        }
    }

    fun getConnection(): WebSocket? {
        return ws
    }

    fun sendPing(){
        ws?.pingInterval = 60 * 1000
    }

    fun sendMessage(message: String) {
        Log.i(TAG, "sending message: $message")
        ws?.sendText(message)
    }

    fun close() {
        ws?.disconnect()
        Log.d(TAG,"disconnecting socket")
    }

    inner class SocketAdapter : WebSocketAdapter() {
        @Throws(Exception::class)
        override fun onConnected(websocket: WebSocket, headers: Map<String, List<String>>) {
            super.onConnected(websocket, headers)
            Log.i(TAG, "onConnected $websocket headers: ${headers.map { it.value }}")
            listener.onConnected()
        }
        @Throws(Exception::class)
        override fun onStateChanged(websocket: WebSocket, newState: WebSocketState) {
            super.onStateChanged(websocket, newState)
            Log.i(TAG, "onStateChanged --> $newState")
        }
        override fun onPingFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onPingFrame(websocket, frame)
            Log.i(TAG, "onPingFrame --> $websocket " +
                    "payload: ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onCloseFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onCloseFrame(websocket, frame)
            Log.i(TAG, "onCloseFrame --> ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onSendError(websocket: WebSocket?, cause: WebSocketException?, frame: WebSocketFrame?) {
            super.onSendError(websocket, cause, frame)
            Log.e(TAG, "onSendError --> ${cause?.error}," +
                    "payload: ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onTextMessage(websocket: WebSocket, message: String) {
            Log.i(TAG, "Message --> $message")
            listener.onSocketMessage(message)
        }
        @Throws(Exception::class)
        override fun onPongFrame(websocket: WebSocket, frame: WebSocketFrame) {
            super.onPongFrame(websocket, frame)
            Log.i(TAG, "onPongFrame payload: ${String(frame.payload, Charset.forName("UTF-8"))} ")
        }
        @Throws(Exception::class)
        override fun onTextMessageError(websocket: WebSocket, cause: WebSocketException, data: ByteArray) {
            super.onTextMessageError(websocket, cause, data)
            Log.e(TAG, "onTextMessageError --> ${cause.error} $data")
            listener.onError(cause)
        }
        @Throws(Exception::class)
        override fun onConnectError(websocket: WebSocket, exception: WebSocketException) {
            super.onConnectError(websocket, exception)
            Log.e(TAG, "onConnectError --> " + exception.error)
            listener.onError(exception)
        }
        override fun onError(websocket: WebSocket, cause: WebSocketException) {
            Log.e(TAG, "Error -->" + cause.message)
            reconnect()
        }
        override fun onBinaryFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onBinaryFrame(websocket, frame)
            Log.i(TAG, "onBinaryFrame " +
                    "payload: ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
            super.onBinaryMessage(websocket, binary)
            Log.i(TAG, "onBinaryMessage " +
                    "payload: ${binary?.let { String(it, Charset.forName("UTF-8")) }} ")
        }
        override fun onFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onFrame(websocket, frame)
            Log.i(TAG, "onFrame " +
                    "payload: ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onFrameError(websocket: WebSocket?, cause: WebSocketException?, frame: WebSocketFrame?) {
            super.onFrameError(websocket, cause, frame)
            Log.i(TAG, "onFrameError cause ${cause?.message}")
        }
        override fun onFrameSent(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onFrameSent(websocket, frame)
            Log.i(TAG, "onFrameSent " +
                    "payload: ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onMessageError(websocket: WebSocket?, cause: WebSocketException?, frames: MutableList<WebSocketFrame>?) {
            super.onMessageError(websocket, cause, frames)
            Log.i(TAG, "onMessageError cause: ${cause?.error} frame ${frames?.map { it.payload }}")
        }
        override fun onTextFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onTextFrame(websocket, frame)
            Log.i(TAG, "onTextFrame" +
                    " payload: ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onSendingHandshake(websocket: WebSocket?, requestLine: String?, headers: MutableList<Array<String>>?) {
            super.onSendingHandshake(websocket, requestLine, headers)
            Log.i(TAG, "onSendingHandshake requestLine $requestLine ${headers?.map{it}}")
        }

        override fun onFrameUnsent(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onFrameUnsent(websocket, frame)
            Log.i(TAG, "onFrameUnsent frame ${frame?.payload?.let { String(it, Charset.forName("UTF-8")) }}")
        }
        override fun onDisconnected(websocket: WebSocket,
                                    serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame,
                                    closedByServer: Boolean) {
            Log.i(TAG, "onDisconnected " +
                    "serverCloseFrame ${serverCloseFrame.payload?.let { String(it, Charset.forName("UTF-8"))}} " +
                    "clientCloseFrame: ${clientCloseFrame.payload?.let { String(it, Charset.forName("UTF-8"))}} " +
                    "closedByServer: $closedByServer")
            if (closedByServer) {
                reconnect()
            }
            listener.onDisconnected()
        }
        override fun onUnexpectedError(websocket: WebSocket, cause: WebSocketException) {
            Log.e(TAG, "Error -->" + cause.message)
            reconnect()
            listener.onError(cause)
        }
    }

    interface SocketListenerCallback {
        fun onConnected()
        fun onSocketMessage(message: String)
        fun onDisconnected()
        fun onError(error: Exception)
    }

}