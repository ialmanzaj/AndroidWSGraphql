package graphql.android.ws.graphql.model

import java.util.*

data class Payload(
        var variables: String? = "{}",
        var extensions: String? = "{}",
        var operationName: String? = null,
        var query: String,
        var errors: ArrayList<Error>? = null
)