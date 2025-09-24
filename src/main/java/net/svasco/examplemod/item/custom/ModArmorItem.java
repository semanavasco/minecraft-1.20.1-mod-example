package net.svasco.examplemod.item.custom;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.svasco.examplemod.item.ModArmorMaterials;

import java.util.Map;

public class ModArmorItem extends ArmorItem {
    private static final Map<ArmorMaterial, MobEffectInstance> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<ArmorMaterial, MobEffectInstance>())
                    .put(ModArmorMaterials.SAPPHIRE, new MobEffectInstance(MobEffects.NIGHT_VISION, 500,
                            1, false, false, true)).build();

    public ModArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if (!world.isClientSide()) {
            if (hasFullSuitOfArmorOn(player)) {
                evaluateArmorEffects(player);
            } else {
                // Remove any effects previously granted by armor
                removeAllArmorEffects(player);
            }
        }
    }

    private void evaluateArmorEffects(Player player) {
        for (Map.Entry<ArmorMaterial, MobEffectInstance> entry : MATERIAL_TO_EFFECT_MAP.entrySet()) {
            ArmorMaterial mapArmorMaterial = entry.getKey();
            MobEffectInstance mapStatusEffect = entry.getValue();

            if (hasCorrectArmorOn(mapArmorMaterial, player)) {
                addOrRefreshInfiniteEffect(player, mapStatusEffect);
            } else {
                // Ensure the effect is removed
                removeEffect(player, mapStatusEffect);
            }
        }
    }

    private void addOrRefreshInfiniteEffect(Player player, MobEffectInstance template) {
        MobEffectInstance current = player.getEffect(template.getEffect());
        // If the effect is missing or has a short duration, replace it with max duration
        if (current == null || current.getDuration() < 600 || current.getAmplifier() != template.getAmplifier()) {
            MobEffectInstance infinite = new MobEffectInstance(
                    template.getEffect(),
                    Integer.MAX_VALUE,
                    template.getAmplifier(),
                    template.isAmbient(),
                    template.isVisible(),
                    template.showIcon()
            );
            player.addEffect(infinite);
        }
    }

    private void removeEffect(Player player, MobEffectInstance template) {
        if (player.hasEffect(template.getEffect())) {
            player.removeEffect(template.getEffect());
        }
    }

    private void removeAllArmorEffects(Player player) {
        for (MobEffectInstance effect : MATERIAL_TO_EFFECT_MAP.values()) {
            removeEffect(player, effect);
        }
    }

    private boolean hasFullSuitOfArmorOn(Player player) {
        ItemStack boots = player.getInventory().getArmor(0);
        ItemStack leggings = player.getInventory().getArmor(1);
        ItemStack breastplate = player.getInventory().getArmor(2);
        ItemStack helmet = player.getInventory().getArmor(3);

        return !helmet.isEmpty() && !breastplate.isEmpty()
                && !leggings.isEmpty() && !boots.isEmpty();
    }

    private boolean hasCorrectArmorOn(ArmorMaterial material, Player player) {
        for (ItemStack armorStack : player.getInventory().armor) {
            if (!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }

        ArmorItem boots = ((ArmorItem) player.getInventory().getArmor(0).getItem());
        ArmorItem leggings = ((ArmorItem) player.getInventory().getArmor(1).getItem());
        ArmorItem breastplate = ((ArmorItem) player.getInventory().getArmor(2).getItem());
        ArmorItem helmet = ((ArmorItem) player.getInventory().getArmor(3).getItem());

        return helmet.getMaterial() == material && breastplate.getMaterial() == material &&
                leggings.getMaterial() == material && boots.getMaterial() == material;
    }
}
