package io.github.vickychijwani.gimmick.data;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    public String name = "";
    public String giantBombUrl = "";
    public String posterUrl = "";
    public String description = "";
    public List<Platform> platforms = new ArrayList<Platform>();

    public boolean isAdded;

    public SearchResult() {

    }

    public void addPlatform(Platform platform) {
        platforms.add(platform);
    }

    @Override
    public String toString() {
        return name + " (" + giantBombUrl + ")";
    }
}
