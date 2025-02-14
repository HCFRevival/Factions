package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.shop.IMerchantVillager;

import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftLivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.UUID;

@Getter
public final class MerchantVillager extends Villager implements IMerchantVillager {
    public final Factions plugin;
    public final UUID merchantId;
    public final PLocatable position;

    public MerchantVillager(Factions plugin, GenericMerchant<?> merchant) {
        super(EntityType.VILLAGER, ((CraftWorld)merchant.getMerchantLocation().getBukkitLocation().getWorld()).getHandle(), VillagerType.SAVANNA);
        this.plugin = plugin;
        this.merchantId = merchant.getId();
        this.position = merchant.getMerchantLocation();

        final CraftLivingEntity livingEntity = (CraftLivingEntity) getBukkitEntity();
        livingEntity.setCustomName(LegacyComponentSerializer.legacySection().serialize(merchant.getMerchantName()));
        livingEntity.setCustomNameVisible(true);
        livingEntity.teleport(merchant.getMerchantLocation().getBukkitLocation());

        goalSelector.addGoal(6, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0f));
    }

    @Override
    public void spawn() {
        level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public void setUnhappy() {
        this.setUnhappyCounter(0);
    }

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
    public boolean hurt(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean alwaysAccepts() {
        return true;
    }

    @Override
    public InteractionResult mobInteract(Player entityhuman, InteractionHand enumhand) {
        return InteractionResult.FAIL;
    }

    @Override
    public int getPlayerReputation(Player entityhuman) {
        return 0;
    }
}
