package graphql.android.ws.graphql

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.android.ws.graphql.NetworkStateReceiver.Companion.ACTION_NETWORK_STATE_CHANGED
import graphql.android.ws.graphql.model.OperationMessage
import graphql.android.ws.graphql.model.Payload
import graphql.android.ws.graphql.model.Subscription
import org.json.JSONObject
import java.util.logging.Logger

class SocketConnection(private val context: Context,
                       private val view: SocketConnectionListener,  URL: String) : ClientWebSocket.SocketListenerCallback {
    companion object {
        private val Log: Logger = Logger.getLogger(SocketConnection::class.java.name)
        private const val HEART_BEAT = 60000L
    }

    private var clientWebSocket: ClientWebSocket? = null
    private var socketConnectionHandler: Handler? = null

    private var queue : MutableList<String> = mutableListOf()

    init {
        socketConnectionHandler = Handler()

        try {
            clientWebSocket = ClientWebSocket(this, URL)
            openConnection()

        } catch (e: Exception) {
            Log.info( "Socket exception $e")
        }
    }

    fun subscribe(subscription: Subscription) {
        this.subscribe(subscription.query, subscription.tag, subscription.variables, subscription.operationName)
    }

    fun subscribe(query: String, tag: String, variables: String?, operationName: String?): Subscription {
        val message = OperationMessage("1",
                 GQL_START,
                Payload(query = query,
                        variables = JSONObject(variables),
                        operationName = operationName
                )
        )
        this.sendMessage(message)
        return Subscription(query, tag)
    }

    fun unsubscribe(subscription: Subscription) {
        this.unsubscribe(subscription.tag)
    }

    fun unsubscribe(tag: String) {
        val message = OperationMessage(tag, GQL_STOP,  null)
        this.sendMessage(message)
    }

    fun unsubscribeAll(subscriptions: Collection<Subscription>) {
        for (subscription in subscriptions) {
            this.unsubscribe(subscription.tag)
        }
    }

    private val checkConnectionRunnable = {
        val open = clientWebSocket?.getConnection()?.isOpen ?: false
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

    private fun openGraphqlConnection(){
        this.sendMessage(OperationMessage(null, GQL_CONNECTION_INIT, null))
    }

    fun openConnection() {
        if (clientWebSocket != null) clientWebSocket!!.close()

        Log.info( "Socket is opening a connection ")
        clientWebSocket?.connect()
        initNetworkListener()
        startCheckConnection()
    }

    fun closeConnection() {
        if(clientWebSocket != null) {
            closeGraphqlConnection()
            clientWebSocket?.close()
            clientWebSocket = null
        }

        releaseNetworkStateListener()
        stopCheckConnection()
    }

    fun closeGraphqlConnection(){
        this.sendMessage(OperationMessage(null, GQL_CONNECTION_TERMINATE, null ))
    }

    fun sendMessage(message: OperationMessage) {
        val response = ObjectMapper().writeValueAsString(message)
        if (isConnected()){
           sendRaw(response)
        }else{
            openConnection()
            queue.add(response)
        }
    }

    private fun sendRaw(response: String){
        Log.info("sending raw message $response")
        clientWebSocket?.sendMessage(response)
    }

    override fun onConnected() {
        Log.info("Websocket is connected")
        openGraphqlConnection()
    }

    override fun onSocketMessage(message: String) {
        val response = JSONObject(message)
        when (response.get("type")) {
            GQL_CONNECTION_ACK -> {
                Log.info("Graphql is connected")
                view.onConnected()
                if (queue.isNotEmpty()){
                    Log.info("sending...message")
                    queue.map { sendRaw(it) }
                    queue = mutableListOf()
                }
            }
            GQL_CONNECTION_KEEP_ALIVE -> {
                Log.info("Ping by server.")
            }
            GQL_ERROR -> {
                view.onReceivedMessage(Response.Error(response.get("payload").toString()))
            }
            GQL_CONNECTION_ERROR -> {
                view.onReceivedMessage(Response.Error(response.get("payload").toString()))
            }
            GQL_DATA -> {
                val data = response.getJSONObject("payload").get("data").toString()
                view.onReceivedMessage(Response.Data(data))
            }
            GQL_COMPLETE -> {
                Log.info("Operation complete.")
            }
            GQL_CONNECTION_TERMINATE -> {
                Log.info("Graphql is disconnected")
                view.onDisconnected()
            }
        }
    }

    override fun onDisconnected() {
        Log.info("Websocket is disconnected")
    }

    override fun onError(error: Exception) {
        Log.info("Websocket error: $error")
    }

    private fun initNetworkListener(){
        context.registerReceiver(mNetworkReceiver,  IntentFilter(ACTION_NETWORK_STATE_CHANGED))
    }

    private fun releaseNetworkStateListener() {
        try {
            context.unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            Log.warning("IllegalArgumentException $e")
        }
    }

    private val mNetworkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val networkIsOn = intent.getBooleanExtra(ACTION_NETWORK_STATE_CHANGED, false)
            if (networkIsOn) {
                Log.info( "networkIsOn -> openConnection")
                openConnection()
            } else {
                Log.info( "networkIsOff -> closeConnection")
                closeConnection()
            }
        }
    }

    fun isConnected(): Boolean {
        return clientWebSocket?.getConnection() != null &&
                clientWebSocket?.getConnection()!!.isOpen
    }

    interface SocketConnectionListener {
        fun onConnected()
        fun onReceivedMessage(response: Response)
        fun onDisconnected()
    }

    sealed class Response {
        data class Error(val message: String) : Response()
        data class Data(val data: String) : Response()
    }

}