package uk.iwaservice.sbwarmory.item;

import uk.iwaservice.sbwarmory.ModRegistry;
import uk.iwaservice.sbwarmory.entity.ActiveDefenseSystemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Right-click a block to place a grenade-intercepting {@link ActiveDefenseSystemEntity} on top of it. */
public class ActiveDefenseSystemItem extends Item {

    public ActiveDefenseSystemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        if (!level.isClientSide) {
            ActiveDefenseSystemEntity system = new ActiveDefenseSystemEntity(ModRegistry.ACTIVE_DEFENSE_SYSTEM.get(), level);
            system.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(system);
            level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        }

        Player player = context.getPlayer();
        if (player != null && !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
