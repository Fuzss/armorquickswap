package fuzs.armorquickswap;

import net.minecraftforge.forgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ModListPrinter {

    public static void dumpModList(List<List<IModInfo>> mods) {
        StringBuilder modListText = new StringBuilder();

        boolean[] lastItemOfNestLevel = new boolean[mods.size()];
        List<List<IModInfo>> topLevelMods = mods.stream()
                .filter(mod -> !mod.isEmpty())
                .sorted(Comparator.comparing(t -> t.get(0).getModId()))
                .toList();
        int topLevelModsCount = topLevelMods.size();

        for (int i = 0; i < topLevelModsCount; i++) {
            boolean lastItem = i == topLevelModsCount - 1;

            if (lastItem) lastItemOfNestLevel[0] = true;

            dumpModList0(topLevelMods.get(i), modListText, 0, lastItemOfNestLevel);
        }

        int modsCount = mods.size();
        YourOptionsShallBeRespected.LOGGER.info("Loading {} mod{}:\n{}", modsCount, modsCount != 1 ? "s" : "", modListText);
    }

    private static void dumpModList0(List<IModInfo> mod, StringBuilder log, int nestLevel, boolean[] lastItemOfNestLevel) {
        if (log.length() > 0) log.append('\n');

        for (int depth = 0; depth < nestLevel; depth++) {
            log.append(depth == 0 ? "\t" : lastItemOfNestLevel[depth] ? "     " : "   | ");
        }

        log.append(nestLevel == 0 ? "\t" : "  ");
        log.append(nestLevel == 0 ? "-" : lastItemOfNestLevel[nestLevel] ? " \\--" : " |--");
        log.append(' ');
        log.append(mod.get(0).getModId());
        log.append(' ');
        log.append(mod.get(0).getVersion());

        List<IModInfo> nestedMods = new ArrayList<>(mod.subList(1, mod.size()));
        nestedMods.sort(Comparator.comparing(IModInfo::getModId));

        if (!nestedMods.isEmpty()) {
            Iterator<IModInfo> iterator = nestedMods.iterator();
            IModInfo nestedMod;
            boolean lastItem;

            while (iterator.hasNext()) {
                nestedMod = iterator.next();
                lastItem = !iterator.hasNext();

                if (lastItem) lastItemOfNestLevel[nestLevel+1] = true;

                dumpModList0(List.of(nestedMod), log, nestLevel + 1, lastItemOfNestLevel);

                if (lastItem) lastItemOfNestLevel[nestLevel+1] = false;
            }
        }
    }
}
