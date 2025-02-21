package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import com.sun.org.apache.xpath.internal.operations.Bool
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.font.Fonts.font40
import net.ccbluex.liquidbounce.ui.font.Fonts.minecraftFont
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.awt.Color
@ModuleInfo(name = "ItemsDetector", category = ModuleCategory.OTHER)
object ItemsDetector {

    private val mode = ListValue("Mode", arrayOf("Murder", "Skywars"), "Skywars")
    private val showText = BoolValue("ShowText", true)
    private val chatValue = BoolValue("Chat", true)

    private var detectedPlayer: EntityPlayer? = null

    // Murder模式物品列表
    private val murderItems = mutableListOf(
        267,  // Items.iron_sword
        272,  // Items.stone_sword
        256,  // Items.iron_shovel
        280,  // Items.stick
        271,  // Items.wooden_axe
        268,  // Items.wooden_sword
        273,  // Items.stone_shovel
        369,  // Items.blaze_rod
        277,  // Items.diamond_shovel
        359   // Items.shears
    )

    // Skywars模式物品列表
    private val skywarsItems = mutableListOf(
        294,  // Items.splash_potion
        341,  // Items.arrow
        346   // Items.ender_pearl
    )


    @EventTarget
    fun onWorld(event: WorldEvent) {
        detectedPlayer = null
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            for (player in mc.theWorld.playerEntities) {
                if (mc.thePlayer.ticksExisted % 2 == 0) return

                val playerItem = player.heldItem
                if (playerItem != null && (
                        (mode.get() == "Murder" && isItemInList(playerItem, murderItems)) ||
                        (mode.get() == "Skywars" && isItemInList(playerItem, skywarsItems) || isEnchantedGoldenApple(playerItem))
                )) {
                    if (detectedPlayer == null || detectedPlayer != player) {
                        if (chatValue.get()) {
                            val itemName = playerItem.displayName
                            Chat.print("§e${player.name}§r has item $itemName")
                        }
                        detectedPlayer = player
                    }
                }
            }
        }
    }

    // 判断物品是否在物品列表中
    private fun isItemInList(itemStack: ItemStack, itemList: List<Int>): Boolean {
        return itemList.contains(Item.getIdFromItem(itemStack.item))
    }

    // 判断物品是否是附魔金苹果
    private fun isEnchantedGoldenApple(itemStack: ItemStack): Boolean {
        // 检查物品是否是金苹果并且其元数据是否为1（附魔金苹果）
        return itemStack.item === Items.golden_apple && itemStack.metadata == 1
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sc = ScaledResolution(mc)
        if (showText.get()) {
            font40.drawString(
                if (detectedPlayer != null) "${detectedPlayer?.name} has item ${detectedPlayer?.heldItem?.displayName}" else "No player detected",
                sc.scaledWidth / 2F - minecraftFont.getStringWidth(
                    if (detectedPlayer != null) "${detectedPlayer?.name} has item ${detectedPlayer?.heldItem?.displayName}"
                    else "No player detected"
                ) / 2F,
                66.5F,
                Color(255, 255, 255).rgb,
                true
            )
        }
    }
}
