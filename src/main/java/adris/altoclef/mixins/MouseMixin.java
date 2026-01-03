package adris.altoclef.mixins;

import adris.altoclef.util.agent.AgentInputBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;
    @Shadow private double x; // Используется ванилью
    @Shadow private double y; // Используется ванилью

    // Виртуальные координаты для Инвентаря
    @Unique private double virtualX = 0;
    @Unique private double virtualY = 0;
    @Unique private boolean firstInit = true;

    // 1. БЛОКИРУЕМ РЕАЛЬНУЮ МЫШЬ (Без изменений)
    // @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    // private void blockRealMouse(long window, double x, double y, CallbackInfo ci) {
    //     if (AgentInputBridge.isAgentInputActive) {
    //         if (firstInit) {
    //             virtualX = x;
    //             virtualY = y;
    //             firstInit = false;
    //         }
    //         ci.cancel(); 
    //     }
    // }

    // 2. ПОДМЕНЯЕМ КООРДИНАТЫ ДЛЯ ИНВЕНТАРЯ (Без изменений)
    // @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    // private void overrideGetX(CallbackInfoReturnable<Double> cir) {
    //     if (AgentInputBridge.isAgentInputActive) {
    //         cir.setReturnValue(virtualX);
    //     }
    // }

    // @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    // private void overrideGetY(CallbackInfoReturnable<Double> cir) {
    //     if (AgentInputBridge.isAgentInputActive) {
    //         cir.setReturnValue(virtualY);
    //     }
    // }

    // 3. ОСНОВНАЯ ЛОГИКА (ВМЕСТО REDIRECT)
    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouseHead(CallbackInfo ci) {
        if (AgentInputBridge.isAgentInputActive) {
            
            // Забираем дельты
            double[] deltas = AgentInputBridge.consumeDeltas();
            double dx = deltas[0];
            double dy = deltas[1];

            // --- ЛОГИКА ИНВЕНТАРЯ (Курсор) ---
            virtualX += dx;
            virtualY += dy;

            // Clamp (ограничение экраном)
            Window window = this.client.getWindow();
            virtualX = Math.max(0, Math.min(window.getWidth(), virtualX));
            virtualY = Math.max(0, Math.min(window.getHeight(), virtualY));

            // Обновляем и ванильные координаты, на всякий случай
            this.x = virtualX;
            this.y = virtualY;

            // --- ЛОГИКА КАМЕРЫ (Вращение) ---
            // Если мы в игре (нет открытого экрана), вращаем игрока
            if (this.client.player != null && this.client.currentScreen == null) {
                // Применяем вращение напрямую. 
                // Это работает даже если окно свернуто (Background mode)
                // dx/dy здесь уже должны быть умножены на sensitivity в AgentActionButtons
                this.client.player.changeLookDirection(dx, dy);
            }
            
            // ВАЖНО: Обнуляем ванильные дельты, чтобы игра не применила вращение второй раз
            // (если вдруг окно в фокусе и ванильная логика сработает следом)
            this.cursorDeltaX = 0;
            this.cursorDeltaY = 0;
            AgentInputBridge.afterTick();
        }
    }
}