package graphql.android.ws.graphql.model

data class Error(var message: String)

data class MessageServer(var id : String?, var type: String, var payload:  Payload?)