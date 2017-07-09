Below are some common problems encountered by users of KICL, and solutions to those problems.

#### A KittehServerMessageException printed to console

If KICL cannot parse a message type that it is written to handle, it will create a detailed
exception for this problem. This sort of exception does not halt processing of further messages,
and does not cause KICL to disconnect from the server or otherwise die. By default, KICL outputs
its various exceptions to console. To change this behavior, utilize
`Client.Builder#listenException(Consumer<Exception>)` during the build phase or
`Client#setExceptionListener(Consumer<Exception> listener)` later on. 

#### Exceptions connecting to Twitch

Twitch will always cause at least one `KittehServerMessageException` because it does not follow
spec on the welcome message. Check out the `TwitchListener` to make that and other problems go away.

#### More here

Some day.