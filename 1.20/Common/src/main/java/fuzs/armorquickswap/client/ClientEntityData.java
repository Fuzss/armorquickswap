package fuzs.armorquickswap.client;

import com.google.common.collect.MapMaker;
import fuzs.armorquickswap.client.handler.ShatterTickHandler;
import fuzs.armorquickswap.mixin.client.accessor.EntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ClientEntityData {
    private static final Map<LivingEntity, ClientEntityData> CLIENT_ENTITY_DATA = new MapMaker().concurrencyLevel(1).weakKeys().makeMap();

    private final EntityInLevelCallback callback;
    private Vec3 deltaMovement;

    private ClientEntityData(EntityInLevelCallback callback, Vec3 deltaMovement) {
        this.callback = callback;
        this.deltaMovement = deltaMovement;
    }

    public EntityInLevelCallback getCallback() {
        return this.callback;
    }

    public Vec3 getDeltaMovement() {
        return this.deltaMovement;
    }

    public Vec3 getAndUpdateDeltaMovement(Vec3 deltaMovement) {

        if (Math.abs(deltaMovement.x) > Math.abs(this.deltaMovement.x)) {
            this.deltaMovement = deltaMovement;
        } else if (Math.abs(deltaMovement.z) > Math.abs(this.deltaMovement.z)) {
            this.deltaMovement = deltaMovement;
        } else if (deltaMovement.y > this.deltaMovement.y) {
            this.deltaMovement = deltaMovement;
        }

        deltaMovement = this.deltaMovement;
        this.deltaMovement = deltaMovement.scale(0.98);
        return deltaMovement;
    }

    public static void submitEntity(LivingEntity entity) {
        // we replace the client-side level callback to be able to keep the entity in the world longer than vanilla would
        // (our death animation takes 100 ticks to play, while vanilla removes death entities after 20 ticks)
        EntityInLevelCallback callback = ((EntityAccessor) entity).armorquickswap$getLevelCallback();
        // the replacement callback deletes the removal reason from an entity when called, which is important,
        // as the removal reason is set right before the level callback is invoked, but independently of it.
        // it leads to Entity::isRemoved returning true which e.g. prevents the entity from ticking which we need
        // instead the removal reason is stored and used by the actual callback when it is called
        RemovalReasonHoldingNullCallback newCallback = new RemovalReasonHoldingNullCallback(entity, callback);
        entity.setLevelCallback(newCallback);
        EntityInLevelCallback forwardingCallback = new ForwardingEntityInLevelCallback(callback, newCallback);
        CLIENT_ENTITY_DATA.put(entity, new ClientEntityData(forwardingCallback, entity.getDeltaMovement()));
    }

    public static void clearEntity(LivingEntity entity) {
        ClientEntityData clientEntityData = CLIENT_ENTITY_DATA.remove(entity);
        if (clientEntityData != null) clientEntityData.getCallback().onRemove(entity.getRemovalReason());
    }

    public static Vec3 getAndUpdateDeltaMovement(LivingEntity entity) {
        ClientEntityData clientEntityData = CLIENT_ENTITY_DATA.get(entity);
        if (clientEntityData != null) {
            return clientEntityData.getAndUpdateDeltaMovement(entity.getDeltaMovement());
        } else {
            return entity.getDeltaMovement();
        }
    }

    public static Vec3 getDeltaMovement(LivingEntity entity) {
        ClientEntityData clientEntityData = CLIENT_ENTITY_DATA.get(entity);
        if (clientEntityData != null) {
            return clientEntityData.getDeltaMovement().multiply(ShatterTickHandler.DELTA_MOVEMENT_SCALE);
        } else {
            return new Vec3(0.005, 0.015, 0.005);
        }
    }

    private interface RemovalReasonHolder {

        Entity.RemovalReason getRemovalReason();
    }

    private static class RemovalReasonHoldingNullCallback implements EntityInLevelCallback, RemovalReasonHolder {
        private final LivingEntity entity;
        private final EntityInLevelCallback callback;
        @Nullable
        private Entity.RemovalReason removalReason;

        private RemovalReasonHoldingNullCallback(LivingEntity entity, EntityInLevelCallback callback) {
            this.entity = entity;
            this.callback = callback;
        }

        @Override
        public void onMove() {
            this.callback.onMove();
        }

        @Override
        public void onRemove(Entity.RemovalReason reason) {
            this.removalReason = reason;
            ((EntityAccessor) this.entity).armorquickswap$callUnsetRemoved();
        }

        @Override
        public Entity.RemovalReason getRemovalReason() {
            return this.removalReason;
        }
    }

    private record ForwardingEntityInLevelCallback(EntityInLevelCallback callback,
                                                   RemovalReasonHolder holder) implements EntityInLevelCallback {

        @Override
        public void onMove() {
            this.callback.onMove();
        }

        @Override
        public void onRemove(Entity.RemovalReason reason) {
            if (reason == null) {
                if (this.holder.getRemovalReason() != null) {
                    reason = this.holder.getRemovalReason();
                } else {
                    reason = Entity.RemovalReason.KILLED;
                }
            }
            this.callback.onRemove(reason);
        }
    }
}
