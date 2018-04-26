package graphql.android.ws.graphql

import android.test.AndroidTestCase
import android.util.Log
import graphql.android.ws.graphql.model.Subscription
import junit.framework.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest : AndroidTestCase(){

    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun socketResponse(){
        val context = context
        Assert.assertNotNull(context)

        val socketConnection  = GraphqlSocketClient(object : GraphqlSocketClient.GraphqlWebSocketListener {
            override fun onConnected() {

            }
            override fun onReceivedMessage(response: GraphqlSocketClient.Response) {
                Log.d("ExampleUnitTest", "reponse $response")
            }
            override fun onDisconnected() {

            }
        }, "192.168.0.101:3030/v1/graphql")


        socketConnection.subscribe(Subscription("", ""))
    }
}