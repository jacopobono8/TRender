package dev.tr7zw.trender.gui.client;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

//#if MC < 12106
//$$import com.mojang.blaze3d.vertex.PoseStack;
//$$import dev.tr7zw.trender.gui.impl.mixin.client.DrawContextAccessor;
//$$import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//#endif

import it.unimi.dsi.fastutil.ints.IntIterator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#endif
//#if MC < 12102
//$$ import com.mojang.blaze3d.vertex.Tesselator;
//#endif
//#if MC >= 12000
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
//#if MC <= 12105
//$$ import net.minecraft.client.renderer.RenderType;
//#endif
//#else
//$$ import net.minecraft.client.gui.screens.Screen;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import net.minecraft.client.renderer.GameRenderer;
//$$ import net.minecraft.client.gui.GuiComponent;
//#endif
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//#if MC < 12000
//$$ import net.minecraft.client.renderer.MultiBufferSource;
//$$ import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//#endif
//#if MC < 12105
//$$ import com.mojang.blaze3d.vertex.BufferUploader;
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//$$ import dev.tr7zw.trender.gui.impl.client.VanillaShaders;
//$$ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//$$ import com.mojang.blaze3d.vertex.Tesselator;
//$$ import com.mojang.blaze3d.vertex.VertexFormat;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import com.mojang.blaze3d.vertex.BufferBuilder;
//#if MC >= 11904
//$$ import org.joml.Matrix4f;
//#else
//$$ import com.mojang.math.Matrix4f;
//#endif
//#if MC <= 11605
//$$ import org.lwjgl.opengl.GL11;
//#endif
//#endif

@AllArgsConstructor
public class RenderContext implements PoseStackHelper {

    @SuppressWarnings("unused")
    private final static Minecraft minecraft = Minecraft.getInstance();

    //#if MC >= 12000
    @Getter
    private final GuiGraphics guiGraphics;
    //#else
    //$$ private final Screen screen;
    //$$ private final PoseStack pose;
    //#endif

    //#if MC >= 12106
    public org.joml.Matrix3x2fStack getPose() {
        //#else
        //$$  public com.mojang.blaze3d.vertex.PoseStack getPose() {
        //#endif
        //#if MC >= 12000
        return guiGraphics.pose();
        //#else
        //$$ return pose;
        //#endif
    }

