package gg.hcfactions.factions.models.logger.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.logger.ICombatLogger;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CombatLogger extends Villager implements ICombatLogger {
    @Getter public UUID ownerId;
    @Getter public String ownerUsername;
    @Getter public List<ItemStack> loggerInventory;
    @Getter public int banDuration;

    public CombatLogger(Location location) {
        super(EntityType.VILLAGER, ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), VillagerType.SNOW);
    }

    public CombatLogger(Location location, Player player, int banDuration) {
        super(EntityType.VILLAGER, ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), VillagerType.SNOW);
        this.ownerId = player.getUniqueId();
        this.ownerUsername = player.getName();
        this.loggerInventory = Lists.newArrayList();
        this.banDuration = banDuration;

        // look at player
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0f));

        // set player attrs
        final CraftLivingEntity livingEntity = (CraftLivingEntity) getBukkitEntity();
        livingEntity.setCustomName(ChatColor.RED + "(Combat-Logger) " + ChatColor.RESET + player.getName());
        livingEntity.setCustomNameVisible(true);
        livingEntity.teleport(location);
        livingEntity.setHealth(player.getHealth());
        livingEntity.setFallDistance(player.getFallDistance());
        livingEntity.setFireTicks(player.getFireTicks());
        livingEntity.setRemainingAir(player.getRemainingAir());
        player.getActivePotionEffects().stream().filter(eff -> !eff.getType().equals(PotionEffectType.INVISIBILITY) && !eff.isInfinite()).forEach(livingEntity::addPotionEffect);

        if (livingEntity.getEquipment() != null && player.getEquipment() != null) {
            livingEntity.getEquipment().setHelmet(player.getEquipment().getHelmet());
            livingEntity.getEquipment().setChestplate(player.getEquipment().getChestplate());
            livingEntity.getEquipment().setLeggings(player.getEquipment().getLeggings());
            livingEntity.getEquipment().setBoots(player.getEquipment().getBoots());
        }

        for (ItemStack i : player.getInventory().getContents()) {
            if (i == null) {
                continue;
            }

            loggerInventory.add(i);
        }
    }

    @Override
    public void spawn() {
        level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public void reapply(Player player) {
        final CraftLivingEntity livingEntity = (CraftLivingEntity) getBukkitEntity();

        player.setHealth(livingEntity.getHealth());
        player.teleport(livingEntity.getLocation());
        player.setFallDistance(livingEntity.getFallDistance());
        player.setFireTicks(livingEntity.getFireTicks());
        player.setRemainingAir(livingEntity.getRemainingAir());
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        livingEntity.getActivePotionEffects().forEach(player::addPotionEffect);

        if (livingEntity.getEquipment() != null && player.getEquipment() != null) {
            player.getEquipment().setHelmet(livingEntity.getEquipment().getHelmet());
            player.getEquipment().setChestplate(livingEntity.getEquipment().getChestplate());
            player.getEquipment().setLeggings(livingEntity.getEquipment().getLeggings());
            player.getEquipment().setBoots(livingEntity.getEquipment().getBoots());
        }

        livingEntity.remove();
    }

    /**
     * Override unhappy values
     */
    @Override
    public void setUnhappy() {}

    /**
     * Remove breeding capability
     */
    @Override
    public boolean canBreed() {
        return false;
    }

    /**
     * Overrides tick func to remove unhappy counter and gossip value decay
     */
    @Override
    public void tick() {
        super.tick();
    }

    /**
     * Overrides interactions with players
     */
    @Override
    public InteractionResult mobInteract(net.minecraft.world.entity.player.Player entityhuman, InteractionHand enumhand) {
        return InteractionResult.FAIL;
    }

    /**
     * Prevents despawning
     */
    @Override
    public boolean removeWhenFarAway(double d0) {
        return false;
    }

    /**
     * Prevents picking up items
     */
    @Override
    protected void pickUpItem(ItemEntity entityitem) {}

    /**
     * Prevents fucking up player rep with other nearby villagers
     */
    @Override
    public void gossip(ServerLevel worldserver, Villager entityvillager, long i) {}

    /**
     * Prevents golem spawn attempts
     */
    @Override
    public boolean wantsToSpawnGolem(long i) {
        return false;
    }

    /**
     * Prevents movement XZ
     */
    @Override
    public void move(MoverType enummovetype, Vec3 vec3d) {
        super.move(enummovetype, new Vec3(0.0, Math.min(vec3d.y, 0.0), 0.0));
    }

    /**
     * Prevents velocity being applied
     */
    @Override
    public void knockback(double d0, double d1, double d2) {
        super.knockback(0.0, 0.0, 0.0);
    }

    /**
     * Prevents collisions
     */
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
