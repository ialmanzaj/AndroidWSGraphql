package graphql.android.ws.graphql

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

class NetworkStateReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = NetworkStateReceiver::class.java.simpleName

        const val ACTION_NETWORK_STATE_CHANGED = "networkStateChanged"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Network connectivity change")

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val networkIsOn = activeNetworkInfo != null && activeNetworkInfo.isConnected

        val broadcastIntent = Intent(ACTION_NETWORK_STATE_CHANGED)
        broadcastIntent.putExtra(ACTION_NETWORK_STATE_CHANGED, networkIsOn)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }

}