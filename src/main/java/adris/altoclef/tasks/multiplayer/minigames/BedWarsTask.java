package adris.altoclef.tasks.multiplayer.minigames;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.multiplayer.GestureTask;
import adris.altoclef.tasks.slot.ClickSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class BedWarsTask extends Task {
    public BedWarsTask(AltoClef mod) {
        // This task is a placeholder for the Bed Wars minigame.
        // It can be extended with specific logic for the game.
        ourColor = getHelmetColor(mod.getPlayer());
        ourColorName = getClosestColorName(ourColor);
    }

    BlockPos bedPos = null; // TODO: get bed pos
    public int ourColor = -1;
    public String ourColorName = "";
    private Task _pickupTask;


    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(ItemHelper.itemsToBlocks(ItemHelper.BED));
    }
    // Red Yellow Orange Green Gray Cyan Blue LIGHT_BLUE
    // got from real bedwars helmet colors
    public static final Map<String, Integer> BEDWARS_COLORS = new HashMap<>();
    static {
        BEDWARS_COLORS.put("red", 0xFF0000);
        BEDWARS_COLORS.put("yellow", 0xFFFF00);
        BEDWARS_COLORS.put("orange", 0xFF8000);
        BEDWARS_COLORS.put("green", 0x336600);
        BEDWARS_COLORS.put("gray", 0x858585);
        BEDWARS_COLORS.put("cyan", 0x30D5C8);
        BEDWARS_COLORS.put("blue", 0x004DFF);
        BEDWARS_COLORS.put("light_blue", 0x00BFFF);
        BEDWARS_COLORS.put("pink", 0xF70AA0);
    }

    public static final Map<String, Block> BEDWARS_BED_COLORS = new HashMap<>();
    static {
        BEDWARS_BED_COLORS.put("red", Blocks.RED_BED);
        BEDWARS_BED_COLORS.put("yellow", Blocks.YELLOW_BED);
        BEDWARS_BED_COLORS.put("orange", Blocks.ORANGE_BED);
        BEDWARS_BED_COLORS.put("green", Blocks.GREEN_BED);
        BEDWARS_BED_COLORS.put("gray", Blocks.GRAY_BED);
        BEDWARS_BED_COLORS.put("cyan", Blocks.CYAN_BED);
        BEDWARS_BED_COLORS.put("blue", Blocks.BLUE_BED);
        BEDWARS_BED_COLORS.put("light_blue", Blocks.LIGHT_BLUE_BED);
        BEDWARS_BED_COLORS.put("pink", Blocks.PINK_BED);
    }

    public String getClosestColorName(int rgb) {
        if (rgb == -1) return "Unknown";

        int r1 = ColorHelper.Argb.getRed(rgb);
        int g1 = ColorHelper.Argb.getGreen(rgb);
        int b1 = ColorHelper.Argb.getBlue(rgb);

        String closestColorName = "Unknown";
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : BEDWARS_COLORS.entrySet()) {
            int colorRgb = entry.getValue();
            int r2 = ColorHelper.Argb.getRed(colorRgb);
            int g2 = ColorHelper.Argb.getGreen(colorRgb);
            int b2 = ColorHelper.Argb.getBlue(colorRgb);

            double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));

            if (distance < minDistance) {
                minDistance = distance;
                closestColorName = entry.getKey();
            }
        }
        return closestColorName;
    }

    public int getHelmetColor(PlayerEntity player) {
        // get helmet itemstack
        // get helmet item
        // get helmet attributes -> get color attribute

        for (ItemStack itemStack : player.getArmorItems()) {
            if (itemStack.isOf(Items.LEATHER_HELMET)) {
                // get color from itemstack

                return DyedColorComponent.getColor(itemStack, DyedColorComponent.DEFAULT_COLOR);
                // int r = ColorHelper.Argb.getRed(color);
                // int g = ColorHelper.Argb.getGreen(color);
                // int b = ColorHelper.Argb.getBlue(color);
                // TODO get color names from color components
                // Possible teams:
                // Red, Blue, Green, Yellow, Aqua, Pink, Gray, White
                // Aqua = Light Blue
                // Голубой Красный Жёлтый Оранжевый Зелёный Серый Бирюзовый Синий

                // lower is working approach too
                //DyedColorComponent colorComponent = itemStack.get(DataComponentTypes.DYED_COLOR);
                //if (colorComponent != null) {
                //    color = colorComponent.rgb();
                //    return color;
                //    // do something with the color
                //}
                // If colorComponent is null, the helmet is not dyed.
            }
        }
        return -1;
    }

    public int getMoney(AltoClef mod) {
        // for experience bar resource bedwars system
        return mod.getPlayer().experienceLevel;
    }

    public boolean inOurTeam(PlayerEntity player) {
        // firstly lets check by helmet color
        int color = getHelmetColor(player);
        return ourColor == color && color != -1;
    }

    public List<Item> itemsToBuy(AltoClef mod) {
        List<Item> shoplist = new ArrayList<>();
        // should be hierarchical:
        // Weapons: if we have wooden axe, then we need iron sword, have iron sword, then diamond sword, etc
        shoplist.add(Items.WOODEN_AXE);
        shoplist.add(Items.IRON_SWORD);
        // if we have diamond sword / diamond axe, we no need weapons more

        // bow
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.BOW)) {
            shoplist.add(Items.BOW);
        }
        if (mod.getItemStorage().getItemCountInventoryOnly(ItemHelper.ARROWS) < 64) {
            shoplist.addAll(List.of(ItemHelper.ARROWS));
        }

        // Build materials
        // if we have enough blocks, we no need more
        if (mod.getItemStorage().getItemCountInventoryOnly(ItemHelper.WOOL) < 64) {
            shoplist.addAll(List.of(ItemHelper.WOOL));
        }

        return shoplist;
    }

    public List<Item> lootableItems(AltoClef mod) {
        List<Item> lootable = new ArrayList<>();
        lootable.addAll(itemsToBuy(mod));
        lootable.add(Items.GOLD_INGOT);
        lootable.add(Items.DIAMOND);
        lootable.add(Items.EMERALD);

        lootable.add(Items.GOLDEN_APPLE);
        lootable.add(Items.ENCHANTED_GOLDEN_APPLE);
        lootable.add(Items.BOW);
        lootable.add(Items.ARROW);
        lootable.add(Items.IRON_INGOT);
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.WATER_BUCKET)) {
            lootable.add(Items.WATER_BUCKET);
        }
        return lootable;
    }

    /**
     * @param mod
     * @return
     */
    @Override
    protected Task onTick(AltoClef mod) {
        if ( mod.getPlayer() == null) {
            return null;
        }

        if (bedPos == null) {
            Block ourBedBlock = BEDWARS_BED_COLORS.get(ourColorName);
            if (ourBedBlock != null) {
                Optional<BlockPos> bedPosOpt = mod.getBlockTracker().getNearestTracking(ourBedBlock);
                if (bedPosOpt.isPresent()) {
                    Debug.logMessage("Found our bed at " + bedPosOpt.get().toShortString());
                    bedPos = bedPosOpt.get();
                }
            }
        } else {
            Optional<Entity> closestEnemyBed = mod.getEntityTracker().getClosestEntity(
                    bedPos.toCenterPos(),
                    toPunk -> !inOurTeam((PlayerEntity) toPunk) && toPunk.getPos().isInRange(bedPos.toCenterPos(), 10),
                    PlayerEntity.class);
            if (closestEnemyBed.isPresent()) {
                Entity enemyBed = closestEnemyBed.get();
                setDebugState("PROTECTING BED FROM " + enemyBed.getName().getString());
                return new KillPlayerTask(enemyBed.getName().getString());
            }
        }


        // we in chest
        if (ContainerType.screenHandlerMatches(ContainerType.CHEST)) {
            // todo add shoptimer
            return new LootContainerTask(new BlockPos(0,0,0), itemsToBuy(mod));
            //StorageHelper.closeScreen();


            //Slot slot = ItemHelper.getCustomItemSlot(mod, ArrayUtils.addAll(MinigamesTitles));
            //
            //if (slot != null) {
                //    mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                //}
        }

        Optional<Entity> closestEnemy = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> !inOurTeam((PlayerEntity) toPunk), PlayerEntity.class);
        if (closestEnemy.isPresent()) {
            Entity enemy = closestEnemy.get();
            if (enemy.getPos().isInRange(mod.getPlayer().getPos(), 25)) {
                setDebugState("Attacking enemy: " + enemy.getName().getString());
                return new KillPlayerTask(enemy.getName().getString());
            }
        }

        // AutoShop

        // if enough resources & shop timer elapsed

        if (getMoney(mod) >= 500) {
            // 1. Get to villager entity in 3 blocks & ensure line of sight clear

            // max shop time - 15 secs, then shop timeout
            return new DoToClosestEntityTask(
                    entity -> {
                        if (entity.isInRange(mod.getPlayer(), 3)) {
                            if (LookHelper.canHitEntity(mod, entity)) {
                                setDebugState("Found villager: " + entity.getName().getString());
                                LookHelper.smoothLookAt(mod, entity);
                                mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);

                                return null; // new ClickSlotTask();
                            } else {
                                // need to get closer & wander from random positions
                                return new GetCloseToBlockTask(entity.getBlockPos());
                            }
                        } else {
                            return new GetToEntityTask(entity, 2);
                        }
                    },
                    entity -> isValidTrader(entity, mod),
                    VillagerEntity.class

            );
        }

        for (Item check : lootableItems(mod)) {
            if (mod.getEntityTracker().itemDropped(check)) {

                Optional<ItemEntity> closestEnt = mod.getEntityTracker().getClosestItemDrop(
                        ent -> mod.getEntityTracker().isEntityReachable(ent)
                                && mod.getPlayer().getPos().isInRange(ent.getEyePos(), 400),check);
                //
                if(closestEnt.isPresent()) {
                    setDebugState("Сбор ресурсов");
                    _pickupTask = new PickupDroppedItemTask(new ItemTarget(check), false, false);
                    return _pickupTask;
                }
            }
        }

        return null;
    }
    public boolean isValidTrader(Entity villager, AltoClef mod) {
        return villager.isAlive() && villager.getName() != null && villager.getName()
                .getString().toLowerCase().contains("магазин");
    }
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(ItemHelper.itemsToBlocks(ItemHelper.BED));
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BedWarsTask;
    }

    @Override
    protected String toDebugString() {
        return "Playing Bed Wars"
                + ((ourColorName.isBlank()) ? "" : ": team " + ourColorName);
    }
}
