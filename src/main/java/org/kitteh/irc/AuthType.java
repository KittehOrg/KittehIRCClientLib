package org.kitteh.irc;

public enum AuthType {
    NICKSERV,
    GAMESURGE(false);

    private final boolean nicksOwned;

    private AuthType() {
        this(true);
    }

    private AuthType(boolean nickOwned) {
        this.nicksOwned = nickOwned;
    }

    /**
     * Are nicks owned on this network?
     *
     * @return
     */
    public boolean isNickOwned() {
        return this.nicksOwned;
    }
}