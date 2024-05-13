package adris.altoclef.eventbus.events;

import adris.altoclef.Debug;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;

public class EntityDamageEvent {
    //public BlockPos blockPos;
    //public BlockState blockState;
    public String attacker;
    public EntityDamageEvent(DamageSource source, float amount) {
        //this.attacker = attacker;
        //if(source.getAttacker() != null){
        //if(source.getAttacker().isPlayer())
        //    Debug.logMessage("ЧЕЛОВЕК Аттакер>"+source.getName()+'>'+amount);
        //else
        //    Debug.logMessage("ЭНТИТИ Аттакер>"+source.getName()+'>'+amount);}
        //Debug.logMessage("Метод ЭНТИТИ ДАМАЖ вызван!"+source.getName()+'>'+amount);
        //this.blockPos = blockPos;
        //this.blockState = blockState;
        //Debug.logMessage("поставлен блок"+blockPos.toString()+" "+blockState.toString());
    }
}