    public void drawSpecial(Consumer<MultiBufferSource> consumer) {
        //#if MC >= 12106
        consumer.accept(Minecraft.getInstance().renderBuffers().bufferSource());
        //#elseif MC >= 12102
        //$$ guiGraphics.drawSpecial(consumer);
        //#elseif MC >= 12100
        //$$ consumer.accept(guiGraphics.bufferSource());
        //$$ guiGraphics.bufferSource().endBatch();
        //#else
        //$$ net.minecraft.client.renderer.MultiBufferSource.BufferSource bs = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        //$$ consumer.accept(bs);
        //$$ bs.endBatch();
        //#endif
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height,
            int textureWidth, int textureHeight) {
        //#if MC >= 12106
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, atlasLocation, x, y, uOffset, vOffset, width, height,
                textureWidth, textureHeight);
        //#elseif MC >= 12102
        //$$ guiGraphics.blit(t -> RenderType.guiTextured(t), atlasLocation, x, y, uOffset, vOffset, width, height,
        //$$         textureWidth, textureHeight);
        //#elseif MC >= 12000
        //$$ guiGraphics.blit(atlasLocation, x, y, 0, uOffset, vOffset, width, height, textureWidth, textureHeight);
        //#elseif MC > 11700
        //$$ RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //$$ RenderSystem.setShaderTexture(0, atlasLocation);
        //$$ screen.blit(pose, x, y, 0, uOffset, vOffset, width, height, textureWidth, textureHeight);
        //#elseif MC >= 11800
        //$$ minecraft.getTextureManager().bind(atlasLocation);
        //$$ GuiComponent.blit(pose, x, y, 0, uOffset, vOffset, width, height, textureWidth, textureHeight);
        //#else
        //$$ minecraft.getTextureManager().bind(atlasLocation);
        //$$ GuiComponent.blit(pose, x, y, 0, uOffset, vOffset, width, height, textureHeight, textureWidth);
        //#endif
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        blit(atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, 64, 64);
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, int blitOffset, float uOffset, float vOffset,
            int uWidth, int vHeight, int textureWidth, int textureHeight) {
        //#if MC >= 12106
        //TODO blitOffset?
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight,
                textureWidth, textureHeight);
        //#elseif MC >= 12102
        //$$//TODO blitOffset
        //$$guiGraphics.blit(t -> RenderType.guiTextured(t), atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight,
        //$$        textureWidth, textureHeight);
        //#elseif MC >= 12000
        //$$ guiGraphics.blit(atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth,
        //$$        textureHeight);
        //#elseif MC > 11700
        //$$ RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //$$ RenderSystem.setShaderTexture(0, atlasLocation);
        //$$ GuiComponent.blit(pose, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
        //#else
        //$$ minecraft.getTextureManager().bind(atlasLocation);
        //$$ GuiComponent.blit(pose, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
        //#endif
    }

    public void blitSprite(ResourceLocation texture, int x, int y, int width, int height, int sliceSide, int sliceTop,
            int txtWidth, int txtHeight) {
        //#if MC >= 12106
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height);
        //#elseif MC >= 12102
        //$$ guiGraphics.blitSprite(t -> RenderType.guiTextured(t), texture, x, y, width, height);
        //#elseif MC >= 12002
        //$$ guiGraphics.blitSprite(texture, x, y, width, height);
        //#else
        //$$ blitNineSliced(texture, x, y, width, height, sliceSide, sliceTop, sliceSide, sliceTop, txtWidth, txtHeight, txtWidth, txtHeight);
        //#endif
    }

    public void blitSpriteLegacy(ResourceLocation texture, int x, int y, int width, int height, int sliceSide,
            int sliceTop, int txtWidth, int txtHeight) {
        blitNineSliced(texture, x, y, width, height, sliceSide, sliceTop, sliceSide, sliceTop, txtWidth, txtHeight,
                txtWidth, txtHeight);
    }

    private void blitNineSliced(ResourceLocation atlasLocation, int x, int y, int width, int height, int leftSliceWidth,
            int topSliceHeight, int rightSliceWidth, int bottomSliceHeight, int uWidth, int vHeight, int textureWidth,
            int textureHeight) {
        leftSliceWidth = Math.min(leftSliceWidth, width / 2);
        rightSliceWidth = Math.min(rightSliceWidth, width / 2);
        topSliceHeight = Math.min(topSliceHeight, height / 2);
        bottomSliceHeight = Math.min(bottomSliceHeight, height / 2);
        if (width == uWidth && height == vHeight) {
            this.blit(atlasLocation, x, y, 0, 0, width, height, textureWidth, textureHeight);
        } else if (height == vHeight) {
            this.blit(atlasLocation, x, y, 0, 0, leftSliceWidth, height, textureWidth, textureHeight);
            this.blitRepeating(atlasLocation, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth, height,
                    leftSliceWidth, 0, uWidth - rightSliceWidth - leftSliceWidth, vHeight, textureWidth, textureHeight);
            this.blit(atlasLocation, x + width - rightSliceWidth, y, uWidth - rightSliceWidth, 0, rightSliceWidth,
                    height, textureWidth, textureHeight);
        } else if (width == uWidth) {
            this.blit(atlasLocation, x, y, 0, 0, width, topSliceHeight, textureWidth, textureHeight);
            this.blitRepeating(atlasLocation, x, y + topSliceHeight, width, height - bottomSliceHeight - topSliceHeight,
                    0, topSliceHeight, uWidth, vHeight - bottomSliceHeight - topSliceHeight, textureWidth,
                    textureHeight);
            this.blit(atlasLocation, x, y + height - bottomSliceHeight, 0, vHeight - bottomSliceHeight, width,
                    bottomSliceHeight, textureWidth, textureHeight);
        } else {
            this.blit(atlasLocation, x, y, 0, 0, leftSliceWidth, topSliceHeight, textureWidth, textureHeight);
            this.blitRepeating(atlasLocation, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth,
                    topSliceHeight, leftSliceWidth, 0, uWidth - rightSliceWidth - leftSliceWidth, topSliceHeight,
                    textureWidth, textureHeight);
            this.blit(atlasLocation, x + width - rightSliceWidth, y, uWidth - rightSliceWidth, 0, rightSliceWidth,
                    topSliceHeight, textureWidth, textureHeight);
            this.blit(atlasLocation, x, y + height - bottomSliceHeight, 0, vHeight - bottomSliceHeight, leftSliceWidth,
                    bottomSliceHeight, textureWidth, textureHeight);
            this.blitRepeating(atlasLocation, x + leftSliceWidth, y + height - bottomSliceHeight,
                    width - rightSliceWidth - leftSliceWidth, bottomSliceHeight, leftSliceWidth,
                    vHeight - bottomSliceHeight, uWidth - rightSliceWidth - leftSliceWidth, bottomSliceHeight,
                    textureWidth, textureHeight);
            this.blit(atlasLocation, x + width - rightSliceWidth, y + height - bottomSliceHeight,
                    uWidth - rightSliceWidth, vHeight - bottomSliceHeight, rightSliceWidth, bottomSliceHeight,
                    textureWidth, textureHeight);
            this.blitRepeating(atlasLocation, x, y + topSliceHeight, leftSliceWidth,
                    height - bottomSliceHeight - topSliceHeight, 0, topSliceHeight, leftSliceWidth,
                    vHeight - bottomSliceHeight - topSliceHeight, textureWidth, textureHeight);
            this.blitRepeating(atlasLocation, x + leftSliceWidth, y + topSliceHeight,
                    width - rightSliceWidth - leftSliceWidth, height - bottomSliceHeight - topSliceHeight,
                    leftSliceWidth, topSliceHeight, uWidth - rightSliceWidth - leftSliceWidth,
                    vHeight - bottomSliceHeight - topSliceHeight, textureWidth, textureHeight);
            this.blitRepeating(atlasLocation, x + width - rightSliceWidth, y + topSliceHeight, leftSliceWidth,
                    height - bottomSliceHeight - topSliceHeight, uWidth - rightSliceWidth, topSliceHeight,
                    rightSliceWidth, vHeight - bottomSliceHeight - topSliceHeight, textureWidth, textureHeight);
        }
    }

    private void blitRepeating(ResourceLocation atlasLocation, int x, int y, int width, int height, int uOffset,
            int vOffset, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight) {
        int i = x;

        int j;
        for (IntIterator intIterator = slices(width, sourceWidth); intIterator.hasNext(); i += j) {
            j = intIterator.nextInt();
            int k = (sourceWidth - j) / 2;
            int l = y;

            int m;
            for (IntIterator intIterator2 = slices(height, sourceHeight); intIterator2.hasNext(); l += m) {
                m = intIterator2.nextInt();
                int n = (sourceHeight - m) / 2;
                this.blit(atlasLocation, i, l, uOffset + k, vOffset + n, j, m, textureWidth, textureHeight);
            }
        }
    }

    private static IntIterator slices(int target, int total) {
        if (total == 0) {
            return new IntIterator() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public int nextInt() {
                    return 0;
                }
            };
        }
        int i = positiveCeilDiv(target, total);
        return new Divisor(target, i);
    }

    private static int positiveCeilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }

    private static class Divisor implements IntIterator {
        private final int denominator;
        private final int quotient;
        private final int mod;
        private int returnedParts;
        private int remainder;

        public Divisor(int numerator, int denominator) {
            this.denominator = denominator;
            if (denominator > 0) {
                this.quotient = numerator / denominator;
                this.mod = numerator % denominator;
            } else {
                this.quotient = 0;
                this.mod = 0;
            }
        }

        public boolean hasNext() {
            return this.returnedParts < this.denominator;
        }

        public int nextInt() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            } else {
                int i = this.quotient;
                this.remainder += this.mod;
                if (this.remainder >= this.denominator) {
                    this.remainder -= this.denominator;
                    ++i;
                }

                ++this.returnedParts;
                return i;
            }
        }

    }

    public void blitSprite(ResourceLocation texture, int x, int y, int width, int height, int color) {
        //#if MC >= 12106
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, color);
        //#elseif MC >= 12102
        //$$ guiGraphics.blitSprite(t -> RenderType.guiTextured(t), texture, x, y, width, height, color);
        //#elseif MC >= 12002
        //$$ guiGraphics.blitSprite(texture, x, y, width, height, color);
        //#else
        //$$ throw new java.lang.RuntimeException();
        //#endif
    }

    public void renderTooltip(Font font, List<FormattedCharSequence> split, int x, int y) {
        //#if MC >= 12006
        guiGraphics.setTooltipForNextFrame(font, split, x, y);
        //#elseif MC >= 12000
        //$$ guiGraphics.renderTooltip(font, split, x, y);
        //#else
        //$$ screen.renderTooltip(pose, split, x, y);
        //#endif
    }

    public void renderTooltip(Font font, MutableComponent translatable, int x, int y) {
        //#if MC >= 12006
        guiGraphics.setTooltipForNextFrame(font, translatable, x, y);
        //#elseif MC >= 12000
        //$$ guiGraphics.renderTooltip(font, translatable, x, y);
        //#else
        //$$ screen.renderTooltip(pose, translatable, x, y);
        //#endif
    }

    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        //#if MC >= 12000
        guiGraphics.fill(minX, minY, maxX, maxY, color);
        //#else
        //$$ GuiComponent.fill(pose, minX, minY, maxX, maxY, color);
        //#endif
    }

    public void invertedRect(int x, int y, int width, int height) {
        //#if MC >= 12106
        guiGraphics.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, x, y, x + width, y + height, -16776961);
        //#elseif MC >= 12105
        //$$ guiGraphics.fill(RenderType.guiTextHighlight(), x, y, x + width, y + height, -16776961);
        //#else
        //#if MC >= 11700
        //$$ Matrix4f model = getPose().last().pose();
        //$$ RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        //$$ RenderSystem.setShader(VanillaShaders.POSITION);
        //$$ RenderSystem.enableColorLogicOp();
        //$$ RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        //#if MC >= 12100
        //$$  BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        //$$  buffer.addVertex(model, x, y + height, 0);
        //$$  buffer.addVertex(model, x + width, y + height, 0);
        //$$ buffer.addVertex(model, x + width, y, 0);
        //$$ buffer.addVertex(model, x, y, 0);
        //$$ BufferUploader.drawWithShader(buffer.buildOrThrow());
        //$$ RenderSystem.disableColorLogicOp();
        //$$ RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //#elseif MC >= 11900
        //$$ Tesselator tessellator = Tesselator.getInstance();
        //$$ BufferBuilder buffer = tessellator.getBuilder();
        //$$ buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        //$$ buffer.vertex(model, x, y + height, 0).endVertex();
        //$$ buffer.vertex(model, x + width, y + height, 0).endVertex();
        //$$ buffer.vertex(model, x + width, y, 0).endVertex();
        //$$ buffer.vertex(model, x, y, 0).endVertex();
        //$$ BufferUploader.drawWithShader(buffer.end());
        //$$ RenderSystem.disableColorLogicOp();
        //$$ RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //#else
        //$$ Tesselator tessellator = Tesselator.getInstance();
        //$$ BufferBuilder buffer = tessellator.getBuilder();
        //$$ buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        //$$ buffer.vertex(model, x, y + height, 0).endVertex();
        //$$ buffer.vertex(model, x + width, y + height, 0).endVertex();
        //$$ buffer.vertex(model, x + width, y, 0).endVertex();
        //$$ buffer.vertex(model, x, y, 0).endVertex();
        //$$ buffer.end();
        //$$ BufferUploader.end(buffer);
        //$$ RenderSystem.disableColorLogicOp();
        //$$ RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //#endif
        //#else
        //$$Tesselator tessellator_1 = Tesselator.getInstance();
        //$$BufferBuilder bufferBuilder_1 = tessellator_1.getBuilder();
        //$$RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        //$$RenderSystem.disableTexture();
        //$$RenderSystem.enableColorLogicOp();
        //$$RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        //$$bufferBuilder_1.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
        //$$bufferBuilder_1.vertex(x,       y+height, 0.0D).endVertex();
        //$$bufferBuilder_1.vertex(x+width, y+height, 0.0D).endVertex();
        //$$bufferBuilder_1.vertex(x+width, y,        0.0D).endVertex();
        //$$bufferBuilder_1.vertex(x,       y,        0.0D).endVertex();
        //$$tessellator_1.end();
        //$$RenderSystem.disableColorLogicOp();
        //$$RenderSystem.enableTexture();
        //#endif
        //#endif
    }

    public void renderFakeItem(ItemStack itemStack, int x, int y) {
        //#if MC >= 12000
        guiGraphics.renderFakeItem(itemStack, x, y);
        //#elseif MC > 11903
        //$$ minecraft.getItemRenderer().renderAndDecorateFakeItem(pose, itemStack, x, y);
        //#else
        //$$ minecraft.getItemRenderer().renderAndDecorateFakeItem(itemStack, x, y);
        //#endif
    }

    public void renderItemDecorations(Font font, ItemStack itemStack, int x, int y) {
        //#if MC >= 12000
        guiGraphics.renderItemDecorations(font, itemStack, x, y);
        //#elseif MC > 11903
        //$$ minecraft.getItemRenderer().renderGuiItemDecorations(pose, font, itemStack, x, y);
        //#else
        //$$ minecraft.getItemRenderer().renderGuiItemDecorations(font, itemStack, x, y);
        //#endif
    }

    public void renderItem(Player player, ItemStack itemStack, int x, int y, int seed) {
        //#if MC >= 12000
        guiGraphics.renderItem(player, itemStack, x, y, seed);
        //#elseif MC > 11903
        //$$ minecraft.getItemRenderer().renderAndDecorateItem(pose, player, itemStack, x, y, seed);
        //#elseif MC > 11700
        //$$ minecraft.getItemRenderer().renderAndDecorateItem(player, itemStack, x, y, seed);
        //#else
        //$$ minecraft.getItemRenderer().renderAndDecorateItem(player, itemStack, x, y);
        //#endif
    }

    public void drawString(Font font, Component name, int x, int y, int color) {
        //#if MC >= 12000
        guiGraphics.drawString(font, name, x, y, color);
        //#else
        //$$ font.draw(pose, name, x, y, color);
        //#endif
    }

    public void drawCenteredString(Font font, Component name, int x, int y, int color) {
        //#if MC >= 12000
        guiGraphics.drawCenteredString(font, name, x, y, color);
        //#else
        //$$ GuiComponent.drawCenteredString(pose, font, name, x, y, color);
        //#endif
    }

    public void drawString(Font textRenderer, String s, int x, int y, int color, boolean dropShadow) {
        //#if MC >= 12000
        guiGraphics.drawString(textRenderer, s, x, y, color, dropShadow);
        //#else
        //$$GuiComponent.drawString(pose, textRenderer, s, x, y, color);
        //#endif
    }

    public void drawString(Font textRenderer, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        //#if MC >= 12000
        guiGraphics.drawString(textRenderer, text, x, y, color, dropShadow);
        //#else
        //$$if(dropShadow){
        //$$ textRenderer.drawShadow(pose, text, x, y, color);
        //$$} else {
        //$$ textRenderer.draw(pose, text, x, y, color);
        //$$}
        //#endif
    }

    public void drawString(Font textRenderer, @Nullable Component suggestion, int x, int y, int suggestionColor,
            boolean b) {
        //#if MC >= 12000
        guiGraphics.drawString(textRenderer, suggestion, x, y, suggestionColor, b);
        //#else
        //$$ GuiComponent.drawString(pose, textRenderer, suggestion, x, y, suggestionColor);
        //#endif
    }

    public void renderComponentHoverEffect(Font font, @Nullable Style textStyle, int x, int y) {
        //#if MC >= 12000
        guiGraphics.renderComponentHoverEffect(font, textStyle, x, y);
        //#else
        //$$ //TODO?
        //#endif
    }

    public void flush() {
        //#if MC <= 12105
        //#if MC >= 12000
        //$$ guiGraphics.flush();
        //#endif
        //#endif
    }

    //#if MC < 12106
    //$$public BufferSource getVertexConsumers() {
    //#if MC >= 12000
    //$$return ((DrawContextAccessor) guiGraphics).libgui$getVertexConsumers();
    //#else
    //$$ return minecraft.renderBuffers().bufferSource();
    //#endif
    //$$}
    //#endif

}