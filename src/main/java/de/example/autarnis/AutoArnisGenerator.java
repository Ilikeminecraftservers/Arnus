@Override
public void onEnable() {
    installHeightDatapack();
    getServer().getPluginManager().registerEvents(this, this);
    getLogger().info("AutoArnisGenerator enabled");
}

private void installHeightDatapack() {
    File dpRoot = new File(Bukkit.getWorldContainer(), "datapacks/height");
    File mcmeta = new File(dpRoot, "pack.mcmeta");
    File dimDir = new File(dpRoot, "data/minecraft/dimension_type");
    File overworld = new File(dimDir, "overworld.json");

    try {
        if (!mcmeta.exists()) {
            dpRoot.mkdirs();
            dimDir.mkdirs();

            // pack.mcmeta schreiben
            String mcmetaText = "{\n" +
                    "  \"pack\": {\n" +
                    "    \"pack_format\": 32,\n" +
                    "    \"description\": \"Max Height Datapack\"\n" +
                    "  }\n" +
                    "}\n";
            java.nio.file.Files.writeString(mcmeta.toPath(), mcmetaText);

            // overworld.json schreiben
            String dimJson = "{\n" +
                    "  \"ultrawarm\": false,\n" +
                    "  \"natural\": true,\n" +
                    "  \"coordinate_scale\": 1.0,\n" +
                    "  \"has_skylight\": true,\n" +
                    "  \"has_ceiling\": false,\n" +
                    "  \"ambient_light\": 0.0,\n" +
                    "  \"fixed_time\": null,\n" +
                    "  \"piglin_safe\": false,\n" +
                    "  \"bed_works\": true,\n" +
                    "  \"respawn_anchor_works\": false,\n" +
                    "  \"min_y\": -2048,\n" +
                    "  \"height\": 4096,\n" +
                    "  \"logical_height\": 4096,\n" +
                    "  \"infiniburn\": \"#minecraft:infiniburn_overworld\",\n" +
                    "  \"effects\": \"minecraft:overworld\",\n" +
                    "  \"monster_spawn_light_level\": {\n" +
                    "    \"type\": \"minecraft:uniform\",\n" +
                    "    \"value\": [0, 7]\n" +
                    "  },\n" +
                    "  \"monster_spawn_block_light_limit\": 0\n" +
                    "}\n";
            java.nio.file.Files.writeString(overworld.toPath(), dimJson);

            getLogger().info("Height Datapack installiert. Bitte /reload ausführen.");
        }
    } catch (IOException e) {
        getLogger().severe("Konnte Height Datapack nicht schreiben: " + e.getMessage());
    }
}
