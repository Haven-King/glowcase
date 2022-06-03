package dev.hephaestus.glowcase;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.hephaestus.glowcase.block.HyperlinkBlock;
import dev.hephaestus.glowcase.block.ItemDisplayBlock;
import dev.hephaestus.glowcase.block.MailboxBlock;
import dev.hephaestus.glowcase.block.TextBlock;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.MailboxBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.tag.TagKey;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class Glowcase implements ModInitializer {
	public static final String MODID = "glowcase";

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(id("items"), () -> new ItemStack(Items.GLOWSTONE));
	public static final TagKey<Item> ITEM_TAG = TagKey.of(Registry.ITEM_KEY, id("items"));


	public static final Block HYPERLINK_BLOCK = Registry.register(Registry.BLOCK, id("hyperlink_block"), new HyperlinkBlock());
	public static final Item HYPERLINK_BLOCK_ITEM = Registry.register(Registry.ITEM, id("hyperlink_block"), new BlockItem(HYPERLINK_BLOCK, new FabricItemSettings().group(ITEM_GROUP)));
	public static final BlockEntityType<HyperlinkBlockEntity> HYPERLINK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("hyperlink_block"), FabricBlockEntityTypeBuilder.create(HyperlinkBlockEntity::new, HYPERLINK_BLOCK).build());

	public static final Block ITEM_DISPLAY_BLOCK = Registry.register(Registry.BLOCK, id("item_display_block"), new ItemDisplayBlock());
	public static final Item ITEM_DISPLAY_BLOCK_ITEM = Registry.register(Registry.ITEM, id("item_display_block"), new BlockItem(ITEM_DISPLAY_BLOCK, new FabricItemSettings().group(ITEM_GROUP)));
	public static final BlockEntityType<ItemDisplayBlockEntity> ITEM_DISPLAY_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("item_display_block"), FabricBlockEntityTypeBuilder.create(ItemDisplayBlockEntity::new, ITEM_DISPLAY_BLOCK).build());

	public static final Block MAILBOX_BLOCK = Registry.register(Registry.BLOCK, id("mailbox"), new MailboxBlock());
	public static final Item MAILBOX_ITEM = Registry.register(Registry.ITEM, id("mailbox"), new BlockItem(MAILBOX_BLOCK, new FabricItemSettings().group(ITEM_GROUP)));
	public static final BlockEntityType<MailboxBlockEntity> MAILBOX_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("mailbox"), FabricBlockEntityTypeBuilder.create(MailboxBlockEntity::new, MAILBOX_BLOCK).build());

	public static final Block TEXT_BLOCK = Registry.register(Registry.BLOCK, id("text_block"), new TextBlock());
	public static final Item TEXT_BLOCK_ITEM = Registry.register(Registry.ITEM, id("text_block"), new BlockItem(TEXT_BLOCK, new FabricItemSettings().group(ITEM_GROUP)));
	public static final BlockEntityType<TextBlockEntity> TEXT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("text_block"), FabricBlockEntityTypeBuilder.create(TextBlockEntity::new, TEXT_BLOCK).build());

	public static Identifier id(String... path) {
		return new Identifier(MODID, String.join(".", path));
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(
					LiteralArgumentBuilder.<ServerCommandSource>literal("mail")
						.then(CommandManager.argument("pos", new BlockPosArgumentType())
								.then(CommandManager.argument("message", StringArgumentType.greedyString()).executes(this::sendMessage)))
			);
		});
	}

	private int sendMessage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");
		String message = ctx.getArgument("message", String.class);
		PlayerEntity sender = ctx.getSource().getPlayer();

		if (sender != null) {
			if (sender.world.getBlockEntity(pos) instanceof MailboxBlockEntity mailbox) {
				mailbox.addMessage(new MailboxBlockEntity.Message(sender.getUuid(), sender.getEntityName(), message));
				ctx.getSource().sendFeedback(new TranslatableText("command.glowcase.message_sent"), false);
				return 0;
			} else {
				ctx.getSource().sendError(new TranslatableText("command.glowcase.failed.no_mailbox"));
				return 100;
			}
		} else {
			ctx.getSource().sendError(new TranslatableText("command.glowcase.failed.no_world"));
			return 100;
		}
	}
}
