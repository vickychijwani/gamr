package io.github.vickychijwani.gimmick.item;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

// TODO make this data dynamic!
public enum Platform {

    PC("PC", "PC"),
    PS3("PS3", "PlayStation 3", "PlayStation Network (PS3)"),
    XBOX_360("X360", "Xbox 360", "Xbox 360 Games Store"),
    PS4("PS4", "PlayStation 4", "PS4", "Orbis"),
    XBOX_ONE("X1", "Xbox One", "Xbox Durango", "XONE", "XBONE", "Xbox 1");

    private String mShortName;
    private HashSet<String> mAliases;

    Platform(@NotNull String shortName, String... aliases) {
        mShortName = shortName;
        mAliases = new HashSet<String>(Arrays.asList(aliases));
    }

    public static Platform fromName(@NotNull String name) {
        for (Platform p : Platform.values())
            if (p.mShortName.equals(name) || p.mAliases.contains(name))
                return p;

        throw new IllegalArgumentException("No platform '" + name + "' found");
    }

    public static TreeSet<Platform> fromCsv(@NotNull String platformsCsv) {
        String[] platformsStr = platformsCsv.split(",");
        TreeSet<Platform> platformSet = new TreeSet<Platform>();
        for (String platformStr : platformsStr) {
            platformSet.add(Platform.fromName(platformStr));
        }
        return platformSet;
    }

    public String getShortName() {
        return mShortName;
    }

    @Override
    public String toString() { return mShortName; }

}
