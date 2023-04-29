package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.MonsterEventSession;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.bukkit.entity.Player;

public interface IMonsterEvent {
    BLocatable getCaptureChestLocation();
    MonsterEventSession getSession();

    void setCaptureChestLocation(BLocatable location);
    void setSession(MonsterEventSession session);

    void captureEvent(Player player);
    void startEvent();
    void startEvent(int uptime);
    void stopEvent();
}
