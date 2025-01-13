package adris.altoclef.mixins;

import net.minecraft.client.render.MapRenderer.MapTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapTexture.class)
public interface MapTextureAccessor {

    @Accessor("texture")
    NativeImageBackedTexture getNativeImage();
}