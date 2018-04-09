# AndroidWSGraphql

## Overview
- [x] Connect to a GraphQL WebSocket server
- [x] Send messages
- [x] Subscribe / unsubscribe
- [x] Close connection
- [x] Data handling 
- [x] Error handling
- [x] Reconnect option
- [x] JSON raw response
- [x] Queue of unsent messages
- [x] Implement all Apollo protocol
- [x] Specify entry protocol

We also use [Websocket](https://github.com/TakahikoKawasaki/nv-websocket-client) thanks to them


## Communication

- Use issue if you have any problem
- Don't hesitate to contribute to the project with a pull request

## Installation
Maven
-----

```xml
<dependency>
	    <groupId>com.github.ialmanzaj</groupId>
	    <artifactId>AndroidWSGraphql</artifactId>
	    <version>-SNAPSHOT</version>
</dependency>
```
  
Gradle
------

```Gradle
dependencies {
     implementation 'com.github.ialmanzaj:AndroidWSGraphql:-SNAPSHOT'
}
```

Description
-----------

#### Create WebSocketFactory


```kotlin
// Create a socket-connection instance.
   val socketConnection = SocketConnection(this, this, "ws://localhost:7003/feedback")
   socketConnection.openConnection()
```

### Subscribe / Unsubscribe

Just call subscribe method, set an tag and your subscription query as well.

```   
   socketConnection.subscribe(Subscription("subscription {feedbackAdded {id, text}}", "feed"))
```

### Received Message

Below implement the method:
```
override fun onReceivedMessage(response: SocketConnection.Response) {
      when (response){
          is SocketConnection.Response.Data -> {
              Log.info("data ${response.data}")
          }
          is SocketConnection.Response.Error -> {
              Log.warning("error ${response.message}")
          }
      }
    }
```
