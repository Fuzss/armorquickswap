package fuzs.armorquickswap.client;

import net.minecraft.world.entity.EquipmentSlot;

public class ArmorStandEquipmentHelper {

    public static double getEquipmentClickHeight(EquipmentSlot equipmentSlot, boolean isSmall) {
        return switch (equipmentSlot) {
            // clickedHeight >= 0.1D && clickedHeight < (isSmall ? 0.9D : 0.55D)
            case FEET -> isSmall ? 0.5 : 0.375;
            // clickedHeight >= (isSmall ? 1.2D : 0.9D) && clickedHeight < (isSmall ? 1.9D : 1.6D)
            case CHEST -> isSmall ? 1.55 : 1.25;
            // clickedHeight >= 0.4D && clickedHeight < (isSmall ? 1.4D : 1.2D)
            case LEGS -> isSmall ? 0.9 : 0.8;
            // clickedHeight >= 1.6D && clickedHeight < 1.975D
            case HEAD -> 1.7875;
            default -> throw new RuntimeException();
        } * (isSmall ? 0.5 : 1.0);
    }
}
