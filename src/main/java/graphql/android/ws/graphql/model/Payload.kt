package graphql.android.ws.graphql.model

import com.google.gson.JsonObject
import java.util.*

data class Payload(
        var variables: JsonObject,
        var extensions: JsonObject,
        var operationName: String? = null,
        var query: String,
        var errors: ArrayList<Error>? = null
)