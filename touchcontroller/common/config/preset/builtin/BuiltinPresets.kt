package top.fifthlight.touchcontroller.common.config.preset.builtin

object BuiltinPresets {
    private fun generate(key: BuiltInPresetKey) {
        val layers = BuiltinLayers.Companion[textureSet]
        val sprintButton = when (sprintButtonLocation) {
            SprintButtonLocation.NONE -> null
            SprintButtonLocation.RIGHT_TOP -> layers.sprintRightTopButton
            SprintButtonLocation.RIGHT -> layers.sprintRightButton
        }
        return LayoutPreset(
            name = "Built-in preset",
            controlInfo = PresetControlInfo(
                splitControls = controlStyle is ControlStyle.SplitControls,
                disableCrosshair = controlStyle !is ControlStyle.SplitControls,
                disableTouchGesture = controlStyle is ControlStyle.SplitControls && controlStyle.buttonInteraction,
            ),
            layout = controllerLayoutOf(
                if (useVanillaChat) {
                    layers.vanillaChatControlLayer
                } else {
                    layers.controlLayer
                },
                layers.interactionLayer.takeIf { controlStyle is ControlStyle.SplitControls && controlStyle.buttonInteraction },
                layers.normalLayer.getByKey(this) + sprintButton,
                layers.swimmingLayer.getByKey(this),
                layers.flyingLayer.getByKey(this),
                layers.onBoatLayer.getByKey(this),
                layers.onMinecartLayer.getByKey(this),
                layers.ridingOnEntityLayer.getByKey(this),
            )
        ).mapWidgets { widget ->
            when (widget) {
                is CustomWidget -> {
                    if ((widget.normalTexture as? ButtonTexture.Fixed)?.texture?.textureItem == TextureSet.TextureKey.Inventory) {
                        return@mapWidgets widget
                    }

                    fun scaleTexture(scale: Float, texture: ButtonTexture) = when (texture) {
                        is ButtonTexture.Fixed -> texture.copy(scale = texture.scale * scale)
                        else -> texture
                    }

                    fun scaleActiveTexture(scale: Float, texture: ButtonActiveTexture) = when (texture) {
                        is ButtonActiveTexture.Texture -> ButtonActiveTexture.Texture(
                            scaleTexture(
                                scale,
                                texture.texture
                            )
                        )

                        else -> texture
                    }
                    widget.copy(
                        normalTexture = scaleTexture(scale, widget.normalTexture),
                        activeTexture = scaleActiveTexture(scale, widget.activeTexture),
                    )
                }

                is DPad -> run {
                    val isClassic =
                        textureSet == TextureSet.TextureSetKey.CLASSIC || textureSet == TextureSet.TextureSetKey.CLASSIC_EXTENSION
                    widget.copy(
                        size = widget.size * scale,
                        textureSet = textureSet,
                        padding = if (isClassic) 4 else -1,
                        showBackwardButton = !isClassic,
                    )
                }

                is Joystick -> widget.copy(
                    size = widget.size * scale,
                    stickSize = widget.stickSize * scale,
                    textureSet = textureSet,
                )

                is BoatButton -> widget.copy(
                    size = widget.size * scale,
                    textureSet = textureSet,
                )
            }.cloneBase(
                opacity = opacity,
                offset = (widget.offset.toOffset() * scale).toIntOffset(),
            )
        }
    }
}