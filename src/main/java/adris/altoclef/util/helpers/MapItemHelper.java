package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.ImageComparer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.client.render.MapRenderer;

import adris.altoclef.mixins.MapRendererInvoker;
import adris.altoclef.mixins.MapTextureAccessor;
import adris.altoclef.mixins.ScreenshotRecorderInvoker;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class MapItemHelper {
    public static String saveNonExistMapToDataset(AltoClef mod){
        return saveNonExistMapToDataset(mod,false);
    }
    public static String saveNonExistMapToDataset(AltoClef mod, boolean neural_solve){
        ItemStack item = ItemHelper.getHandItem(mod);
        if (item != null){
            return saveNonExistMapToDataset(item, mod, neural_solve);
        }
        return "";
    }
    public static String saveNonExistMapToDataset(ItemStack stack, AltoClef mod) {
        return saveNonExistMapToDataset(stack, mod, false);
    }
    public static String saveNonExistMapToDataset(ItemStack stack, AltoClef mod, boolean neural_solve) {
        //Debug.logMessage("itemstack"+stack.getName()+stack.isOf(Items.FILLED_MAP));
        if(stack.isOf(Items.FILLED_MAP)){

            Integer mapId = FilledMapItem.getMapId(stack);
            MapState mapState = FilledMapItem.getMapState(mapId, mod.getWorld());
            if (mapState != null){
                String saveResult = saveMapFile(mod, mapId,mapState, neural_solve);
                //Debug.logMessage(""+saveResult);
                return saveResult;
            }
        }
        return "";
    }

    public static String saveMapFile(AltoClef mod, Integer mapId, MapState mapState, boolean neural_solve){
        File screensDir = new File(MinecraftClient.getInstance().runDirectory,"map_screenshots");
        screensDir.mkdir();
        MapRenderer.MapTexture map_texture = ((MapRendererInvoker)MinecraftClient.getInstance().gameRenderer.getMapRenderer()).invokeGetMapTexture(mapId, mapState);
        //Debug.logMessage("map texture"+map_texture);
        File screenshot = ScreenshotRecorderInvoker.invokeGetScreenshotFileName(screensDir);
        //Debug.logMessage("screenshotFile"+screenshot.getAbsolutePath()+" choo "+screenshot.getName());
        NativeImage img = ((MapTextureAccessor)map_texture).getNativeImage().getImage();
        String check_result = "";
        try {
            byte[] bytes_img = img.getBytes();


            check_result = ImageComparer.checkBytesImageInDataset(bytes_img);

            if (check_result == "") {

                if(neural_solve){
                    mod.getInfoSender().onCaptchaSolveRequest(bytes_img);
                }
                else {
                    saveImageFile(bytes_img, screenshot);
                }
            }else{
                if(check_result.equals("black.png")){
                    Debug.logMessage("[CAPTCHA NOT LOADED] BLACK FILE!!! = '" + check_result + "'");
                    check_result = "";

                } else if(check_result.length()>9){ //"44235.png"
                    Debug.logMessage("[CAPTCHA NOT TOO LONG FILENAME!!! = "+check_result);
                    check_result = "";

                }
                else {
                    Debug.logMessage("[CAPTCHA FOUND] FILE EXISTS = '" + check_result + "'");
                }
                return check_result;
            }
        }catch (Exception e){
            e.printStackTrace();
            Debug.logMessage("ERR WHEN CHECHING CAPTCHA!!!!!"+e.toString());
        }
        return "";
    }
    public static void saveImageFile(byte[] bytes_img, File screenshot){
        Util.getIoWorkerExecutor().execute(() -> {
            try {
                BufferedImage buffered_img = ImageComparer.byte2BufferedImage(bytes_img);
                ImageIO.write(buffered_img,"png",screenshot);
                Debug.logMessage("[CAPTCHA] IMAGE SAVED! Name="+screenshot.getName());
                //((MapTextureAccessor) map_texture).getNativeImage().getImage().writeTo(screenshot);
                //Text text = (new LiteralText(screenshot.getName())).formatted(Formatting.UNDERLINE, Formatting.GREEN).styled((style) -> {
                //    return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshot.getAbsolutePath()));
                //});
                //Debug.logMessage("IMAGE SAVED!");
                //MinecraftClient.getInstance().player.sendMessage(new TranslatableText("map_saver.success", "Map #" + mapId, text), false);
//
            } catch (IOException e) {
                e.printStackTrace();
            }
//
        });
    }
}
