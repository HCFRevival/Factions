package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.shop.IMerchantVillager;

import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.UUID;

public final class MerchantVillager extends Villager implements IMerchantVillager {
    @Getter public final Factions plugin;
    @Getter public final UUID merchantId;
    @Getter public final BLocatable position;

    public MerchantVillager(Factions plugin, GenericMerchant merchant) {
        super(EntityType.VILLAGER, ((CraftWorld)merchant.getMerchantLocation().getBukkitBlock().getWorld()).getHandle(), VillagerType.SWAMP);
        this.plugin = plugin;
        this.merchantId = merchant.getId();
        this.position = merchant.getMerchantLocation();

        final CraftLivingEntity livingEntity = (CraftLivingEntity) getBukkitEntity();
        livingEntity.setCustomName(merchant.getMerchantName());
        livingEntity.setCustomNameVisible(true);
        livingEntity.teleport(merchant.getMerchantLocation().getBukkitBlock().getLocation());

        goalSelector.addGoal(6, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0f));
    }

    @Override
    public void spawn() {
        level.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public void setUnhappy() {}

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean removeWhenFarAway(double d0) {
        return false;
    }

    @Override
    protected void pickUpItem(ItemEntity entityitem) {}

    @Override
    public void gossip(ServerLevel worldserver, Villager entityvillager, long i) {}

    @Override
    public boolean wantsToSpawnGolem(long i) {
        return false;
    }

    @Override
    public void move(MoverType enummovetype, Vec3 vec3d) {
        super.move(enummovetype, new Vec3(0.0, 0.0, 0.0));
    }

    @Override
    public void knockback(double d0, double d1, double d2) {
        super.knockback(0.0, 0.0, 0.0);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean alwaysAccepts() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player entityhuman, InteractionHand enumhand) {
        plugin.getShopManager().getMerchantById(merchantId).ifPresent(merchant ->
                plugin.getShopManager().getExecutor().openMerchant(Bukkit.getPlayer(entityhuman.getUUID()), (GenericMerchant) merchant));

        return InteractionResult.FAIL;
    }
}
