package graphql.android.ws.graphql

import android.os.Handler
import com.google.gson.Gson
import com.google.gson.JsonParser
import graphql.android.ws.graphql.model.OperationMessage
import graphql.android.ws.graphql.model.Payload
import graphql.android.ws.graphql.model.Subscription
import graphql.android.ws.graphql.utils.SingletonHolder
import org.json.JSONObject
import java.util.logging.Logger

class GraphqlSocketClient private constructor(private val view: GraphqlWebSocketListener, private val URL: String) : ClientWebSocket.SocketListenerCallback {

    companion object  : SingletonHolder<GraphqlSocketClient, GraphqlWebSocketListener, String>(::GraphqlSocketClient){
        private val Log: Logger = Logger.getLogger(GraphqlSocketClient::class.java.name)
    }

    private var clientWebSocket: ClientWebSocket? = null
    private var socketConnectionHandler: Handler? = null

    private var queue : MutableList<String> = mutableListOf()

    init {
        socketConnectionHandler = Handler()

        try {
            clientWebSocket = ClientWebSocket(this)

        } catch (e: Exception) {
            Log.info( "Socket exception $e")
        }
    }

    fun subscribe(subscription: Subscription) {
        val parser = JsonParser()
        val message = OperationMessage(
                id = subscription.tag,
                payload = Payload(
                        query = subscription.query,
                        variables = parser.parse(subscription.variables ?: "{}").asJsonObject,
                        operationName = subscription.operationName
                ),
                type = GQL_START
        )
        this.sendMessage(message)
    }

    fun unsubscribe(tag: String) {
        val message = OperationMessage(id = tag, type = GQL_STOP,  payload = null)
        this.sendMessage(message)
    }

    private val checkConnectionRunnable = {
       /* val open = clientWebSocket?.getConnection()?.isOpen ?: false
        if (!open) {
            openConnection()
        }
        startCheckConnection()*/
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

    @Throws
    fun openConnection() {
        if (clientWebSocket == null){
            return
        }

        if (!isConnected()){
            Log.info( "Socket is opening a connection $clientWebSocket")
            clientWebSocket?.connect(URL)
        }else{
            clientWebSocket?.reconnect()
        }

        //initNetworkListener()
        startCheckConnection()
    }

    fun closeConnection() {
        if(clientWebSocket != null) {
            closeGraphqlConnection()
            clientWebSocket?.close()
            clientWebSocket = null
        }

        //releaseNetworkStateListener()
        stopCheckConnection()
    }

    fun closeGraphqlConnection(){
        this.sendMessage(OperationMessage(id = null, type = GQL_CONNECTION_TERMINATE, payload = null))
    }

    fun sendMessage(message: OperationMessage) {
        val response = Gson().toJson(message)
        if (isConnected()){
           sendRaw(response)
        }else{
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
                    Log.info("sending...queue-message")
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

    fun isConnected(): Boolean {
        return clientWebSocket?.getConnection() != null
    }

    interface GraphqlWebSocketListener {
        fun onConnected()
        fun onReceivedMessage(response: Response)
        fun onDisconnected()
    }

    sealed class Response {
        data class Error(val error: String) : Response()
        data class Data(val data: String) : Response()
    }

}