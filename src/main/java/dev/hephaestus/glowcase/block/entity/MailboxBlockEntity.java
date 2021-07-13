package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.MailboxBlock;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class MailboxBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private final Deque<Message> messages = new ArrayDeque<>();
    private UUID owner;

    public MailboxBlockEntity(BlockPos pos, BlockState state) {
        super(Glowcase.MAILBOX_BLOCK_ENTITY, pos, state);
    }

    public void setOwner(ServerPlayerEntity player) {
        this.owner = player.getUuid();
        this.markDirty();
        this.sync();
    }

    public void addMessage(Message message) {
        this.messages.addFirst(message);

        if (this.world != null) {
            this.world.setBlockState(this.pos, this.getCachedState().with(MailboxBlock.HAS_MAIL, true));
        }

        this.markDirty();
        this.sync();
    }

    public void removeMessage() {
        if (this.messages.removeFirst() != null && this.world != null && this.messages.isEmpty()) {
            this.world.setBlockState(this.pos, this.getCachedState().with(MailboxBlock.HAS_MAIL, false));
            this.markDirty();
            this.sync();
        }
    }

    public int messageCount() {
        return this.messages.size();
    }

    public Message getMessage() {
        return this.messages.getFirst();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putUuid("Owner", this.owner);

        NbtList list = nbt.getList("Messages", NbtType.COMPOUND);

        for (Message message : this.messages) {
            NbtCompound messageTag = new NbtCompound();

            messageTag.putUuid("Sender", message.sender);
            messageTag.putString("SenderName", message.senderName);
            messageTag.putString("Message", message.message);

            list.add(messageTag);
        }

        nbt.put("Messages", list);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.owner = nbt.getUuid("Owner");

        for (NbtElement element : nbt.getList("Messages", NbtType.COMPOUND)) {
            if (element instanceof NbtCompound message) {
                this.messages.addLast(new Message(
                        message.getUuid("Sender"),
                        message.getString("SenderName"),
                        message.getString("Message")
                ));
            }
        }
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return this.writeNbt(tag);
    }

    public UUID owner() {
        return this.owner;
    }

    public void removeAllMessagesFromMostRecentSender() {
        if (!this.messages.isEmpty()) {
            UUID sender = this.messages.pop().sender;

            this.messages.removeIf(message -> message.sender.equals(sender));
            this.markDirty();
            this.sync();
        }
    }

    public static record Message(UUID sender, String senderName, String message) {}
}
