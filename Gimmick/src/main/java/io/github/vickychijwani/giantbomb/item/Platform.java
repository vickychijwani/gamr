package io.github.vickychijwani.giantbomb.item;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

// TODO make this data dynamic!
public enum Platform {

    PC(3, "PC", "PC"),
    PS3(1, "PS3", "PlayStation 3", "PlayStation Network (PS3)"),
    XBOX_360(2, "X360", "Xbox 360", "Xbox 360 Games Store"),
    PS4(72496, "PS4", "PlayStation 4", "PS4", "Orbis"),
    XBOX_ONE(80000, "X1", "Xbox One", "Xbox Durango", "XONE", "XBONE", "Xbox 1");

    private final int mMetacriticId;
    private final String mShortName;
    private final HashSet<String> mAliases;

    Platform(int metacriticId, @NotNull String shortName, String... aliases) {
        mMetacriticId = metacriticId;
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

    public int getMetacriticId() {
        return mMetacriticId;
    }

    public String getShortName() {
        return mShortName;
    }

    @Override
    public String toString() {
        return mShortName;
    }

}
