package me.yaimsputnik5.mcping.data;

import com.google.gson.annotations.SerializedName;
import me.yaimsputnik5.mcping.rawData.Players;
import me.yaimsputnik5.mcping.rawData.Version;

class MCResponse {
    @SerializedName("players")
    Players players;
    @SerializedName("version")
    Version version;
    @SerializedName("favicon")
    String favicon;
}
