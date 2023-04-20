package gg.hcfactions.factions.models.classes;

import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.listeners.events.player.ClassActivateEvent;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IClass {
    ClassManager getManager();

    /**
     * @return Class name
     */
    String getName();

    /**
     * @return Class Description
     */
    String getDescription();

    /**
     * @return Warmup timer duration (in seconds)
     */
    int getWarmup();

    /**
     * @return Helmet material type
     */
    Material getHelmet();

    /**
     * @return Chestplate material type
     */
    Material getChestplate();

    /**
     * @return Leggings material type
     */
    Material getLeggings();

    /**
     * @return Boots material type
     */
    Material getBoots();

    /**
     * @return Set containing Bukkit UUIDs for players actively using this class instance
     */
    Set<UUID> getActivePlayers();

    /**
     * @return Map of potion effects and amplifiers given to members of this class
     */
    Map<PotionEffectType, Integer> getPassiveEffects();

    /**
     * @return Collection of consumable items this player can click to receive additional buffs/debuffs
     */
    List<IConsumeable> getConsumables();

    /**
     * @param material Bukkit Material
     * @return Consumable tied to this material
     */
    default IConsumeable getConsumableByMaterial(Material material) {
        return getConsumables().stream().filter(c -> c.getMaterial().equals(material)).findFirst().orElse(null);
    }

    /**
     * @param player Bukkit Player
     * @param printMessage If true an activation message will print to the player
     */
    default void activate(Player player, boolean printMessage) {
        if (!hasArmorRequirements(player)) {
            player.sendMessage(FMessage.ERROR + FError.C_DOES_NOT_MEET_ARMOR_REQ.getErrorDescription());
            return;
        }

        if (getActivePlayers().contains(player.getUniqueId())) {
            return;
        }

        final ClassActivateEvent activateEvent = new ClassActivateEvent(player, this);
        Bukkit.getPluginManager().callEvent(activateEvent);

        if (activateEvent.isCancelled()) {
            return;
        }

        if (printMessage) {
            FMessage.printClassActivated(player, this);
        }

        getPassiveEffects().forEach((effect, level) -> {
            if (player.hasPotionEffect(effect)) {
                player.removePotionEffect(effect);
            }

            player.addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE, level));
        });

        getActivePlayers().add(player.getUniqueId());
    }

    /**
     * Activate player class
     * @param player Bukkit Player
     */
    default void activate(Player player) {
        activate(player, true);
    }

    /**
     * Deactive player class, removing all passive effects and removing the player from the class
     * @param player Bukkit Player
     */
    default void deactivate(Player player) {
        FMessage.printClassDeactivated(player, this);
        getPassiveEffects().keySet().forEach(player::removePotionEffect);
        getActivePlayers().remove(player.getUniqueId());
    }

    /**
     * @param player Bukkit Player
     * @return If true the player meets the armor requirements to use this class
     */
    default boolean hasArmorRequirements(Player player) {
        if (player.getEquipment() == null) {
            return false;
        }

        if (player.getEquipment().getHelmet() == null || !player.getEquipment().getHelmet().getType().equals(getHelmet())) {
            return false;
        }

        if (player.getEquipment().getChestplate() == null || !player.getEquipment().getChestplate().getType().equals(getChestplate())) {
            return false;
        }

        if (player.getEquipment().getLeggings() == null || !player.getEquipment().getLeggings().getType().equals(getLeggings())) {
            return false;
        }

        if (player.getEquipment().getBoots() == null || !player.getEquipment().getBoots().getType().equals(getBoots())) {
            return false;
        }

        return true;
    }
}
