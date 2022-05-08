package me.yaimsputnik5.mcping.data;

import me.yaimsputnik5.mcping.rawData.Players;
import me.yaimsputnik5.mcping.rawData.Version;

public class FinalResponse extends MCResponse {

    private final String description;

    public FinalResponse(Players players, Version version,String favicon,String description){
        this.description = Input.stripMinecraft(description);
        this.favicon = favicon;
        this.players = players;
        this.version = version;
    }

    public Players getPlayers() {
        if (players == null) {
            return new Players();
        }
        return players;
    }

    public Version getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getFavIcon () { return favicon; }
}
