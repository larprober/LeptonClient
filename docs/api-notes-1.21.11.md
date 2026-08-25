# MC 1.21.11 yarn API gotchas (Lepton)

Confirmed by javap against the remapped jar. Use these, not the pre-1.21.9 names.

- Entity.getPos()            -> Entity.getEntityPos()
- InputUtil.isKeyPressed(long, int) -> isKeyPressed(Window, int)
- InputUtil.fromKeyCode(int,int)    -> InputUtil.Type.KEYSYM.createFromCode(int)
- Keyboard.onKey(long, int action, KeyInput)
- Keyboard.onChar(long, CharInput)
- Mouse.onMouseButton(long, MouseInput, int action)
- Mouse.onMouseScroll(long, double, double)
- PlayerInventory.selectedSlot     -> getSelectedSlot() / setSelectedSlot(int)
- ClientPlayerEntity.input         -> net.minecraft.client.input.Input
    .playerInput is a PlayerInput record(forward,backward,left,right,jump,sneak,sprint)
- Screen/Element input:
    mouseClicked(Click, boolean), mouseReleased(Click), mouseDragged(Click,double,double)
    keyPressed(KeyInput), charTyped(CharInput)
    Click record: x(), y(), button()
- Camera.getCameraPos() (not getPos)
- WorldRenderer.render() has NO MatrixStack. Hook:
    WorldRenderer.renderTargetBlockOutline(VertexConsumerProvider$Immediate, MatrixStack, boolean, WorldRenderState)
    inject at HEAD (it returns early when not looking at a block, so TAIL misses frames)
- Line/quad layers: RenderLayers.lines(), linesTranslucent(), debugQuads(),
    debugFilledBox(), debugPoint(), debugTriangleFan()
- Fabric API WorldRenderEvents / WorldRenderContext: REMOVED in this version.
