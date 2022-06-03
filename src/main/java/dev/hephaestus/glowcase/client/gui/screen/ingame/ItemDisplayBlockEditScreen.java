package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.networking.ItemDisplayBlockChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ItemDisplayBlockEditScreen extends GlowcaseScreen {
	private final ItemDisplayBlockEntity displayBlock;

	private ButtonWidget givesItemButtom;
	private ButtonWidget rotationTypeButton;
	private ButtonWidget showNameButton;

	public ItemDisplayBlockEditScreen(ItemDisplayBlockEntity displayBlock) {
		this.displayBlock = displayBlock;
	}

	@Override
	public void init() {
		super.init();

		if (this.client != null) {
			int padding = width / 100;
			int individualPadding = padding / 2;
			int centerW = width / 2;
			int centerH = height / 2;

			this.givesItemButtom = new ButtonWidget(centerW - 75, centerH - 30 - individualPadding, 150, 20, new TranslatableText("gui.glowcase.gives_item", this.displayBlock.givesItem), (action) -> {
				this.displayBlock.cycleGiveType();
				this.givesItemButtom.setMessage(new TranslatableText("gui.glowcase.gives_item", this.displayBlock.givesItem));
				ItemDisplayBlockChannel.sync(this.displayBlock, true);
			});

			this.rotationTypeButton = new ButtonWidget(centerW - 75, centerH - 10, 150, 20, new TranslatableText("gui.glowcase.rotation_type", this.displayBlock.rotationType), (action) -> {
				this.displayBlock.cycleRotationType(this.client.player);
				this.rotationTypeButton.setMessage(new TranslatableText("gui.glowcase.rotation_type", this.displayBlock.rotationType));
				ItemDisplayBlockChannel.sync(this.displayBlock, true);
			});

			this.showNameButton = new ButtonWidget(centerW - 75, centerH + 10 + individualPadding, 150, 20, new TranslatableText("gui.glowcase.show_name", this.displayBlock.showName), (action) -> {
				this.displayBlock.showName = !this.displayBlock.showName;
				this.showNameButton.setMessage(new TranslatableText("gui.glowcase.show_name", this.displayBlock.showName));
				ItemDisplayBlockChannel.sync(this.displayBlock, false);
			});

			this.addDrawableChild(this.givesItemButtom);
			this.addDrawableChild(this.rotationTypeButton);
			this.addDrawableChild(this.showNameButton);
		}
	}
}
