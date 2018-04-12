package graphql.android.ws.graphql.model

import org.json.JSONObject


enum class SocketOperation {
    CONNECT, DISCONNECT
}

data class OperationMessage(
        var id: String?,
        var type: String?,
        var payload: Payload?
)

data class Payload(
        var query: String?,
        var variables: JSONObject?,
        var operationName: String?
)

data class PayloadServer(var data: Any)

data class OperationMessageServer(
        var id: String?,
        var type: String?,
        var payload: PayloadServer?
)
