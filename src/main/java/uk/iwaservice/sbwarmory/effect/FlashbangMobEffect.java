package uk.iwaservice.sbwarmory.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Flashbanged. Players get a white-out screen that fades over the last {@link #FADE_TICKS}
 * (drawn client-side in ClientForgeEvents#onRenderFlashbang); mobs lose their target for the duration.
 */
public class FlashbangMobEffect extends MobEffect {

    /** Screen stays fully white until this many ticks remain, then fades out linearly. */
    public static final int FADE_TICKS = 40;

    public FlashbangMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFF0);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
