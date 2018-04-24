package graphql.android.ws.graphql

import com.google.gson.Gson
import com.google.gson.JsonParser
import graphql.android.ws.graphql.model.OperationMessage
import graphql.android.ws.graphql.model.Payload
import graphql.android.ws.graphql.model.Subscription
import graphql.android.ws.graphql.utils.SingletonHolder
import graphql.android.ws.graphql.websocket.ClientWebsocket
import org.json.JSONObject
import java.util.logging.Logger

class ClientGraphql private constructor(private val view: GraphqlConnectionListener) : ClientWebsocket.ClientWebsocketListener {

    private var mClientWebsocket: ClientWebsocket? = null

    companion object : SingletonHolder<ClientGraphql, GraphqlConnectionListener>(::ClientGraphql){
        private val Log: Logger = Logger.getLogger(ClientGraphql::class.java.name)
    }

    private var queue : MutableList<String> = mutableListOf()

    fun create(url: String){
        mClientWebsocket = ClientWebsocket(this)
        mClientWebsocket?.createConnection(url)
    }

    fun connect() {
        mClientWebsocket?.openConnection()
    }

    fun disconnect() {
        mClientWebsocket?.closeConnection()
    }

    fun subscribe(subscription: Subscription) {
        val parser = JsonParser()
        val message = OperationMessage(
                id = subscription.tag,
                payload = Payload(query = subscription.query,
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

    fun unsubscribeAll(subscriptions: Collection<Subscription>) {
        for (subscription in subscriptions) {
            this.unsubscribe(subscription.tag)
        }
    }

    private fun openGraphqlConnection(){
        this.sendMessage(OperationMessage(id = null, type = GQL_CONNECTION_INIT, payload = null))
    }

    private fun closeGraphqlConnection(){
        this.sendMessage(OperationMessage(id = null, type = GQL_CONNECTION_TERMINATE, payload = null))
    }

    fun sendMessage(message: OperationMessage) {
        val response = Gson().toJson(message)
        if (isConnected()){
            mClientWebsocket?.send(response)
        }else{
            queue.add(response)
        }
    }

    private fun isConnected(): Boolean{
        return mClientWebsocket?.isConnected() ?: false
    }

    override fun messageReceived(message: String?) {
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

    private fun sendRaw(response: String){
        Log.info("sending raw message $response")
        mClientWebsocket?.send(response)
    }

    override fun onConnected() {
        openGraphqlConnection()
    }

    override fun onDisconnected() {
        Log.info("Websocket is disconnected")
        closeGraphqlConnection()
    }

    interface GraphqlConnectionListener {
        fun onConnected()
        fun onReceivedMessage(response: Response)
        fun onDisconnected()
    }

    sealed class Response {
        data class Error(val message: String) : Response()
        data class Data(val data: String) : Response()
    }

}