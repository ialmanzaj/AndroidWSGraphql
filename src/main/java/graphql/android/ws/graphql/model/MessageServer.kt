package graphql.android.ws.graphql.model

import com.google.gson.JsonObject
import java.util.*

data class Error(var message: String)
data class Payload(
        var variables: JsonObject?,
        var extensions: JsonObject?,
        var operationName: String? = null, var query: String, var errors: ArrayList<Error>? = null)
data class MessageServer(var id : String?, var type: String, var payload:  Payload?)