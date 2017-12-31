This page documents IRCv3 features. With two exceptions noted, all of the IRCv3 3.1 and 3.2
specifications are implemented. Draft/WIP specs have some support (STS, SNI).

See the `CapabilityManager` and `CapabilityRequestCommand` for more details.

### Capabilities supported

* CAP
  * The client starts the connection with `CAP LS 302` and will request a modifiable selection of capabilities.
  * It supports CAP spec 3.2, including the NEW and DEL commands.
* multi-prefix
  * Prefixes sorted by importance according to ISUPPORT info.
* sasl
  * This is requested if the below mechanism classes are used and given to the AuthManager.
      * PLAIN - `SaslPlain`.
      * [ECDSA-NIST256P-CHALLENGE](advanced/ecdsa.md) - `SaslECDSANIST256PChallenge`.
      * EXTERNAL - `SaslExternal`.
* account-notify
  * Tracked for `User#getAccount()`.
  * `UserAccountStatusEvent`.
* away-notify
  * Boolean `User#isAway()`.
  * `User#getAwayMessage()`.
  * `UserAwayMessageEvent`.
* extended-join
  * Information tracked automagically.
* Message tags
  * Getters in `ServerMessage`.
  * Custom class registration in `MessageTagManager`.
* Monitor
  * `MonitorCommand` to get the party started.
  * `MonitoredNickOnlineEvent`, `MonitoredNickOfflineEvent` to track.
  * `MonitoredNickListEvent`, `MonitoredNickListFullEvent`.
* account-tag
* cap-notify
  * Not actually requested because it's implicitly enabled with `CAP LS 302`.
* chghost
  * `UserHostnameChangeEvent` and `UserUserStringChangeEvent`.
  * Common shared `UserInfoChangeEvent` includes nick changes too.
* echo-message
  * Not requested by default.
  * `@EchoMessage` filter acquires only messages sent by self.
* invite-notify
  * Not automatically requested.
  * Picked up by `ChannelInviteEvent`.
* server-time
* userhost-in-names
* STS
  * Draft spec supported.
  * `STSPropertiesStorageManager` as default implementation.
* SNI
  * Draft spec supported.
* [Twitch capabilities optionally supported](advanced/twitch.md)
* WEBIRC
  * Supported in the client builder.
  * For more information see [here](advanced/webirc.md).


### Not yet supported

* batch
* message-tags draft
* client-only tags
  * reply
  * react
* labeled-message draft
* message-ids draft

### No plans to support

* Metadata
  * The metadata 3.2 spec was deprecated.
  * KICL will consider supporting a new spec if one is proposed.
* TLS
  * This is a STARTTLS approach, which is silly when the default KICL strategy is SSL.
  * Use the [SSL](advanced/ssl.md) and [STS](advanced/sts.md) support that KICL has built-in instead.
