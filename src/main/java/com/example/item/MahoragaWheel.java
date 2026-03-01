package com.example.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MahoragaWheel extends Item {
    // Храним данные об адаптации для каждого игрока
    private static final Map<UUID, String> lastDamageType = new HashMap<>();
    private static final Map<UUID, Integer> adaptationLevel = new HashMap<>();

    public MahoragaWheel(Settings settings) {
        super(settings.maxDamage(500)); // Прочность 500
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof PlayerEntity player && !world.isClient) {
            // Проверка: предмет должен быть в инвентаре для работы
            if (adaptationLevel.getOrDefault(player.getUuid(), 0) > 0) {
                spawnParticles(world, player);
            }
        }
    }

    public static void handleDamage(PlayerEntity player, DamageSource source, ItemStack wheelStack) {
        String type = source.getName();
        UUID uuid = player.getUuid();

        if (type.equals(lastDamageType.get(uuid))) {
            int level = adaptationLevel.getOrDefault(uuid, 0);
            if (level < 8) {
                adaptationLevel.put(uuid, level + 1);
                // Звуковой эффект адаптации
                player.getWorld().playSound(null, player.getBlockPos(), 
                    SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.PLAYERS, 1.0f, 0.5f);
            }
        } else {
            lastDamageType.put(uuid, type);
            adaptationLevel.put(uuid, 1);
        }

        // Применяем эффекты
        applyEffects(player, adaptationLevel.get(uuid));
        
        // Тратим прочность
        wheelStack.damage(1, player, (p) -> p.sendToolBreakStatus(player.getActiveHand()));
    }

    private static void applyEffects(PlayerEntity player, int level) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, Math.min(level / 3, 2)));
        if (level >= 8) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 0));
        }
    }

    private static void spawnParticles(World world, PlayerEntity player) {
        if (world.getTime() % 5 == 0) {
            // Фиолетовые частицы (портал имитирует проклятую энергию)
            world.addParticle(ParticleTypes.PORTAL, 
                player.getX(), player.getY() + 2.2, player.getZ(), 0, 0.1, 0);
        }
    }
}
