package adris.altoclef.eventbus.events.multiplayer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
/*
 * Event for when an item is used.
 */
public class ProjectileEvent {

    public ProjectileEntity entity;
    public boolean sticked;
    /*
    release is True when item is released, otherwise event fires when started using item
     */
    public ProjectileEvent(ProjectileEntity entity, boolean sticked) {
        this.entity = entity;
        this.sticked = sticked;
    }
}

