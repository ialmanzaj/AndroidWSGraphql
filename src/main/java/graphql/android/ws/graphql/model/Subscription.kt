package graphql.android.ws.graphql.model

enum class TYPE {
    SUBSCRIBE, UNSUBSCRIBE
}
class OperationType(var subscription: Subscription, var type: TYPE)
data class Subscription(var query: String, var tag: String, var variables: String? = null, var operationName: String? = null)