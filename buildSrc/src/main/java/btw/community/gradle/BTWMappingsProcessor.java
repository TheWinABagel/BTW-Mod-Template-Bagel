package btw.community.gradle;

import net.fabricmc.loom.api.processor.MappingProcessorContext;
import net.fabricmc.loom.api.processor.MinecraftJarProcessor;
import net.fabricmc.loom.util.Pair;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.util.ArrayList;
import java.util.List;

public class BTWMappingsProcessor implements MinecraftJarProcessor.MappingsProcessor<BTWProcessor.Spec>{

    public static final BTWMappingsProcessor INSTANCE = new BTWMappingsProcessor();

    @Override
    public boolean transform(MemoryMappingTree mappings, BTWProcessor.Spec spec, MappingProcessorContext context) {
        cleanupMappingLeakageToOfficial(mappings);
        return true;
    }

    private static void cleanupMappingLeakageToOfficial(MemoryMappingTree tree) {
        int intermediaryId = tree.getNamespaceId("intermediary");
        int officialId = tree.getNamespaceId("named");

        List<Pair<String, String>> entriesToRemove = new ArrayList<>();

        for (MappingTree.ClassMapping classMapping : tree.getClasses()) {
            for (MappingTree.MethodMapping methodMapping : classMapping.getMethods()) {
                String intermediary = methodMapping.getName(intermediaryId);
                String official = methodMapping.getName(officialId);
                if (intermediary != null && official != null && intermediary.startsWith("method_") && intermediary.equals(official) /*&& methodMapping.getDesc(intermediaryId) != null*/) {
                    System.out.println("Removing method " + methodMapping.getSrcName() + " from class " + classMapping.getSrcName());
                    entriesToRemove.add(new Pair<>(methodMapping.getSrcName(), methodMapping.getSrcDesc()));
                }
            }

            for (Pair<String, String> entry : entriesToRemove) {
                classMapping.removeMethod(entry.left(), entry.right());
            }
        }
    }
}
