package com.konorpg.registry;

import com.konorpg.KonoRPG;
import com.konorpg.item.AccessoryItem;
import com.konorpg.item.CardItem;
import com.konorpg.item.HunterBowItem;
import com.konorpg.item.RestoreItem;
import com.konorpg.item.ReturnScrollItem;
import com.konorpg.item.RpgTier;
import com.konorpg.item.RpgWeaponItem;
import com.konorpg.item.StaffItem;
import com.konorpg.rpg.RpgRarity;
import com.konorpg.rpg.Stat;
import java.util.Map;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KonoRPG.MOD_ID);

    private static DeferredItem<Item> mat(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static DeferredItem<Item> food(String name, int nutrition, float sat) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().food(
                new FoodProperties.Builder().nutrition(nutrition).saturationModifier(sat).build())));
    }

    // ---- materials ----
    public static final DeferredItem<Item> GOBLIN_EAR = mat("goblin_ear");
    public static final DeferredItem<Item> KOBOLD_FANG = mat("kobold_fang");
    public static final DeferredItem<Item> ORC_TUSK = mat("orc_tusk");
    public static final DeferredItem<Item> LIZARD_SCALE = mat("lizard_scale");
    public static final DeferredItem<Item> BONE_DUST = mat("bone_dust");
    public static final DeferredItem<Item> MAGIC_CRYSTAL = ITEMS.register("magic_crystal", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> TOAD_LEG = food("toad_leg", 2, 0.1f);
    public static final DeferredItem<Item> FRIED_TOAD_LEG = food("fried_toad_leg", 6, 0.8f);
    public static final DeferredItem<Item> CABBAGE = food("cabbage", 4, 0.4f);

    // ---- consumables ----
    public static final DeferredItem<RestoreItem> HEALTH_POTION = ITEMS.register("health_potion", () -> new RestoreItem(12, 0, Rarity.COMMON));
    public static final DeferredItem<RestoreItem> GREATER_HEALTH_POTION = ITEMS.register("greater_health_potion", () -> new RestoreItem(26, 0, Rarity.UNCOMMON));
    public static final DeferredItem<RestoreItem> MANA_POTION = ITEMS.register("mana_potion", () -> new RestoreItem(0, 35, Rarity.COMMON));
    public static final DeferredItem<RestoreItem> GREATER_MANA_POTION = ITEMS.register("greater_mana_potion", () -> new RestoreItem(0, 90, Rarity.UNCOMMON));
    public static final DeferredItem<ReturnScrollItem> RETURN_SCROLL = ITEMS.register("return_scroll", ReturnScrollItem::new);
    public static final DeferredItem<CardItem> ADVENTURER_CARD = ITEMS.register("adventurer_card", CardItem::new);
    public static final DeferredItem<Item> SAKE_BOTTLE = mat("sake_bottle");

    // ---- weapons ----
    public static final DeferredItem<RpgWeaponItem> ADVENTURER_SWORD = ITEMS.register("adventurer_sword",
            () -> new RpgWeaponItem(RpgTier.ADVENTURER, 3, -2.4f, RpgRarity.COMMON, Map.of()));
    public static final DeferredItem<RpgWeaponItem> GOBLIN_HATCHET = ITEMS.register("goblin_hatchet",
            () -> new RpgWeaponItem(RpgTier.ADVENTURER, 5, -3.1f, RpgRarity.COMMON, Map.of(Stat.STR, 1)));
    public static final DeferredItem<RpgWeaponItem> KNIGHT_LONGSWORD = ITEMS.register("knight_longsword",
            () -> new RpgWeaponItem(RpgTier.KNIGHT, 4, -2.4f, RpgRarity.UNCOMMON, Map.of(Stat.STR, 2)));
    public static final DeferredItem<RpgWeaponItem> CRUSADER_BLADE = ITEMS.register("crusader_blade",
            () -> new RpgWeaponItem(RpgTier.KNIGHT, 5, -2.3f, RpgRarity.RARE, Map.of(Stat.STR, 3, Stat.VIT, 2)));
    public static final DeferredItem<RpgWeaponItem> GRAM_SWORD = ITEMS.register("gram_sword",
            () -> new RpgWeaponItem(RpgTier.LEGEND, 7, -2.2f, RpgRarity.LEGENDARY, Map.of(Stat.STR, 4, Stat.LUCK, 2)));
    public static final DeferredItem<HunterBowItem> HUNTER_BOW = ITEMS.register("hunter_bow",
            () -> new HunterBowItem(RpgRarity.UNCOMMON, Map.of(Stat.DEX, 2), 520));
    public static final DeferredItem<StaffItem> APPRENTICE_STAFF = ITEMS.register("apprentice_staff",
            () -> new StaffItem(RpgRarity.COMMON, Map.of(Stat.INT, 2), 250));
    public static final DeferredItem<StaffItem> ARCHMAGE_STAFF = ITEMS.register("archmage_staff",
            () -> new StaffItem(RpgRarity.RARE, Map.of(Stat.INT, 4, Stat.MAG, 3), 600));

    // ---- accessories ----
    public static final DeferredItem<AccessoryItem> IRON_RING = ITEMS.register("iron_ring", () -> new AccessoryItem(AccessoryItem.Kind.RING, RpgRarity.COMMON, Map.of(Stat.STR, 2)));
    public static final DeferredItem<AccessoryItem> MANA_RING = ITEMS.register("mana_ring", () -> new AccessoryItem(AccessoryItem.Kind.RING, RpgRarity.UNCOMMON, Map.of(Stat.INT, 3)));
    public static final DeferredItem<AccessoryItem> LUCKY_RING = ITEMS.register("lucky_ring", () -> new AccessoryItem(AccessoryItem.Kind.RING, RpgRarity.RARE, Map.of(Stat.LUCK, 4)));
    public static final DeferredItem<AccessoryItem> SWIFT_RING = ITEMS.register("swift_ring", () -> new AccessoryItem(AccessoryItem.Kind.RING, RpgRarity.UNCOMMON, Map.of(Stat.DEX, 3)));
    public static final DeferredItem<AccessoryItem> GUARDIAN_RING = ITEMS.register("guardian_ring", () -> new AccessoryItem(AccessoryItem.Kind.RING, RpgRarity.UNCOMMON, Map.of(Stat.VIT, 3)));
    public static final DeferredItem<AccessoryItem> FLAME_NECKLACE = ITEMS.register("flame_necklace", () -> new AccessoryItem(AccessoryItem.Kind.NECKLACE, RpgRarity.RARE, Map.of(Stat.MAG, 3, Stat.INT, 1)));
    public static final DeferredItem<AccessoryItem> ERIS_AMULET = ITEMS.register("eris_amulet", () -> new AccessoryItem(AccessoryItem.Kind.NECKLACE, RpgRarity.LEGENDARY, Map.of(Stat.INT, 5, Stat.MAG, 5)));

    // ---- spawn eggs ----
    public static final DeferredItem<DeferredSpawnEggItem> GOBLIN_EGG = ITEMS.register("goblin_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.GOBLIN, 0x5a8f3a, 0x2b3d1a, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KOBOLD_EGG = ITEMS.register("kobold_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.KOBOLD, 0xa0522d, 0x5a2d0c, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ORC_EGG = ITEMS.register("orc_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.ORC, 0x3b6b2a, 0x111111, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LIZARDMAN_EGG = ITEMS.register("lizardman_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.LIZARDMAN, 0x2a9d8f, 0x264653, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SKELETON_WARRIOR_EGG = ITEMS.register("skeleton_warrior_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.SKELETON_WARRIOR, 0xe8e4d0, 0x555555, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GIANT_TOAD_EGG = ITEMS.register("giant_toad_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.GIANT_TOAD, 0x6b8e23, 0xd2b48c, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TOWN_NPC_EGG = ITEMS.register("town_npc_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.TOWN_NPC, 0xcaa472, 0x335577, new Item.Properties()));

    // ---- block items ----
    public static final DeferredItem<net.minecraft.world.item.BlockItem> QUEST_BOARD = ITEMS.registerSimpleBlockItem("quest_board", ModBlocks.QUEST_BOARD);

    private ModItems() {}
}
