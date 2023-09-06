package fuzs.armorquickswap.client;

import fuzs.armorquickswap.mixin.client.accessor.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModelPartHelper {

    public static List<ModelPart> explode(List<ModelPart> parts) //separates the children from their parents (YOU MONSTER >:( )
    {
        List<ModelPart> models = new ArrayList<>();

        parts.forEach(part -> explodeRecursive(models, part));

        return models;
    }

    private static void explodeRecursive(List<ModelPart> parts, ModelPart part)
    {
        parts.add(part);

        for (ModelPart next : ModelPartAccessor.class.cast(part).armorquickswap$getChildren().values()) {
            next.x += part.x;
            next.y += part.y;
            next.z += part.z;
            next.xRot += part.xRot;
            next.yRot += part.yRot;
            next.zRot += part.zRot;
            explodeRecursive(parts, next);
        }
    }
}
