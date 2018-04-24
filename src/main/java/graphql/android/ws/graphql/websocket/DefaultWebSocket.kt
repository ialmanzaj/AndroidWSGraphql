package graphql.android.ws.graphql.websocket

import com.neovisionaries.ws.client.HostnameUnverifiedException
import com.neovisionaries.ws.client.OpeningHandshakeException
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketFactory
import graphql.android.ws.graphql.GRAPHQL_WS
import graphql.android.ws.graphql.TIMEOUT
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.util.logging.Logger


class DefaultWebSocket(private val uri: String, val adapter: MyWebSocketAdapter) : MyWebSocket {

    companion object {
        val Log: Logger = Logger.getLogger(DefaultWebSocket::class.java.canonicalName)
    }

    var isConnected = false
    private var mWebSocket: WebSocket? = null

     override  fun connect() {
         // don't connect if already connected .. user needs to disconnect first
         if (isConnected) {
             throw  WebSocketException("already connected")
         }

         if (mWebSocket == null){
            // use async connector on short-lived background thread
            WebSocketConnector(uri).start()
         }else {

             reconnect()
         }
    }

    inner class WebSocketConnector(private val uri: String) : Thread() {
        override fun run() {
            Thread.currentThread().name = "WebSocketConnector"
            /*
             * connect TCP socket
             */

            try {
                val factory = WebSocketFactory().setConnectionTimeout(TIMEOUT)
                //val context = NaiveSSLContext.getInstance("TLS")
                //factory.sslContext = context
                mWebSocket = factory.createSocket(uri)
                mWebSocket?.addProtocol(GRAPHQL_WS)
                mWebSocket?.addListener(adapter)
                mWebSocket?.connect()

                Log.info("creating web-socket $mWebSocket")

            } catch (e: OpeningHandshakeException) {
                Log.warning("OpeningHandshakeException $e")
                // A violation against the WebSocket protocol was detected
                // during the opening handshake.
                // Status line.
                val sl = e.statusLine
                Log.info("=== Status Line ===")
                Log.info("HTTP Version  = %s\n ${sl.httpVersion}")
                Log.info("Status Code   = %d\n  ${sl.statusCode}")
                Log.info("Reason Phrase = %s\n ${sl.reasonPhrase}")

                // HTTP headers.
                val headers = e.headers
                Log.info("=== HTTP Headers ===")
                for ((name, values) in headers) {
                    // Header name.

                    // Values of the header.

                    if (values == null || values.size == 0) {
                        // Print the name only.
                        Log.info(name)
                        continue
                    }

                    for (value in values) {
                        // Print the name and the value.
                        Log.info("%s: %s\n $name, $value")
                    }
                }
            } catch (e: HostnameUnverifiedException) {
                Log.warning("HostnameUnverifiedException $e")
                // The certificate of the peer does not match the expected hostname.
            }catch (e: NoSuchAlgorithmException) {
                Log.warning("NoSuchAlgorithmException $e")
            } catch (e: WebSocketException) {
                Log.warning("WebSocketException $e")
            } catch (e: IOException) {
                Log.warning("IOException $e")
            }

        }

    }

    private fun reconnect() {
        try {
            mWebSocket = mWebSocket?.recreate()?.connect()
            Log.info("reconnecting")
        } catch (e: WebSocketException) {
            Log.info("WebSocketException $e")
        } catch (e: IOException) {
            Log.info("IOException $e")
        }
    }

    override fun send(data: String) {
        mWebSocket?.sendText(data)
    }

    override fun send(data: ByteArray) {
        mWebSocket?.sendBinary(data)
    }

    override fun stream(data: String, isFinalChunk: Boolean) {

    }

    override fun stream(data: ByteArray, isFinalChunk: Boolean) {

    }

    override fun ping() {
        mWebSocket?.sendPing()
    }

    override fun ping(data: String) {
        mWebSocket?.sendPing(data)
    }

    override fun ping(data: ByteArray) {
        mWebSocket?.sendPing(data)
    }

    override fun pong() {
        mWebSocket?.sendPong()
    }

    override fun pong(data: String) {
        mWebSocket?.sendPong(data)
    }

    override fun pong(data: ByteArray) {
        mWebSocket?.sendPong(data)
    }

    override fun close() {
        mWebSocket?.disconnect()
    }

    override fun close(reason: String) {
        mWebSocket?.disconnect(reason)
    }

    enum class PayloadOrigin {
        DATA_FRAME, PING_FRAME, PONG_FRAME
    }

}