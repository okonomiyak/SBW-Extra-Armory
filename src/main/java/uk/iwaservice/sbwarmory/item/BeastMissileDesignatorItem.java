package uk.iwaservice.sbwarmory.item;

import uk.iwaservice.sbwarmory.entity.BeastMissileEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Same everything as {@link LaserDesignatorItem} - calls in a {@link BeastMissileEntity} instead. */
public class BeastMissileDesignatorItem extends LaserDesignatorItem {

    public BeastMissileDesignatorItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void spawnMissile(ServerPlayer player, Level level, Vec3 target) {
        BeastMissileEntity missile = new BeastMissileEntity(player, level, target.y);
        missile.setPos(target.x, target.y + SPAWN_HEIGHT_ABOVE_TARGET, target.z);
        level.addFreshEntity(missile);
    }

    @Override
    protected String inboundMessageKey() {
        return "chat.sbwarmory.beast_missile_inbound";
    }
}
