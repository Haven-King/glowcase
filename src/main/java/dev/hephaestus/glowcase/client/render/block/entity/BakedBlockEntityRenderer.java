package dev.hephaestus.glowcase.client.render.block.entity;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public abstract class BakedBlockEntityRenderer<T extends BlockEntity> extends BlockEntityRenderer<T> {
	public BakedBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	/**
	 * Handles invalidation and passing of rendered vertices to the baking system.
	 * Override renderBaked and renderUnbaked instead of this method.
	 */
	@Override
	public final void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		renderUnbaked(entity, tickDelta, matrices, vertexConsumers, light, overlay);
		VertexBufferManager.INSTANCE.activateRegion(entity.getPos());
	}

	/**
	 * Render vertices to be baked into the render region. This method will be called every time the render region is rebuilt - so
	 * you should only render vertices that don't move here. You can call invalidateSelf or VertexBufferManager.invalidate to
	 * cause the render region to be rebuilt, but do not call this too frequently as it will affect performance.
	 *
	 * You must use the provided VertexConsumerProvider and MatrixStack to render your vertices - any use of Tessellator
	 * or RenderSystem here will not work. If you need custom rendering settings, you can use a custom RenderLayer.
	 */
	public abstract void renderBaked(T entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

	/**
	 * Render vertices immediately. This works exactly the same way as a normal BER render method, and can be used for dynamic
	 * rendering that changes every frame. In this method you can also check for render invalidation and call invalidateSelf
	 * as appropriate.
	 */
	public abstract void renderUnbaked(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

	/**
	 * Causes the render region containing this BlockEntity to be rebuilt -
	 * do not call this too frequently as it will affect performance.
	 * An invalidation will not immediately cause the next frame to contain an updated view (and call to renderBaked)
	 * as all render region rebuilds must call every BER that is to be rendered, otherwise they will be missing from the
	 * vertex buffer.
	 */
	public void invalidateSelf(T entity) {
		VertexBufferManager.INSTANCE.invalidate(entity.getPos());
	}

	private static class RenderRegionPos {
		private final int x;
		private final int z;

		public RenderRegionPos(BlockPos pos) {
			this.x = pos.getX() >> VertexBufferManager.REGION_SHIFT;
			this.z = pos.getZ() >> VertexBufferManager.REGION_SHIFT;
		}

		public RenderRegionPos(int x, int z) {
			this.x = x;
			this.z = z;
		}

		public BlockPos getOrigin() {
			return new BlockPos(x << VertexBufferManager.REGION_SHIFT, 0, z << VertexBufferManager.REGION_SHIFT);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RenderRegionPos that = (RenderRegionPos) o;
			return x == that.x &&
				z == that.z;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, z);
		}
	}

	// TODO: lazy init?
	public static class VertexBufferManager {
		public static final VertexBufferManager INSTANCE = new VertexBufferManager();

		// 2x2 chunks size for regions
		public static final int REGION_FROMCHUNK_SHIFT = 1;
		public static final int REGION_SHIFT = 4 + REGION_FROMCHUNK_SHIFT;
		public static final int MAX_XZ_IN_REG = (16 << REGION_FROMCHUNK_SHIFT) - 1;
		public static final int VIEW_RADIUS = 3;

		private final Map<RenderRegionPos, RegionBuffer> cachedRegions = new HashMap<>();

		private final ObjectSet<RenderRegionPos> invalidRegions = new ObjectArraySet<>();
		private final Map<RenderRegionPos, RegionBuilder> rebuilders = new Object2ObjectArrayMap<>();

		private ClientWorld currWorld = null;

		public VertexBufferManager() {
			// Register callback, to rebuild all when fonts/render chunks are changed
			InvalidateRenderStateCallback.EVENT.register(this::reset);
		}

		private static class RegionBuffer {
			private final Map<RenderLayer, VertexBuffer> layerBuffers = new Object2ObjectArrayMap<>();
			public final BlockPos origin;

			public RegionBuffer(RenderRegionPos region) {
				origin = region.getOrigin();
			}

			public boolean hasLayer(RenderLayer rl) {
				return layerBuffers.containsKey(rl);
			}

			public void render(RenderLayer rl, MatrixStack matrices) {
				VertexBuffer buf = layerBuffers.get(rl);
				buf.bind();
				rl.getVertexFormat().startDrawing(0L);
				buf.draw(matrices.peek().getModel(), rl.getDrawMode());
			}

			public void rebuild(RenderLayer rl, BufferBuilder newBuf) {
				VertexBuffer buf = layerBuffers.computeIfAbsent(rl, renderLayer -> new VertexBuffer(rl.getVertexFormat()));
				// TODO: check translucency of RenderLayer first?
				newBuf.sortQuads(0, 0, 0);
				newBuf.end();
				buf.upload(newBuf);
			}

			public void deallocate() {
				for (VertexBuffer buf : layerBuffers.values()) {
					buf.close();
				}
			}

			public void removeUnusedLayers(Set<RenderLayer> usedLayers) {
				Iterator<RenderLayer> iter = layerBuffers.keySet().iterator();
				while (iter.hasNext()) {
					RenderLayer rl = iter.next();
					if (!usedLayers.contains(rl)) {
						layerBuffers.get(rl).close();
						iter.remove();
					}
				}
			}

			public Set<RenderLayer> getAllLayers() {
				return layerBuffers.keySet();
			}
		}

		private static class RegionBuilder implements VertexConsumerProvider, Iterable<Map.Entry<RenderLayer, BufferBuilder>> {
			private final Map<RenderLayer, BufferBuilder> bufs = new Object2ObjectArrayMap<>();

			private <E extends BlockEntity> void render(E blockEntity) {
				BlockEntityRenderer<E> ber = BlockEntityRenderDispatcher.INSTANCE.get(blockEntity);
				if (ber instanceof BakedBlockEntityRenderer) {
					MatrixStack bakeStack = new MatrixStack();
					BlockPos pos = blockEntity.getPos();
					bakeStack.translate(pos.getX() & VertexBufferManager.MAX_XZ_IN_REG, pos.getY(), pos.getZ() & VertexBufferManager.MAX_XZ_IN_REG);
					World world = blockEntity.getWorld();
					int light;
					if (world != null) {
						light = WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos());
					} else {
						light = 15728880;
					}
					((BakedBlockEntityRenderer<E>) ber).renderBaked(blockEntity, bakeStack, this, light, OverlayTexture.DEFAULT_UV);
				}
			}

			public void build(List<BlockEntity> blockEntities) {
				for (BlockEntity be : blockEntities) {
					render(be);
				}
			}

			@Override
			public VertexConsumer getBuffer(RenderLayer layer) {
				return bufs.computeIfAbsent(layer, l -> {
					BufferBuilder buf = new BufferBuilder(l.getExpectedBufferSize());
					buf.begin(l.getDrawMode(), l.getVertexFormat());
					return buf;
				});
			}

			public Iterator<Map.Entry<RenderLayer, BufferBuilder>> iterator() {
				return bufs.entrySet().iterator();
			}
		}

		public void invalidate(BlockPos pos) {
			// Mark a region as invalid. After the current set of rebuilding regions (invalid regions from the last frame) have been
			// built, a RegionBuilder will be created for this region and passed to all BERs to render to
			invalidRegions.add(new RenderRegionPos(pos));
		}

		// TODO: move chunk baking off-thread?

		private boolean isVisiblePos(RenderRegionPos rrp, RenderRegionPos center) {
			return Math.abs(rrp.x - center.x) <= VIEW_RADIUS && Math.abs(rrp.z - center.z) <= VIEW_RADIUS;
		}

		public void render(MatrixStack matrices, Camera camera) {
			Vec3d vec3d = camera.getPos();
			double camX = vec3d.getX();
			double camY = vec3d.getY();
			double camZ = vec3d.getZ();
			RenderRegionPos centerRegion = new RenderRegionPos((int)camX >> REGION_SHIFT, (int)camZ >> REGION_SHIFT);

			// Iterate over all RegionBuilders, render and upload to RegionBuffers
			Set<RenderLayer> usedRenderLayers = new ObjectArraySet<>();
			List<BlockEntity> blockEntities = new ArrayList<>();
			for (Map.Entry<RenderRegionPos, RegionBuilder> entryBuilder : rebuilders.entrySet()) {
				RenderRegionPos rrp = entryBuilder.getKey();
				if (isVisiblePos(rrp, centerRegion)) {
					// For the current region, rebuild each render layer using the buffer builders
					// Find all block entities in this region
					if (currWorld == null) {
						break;
					}
					for (int chunkX = rrp.x << REGION_FROMCHUNK_SHIFT; chunkX < (rrp.x + 1) << REGION_FROMCHUNK_SHIFT; chunkX++) {
						for (int chunkZ = rrp.z << REGION_FROMCHUNK_SHIFT; chunkZ < (rrp.z + 1) << REGION_FROMCHUNK_SHIFT; chunkZ++) {
							blockEntities.addAll(currWorld.getChunk(chunkX, chunkZ).getBlockEntities().values());
						}
					}
					entryBuilder.getValue().build(blockEntities);
					blockEntities.clear();
					RegionBuffer buf = cachedRegions.computeIfAbsent(entryBuilder.getKey(), RegionBuffer::new);
					for (Map.Entry<RenderLayer, BufferBuilder> layerBuilder : entryBuilder.getValue()) {
						buf.rebuild(layerBuilder.getKey(), layerBuilder.getValue());
						usedRenderLayers.add(layerBuilder.getKey());
					}
					buf.removeUnusedLayers(usedRenderLayers);
					usedRenderLayers.clear();
				}
			}
			// End the current region rebuild pass, make builders for invalidated regions
			// TODO: move this phase - we don't need to wait anymore
			// TODO: reuse bufferbuilders?
			rebuilders.clear();
			for (RenderRegionPos rrp : invalidRegions) {
				rebuilders.put(rrp, new RegionBuilder());
			}
			invalidRegions.clear();

			// TODO: reuse VBOs?
			// Get a list of layers, remove unused RegionBuffers
			Set<RenderLayer> renderLayers = new ObjectArraySet<>();
			Iterator<Map.Entry<RenderRegionPos, RegionBuffer>> iterBuffers = cachedRegions.entrySet().iterator();
			while (iterBuffers.hasNext()) {
				Map.Entry<RenderRegionPos, RegionBuffer> entryBuffer = iterBuffers.next();
				if (isVisiblePos(entryBuffer.getKey(), centerRegion)) {
					renderLayers.addAll(entryBuffer.getValue().getAllLayers());
				} else {
					entryBuffer.getValue().deallocate();
					iterBuffers.remove();
				}
			}

			// Iterate over all RegionBuffers, render them
			for (RenderLayer layer : renderLayers) {
				layer.startDrawing();
				for (RegionBuffer cb : cachedRegions.values()) {
					if (cb.hasLayer(layer)) {
						BlockPos origin = cb.origin;
						matrices.push();
						matrices.translate(origin.getX() - camX, origin.getY() - camY, origin.getZ() - camZ);
						cb.render(layer, matrices);
						matrices.pop();
					}
				}
				VertexBuffer.unbind();
				layer.getVertexFormat().endDrawing();
				layer.endDrawing();
			}
		}

		public void activateRegion(BlockPos pos) {
			RenderRegionPos rrp = new RenderRegionPos(pos);
			if (!cachedRegions.containsKey(rrp)) {
				rebuilders.put(rrp, new RegionBuilder());
			}
		}

		private void reset() {
			// Reset everything
			for (RegionBuffer buf : cachedRegions.values()) {
				buf.deallocate();
			}
			cachedRegions.clear();
			invalidRegions.clear();
			rebuilders.clear();
		}

		public void setWorld(ClientWorld world) {
			reset();
			currWorld = world;
		}
	}
}
