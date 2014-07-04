package org.kitteh.irc.components;

import java.util.HashMap;
import java.util.Map;

/**
 * Commands used in client/server communication
 */
public enum Command {
    INVITE,
    JOIN,
    KICK,
    MODE,
    NICK,
    NOTICE,
    PART,
    PRIVMSG,
    QUIT;

    private static final Map<String, Command> nameMap = new HashMap<>();

    static {
        for (Command command : values()) {
            nameMap.put(command.name(), command);
        }
    }

    public static Command getByName(String name) {
        return nameMap.get(name.toUpperCase());
    }

    @Override
    public String toString() {
        return this.name(); // Explicitly overriding as a reminder that this is used as such
    }
}