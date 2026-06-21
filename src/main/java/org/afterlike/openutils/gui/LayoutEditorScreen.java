package org.afterlike.openutils.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.api.Feature;
import org.afterlike.openutils.feature.api.hud.Anchor;
import org.afterlike.openutils.feature.api.hud.HudFeature;
import org.afterlike.openutils.feature.api.hud.Position;
import org.afterlike.openutils.feature.impl.client.GuiFeature;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import re.tsuku.confikure.forge.ForgeGuiRenderer;
import re.tsuku.confikure.gui.ConfigColorScheme;
import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.GuiPrimitives;

public final class LayoutEditorScreen extends GuiScreen {
	private static final int PANEL_PADDING = 8;
	private static final int PANEL_WIDTH = 360;
	private static final int COMPACT_PANEL_WIDTH = 250;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW_GAP = 5;
	private final GuiScreen previousScreen;
	private final List<HudEntry> hudEntries = new ArrayList<>();
	private HudEntry selectedEntry;
	private ForgeGuiRenderer renderer;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	public LayoutEditorScreen(final GuiScreen previousScreen) {
		this.previousScreen = previousScreen;
	}

	@Override
	public void initGui() {
		this.renderer = new ForgeGuiRenderer(this.mc);
		final Feature previousSelection = this.selectedEntry != null
				? this.selectedEntry.feature
				: null;
		this.hudEntries.clear();
		for (final Feature feature : OpenUtils.get().getFeatureHandler().getFeatures()) {
			if (feature instanceof HudFeature) {
				this.hudEntries.add(new HudEntry(feature, (HudFeature) feature));
			}
		}
		this.selectedEntry = findEntry(previousSelection);
		if (this.selectedEntry == null) {
			this.selectedEntry = this.hudEntries.isEmpty() ? null : this.hudEntries.get(0);
		}
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		ensureInitialized();
		final int currentMouseX = rawMouseX();
		final int currentMouseY = rawMouseY();
		updateDragging(currentMouseX, currentMouseY);
		if (drawBackground()) {
			drawDefaultBackground();
		}
		final ConfigTheme theme = currentTheme();
		drawEditorPanel(currentMouseX, currentMouseY, theme);
		drawHudElements(currentMouseX, currentMouseY, theme);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton)
			throws IOException {
		ensureInitialized();
		if (mouseButton != 0) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}
		final int currentMouseX = rawMouseX();
		final int currentMouseY = rawMouseY();
		for (int index = this.hudEntries.size() - 1; index >= 0; index--) {
			final HudEntry entry = this.hudEntries.get(index);
			final GuiBounds bounds = hudBounds(entry);
			if (bounds.contains(currentMouseX, currentMouseY)) {
				this.selectedEntry = entry;
				this.dragging = true;
				this.dragOffsetX = currentMouseX - (bounds.x + 4);
				this.dragOffsetY = currentMouseY - (bounds.y + 4);
				return;
			}
		}
		if (handlePanelClick(currentMouseX, currentMouseY)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
		if (state == 0) {
			finishDragging();
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			closeToPrevious();
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		saveLayout();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private void updateDragging(final int mouseX, final int mouseY) {
		if (!this.dragging || this.selectedEntry == null) {
			return;
		}
		if (!Mouse.isButtonDown(0)) {
			finishDragging();
			return;
		}
		final GuiBounds bounds = hudBounds(this.selectedEntry);
		this.selectedEntry.hud.getHudPosition().setScreenPosition(mouseX - this.dragOffsetX,
				mouseY - this.dragOffsetY, bounds.width - 8, bounds.height - 8);
	}

	private boolean handlePanelClick(final int mouseX, final int mouseY) {
		final GuiBounds panel = editorPanelBounds();
		if (!panel.contains(mouseX, mouseY)) {
			return false;
		}
		if (doneButton().contains(mouseX, mouseY)) {
			closeToPrevious();
			return true;
		}
		if (resetButton().contains(mouseX, mouseY)) {
			resetSelected();
			return true;
		}
		for (int index = 0; index < this.hudEntries.size(); index++) {
			if (featureButton(index).contains(mouseX, mouseY)) {
				this.selectedEntry = this.hudEntries.get(index);
				return true;
			}
		}
		return true;
	}

	private void drawEditorPanel(final int mouseX, final int mouseY, final ConfigTheme theme) {
		final GuiBounds panel = editorPanelBounds();
		GuiPrimitives.frame(this.renderer, theme, panel);
		this.renderer.centeredText("Layout Editor", panel.x, panel.y + PANEL_PADDING, panel.width,
				theme.text);
		for (int index = 0; index < this.hudEntries.size(); index++) {
			drawFeatureButton(index, mouseX, mouseY, theme);
		}
		drawFooterButton(resetButton(), "Reset", mouseX, mouseY, theme, this.selectedEntry != null);
		drawFooterButton(doneButton(), "Done", mouseX, mouseY, theme, true);
	}

	private void drawFeatureButton(final int index, final int mouseX, final int mouseY,
			final ConfigTheme theme) {
		final HudEntry entry = this.hudEntries.get(index);
		final GuiBounds button = featureButton(index);
		final boolean hovered = button.contains(mouseX, mouseY);
		final boolean selected = entry == this.selectedEntry;
		GuiPrimitives.inlineButton(this.renderer, theme, button.x, button.y, button.width,
				button.height, hovered, true);
		if (selected) {
			GuiPrimitives.border(this.renderer, theme, button, theme.accent);
		}
		this.renderer.centeredText(
				GuiPrimitives.clip(entry.feature.getName(), this.renderer, button.width - 8),
				button.x, button.y + 6, button.width, selected ? theme.text : theme.mutedText);
	}

	private void drawFooterButton(final GuiBounds button, final String text, final int mouseX,
			final int mouseY, final ConfigTheme theme, final boolean enabled) {
		GuiPrimitives.inlineButton(this.renderer, theme, button.x, button.y, button.width,
				button.height, button.contains(mouseX, mouseY), enabled);
		this.renderer.centeredText(text, button.x, button.y + 6, button.width,
				enabled ? theme.text : theme.disabledText);
	}

	private void drawHudElements(final int mouseX, final int mouseY, final ConfigTheme theme) {
		for (final HudEntry entry : this.hudEntries) {
			final GuiBounds bounds = hudBounds(entry);
			final boolean hovered = bounds.contains(mouseX, mouseY);
			final boolean selected = entry == this.selectedEntry;
			GuiPrimitives.frame(this.renderer, theme, bounds.x, bounds.y, bounds.width,
					bounds.height, hovered, selected, true);
			entry.hud.renderHudPreview(bounds.x + 4, bounds.y + 4);
		}
	}

	private GuiBounds editorPanelBounds() {
		final int columns = featureColumns();
		final int rows = Math.max(1, (this.hudEntries.size() + columns - 1) / columns);
		final int requestedWidth = columns == 3 ? PANEL_WIDTH : COMPACT_PANEL_WIDTH;
		final int panelWidth = Math.min(requestedWidth, Math.max(180, this.width - 24));
		final int panelHeight = PANEL_PADDING + this.mc.fontRendererObj.FONT_HEIGHT + 7
				+ rows * BUTTON_HEIGHT + Math.max(0, rows - 1) * ROW_GAP + PANEL_PADDING
				+ BUTTON_HEIGHT + PANEL_PADDING;
		return new GuiBounds((this.width - panelWidth) / 2,
				Math.max(PANEL_PADDING, (this.height - panelHeight) / 2), panelWidth, panelHeight);
	}

	private GuiBounds featureButton(final int index) {
		final GuiBounds panel = editorPanelBounds();
		final int columns = featureColumns();
		final int column = index % columns;
		final int row = index / columns;
		final int gapWidth = (columns - 1) * ROW_GAP;
		final int buttonWidth = (panel.width - PANEL_PADDING * 2 - gapWidth) / columns;
		return new GuiBounds(
				panel.x + PANEL_PADDING + column * (buttonWidth + ROW_GAP), panel.y + PANEL_PADDING
						+ this.mc.fontRendererObj.FONT_HEIGHT + 7 + row * (BUTTON_HEIGHT + ROW_GAP),
				buttonWidth, BUTTON_HEIGHT);
	}

	private GuiBounds resetButton() {
		final GuiBounds panel = editorPanelBounds();
		final int buttonWidth = (panel.width - PANEL_PADDING * 2 - ROW_GAP) / 2;
		return new GuiBounds(panel.x + PANEL_PADDING,
				panel.y + panel.height - PANEL_PADDING - BUTTON_HEIGHT, buttonWidth, BUTTON_HEIGHT);
	}

	private GuiBounds doneButton() {
		final GuiBounds reset = resetButton();
		return new GuiBounds(reset.x + reset.width + ROW_GAP, reset.y, reset.width, reset.height);
	}

	private GuiBounds hudBounds(final HudEntry entry) {
		final Position position = entry.hud.getHudPosition();
		final int elementWidth = entry.hud.getHudPreviewWidth();
		final int elementHeight = Math.max(this.mc.fontRendererObj.FONT_HEIGHT,
				entry.hud.getHudPreviewHeight());
		return new GuiBounds(position.getX(elementWidth) - 4, position.getY(elementHeight) - 4,
				elementWidth + 8, elementHeight + 8);
	}

	private void resetSelected() {
		if (this.selectedEntry == null) {
			return;
		}
		this.selectedEntry.hud.getHudPosition().reset();
		saveLayout();
	}

	private void finishDragging() {
		if (!this.dragging) {
			return;
		}
		this.dragging = false;
		updateSelectedAnchor();
		saveLayout();
	}

	private void updateSelectedAnchor() {
		if (this.selectedEntry == null) {
			return;
		}
		final GuiBounds bounds = hudBounds(this.selectedEntry);
		final Position position = this.selectedEntry.hud.getHudPosition();
		final ScaledResolution resolution = new ScaledResolution(this.mc);
		final int elementWidth = bounds.width - 8;
		final int elementHeight = bounds.height - 8;
		final int screenX = position.getX(elementWidth);
		final int screenY = position.getY(elementHeight);
		final Anchor anchor = Anchor.detect(bounds.x + bounds.width / 2,
				bounds.y + bounds.height / 2, resolution.getScaledWidth(),
				resolution.getScaledHeight());
		position.setAnchor(anchor);
		position.setScreenPosition(screenX, screenY, elementWidth, elementHeight);
	}

	private HudEntry findEntry(final Feature feature) {
		if (feature == null) {
			return null;
		}
		for (final HudEntry entry : this.hudEntries) {
			if (entry.feature == feature) {
				return entry;
			}
		}
		return null;
	}

	private void closeToPrevious() {
		saveLayout();
		this.mc.displayGuiScreen(this.previousScreen);
	}

	private void saveLayout() {
		OpenUtils.get().getConfigHandler().saveConfiguration();
	}

	private void ensureInitialized() {
		if (this.renderer == null) {
			initGui();
		}
	}

	private int rawMouseX() {
		return Mouse.getX() * this.width / this.mc.displayWidth;
	}

	private int rawMouseY() {
		return this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
	}

	private int featureColumns() {
		return this.width >= 320 ? 3 : 2;
	}

	private static boolean drawBackground() {
		final GuiFeature guiFeature = OpenUtils.get().getFeatureHandler()
				.getFeature(GuiFeature.class);
		return guiFeature.background;
	}

	private static ConfigTheme currentTheme() {
		final GuiFeature guiFeature = OpenUtils.get().getFeatureHandler()
				.getFeature(GuiFeature.class);
		return ConfigColorScheme.byDisplayName(guiFeature.theme).theme();
	}
	private static final class HudEntry {
		private final Feature feature;
		private final HudFeature hud;
		private HudEntry(final Feature feature, final HudFeature hud) {
			this.feature = feature;
			this.hud = hud;
		}
	}
}
