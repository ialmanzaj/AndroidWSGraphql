package graphql.android.ws.graphql.websocket

enum class WebSocketClosureStatusCode constructor(private val code: Int) {
    /*
     * The missing codes are not relevant for client connection closure
     * or are reserved for future use.
     * See: http://tools.ietf.org/html/rfc6455#section-7.4.1
     */

    NORMAL(1000),
    ENDPOINT_GOING_DOWN(1001),
    PROTOCOL_ERROR(1002),
    RECEIVED_DATA_NOT_ACCEPTABLE(1003),
    RECEIVED_DATA_NOT_CONSISTENT_WITH_TYPE_OF_MESSAGE(1007),
    POLICY_VIOLATED(1008),
    MESSAGE_TOO_LARGE(1009),
    EXTENSION_NEGOTIATION_FAILED(1010);

    fun code(): Int {
        return code
    }

    override fun toString(): String {
        return code.toString()
    }
}