package fuzs.armorquickswap.client.init;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class BloodParticle extends TextureSheetParticle {

    public BloodParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.gravity = 0.06F;
        this.setColor(1.0F, 0.0F, 0.0F);
        this.setPower(1.2F);
        this.xd *= 0.4F / (Math.random() * 0.9F + 0.1F);
        this.yd += Math.random() * 0.15F;
        this.zd *= 0.4F / (Math.random() * 0.9F + 0.1F);
        this.lifetime = (int) (200F + (20F / (Math.random() * 0.9F + 0.1F)));
        this.setSize(0.01F, 0.01F);
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.xd != 0.0 && this.zd != 0.0 && !this.onGround) {
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            if (this.onGround) {
                this.xd *= 0.7;
                this.zd *= 0.7;
                this.y += 0.2;
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public record Provider(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BloodParticle particle = new BloodParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
