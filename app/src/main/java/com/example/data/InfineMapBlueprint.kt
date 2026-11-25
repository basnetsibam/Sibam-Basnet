package com.example.data

data class MapRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val kind: ObstacleKind,
    val label: String? = null
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

enum class ObstacleKind {
    CLOCK_TOWER,
    SMALL_HOUSE,
    FACTORY_HALL,
    ARENA_WALL,
    SHOP_STALL,
    SCHOOL_BLOCK,
    ASSOCIATION_HALL,
    GARDEN_HEDGE,
    HOSPITAL_WING
}

enum class LootTier(val label: String) {
    HIGH("High-Tier Loot"),
    MEDIUM("Balanced Loot"),
    MEDICAL("Medical & Gear")
}

data class InfineLocation(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val lootTier: LootTier,
    val description: String,
    val hasRevivalPoint: Boolean
)

data class RevivalPointBlueprint(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float
)

data class VehicleSpawnPoint(
    val type: VehicleType,
    val x: Float,
    val y: Float,
    val headingRad: Float
)

data class RoadSegment(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val width: Float = 74f
)

object InfineMapBlueprint {
    const val WORLD_SIZE = 2400f

    val locations: List<InfineLocation> = listOf(
        InfineLocation(
            id = "clock_tower",
            name = "Clock Tower",
            x = 1200f,
            y = 1200f,
            lootTier = LootTier.HIGH,
            description = "Infine's iconic central landmark with a giant clock tower surrounded by small loot houses.",
            hasRevivalPoint = true
        ),
        InfineLocation(
            id = "school",
            name = "School",
            x = 560f,
            y = 540f,
            lootTier = LootTier.HIGH,
            description = "North-west campus complex with twin classrooms, courtyard cover & AR spawns.",
            hasRevivalPoint = false
        ),
        InfineLocation(
            id = "factory",
            name = "Factory",
            x = 1840f,
            y = 540f,
            lootTier = LootTier.HIGH,
            description = "Heavy industrial plant with steel warehouses, cargo containers & sniper vantage points.",
            hasRevivalPoint = false
        ),
        InfineLocation(
            id = "garden",
            name = "Garden",
            x = 460f,
            y = 1200f,
            lootTier = LootTier.MEDIUM,
            description = "Verdant botanical park with sculpted hedges, fountain plaza & Revival Point.",
            hasRevivalPoint = true
        ),
        InfineLocation(
            id = "hospital",
            name = "Hospital",
            x = 1920f,
            y = 1200f,
            lootTier = LootTier.MEDICAL,
            description = "East medical center stocked with Medkits, Inhalers & an emergency Revival Point.",
            hasRevivalPoint = true
        ),
        InfineLocation(
            id = "arena",
            name = "Arena",
            x = 540f,
            y = 1860f,
            lootTier = LootTier.HIGH,
            description = "Competitive battle pit built for intense Clash Squad and close-range SMG duels.",
            hasRevivalPoint = false
        ),
        InfineLocation(
            id = "shop",
            name = "Shop",
            x = 1200f,
            y = 1880f,
            lootTier = LootTier.MEDIUM,
            description = "Bustling market plaza lined with vendor stalls, armor crates & street vehicles.",
            hasRevivalPoint = false
        ),
        InfineLocation(
            id = "association",
            name = "Association",
            x = 1860f,
            y = 1860f,
            lootTier = LootTier.HIGH,
            description = "Tactical Guild HQ with reinforced walls, elite weapons & south-east Revival Point.",
            hasRevivalPoint = true
        )
    )

    val revivalPoints: List<RevivalPointBlueprint> = listOf(
        RevivalPointBlueprint("rp_clock", "Clock Tower Plaza", 1200f, 1060f),
        RevivalPointBlueprint("rp_garden", "Garden Fountain", 460f, 1200f),
        RevivalPointBlueprint("rp_hospital", "Hospital Helipad", 1920f, 1050f),
        RevivalPointBlueprint("rp_association", "Association Gate", 1860f, 1710f)
    )

    val roads: List<RoadSegment> = listOf(
        // Ring road connecting outer POIs
        RoadSegment(540f, 540f, 1840f, 540f, 76f),
        RoadSegment(1840f, 540f, 1860f, 1860f, 76f),
        RoadSegment(1860f, 1860f, 540f, 1860f, 76f),
        RoadSegment(540f, 1860f, 540f, 540f, 76f),
        // Cross avenues through Clock Tower
        RoadSegment(460f, 1200f, 1920f, 1200f, 72f),
        RoadSegment(1200f, 540f, 1200f, 1880f, 72f)
    )

