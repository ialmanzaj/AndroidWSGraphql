package graphql.android.ws.graphql

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import com.google.gson.Gson
import com.google.gson.JsonParser
import graphql.android.ws.graphql.NetworkStateReceiver.Companion.ACTION_NETWORK_STATE_CHANGED
import graphql.android.ws.graphql.model.MessageClient
import graphql.android.ws.graphql.model.MessageServer
import graphql.android.ws.graphql.model.Payload
import graphql.android.ws.graphql.model.Subscription
import java.util.logging.Logger

class SocketConnection(private val context: Context,
                       private val view: SocketConnectionListener,  URL: String) : ClientWebSocket.SocketListenerCallback {
    companion object {
        private val Log: Logger = Logger.getLogger(SocketConnection::class.java.name)
        private const val HEART_BEAT = 60000L
    }

    private var clientWebSocket: ClientWebSocket? = null
    private var socketConnectionHandler: Handler? = null

    private var dic : Array<String>? = null

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
        val parser = JsonParser()
        val message = MessageServer("1",
                GQL_START,
                Payload(variables = variables,
                        operationName = operationName,
                        query = query)
        )
        this.sendMessage(message)
        return Subscription(query, tag)
    }

    fun unsubscribe(subscription: Subscription) {
        this.unsubscribe(subscription.tag)
    }

    fun unsubscribe(tag: String) {
        val message = MessageServer(tag, GQL_STOP, null)
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
        this.sendMessage(MessageServer(null, GQL_CONNECTION_INIT, null))
    }

    fun openConnection() {
        if (clientWebSocket != null) clientWebSocket!!.close()

        clientWebSocket?.connect()

        initNetworkListener()
        startCheckConnection()
        Log.info( "Socket is opening a connection ")
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
        this.sendMessage(MessageServer(null, GQL_CONNECTION_TERMINATE, null ))
    }

    fun sendMessage(message: MessageServer) {
        val response = Gson().toJson(message)
        if (isConnected()){
           sendRaw(response)
        }else{
            dic?.set(0, response)
        }
    }

    private fun sendRaw(response: String){
        clientWebSocket?.sendMessage(response)
    }

    override fun onConnected() {
        Log.info("Websocket is connected")
        openGraphqlConnection()
    }

    override fun onSocketMessage(message: String) {
        val response: MessageClient = Gson().fromJson(message, MessageClient::class.java)
        when (response.type) {
            GQL_CONNECTION_ACK -> {
                Log.info("Graphql is connected")
                view.onConnected()
                if (dic == null){
                    dic?.get(0)?.let { sendRaw(it) }
                }
            }
            GQL_CONNECTION_KEEP_ALIVE -> {
                Log.info("Ping by server.")
            }
            GQL_ERROR -> {
                Log.warning("error ${response.payload?.data.toString()}")
                view.onReceivedMessage(Response.Error(response.payload?.data.toString()))
            }
            GQL_CONNECTION_ERROR -> {
                Log.warning("error ${response.payload?.data.toString()}")
                view.onReceivedMessage(Response.Error(response.payload?.data.toString()))
            }
            GQL_DATA -> {
                Log.info("data successful ${response.payload!!.data}")
                view.onReceivedMessage(Response.Data(response.payload!!.data))
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
        data class Data(val data: Any) : Response()
    }

}