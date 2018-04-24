package graphql.android.ws.graphql.websocket

import android.os.Handler
import graphql.android.ws.graphql.ClientGraphql
import graphql.android.ws.graphql.utils.SingletonHolder
import java.util.logging.Logger

class ClientWebsocket(private val listener: ClientWebsocketListener) : WebSocketListener {

    companion object : SingletonHolder<ClientWebsocket, ClientWebsocketListener>(::ClientWebsocket) {
        private val Log: Logger = Logger.getLogger(ClientGraphql::class.java.name)
        private const val HEART_BEAT = 60000L
    }

    private lateinit var webSocket: DefaultWebSocket
    private var socketConnectionHandler: Handler? = null


    init {
        socketConnectionHandler = Handler()
    }

    fun createConnection(url: String){
        try {

            webSocket  = DefaultWebSocket(url, MyWebSocketAdapter(this))
        } catch (e: Exception) {
            Log.info( "Socket exception $e")
        }
    }

    override fun onConnect() {
        Log.info("Websocket is connected")
    }

    override fun onClose() {
        listener.onDisconnected()
    }

    override fun onMessage(message: String?) {
        listener.messageReceived(message)
    }

    override fun onMessage(message: ByteArray?) {

    }

    override fun onPing() {}

    override fun onPing(data: ByteArray) {}

    override fun onPong() {}

    override fun onPong(data: ByteArray) {}


    fun send(data: String){
        webSocket.send(data)
    }

    fun isConnected(): Boolean {
        return webSocket.isConnected
    }


    fun openConnection() {
        webSocket.connect()

        //initNetworkListener()
        startCheckConnection()
    }

    fun closeConnection() {
        webSocket.close()

        //releaseNetworkStateListener()
        stopCheckConnection()
    }

    private val checkConnectionRunnable = {
        val open = webSocket.isConnected
        if (!open) {
            openConnection()
        }
        startCheckConnection()
    }

    private fun startCheckConnection() {
        socketConnectionHandler?.postDelayed(checkConnectionRunnable, HEART_BEAT)
    }

    private fun stopCheckConnection() {
        socketConnectionHandler?.removeCallbacks(checkConnectionRunnable)
    }

    interface ClientWebsocketListener {
        fun onConnected()
        fun onDisconnected()
        fun messageReceived(message: String?)
    }

}