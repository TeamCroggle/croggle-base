package de.croggle.ui.actors;

import static de.croggle.data.LocalizationHelper._;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.croggle.AlligatorApp;
import de.croggle.game.GameController;
import de.croggle.ui.StyleHelper;
import de.croggle.ui.screens.AbstractScreen;

public class IngameMenuDialog extends Dialog {

	private final AlligatorApp game;
	private final GameController gameController;
	private final AbstractScreen predecessor;

	public IngameMenuDialog(AlligatorApp game, GameController gameController, AbstractScreen predecessor) {
		super("", StyleHelper.getInstance().getDialogStyle());
		this.game = game;
		this.gameController = gameController;
		this.predecessor = predecessor;

		fillTable();
	}

	private void fillTable() {
		StyleHelper helper = StyleHelper.getInstance();

		ImageTextButton continueGame = new ImageTextButton(
				_("ingame_menu_continue"),
				helper.getImageTextButtonStyleTransparent("widgets/icon-next"));
		ImageTextButton reset = new ImageTextButton(_("ingame_menu_reset"),
				helper.getImageTextButtonStyleTransparent("widgets/icon-reset"));
		ImageTextButton settings = new ImageTextButton(
				_("ingame_menu_settings"),
				helper.getImageTextButtonStyleTransparent("widgets/icon-settings"));
		ImageTextButton levelOverview = new ImageTextButton(
				_("ingame_menu_level_overview"),
				helper.getImageTextButtonStyleTransparent("widgets/icon-levels"));
		ImageTextButton achievements = new ImageTextButton(
				_("ingame_menu_achievements"),
				helper.getImageTextButtonStyleTransparent("widgets/icon-trophy"));
		ImageTextButton mainMenu = new ImageTextButton(
				_("ingame_menu_main_menu"),
				helper.getImageTextButtonStyleTransparent("widgets/icon-home"));

		// hard code ALL the stuff! (force buttons to look like they are
		// supposed to look)
		continueGame.getImageCell().padRight(50).padLeft(100).size(100);
		reset.getImageCell().padRight(50).padLeft(100).size(100);
		settings.getImageCell().padRight(50).padLeft(100).size(100);
		levelOverview.getImageCell().padRight(50).padLeft(100).size(100);
		achievements.getImageCell().padRight(50).padLeft(100).size(100);
		mainMenu.getImageCell().padRight(50).padLeft(100).size(100);

		continueGame.getLabelCell().expandX().left();
		reset.getLabelCell().expandX().left();
		settings.getLabelCell().expandX().left();
		levelOverview.getLabelCell().expandX().left();
		achievements.getLabelCell().expandX().left();
		mainMenu.getLabelCell().expandX().left();

		// add listeners
		continueGame.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hide();
			}
		});
		reset.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				// TODO notification?
				gameController.reset();
				game.showPlacementModeScreen(gameController);
				hide();
			}
		});
		settings.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showSettingsScreen(predecessor);
			}
		});
		levelOverview.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showLevelOverviewScreen(game.getLevelPackagesController()
						.getLevelController(
								gameController.getLevel().getPackageIndex()), predecessor);
			}
		});
		achievements.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showAchievementScreen(predecessor);
			}
		});
		mainMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showMainMenuScreen();
			}
		});

		clear();

		defaults().width(500).height(90).padLeft(100).padRight(100);
		add(continueGame).padTop(20).row();
		add(reset).row();
		add(settings).row();
		add(levelOverview).row();
		add(achievements).row();
		add(mainMenu).padBottom(20).row();
	}
}
