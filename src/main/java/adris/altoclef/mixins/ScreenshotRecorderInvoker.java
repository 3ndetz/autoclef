package adris.altoclef.mixins;

import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(ScreenshotRecorder.class)
public interface ScreenshotRecorderInvoker {

    @Invoker("getScreenshotFilename")
    public static File invokeGetScreenshotFileName(File dir) {
        throw new AssertionError();
    }
}
