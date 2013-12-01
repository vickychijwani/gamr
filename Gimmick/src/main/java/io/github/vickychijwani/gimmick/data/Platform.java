package io.github.vickychijwani.gimmick.data;

import java.util.Arrays;
import java.util.HashSet;

public enum Platform {

    PC(new String[] { "PC" }),
    PS3(new String[] { "PlayStation 3", "PlayStation Network (PS3)" }),
    XBOX_360(new String[] { "Xbox 360", "Xbox 360 Games Store" }),
    PS4(new String[] { "PlayStation 4", "PS4", "Orbis" }),
    XBOX_ONE(new String[] { "Xbox One", "Xbox Durango", "XONE", "XBONE", "Xbox 1" });

    private HashSet<String> mAliases;

    Platform(String[] aliases) {
        mAliases = new HashSet<String>(Arrays.asList(aliases));
    }

    public static Platform fromString(String name) throws IllegalArgumentException {
        if (name == null)
            throw new IllegalArgumentException("Platform name cannot be null");

        for (Platform p : Platform.values())
            if (p.mAliases.contains(name))
                return p;

        throw new IllegalArgumentException("No platform '" + name + "' found");
    }

}
