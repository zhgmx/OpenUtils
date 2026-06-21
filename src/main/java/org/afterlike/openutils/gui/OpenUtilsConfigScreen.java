package org.afterlike.openutils.gui;

import java.io.IOException;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.client.GuiFeature;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import re.tsuku.confikure.forge.ForgeGuiRenderer;
import re.tsuku.confikure.gui.ConfigColorScheme;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.gui.ConfigGuiState;
import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.GuiPrimitives;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;

public final class OpenUtilsConfigScreen extends GuiScreen {
	private static final int SIDEBAR_WIDTH = 112;
	private static final int SIDEBAR_FOOTER_HEIGHT = 20;
	private final ConfigDefinition definition;
	private final ConfigGuiState guiState;
	private final Consumer<ConfigGuiState> closeHandler;
	private ConfigGui gui;
	private ForgeGuiRenderer renderer;
	public OpenUtilsConfigScreen(final ConfigDefinition definition, final ConfigGuiState guiState,
			final Consumer<ConfigGuiState> closeHandler) {
		this.definition = definition;
		this.guiState = guiState;
		this.closeHandler = closeHandler;
	}

	@Override
	public void initGui() {
		this.gui = new ConfigGui(this.definition);
		this.gui.keyNameProvider(new ConfigGui.KeyNameProvider() {
			public String name(final int keyCode) {
				final String name = Keyboard.getKeyName(keyCode);
				return name == null ? String.valueOf(keyCode) : name.toLowerCase();
			}
		});
		this.gui.themeSupplier(OpenUtilsConfigScreen::currentTheme);
		this.gui.sidebarHeader(new OpenUtilsSidebarHeader());
		this.renderer = new ForgeGuiRenderer(this.mc);
		Keyboard.enableRepeatEvents(true);
		this.gui.state(this.guiState);
		this.gui.closeHandler(new Runnable() {
			public void run() {
				mc.displayGuiScreen(null);
			}
		});
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		ensureInitialized();
		if (drawBackground()) {
			drawDefaultBackground();
		}
		if (Mouse.isButtonDown(0)) {
			this.gui.drag(this.width, this.height, rawMouseX(), rawMouseY());
		}
		this.gui.render(this.renderer, this.width, this.height, mouseX, mouseY);
		drawLayoutEditorButton(mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton)
			throws IOException {
		ensureInitialized();
		if (mouseButton == 0) {
			if (layoutEditorButton().contains(mouseX, mouseY)) {
				this.mc.displayGuiScreen(new LayoutEditorScreen(this));
				return;
			}
			this.gui.click(this.width, this.height, mouseX, mouseY);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton,
			final long timeSinceLastClick) {
		ensureInitialized();
		if (clickedMouseButton == 0) {
			this.gui.drag(this.width, this.height, mouseX, mouseY);
		}
	}

	@Override
	protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
		ensureInitialized();
		if (state == 0) {
			this.gui.release();
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
		ensureInitialized();
		if (this.gui.keyTyped(typedChar, keyCode)) {
			return;
		}
		if (keyCode == 1) {
			this.mc.displayGuiScreen(null);
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		ensureInitialized();
		super.handleMouseInput();
		final int wheel = Mouse.getEventDWheel();
		if (wheel != 0) {
			this.gui.scroll(wheel < 0 ? 18 : -18);
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
		if (this.gui != null) {
			copyState(this.definition, this.gui.state(), this.guiState);
		}
		if (this.closeHandler != null) {
			this.closeHandler.accept(this.guiState);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private void ensureInitialized() {
		if (this.gui == null || this.renderer == null) {
			initGui();
		}
	}

	private int rawMouseX() {
		return Mouse.getX() * this.width / this.mc.displayWidth;
	}

	private int rawMouseY() {
		return this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
	}

	private void drawLayoutEditorButton(final int mouseX, final int mouseY) {
		final GuiBounds button = layoutEditorButton();
		final ConfigTheme theme = currentTheme();
		GuiPrimitives.inlineButton(this.renderer, theme, button.x, button.y, button.width,
				button.height, button.contains(mouseX, mouseY), true);
		this.renderer.centeredText("Layout Editor", button.x, button.y + 6, button.width,
				theme.text);
	}

	private GuiBounds layoutEditorButton() {
		final GuiBounds panel = panelBounds();
		return new GuiBounds(panel.x + 8, panel.y + panel.height - SIDEBAR_FOOTER_HEIGHT - 8,
				SIDEBAR_WIDTH - 16, SIDEBAR_FOOTER_HEIGHT);
	}

	private GuiBounds panelBounds() {
		final int panelWidth = Math.min(620, Math.max(360, this.width - 42));
		final int panelHeight = Math.min(390, Math.max(230, this.height - 36));
		return new GuiBounds((this.width - panelWidth) / 2, (this.height - panelHeight) / 2,
				panelWidth, panelHeight);
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

	private static void copyState(final ConfigDefinition definition, final ConfigGuiState source,
			final ConfigGuiState target) {
		target.clear();
		target.selectedCategoryId(source.selectedCategoryId());
		for (final ConfigCategory category : definition.categories()) {
			for (final ConfigGroup group : category.groups()) {
				if (source.collapsed(category.id(), group.id())) {
					target.collapsed(category.id(), group.id(), true);
				}
			}
		}
	}
	private static final class OpenUtilsSidebarHeader implements ConfigGui.SidebarHeader {
		public void render(final GuiRenderer renderer, final GuiBounds bounds,
				final ConfigTheme theme) {
			renderer.text("OpenUtils", bounds.x, bounds.y, theme.text);
			renderer.text("v" + OpenUtils.get().getVersion(), bounds.x, bounds.y + 12,
					theme.mutedText);
		}
	}
}
