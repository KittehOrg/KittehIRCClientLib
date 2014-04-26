package org.kitteh.irc;

/**
 * Channel mode types.
 */
public enum ChannelModeType {
    /**
     * A list which always has parameters.
     */
    A(true, true),
    /**
     * Always has parameters.
     */
    B(true, true),
    /**
     * Has parameter when setting.
     */
    C(true, false),
    /**
     * Never has parameters.
     */
    D(false, false);

    private final boolean parameterOnRemoval;
    private final boolean parameterOnSetting;

    private ChannelModeType(boolean parameterOnSetting, boolean parameterOnRemoval) {
        this.parameterOnRemoval = parameterOnRemoval;
        this.parameterOnSetting = parameterOnSetting;
    }

    public boolean isParameterOnRemoval() {
        return this.parameterOnRemoval;
    }

    public boolean isParameterOnSetting() {
        return this.parameterOnSetting;
    }
}