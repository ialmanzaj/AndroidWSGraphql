package graphql.android.ws.graphql


const val HEART_BEAT = 60000L
const val WEBSOCKET_TIMEOUT: Int = 5000

const val GRAPHQL_WS = "graphql-ws"

const val GQL_STOP = "stop"
const val GQL_START = "start"
const val GQL_CONNECTION_INIT = "connection_init"
const val GQL_CONNECTION_ACK = "connection_ack"
const val GQL_CONNECTION_ERROR = "connection_error"
const val GQL_CONNECTION_KEEP_ALIVE = "keepalive"
const val GQL_CONNECTION_TERMINATE = "connection_terminate"

const val GQL_DATA = "data"
const val GQL_COMPLETE = "complete"
const val GQL_ERROR = "error"
