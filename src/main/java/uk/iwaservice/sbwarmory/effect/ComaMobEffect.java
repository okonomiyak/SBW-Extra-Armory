package uk.iwaservice.sbwarmory.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Drowsy, not incapacitated: movement is slowed and the screen stays blacked out (piggybacking
 * vanilla Blindness, refreshed every tick) for as long as it lasts. Attack power is untouched.
 */
public class ComaMobEffect extends MobEffect {

    private static final String SPEED_MODIFIER_UUID = "a1e4d9c2-3b1a-4f2e-9c3a-a00000000001";
    private static final int BLINDNESS_TOPUP_TICKS = 30; // > 1 effect tick, so it never visibly lapses

    public ComaMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x2B2B3A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, -0.60, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_TOPUP_TICKS, 0, true, false));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // every tick, not just every 25 - the blindness top-up needs to keep up
    }
}
