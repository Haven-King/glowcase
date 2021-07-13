package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.mixin.client.MinecraftClientAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MailboxChannel implements ClientModInitializer {
    private static final Identifier ID = Glowcase.id("channel", "mailbox");

    public static void openChat(ServerPlayerEntity player, BlockPos pos) {
        ServerPlayNetworking.send(player, ID, new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register(this::registerListener);
    }

    @Environment(EnvType.CLIENT)
    private void registerListener(ClientPlayNetworkHandler handler, MinecraftClient client) {
        ClientPlayNetworking.registerReceiver(ID, this::openChat);
    }

    @Environment(EnvType.CLIENT)
    private void openChat(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        client.execute(new ChatOpener(client, buf.readBlockPos()));
    }

    @Environment(EnvType.CLIENT)
    private static record ChatOpener(MinecraftClient client, BlockPos pos) implements Runnable {
        @Override
        public void run() {
            ((MinecraftClientAccessor) this.client).invokeOpenChatScreen(String.format("/mail %d %d %d ", this.pos.getX(), this.pos.getY(), this.pos.getZ()));
        }
    }
}
