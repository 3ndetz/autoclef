package adris.altoclef.eventbus.events;

//import adris.altoclef.Debug;
import net.minecraft.entity.Entity;

public class AnimEvent {
    public Entity _entity;

    public AnimType _type;

    public AnimEvent(Entity entity, AnimType animType){
        // Debug.logMessage("Got animation "+ animType.toString());
        _entity = entity;
        _type = animType;
    }
    public AnimEvent(Entity entity, int animType){
        this(entity, AnimType.values()[animType]);
    }
}
