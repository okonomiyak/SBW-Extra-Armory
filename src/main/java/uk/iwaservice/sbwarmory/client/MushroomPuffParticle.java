package uk.iwaservice.sbwarmory.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** One puff of the mushroom cloud's stem/cap; randomly tinted orange or yellow for a fiery, varied look. */
public class MushroomPuffParticle extends TextureSheetParticle {

    protected MushroomPuffParticle(ClientLevel level, double x, double y, double z,
                                    double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.friction = 0.96f;
        this.gravity = 0f;
        this.lifetime = 30 + this.random.nextInt(25);
        this.quadSize = 1.2f + this.random.nextFloat() * 1.3f;

        if (this.random.nextBoolean()) {
            // darker orange
            this.rCol = 1.0f;
            this.gCol = 0.45f + this.random.nextFloat() * 0.15f;
            this.bCol = 0.1f;
        } else {
            // brighter yellow
            this.rCol = 1.0f;
            this.gCol = 0.8f + this.random.nextFloat() * 0.15f;
            this.bCol = 0.25f;
        }

        this.pickSprite(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double dx, double dy, double dz) {
            return new MushroomPuffParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
