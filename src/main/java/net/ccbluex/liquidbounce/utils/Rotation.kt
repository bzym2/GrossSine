package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.utils.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Rotations
 */
data class Rotation(var yaw: Float, var pitch: Float) {

    /**
     * Set rotations to [player]
     */
        fun toPlayer(player: EntityPlayer) {
        if ((yaw.isNaN() || pitch.isNaN()))
            return

        player.rotationYaw = yaw
        player.rotationPitch = pitch
    }

    /**
     * Patch gcd exploit in aim
     *
     * @see net.minecraft.client.renderer.EntityRenderer.updateCameraAndRender
     */
    fun fixedSensitivity(sensitivity: Float = MinecraftInstance.mc.gameSettings.mouseSensitivity): Rotation {
        // Previous implementation essentially floored the subtraction.
        // This way it returns rotations closer to the original.

        // Only calculate GCD once
        val gcd = getFixedAngleDelta(sensitivity)

        yaw = getFixedSensitivityAngle(yaw, serverRotation.yaw, gcd)
        pitch = getFixedSensitivityAngle(pitch, serverRotation.pitch, gcd)

        return this.withLimitedPitch()
    }
    private fun withLimitedPitch(value: Float = 90f): Rotation {
        pitch = pitch.coerceIn(-value, value)
        return this
    }
    /**
     * Returns the smallest angle difference possible with a specific sensitivity ("gcd")
     */
    private fun getFixedAngleDelta(sensitivity: Float = MinecraftInstance.mc.gameSettings.mouseSensitivity) =
        (sensitivity * 0.6f + 0.2f).pow(3) * 1.2f

    /**
     * Returns angle that is legitimately accomplishable with player's current sensitivity
     */
    private fun getFixedSensitivityAngle(targetAngle: Float, startAngle: Float = 0f, gcd: Float = getFixedAngleDelta()) =
        startAngle + ((targetAngle - startAngle) / gcd).roundToInt() * gcd

    /**
     * Apply strafe to player
     *
     * @author bestnub
     */
    fun applyStrafeToPlayer(event: StrafeEvent) {
        val player = MinecraftInstance.mc.thePlayer
        val dif = ((MathHelper.wrapAngleTo180_float(player.rotationYaw - this.yaw -
                23.5f - 135) +
                180) / 45).toInt()

        val yaw = this.yaw

        val strafe = event.strafe
        val forward = event.forward
        val friction = event.friction

        var calcForward = 0f
        var calcStrafe = 0f

        when (dif) {
            0 -> {
                calcForward = forward
                calcStrafe = strafe
            }
            1 -> {
                calcForward += forward
                calcStrafe -= forward
                calcForward += strafe
                calcStrafe += strafe
            }
            2 -> {
                calcForward = strafe
                calcStrafe = -forward
            }
            3 -> {
                calcForward -= forward
                calcStrafe -= forward
                calcForward += strafe
                calcStrafe -= strafe
            }
            4 -> {
                calcForward = -forward
                calcStrafe = -strafe
            }
            5 -> {
                calcForward -= forward
                calcStrafe += forward
                calcForward -= strafe
                calcStrafe -= strafe
            }
            6 -> {
                calcForward = -strafe
                calcStrafe = forward
            }
            7 -> {
                calcForward += forward
                calcStrafe += forward
                calcForward -= strafe
                calcStrafe += strafe
            }
        }

        if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
            calcForward *= 0.5f
        }

        if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
            calcStrafe *= 0.5f
        }

        var d = calcStrafe * calcStrafe + calcForward * calcForward

        if (d >= 1.0E-4f) {
            d = MathHelper.sqrt_float(d)
            if (d < 1.0f) d = 1.0f
            d = friction / d
            calcStrafe *= d
            calcForward *= d
            val yawSin = MathHelper.sin((yaw * Math.PI / 180f).toFloat())
            val yawCos = MathHelper.cos((yaw * Math.PI / 180f).toFloat())
            player.motionX += calcStrafe * yawCos - calcForward * yawSin.toDouble()
            player.motionZ += calcForward * yawCos + calcStrafe * yawSin.toDouble()
        }
    }
    fun toDirection(): Vec3 {
        val f: Float = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
        val f1: Float = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
        val f2: Float = -MathHelper.cos(-pitch * 0.017453292f)
        val f3: Float = MathHelper.sin(-pitch * 0.017453292f)
        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }
    override fun toString(): String {
        return "Rotation(yaw=$yaw, pitch=$pitch)"
    }
}

/**
 * Rotation with vector
 */
data class VecRotation(val vec: Vec3, val rotation: Rotation)

/**
 * Rotation with place info
 */
data class PlaceRotation(val placeInfo: PlaceInfo, val rotation: Rotation)