    val obstacles: List<MapRect> = listOf(
        // === CLOCK TOWER (Center 1200, 1200) + Small Houses ===
        MapRect(1145f, 1145f, 1255f, 1255f, ObstacleKind.CLOCK_TOWER, "CLOCK TOWER"),
        // 6 small houses encircling Clock Tower
        MapRect(980f, 1010f, 1055f, 1080f, ObstacleKind.SMALL_HOUSE, "House"),
        MapRect(1345f, 1010f, 1420f, 1080f, ObstacleKind.SMALL_HOUSE, "House"),
        MapRect(960f, 1290f, 1035f, 1365f, ObstacleKind.SMALL_HOUSE, "House"),
        MapRect(1365f, 1290f, 1440f, 1365f, ObstacleKind.SMALL_HOUSE, "House"),
        MapRect(1045f, 1405f, 1125f, 1470f, ObstacleKind.SMALL_HOUSE, "House"),
        MapRect(1275f, 1405f, 1355f, 1470f, ObstacleKind.SMALL_HOUSE, "House"),

        // === SCHOOL (NW ~560, 540) ===
        MapRect(410f, 380f, 560f, 455f, ObstacleKind.SCHOOL_BLOCK, "Classroom A"),
        MapRect(610f, 380f, 740f, 455f, ObstacleKind.SCHOOL_BLOCK, "Classroom B"),
        MapRect(410f, 610f, 520f, 695f, ObstacleKind.SCHOOL_BLOCK, "Library"),

        // === FACTORY (NE ~1840, 540) ===
        MapRect(1690f, 370f, 1850f, 465f, ObstacleKind.FACTORY_HALL, "Turbine Hall"),
        MapRect(1910f, 390f, 2040f, 520f, ObstacleKind.FACTORY_HALL, "Foundry"),
        MapRect(1720f, 620f, 1830f, 700f, ObstacleKind.FACTORY_HALL, "Crate Yard"),

        // === GARDEN (West ~460, 1200) ===
        MapRect(300f, 1040f, 390f, 1125f, ObstacleKind.GARDEN_HEDGE),
        MapRect(540f, 1040f, 630f, 1125f, ObstacleKind.GARDEN_HEDGE),
        MapRect(300f, 1280f, 390f, 1360f, ObstacleKind.GARDEN_HEDGE),
        MapRect(540f, 1280f, 630f, 1360f, ObstacleKind.GARDEN_HEDGE),

        // === HOSPITAL (East ~1920, 1200) ===
        MapRect(1990f, 1060f, 2110f, 1170f, ObstacleKind.HOSPITAL_WING, "ER Wing"),
        MapRect(1990f, 1240f, 2110f, 1350f, ObstacleKind.HOSPITAL_WING, "Med Lab"),
        MapRect(1770f, 1265f, 1875f, 1350f, ObstacleKind.HOSPITAL_WING, "Clinic"),

        // === ARENA (SW ~540, 1860) ===
        MapRect(370f, 1710f, 470f, 1780f, ObstacleKind.ARENA_WALL),
        MapRect(610f, 1710f, 710f, 1780f, ObstacleKind.ARENA_WALL),
        MapRect(370f, 1950f, 470f, 2020f, ObstacleKind.ARENA_WALL),
        MapRect(610f, 1950f, 710f, 2020f, ObstacleKind.ARENA_WALL),

        // === SHOP (South ~1200, 1880) ===
        MapRect(1040f, 1740f, 1135f, 1810f, ObstacleKind.SHOP_STALL, "Market"),
        MapRect(1265f, 1740f, 1360f, 1810f, ObstacleKind.SHOP_STALL, "Armory"),
        MapRect(1140f, 1965f, 1260f, 2045f, ObstacleKind.SHOP_STALL, "Bazaar"),

        // === ASSOCIATION (SE ~1860, 1860) ===
        MapRect(1960f, 1730f, 2080f, 1830f, ObstacleKind.ASSOCIATION_HALL, "Guild HQ"),
        MapRect(1710f, 1930f, 1840f, 2030f, ObstacleKind.ASSOCIATION_HALL, "Vault"),
        MapRect(1920f, 1930f, 2040f, 2025f, ObstacleKind.ASSOCIATION_HALL, "Annex")
    )

    val vehicleSpawns: List<VehicleSpawnPoint> = listOf(
        VehicleSpawnPoint(VehicleType.JEEP, 1080f, 1190f, 0f),
        VehicleSpawnPoint(VehicleType.TUK_TUK, 1210f, 1690f, -1.57f),
        VehicleSpawnPoint(VehicleType.SUPERCAR, 1660f, 540f, 3.14f),
        VehicleSpawnPoint(VehicleType.BIKE, 690f, 540f, 0f),
        VehicleSpawnPoint(VehicleType.TUK_TUK, 540f, 1660f, -1.57f),
        VehicleSpawnPoint(VehicleType.JEEP, 1790f, 1190f, 3.14f),
        VehicleSpawnPoint(VehicleType.BIKE, 1200f, 740f, 1.57f),
        VehicleSpawnPoint(VehicleType.SUPERCAR, 1710f, 1860f, 3.14f)
    )

    fun nearestLocationName(x: Float, y: Float): String {
        return locations.minByOrNull { loc ->
            val dx = loc.x - x
            val dy = loc.y - y
            dx * dx + dy * dy
        }?.name ?: "Infine"
    }
}
