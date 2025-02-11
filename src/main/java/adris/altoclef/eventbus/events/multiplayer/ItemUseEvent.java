package adris.altoclef.eventbus.events.multiplayer;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
/*
    * Event for when an item is used.
 */
public class ItemUseEvent {

    public Entity entity;
    public boolean released;
    /*
    release is True when item is released, otherwise event fires when started using item
     */
    public ItemUseEvent(Entity entity, boolean released) {
        this.entity = entity;
        this.released = released;
    }
}

