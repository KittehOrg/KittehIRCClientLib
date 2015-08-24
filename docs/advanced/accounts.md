It is possible to use KICL to track when a user is logged into a server account.

The method `User#getAcccount()` provides the account if known.

### Requirements

There are a few requirements for this feature to play nice:

1. The server must support the accounts-notify capability.
    * This allows KICL to find out when a user sharing a channel with the client changes account status.
    * Query this with `CapabilityManager#getCapabilities()`.
2. The server must support the extended-join capability.
    * This allows KICL to find out a user's account when they join a channel.
    * Query this with `CapabilityManager#getCapabilities()`.
3. The server must support WHOX.
    * This allows KICL to get accounts of all users in a channel upon join.
    * Query this with `ServerInfo#hasWhoXSupport()`.