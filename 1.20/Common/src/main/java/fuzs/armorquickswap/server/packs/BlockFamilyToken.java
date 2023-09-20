package fuzs.armorquickswap.server.packs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record BlockFamilyToken(String prefix, String suffix, String regex) {
    private static final Multimap<Type, BlockFamilyToken> TOKEN_TYPES = HashMultimap.create();
    public static final BlockFamilyToken PLANKS = register(Type.WOOD, "_planks");
    public static final BlockFamilyToken LOG = register(Type.WOOD, List.of(), List.of("_log", "_stem"));
    public static final BlockFamilyToken WOOD = register(Type.WOOD, List.of(), List.of("_wood", "_hyphae"));
    public static final BlockFamilyToken STRIPPED_LOG = register(Type.WOOD, List.of("stripped_"), List.of("_log", "_stem"));
    public static final BlockFamilyToken STRIPPED_WOOD = register(Type.WOOD, List.of("stripped_"), List.of("_wood", "_hyphae"));
    public static final BlockFamilyToken SIGN = register(Type.WOOD, "_sign");
    public static final BlockFamilyToken HANGING_SIGN = register(Type.WOOD, "_hanging_sign");
    public static final BlockFamilyToken PRESSURE_PLATE = register(Type.WOOD, "_pressure_plate");
    public static final BlockFamilyToken TRAPDOOR = register(Type.WOOD, "_trapdoor");
    public static final BlockFamilyToken STAIRS = register(Type.WOOD, "_stairs");
    public static final BlockFamilyToken BUTTON = register(Type.WOOD, "_button");
    public static final BlockFamilyToken SLAB = register(Type.WOOD, "_slab");
    public static final BlockFamilyToken FENCE_GATE = register(Type.WOOD, "_fence_gate");
    public static final BlockFamilyToken FENCE = register(Type.WOOD, "_fence");
    public static final BlockFamilyToken DOOR = register(Type.WOOD, "_door");
    public static final BlockFamilyToken BOAT = register(Type.WOOD, "_boat");
    public static final BlockFamilyToken CHEST_BOAT = register(Type.WOOD, "_chest_boat");

    public static BlockFamilyToken register(Type type, String postfix) {
        return register(type, List.of(), List.of(postfix));
    }

    public static BlockFamilyToken register(Type type, String prefix, String postfix) {
        return register(type, List.of(prefix), List.of(postfix));
    }

    public static BlockFamilyToken register(Type type, List<String> prefixList, List<String> postfixList) {
        String prefix = prefixList.stream().collect(Collectors.joining("|", "(?:", ")"));
        String postfix = postfixList.stream().collect(Collectors.joining("|", "(?:", ")"));
        BlockFamilyToken token = new BlockFamilyToken(prefix, postfix, "^" + prefix + "[a-z0-9/._-]+" + postfix + "$");
        TOKEN_TYPES.put(type, token);
        return token;
    }

    public static Collection<BlockFamilyToken> getBlockFamily(Type type) {
        return Collections.unmodifiableCollection(TOKEN_TYPES.get(type));
    }

    public boolean test(String path) {
        return path.matches(this.regex);
    }

    public String strip(String s) {
        return s.replaceAll("^" + this.prefix, "").replaceAll(this.suffix + "$", "");
    }

    public enum Type {
        WOOD
    }
}
