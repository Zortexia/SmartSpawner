package github.nighter.smartspawner.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpawnerBreakConfig {
    private final Supplier<FileConfiguration> configSupplier;
    private final Logger logger;

    private volatile boolean breakEnabled;
    private volatile boolean directToInventory;
    private volatile int durabilityLoss;
    private volatile boolean sneakBreakEnabled;
    private volatile boolean silkTouchRequired;
    private volatile int silkTouchLevel;
    private volatile boolean naturalBreakable;
    private volatile boolean convertNaturalToSmartSpawner;
    private volatile boolean sellAndXpBreak;
    private volatile Set<Material> requiredTools = Set.of();
    private volatile Map<EntityType, Double> naturalSpawnerDropChances = Map.of();

    public SpawnerBreakConfig(Supplier<FileConfiguration> configSupplier, Logger logger) {
        this.configSupplier = configSupplier;
        this.logger = logger;
        load();
    }

    public void load() {
        FileConfiguration config = configSupplier.get();
        this.breakEnabled = config.getBoolean("spawner_break.enabled", true);
        this.directToInventory = config.getBoolean("spawner_break.direct_to_inventory", false);
        this.durabilityLoss = config.getInt("spawner_break.durability_loss", 1);
        this.sneakBreakEnabled = config.getBoolean("spawner_break.sneak_break", true);
        this.silkTouchRequired = config.getBoolean("spawner_break.silk_touch.required", true);
        this.silkTouchLevel = config.getInt("spawner_break.silk_touch.level", 1);
        this.naturalBreakable = config.getBoolean("natural_spawner.breakable", false);
        this.convertNaturalToSmartSpawner = config.getBoolean("natural_spawner.convert_to_smart_spawner", false);
        this.sellAndXpBreak = config.getBoolean("spawner_break.sell_and_xp_break",
                config.getBoolean("spawner_break.auto_sell_and_claim_exp_on_break", true));
        this.naturalSpawnerDropChances = loadNaturalSpawnerDropChances(config);

        this.requiredTools = config.getStringList("spawner_break.required_tools")
                .stream()
                .map(toolName -> {
                    try {
                        return Material.valueOf(toolName.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        logger.warning("Invalid material in spawner_break.required_tools: " + toolName);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Map<EntityType, Double> loadNaturalSpawnerDropChances(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("natural_spawner.drop_chance");
        if (section == null) {
            return Map.of();
        }

        Map<EntityType, Double> loadedDropChances = new EnumMap<>(EntityType.class);
        for (String entityName : section.getKeys(false)) {
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(entityName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                logger.warning("Invalid entity in natural_spawner.drop_chance: " + entityName);
                continue;
            }

            double dropChance = section.getDouble(entityName, 100.0);
            if (dropChance < 0.0 || dropChance > 100.0) {
                logger.warning("Invalid drop chance for natural_spawner.drop_chance." + entityName +
                        ". Value must be between 0.0 and 100.0; using 100.0");
                dropChance = 100.0;
            }

            loadedDropChances.put(entityType, dropChance);
        }

        return Map.copyOf(loadedDropChances);
    }

    public boolean isBreakEnabled() {
        return breakEnabled;
    }

    public boolean isDirectToInventory() {
        return directToInventory;
    }

    public int getDurabilityLoss() {
        return durabilityLoss;
    }

    public boolean isSneakBreakEnabled() {
        return sneakBreakEnabled;
    }

    public boolean isSilkTouchRequired() {
        return silkTouchRequired;
    }

    public int getSilkTouchLevel() {
        return silkTouchLevel;
    }

    public boolean isNaturalBreakable() {
        return naturalBreakable;
    }

    public boolean isConvertNaturalToSmartSpawner() {
        return convertNaturalToSmartSpawner;
    }

    public boolean isSellAndXpBreak() {
        return sellAndXpBreak;
    }

    public boolean isRequiredTool(Material material) {
        return requiredTools.contains(material);
    }

    public double getNaturalSpawnerDropChance(EntityType entityType) {
        return naturalSpawnerDropChances.getOrDefault(entityType, 100.0);
    }
}
