package ru.nyansus.mc.fallenlink.player;

import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public interface StatisticKeyCatalog {

    Collection<Material> blockMaterials();

    Collection<EntityType> mobTypes();
}
