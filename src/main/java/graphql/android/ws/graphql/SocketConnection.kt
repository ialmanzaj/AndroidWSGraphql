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


class SocketConnection(private val context: Context, private val view: SocketView,  URL: String) : ClientWebSocket.SocketListenerCallback {

    companion object {
        val Log: Logger = Logger.getLogger(SocketConnection::class.java.name)
        const val HEART_BEAT = 60000L
    }

    private var clientWebSocket: ClientWebSocket? = null
    private var socketConnectionHandler: Handler? = null

    init {
        socketConnectionHandler = Handler()
        try {
            clientWebSocket = ClientWebSocket(this, URL)
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
                Payload(parser.parse(variables
                        ?: "{}").asJsonObject, parser.parse("{}").asJsonObject, operationName, query)
        )
        this.sendMessage(message)
        return Subscription(query, tag)
    }

    fun unsubscribe(subscription: Subscription) {
        this.unsubscribe(subscription.tag)
    }

    private fun unsubscribe(tag: String) {
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
        Log.info("clientWebSocket connection is $open")
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

    fun openGraphqlConnection(){
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
            clientWebSocket?.close()
            clientWebSocket = null
        }

        releaseNetworkStateListener()
        stopCheckConnection()
    }

    fun sendMessage(message: MessageServer) {
        clientWebSocket?.sendMessage(Gson().toJson(message))
    }

    override fun onConnected() {
        view.onConnected()
    }

    override fun onSocketMessage(message: String) {
        view.onReceivedMessage(Gson().fromJson<MessageClient>(message, MessageClient::class.java))
    }

    override fun onDisconnected() {
        view.onDisconnected()
    }

    override fun onError(error: Exception) {
        view.onSocketError(error)
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

    interface SocketView {
        fun onConnected()
        fun onReceivedMessage(response: MessageClient)
        fun onDisconnected()
        fun onSocketError(error: Exception)
    }

}