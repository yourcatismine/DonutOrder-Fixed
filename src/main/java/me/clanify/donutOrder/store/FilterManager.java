/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package me.clanify.donutOrder.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.clanify.donutOrder.DonutOrder;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class FilterManager {
    private final DonutOrder pl;
    private final LinkedHashMap<String, Set<Material>> categories = new LinkedHashMap();
    private File file;
    private YamlConfiguration yaml;

    public FilterManager(DonutOrder pl) {
        this.pl = pl;
        this.file = new File(pl.getDataFolder(), "filter.yml");
        this.reload();
    }

    public void reload() {
        this.categories.clear();
        if (!this.file.exists()) {
            try {
                this.pl.getDataFolder().mkdirs();
                this.file.createNewFile();
            } catch (Exception exception) {
                // empty catch block
            }
        }
        this.yaml = YamlConfiguration.loadConfiguration((File) this.file);
        for (String key : this.yaml.getKeys(false)) {
            List<String> list = this.yaml.getStringList(key);
            if (list == null) {
                list = Collections.emptyList();
            }
            Set<Material> mats = list.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
                    .map(s -> {
                        Material m = Material.matchMaterial(s);
                        if (m == null) {
                            m = Material.matchMaterial(s.toUpperCase(Locale.ENGLISH));
                        }
                        return m;
                    }).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
            this.categories.put(key, mats);
        }
    }

    public List<String> categoryNames() {
        return new ArrayList<String>(this.categories.keySet());
    }

    public Set<Material> resolve(String category) {
        if (category == null) {
            return Collections.emptySet();
        }
        for (Map.Entry<String, Set<Material>> e : this.categories.entrySet()) {
            if (!e.getKey().equalsIgnoreCase(category))
                continue;
            return e.getValue();
        }
        return Collections.emptySet();
    }
}
