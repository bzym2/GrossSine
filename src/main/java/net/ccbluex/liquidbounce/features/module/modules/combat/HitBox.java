
package net.ccbluex.liquidbounce.features.module.modules.combat;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import org.jetbrains.annotations.Nullable;

@ModuleInfo(name = "HitBox", category = ModuleCategory.COMBAT)
public class HitBox extends Module {
    public static FloatValue HitboxMax = new FloatValue("Max", 3.5f, 3f, 7f);
    public static FloatValue HitboxMin = new FloatValue("Min", 3.5f, 3f, 7f);


    public static double getSize() {
        double min = Math.min(HitboxMin.getValue(), HitboxMax.getValue());
        double max = Math.max(HitboxMin.getValue(), HitboxMax.getValue());
        return Math.random() * (max - min) + min;
    }
    @Nullable
    @Override
    public String getTag() {
        return HitboxMax.get() + "-" + HitboxMin.get();
    }
}
