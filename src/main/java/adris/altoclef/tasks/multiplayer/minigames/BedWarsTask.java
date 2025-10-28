package adris.altoclef.tasks.multiplayer.minigames;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.multiplayer.RejoinEvent;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BedWarsTask extends Task {
    public BedWarsTask(AltoClef mod) {
        // This task is a placeholder for the Bed Wars minigame.
        // It can be extended with specific logic for the game.
        initializeNewGame(mod);
    }

    BlockPos bedPos = null; // TODO: get bed pos
    public int ourColor = -1;
    public String ourColorName = "Unknown";
    private Task _pickupTask;
    
    public boolean virtualResourcesType = true;
    public boolean ownBedDestroyed = false; // if our bed is destroyed, we should not respawn

    // Shop timer fields using TimerGame
    private final TimerReal shopTimer = new TimerReal(7); // shopping process time
    private final TimerReal shopCooldown = new TimerReal(20); // cooldown between shop sessions
    private final TimerReal preShopTimer = new TimerReal(15); // shopping process time + go to villager
    private final TimerReal _teamDetermineCooldown = new TimerReal(10); // re-determine undetermined team cooldown
    private boolean inShop = false;

    public boolean teamDetermined = false;

    protected boolean determineSelfColor(AltoClef mod) {
        // get self color from helmet
        ourColor = getHelmetColor(mod.getPlayer());
        if (ourColor == -1) {
            // Debug.logWarning("Could not determine our team color from helmet. Defaulting to unknown.");
            ourColorName = "Unknown";
            teamDetermined = false;
            return false;
        }
        ourColorName = getClosestColorName(ourColor);
        teamDetermined = true;
        // Debug.logMessage("Our color: " + ourColorName + " (" + Integer.toHexString(ourColor) + ")");
        return true;
    }

    public void initializeNewGame(AltoClef mod) {
        // resetting variables
        // Reset shop timers when starting new game
        shopTimer.forceElapse();
        shopCooldown.forceElapse();
        ourColor = -1;
        _pickupTask = null;
        virtualResourcesType = true; // Reset virtual resources type
        teamDetermined = false;
        bedPos = null;
        inShop = false;
        ownBedDestroyed = false;
        // get our team color and name

        if (determineSelfColor(mod))
            ourBedBlock = BEDWARS_BED_COLORS.get(ourColorName);
        else
            ourBedBlock = null;

        enemyBedBlocks = new ArrayList<>(Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.BED)).toList());
        if (ourBedBlock != null)
            enemyBedBlocks.remove(ourBedBlock);
    }

    private Subscription<RejoinEvent> _rejoinSubscription;

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(ItemHelper.itemsToBlocks(ItemHelper.BED));
        

        _rejoinSubscription = EventBus.subscribe(RejoinEvent.class, evt -> {
            Debug.logMessage("Rejoined game, resetting BedWars task.");
            initializeNewGame(mod);
        });
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

    public int getBalance(AltoClef mod) {
        // for experience bar resource bedwars system

        if (!virtualResourcesType) {
            // get count of items for shop
            // count iron ingots * 4 + gold ingots * 16
            // if count iron is less than 64 or gold less than 64, return 0
            // BAD approach since for virtual resources we should calculate resources for EVERY item
            // with acknowledging type

            int ironCount = mod.getItemStorage().getItemCountInventoryOnly(Items.IRON_INGOT);
            int goldCount = mod.getItemStorage().getItemCountInventoryOnly(Items.GOLD_INGOT);
            if (ironCount < 64 || goldCount < 45) {
                return 0;
            } else {
                return ironCount * 4 + goldCount * 16;
            }
        }

        return mod.getPlayer().experienceLevel;
    }

    public boolean inOurTeam(PlayerEntity player) {
        // firstly lets check by helmet color
        int color = getHelmetColor(player);
        return ourColor == color && color != -1;
    }

    public List<Item> itemsToBuy(AltoClef mod) {
        List<Item> shoplist = new ArrayList<>();

        // Build materials
        // if we have enough blocks, we no need more
        if (mod.getItemStorage().getItemCountInventoryOnly(ItemHelper.WOOL) < 64) {
            shoplist.addAll(List.of(ItemHelper.WOOL));
        }

        // should be hierarchical:
        // Weapons: if we have wooden axe, then we need iron sword, have iron sword, then diamond sword, etc
        // shoplist.add(Items.WOODEN_AXE);
        // shoplist.add(Items.IRON_SWORD);

        if (!mod.getItemStorage().hasItemInventoryOnly(Items.IRON_SWORD)) {
            shoplist.add(Items.IRON_SWORD);
        }
        // if we have diamond sword / diamond axe, we no need weapons more

        // bow
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.BOW)) {
            shoplist.add(Items.BOW);
        }
        if (mod.getItemStorage().getItemCountInventoryOnly(ItemHelper.ARROWS) < 64) {
            shoplist.addAll(List.of(ItemHelper.ARROWS));
        }
        // getItemCountInventoryOnly DOES NOT COUNT ARMOR SLOTS!!!
        // hasItem counts chest slots (in shop!!!)
        // CORRECT CHECK FOR ARMOR!
        if (!StorageHelper.isArmorEquipped(mod, Items.IRON_BOOTS)) {
            shoplist.add(Items.IRON_BOOTS);
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
    List<Block> enemyBedBlocks;
    Block ourBedBlock;
    /**
     * @param mod
     * @return
     */
    @Override
    protected Task onTick(AltoClef mod) {
        if ( mod.getPlayer() == null) {
            return null;
        }

        // Debug.logInternal("t" + mod.getItemStorage().hasItem(Items.IRON_BOOTS));
        boolean inChest = ContainerType.screenHandlerMatches(ContainerType.CHEST);

        // Reset shop state if we're not in a chest anymore
        if (inShop && !inChest) {
            inShop = false;
        }

        if (!teamDetermined) {
            if (_teamDetermineCooldown.elapsed()) {
                determineSelfColor(mod);
                _teamDetermineCooldown.reset();
            }
        }

        if (virtualResourcesType) {
            if (mod.getItemStorage().hasItemInventoryOnly(Items.GOLD_INGOT, Items.IRON_INGOT)) {
                virtualResourcesType = false;
                Debug.logMessage("Virtual resources type disabled, using real resources");
            }
        }
        if (ourBedBlock != null) {
            if (bedPos == null) {
                Optional<BlockPos> bedPosOpt = mod.getBlockTracker().getNearestTracking(ourBedBlock);
                if (bedPosOpt.isPresent()) {
                    Debug.logMessage("Found our bed at " + bedPosOpt.get().toShortString());
                    bedPos = bedPosOpt.get();
                }

            } else {
                if (!ownBedDestroyed) {
                    Optional<BlockPos> bedPosOpt = mod.getBlockTracker().getNearestTracking(ourBedBlock);
                    if (bedPosOpt.isEmpty() && mod.getPlayer().getPos().distanceTo(bedPos.toCenterPos()) < 25) {
                        ownBedDestroyed = true;
                    }
                    Optional<Entity> closestEnemyNearBed = mod.getEntityTracker().getClosestEntity(
                            bedPos.toCenterPos(),
                            toPunk -> isValidEnemy(toPunk) && toPunk.getPos().isInRange(bedPos.toCenterPos(), 15),
                            PlayerEntity.class);
                    if (closestEnemyNearBed.isPresent()) {
                        Entity enemyBed = closestEnemyNearBed.get();
                        setDebugState("PROTECTING BED FROM " + enemyBed.getName().getString());
                        return new KillPlayerTask(enemyBed.getName().getString());
                    }
                }
                // Enemy beds = all beds - our bed
            }
        }

        Optional<BlockPos> enemyBedPosOpt = mod.getBlockTracker()
                .getNearestTracking(mod.getPlayer().getPos(),
                        to -> to.isWithinDistance(mod.getPlayer().getBlockPos(), 10),
                        enemyBedBlocks.toArray(Block[]::new));
        if (enemyBedPosOpt.isPresent()) {
            BlockPos enemyBedPos = enemyBedPosOpt.get();
            setDebugState("Destroying enemy bed at " + enemyBedPos.toShortString());
            return new DestroyBlockTask(enemyBedPos);
        }

        // we in chest
        if (inChest) {
            setDebugState("shopping");

            if (!inShop && !preShopTimer.elapsed()) {
                inShop = true;
                shopTimer.reset();
            }
            
            // Check if shop timeout reached OR maximum shop time elapsed
            if (shopTimer.elapsed() || preShopTimer.elapsed()) {
                inShop = false;
                shopCooldown.reset();
                StorageHelper.closeScreen();
                return null;
            }
            
            // return new LootContainerTask(new BlockPos(0,0,0), itemsToBuy(mod));
            //StorageHelper.closeScreen();

            // can be category in shop...
            // Slot slot = ItemHelper.getCustomItemSlot(mod, itemsToBuy(mod).toArray(Item[]::new));
            
            // general category, skipping categories
            List<Item> toBuy = itemsToBuy(mod);
            if (!toBuy.isEmpty()) {
                Slot slot = getSlotShopBW(mod, false, toBuy.toArray(Item[]::new));
                if (slot != null) {
                    mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                    return null;
                } else {
                    // shop finished!
                    shopCooldown.reset();
                }
            } else {
                shopCooldown.reset();
                // shop finished!
            }
        }

        Optional<Entity> closestEnemy = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(),
                toPunk -> isValidEnemy(toPunk)
                , PlayerEntity.class);
        if (closestEnemy.isPresent()) {
            Entity enemy = closestEnemy.get();
            double range = mod.getPlayer().getPos().distanceTo(enemy.getPos());
            if (range <= 20) {
                setDebugState("Attacking enemy: " + enemy.getName().getString());
                return new KillPlayerTask(enemy.getName().getString());
            } else {
                boolean preferBow = ShootArrowSimpleProjectileTask.canUseRanged(mod, enemy) &&
                        mod.getItemStorage().getItemCountInventoryOnly(ItemHelper.ARROWS) > 20;
                if (preferBow) {
                    setDebugState("Attacking ranged enemy: " + enemy.getName().getString());
                    return new ShootArrowSimpleProjectileTask(enemy);
                }
            }
        }

        // AutoShop

        // if enough resources & shop timer elapsed *NEED TEST*
        if (!inChest && getBalance(mod) >= 350 && shopCooldown.elapsed()) {
            // 1. Get to villager entity in 3 blocks & ensure line of sight clear

            // max shop time - 15 secs, then shop timeout
            return new DoToClosestEntityTask(
                    entity -> {
                        if (entity.isInRange(mod.getPlayer(), 3)) {
                            if (LookHelper.canHitEntity(mod, entity)) {
                                setDebugState("Found villager: " + entity.getName().getString());
                                LookHelper.smoothLookAt(mod, entity);
                                mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);
                                preShopTimer.reset();
                                // Reset shop state when we start interacting with villager
                                inShop = false;

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
                    setDebugState("Resource collecting");
                    _pickupTask = new PickupDroppedItemTask(new ItemTarget(check), false, false);
                    return _pickupTask;
                }
            }
        }

        return null;
    }

    public static Slot getSlotShopBW(AltoClef mod, boolean chooseCategories, Item... checkItem) {
        Iterable<Slot> slots = Slot.getCurrentScreenSlots();
        if (AltoClef.inGame() && mod.getPlayer() != null && slots != null) {
            for (Slot slot : slots) {
                // specifically for bedwars shop with categories; 0-8 slots are categories
                // should skip
                // int invSlot = slot.getInventorySlot();  // CAN BE NEGATIVE
                int windowSlot = slot.getWindowSlot();

                boolean check;
                if (chooseCategories){
                    check = windowSlot >= 0 && windowSlot < 9; // check only first 9 slots
                } else {
                    check = windowSlot >= 9;
                }
                if (check) {
                    ItemStack itemStack = StorageHelper.getItemStackInSlot(slot);
                    if (itemStack != null && itemStack.getItem() instanceof Item item && !(item.equals(Items.AIR))) {
                        // Debug.logMessage("Checking slot " + windowSlot + " for item " + item.getName().getString());
                        // Debug.logMessage("All items: " + Arrays.asList(checkItem));
                        if (Arrays.asList(checkItem).contains(item)) {
                            return slot;
                        }
                    }
                }
            }

        }
        return null;
    }

    protected boolean isValidEnemy(Entity entity) {
        return entity instanceof PlayerEntity player && !inOurTeam(player)
                && MurderMysteryTask.isValidPlayerMM(player); // TODO untested!
                // && player.isAlive() && !player.isInvisible();
    }

    public boolean isValidTrader(Entity villager, AltoClef mod) {
        return villager.isAlive() && villager.getName() != null && villager.getName()
                .getString().toLowerCase().contains("магазин");
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(ItemHelper.itemsToBlocks(ItemHelper.BED));
        mod.getBehaviour().pop();
        EventBus.unsubscribe(_rejoinSubscription);
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
