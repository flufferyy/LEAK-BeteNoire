package me.yaimsputnik5.mcping.data;

import com.google.gson.annotations.SerializedName;
import me.yaimsputnik5.mcping.rawData.ForgeDescriptionTranslate;
import me.yaimsputnik5.mcping.rawData.ForgeModInfo;
import me.yaimsputnik5.mcping.rawData.Players;
import me.yaimsputnik5.mcping.rawData.Version;

public class ForgeResponseTranslate {

    @SerializedName("description")
    private ForgeDescriptionTranslate description;

    @SerializedName("players")
    private Players players;

    @SerializedName("version")
    private Version version;

    @SerializedName("modinfo")
    private ForgeModInfo modinfo;

    public FinalResponse toFinalResponse(){
        return new FinalResponse(players,version,"",description.getTranslate());
    }

    public ForgeDescriptionTranslate getDescription() {
        return description;
    }

    public Players getPlayers() {
        return players;
    }

    public Version getVersion() {
        return version;
    }

    public ForgeModInfo getModinfo() {
        return modinfo;
    }
}
