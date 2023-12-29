This page documents IRCv3 features. With two exceptions noted, all of the IRCv3 3.1 and 3.2
specifications are implemented along with more recent specifications. Draft/WIP specs have some support (SNI).

See the `CapabilityManager` and `CapabilityRequestCommand` for more details.

### Capabilities supported

* CAP
    * The client starts the connection with `CAP LS 302` and will request a modifiable selection of capabilities.
    * It supports CAP spec 3.2, including the NEW and DEL commands.
* Standard Replies (FAIL, NOTE, WARN) and ACK.
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
* batch
* Bot mode
* extended-join
    * Information tracked automagically.
* extended-monitor
* Message tags
    * Getters in `ServerMessage`.
    * Custom class registration in `MessageTagManager`.
    * Message ID (`msgid`).
    * Labeled response (`label` and ACK messages)
    * Client-only tags.
      * `talking`.
    * Tag-only (TAGMSG) messages.
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
    * Supported as either 'sts' or 'draft/sts' capability.
    * `STSPropertiesStorageManager` as default implementation.
* SNI
    * Draft spec supported.
* [Twitch capabilities optionally supported](advanced/twitch.md)
* WEBIRC
    * Supported in the client builder.
    * For more information see [here](advanced/webirc.md).


### No plans to support

* Metadata
    * The metadata 3.2 spec was deprecated.
    * KICL will consider supporting a new spec if one is proposed.
* STARTTLS (`TLS` capability)
    * This is an [officially deprecated](https://github.com/ircv3/ircv3.github.io/pull/211) STARTTLS approach, which is silly when the default KICL strategy is TLS.
    * Use the [TLS](advanced/tls.md) and [STS](advanced/sts.md) support that KICL has built-in instead.
