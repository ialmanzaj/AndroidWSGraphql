package graphql.android.ws.graphql.model

data class PayloadClient(val data: Any)
data class MessageClient(var id:String?,var type: String?, var payload: PayloadClient?)