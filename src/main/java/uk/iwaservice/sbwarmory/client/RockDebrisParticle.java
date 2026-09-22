package uk.iwaservice.sbwarmory.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** A chunk of the beast missile's own rock texture, thrown outward and falling with gravity. */
public class RockDebrisParticle extends TextureSheetParticle {

    protected RockDebrisParticle(ClientLevel level, double x, double y, double z,
                                  double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.friction = 0.9f;
        this.gravity = 1.0f;
        this.lifetime = 40 + this.random.nextInt(30);
        this.quadSize = 0.8f + this.random.nextFloat() * 1.0f;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.pickSprite(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double dx, double dy, double dz) {
            return new RockDebrisParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
