package ru.nyansus.mc.fallenlink.player;

import java.util.Arrays;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public final class BukkitStatisticKeyCatalog implements StatisticKeyCatalog {

    private final Collection<Material> blockMaterials = Arrays.stream(Material.values())
            .filter(Material::isBlock)
            .toList();
    private final Collection<EntityType> mobTypes = Arrays.stream(EntityType.values())
            .filter(EntityType::isAlive)
            .filter(entityType -> entityType != EntityType.PLAYER)
            .filter(entityType -> entityType.getKey() != null)
            .toList();

    @Override
    public Collection<Material> blockMaterials() {
        return blockMaterials;
    }

    @Override
    public Collection<EntityType> mobTypes() {
        return mobTypes;
    }
}
