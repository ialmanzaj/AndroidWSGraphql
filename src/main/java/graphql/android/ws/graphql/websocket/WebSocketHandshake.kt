package graphql.android.ws.graphql.websocket

import java.net.URI
import java.util.*

class WebSocketHandshake @Throws(WebSocketException::class) constructor(private val url:URI) {

    init{
    }

    @Throws(WebSocketException::class)
     fun verifyServerStatusLine(statusLine:String) {
        val statusCode = Integer.valueOf(statusLine.substring(9, 12))
        when {
            statusCode == 407 -> throw WebSocketException("connection failed: proxy authentication not supported")
            statusCode == 404 -> throw WebSocketException("connection failed: 404 not found")
            statusCode != 101 -> throw WebSocketException("connection failed: unknown status code $statusCode")
            else -> {
            }
        }
    }


    @Throws(WebSocketException::class)
    fun verifyServerHandshakeHeaders(headers:HashMap<String, String>) {
        val upgradeKey:String = when {
            headers.containsKey("Upgrade") -> "Upgrade"
            headers.containsKey("upgrade") -> "upgrade"
            else -> throw WebSocketException("connection failed: missing header field in server opening handshake: Upgrade")
        }

        if (!headers[upgradeKey].equals("websocket", ignoreCase = true)) {
            throw WebSocketException("connection failed: 'Upgrade' header in server opening handshake does not match 'websocket'")
        }

        val connectionKey:String = when {
            headers.containsKey("Connection") -> "Connection"
            headers.containsKey("connection") -> "connection"
            else -> throw WebSocketException("connection failed: missing header field in server opening handshake: Connection")
        }

        if (!headers[connectionKey].equals("upgrade", ignoreCase = true)) {
            throw WebSocketException("connection failed: 'Connection' header in server opening handshake does not match 'Upgrade'")
        }

         /* Browsers should also check the origin policy! But, we aren't a browser.
                else if (!headers.get("Sec-WebSocket-Origin").equals(origin)) {
                    throw new WebSocketException("connection failed: missing header field in server handshake: Sec-WebSocket-Origin");
                }
            */
	}


    private fun <T> arrayContains(array:Array<T>, `object`:T):Boolean {
        for (elem in array) { if (elem == `object`) { return true } }
        return false
    }

    /* ######################################################################## */
    /* ######################################################################## */
    /* ######################################################################## */

    private inner class KeyGenerationResult(val key:String,  val expectedSecWebSocketAcceptValue:ByteArray)

    companion object {

        private const val CRLF = "\r\n"
        private const val GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
    }
}