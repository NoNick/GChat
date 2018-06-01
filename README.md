**Description**

Once upon a time a general decided to make his very own secure chat by the name of **GChat** (General’s Chat) for communications between his subordinates. Here are his requirements:

- Every soldier and officer supposed to be given shared secret key for authorization, one secret key per each rank (i.e. one key for all soldiers, one for all sergeants etc.). Using this key one can open websocket session and pick a nickname. Multiple connections with the same name are not allowed. List of ranks: soldier < sergeant < sublieutenant < lieutenant < The General.

- Chat is divided into rooms. When a user sends a room subscription package, the room should be created (if it wasn’t). Once subscribed the user will receive new messages sent in this room according to his rank, but won’t receive history (only messages created after this subscription, judging by the time of DB inserts or Hibernate timestamps). The unsubscription is as impossible as western spies breaching the Chat’s security (required to be impossible). After subscription a public message about this action should be sent to all room subscribers (including the new one).

- Any user can send two types of messages into a specified room: public and secret. The former is visible to everyone, while the latter should be received only by users with the same or higher rank. When a user sends message in a room, he subscribes to it (if wasn’t) and receives his message.

- The General wants to keep track of who receives messages in current session (i.e. since server was started). Some POST-method should return list of messages with their ids and list of receiver’s names. The receiver of a message is the user who joined a room before the message was sent, and has high enough rank to see the message (or has any rank if the message is public).

- Names of rooms should be returned together with total number of messages in them. This statistics should be available only for the General.


Since “developers” were chosen among soldiers with little to nothing background, the General soon found out that the letter G in the product stands not for “General’s”. After counseling with his advisers he decided current “developers” would be better off shoveling dirt.
They haven’t finished the project, leaving lines of undocumented code with poor tests. You should debug, refactor and optimize those lines. Application’s behavior may differ from described above, that should be fixed as well. Protocol for ‘report’, ‘subscribe’, ‘message’ WS-packages and /rooms, /pleaseGeneral POST-methods should be kept as is (although implementation may differ), the rest is up to you.

Unfortunately, the General is not tech savvy enough, so he didn’t understand adviser’s rant, but here’s some keywords he remembered: spring-security, getting rid of God-classes (these words were mentioned quite often), loose coupling (these too), Jackson, HQL, java.util.concurrency (same thing), logging, exception handling.

**Motherland counts on you, comrade!**

**Steps to run the application:**
- Setup tomcat
- Build application via maven (clean compile package, optionally skip tests).
- Deploy the .war archive
- Open developer console in Chrome (Ctrl+Shift+C). Use a tab with insecure connection (new empty tab won’t suit).
- Initialize websocket connection like this (“Console” tab): `var socket = new WebSocket("ws://localhost:8080/template/WebSocket");`
- Send packages (“Console” tab) like this: `socket.send('{"action":"subscribe", "name": "Simon", "hash": "E+pl1T31nObs76mdbZORgQ==", "room": "room0"}');`
- Responses from the server are available as websocket frames in “Network” tab of the developer console.

**Example of communication:**

`<Tab 1>: var socket = new WebSocket("ws://localhost:8080/template/WebSocket");`

`<Tab 1>: socket.send('{"action":"subscribe", "name": "Simon", "hash": "E+pl1T31nObs76mdbZORgQ==", "room": "room0"}');`

`<Tab 2>: var socket = new WebSocket("ws://localhost:8080/template/WebSocket");`

`<Tab 2>: socket.send('{"action":"report", "name": "Peter", "hash": "iHDxWFurtv+PN6akU31KqQ==", "secret": false, "message": "text", "room": "room0"}');`
