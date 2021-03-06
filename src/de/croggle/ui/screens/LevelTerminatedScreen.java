package de.croggle.ui.screens;

import static de.croggle.backends.BackendHelper.getAssetDirPath;
import static de.croggle.data.LocalizationHelper._;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.croggle.AlligatorApp;
import de.croggle.data.AssetManager;
import de.croggle.game.GameController;
import de.croggle.game.achievement.Achievement;
import de.croggle.game.achievement.AchievementController;
import de.croggle.game.level.Level;
import de.croggle.game.level.LevelPackagesController;
import de.croggle.ui.StyleHelper;
import de.croggle.ui.actors.NewAchievementDialog;

/**
 * First screen seen after completing a level. For reference see ``Pflichtenheft
 * 10.5.6 / Abbildung 15''.
 */
public class LevelTerminatedScreen extends AbstractScreen {

	private final GameController gameController;
	private final AchievementController achievementController;
	private final LevelPackagesController packagesController;
	private final boolean won;

	/**
	 * Creates the level terminated screen that is shown to the player after the
	 * completion of a level.
	 * 
	 * @param game
	 *            the backreference to the central game
	 * @param controller
	 *            the game controller, who is responsible for the completed
	 *            level
	 * @param won
	 */
	public LevelTerminatedScreen(AlligatorApp game, GameController controller,
			boolean won) {
		super(game);
		gameController = controller;
		achievementController = game.getAchievementController();
		packagesController = game.getLevelPackagesController();
		this.won = won;

		setBackground(getAssetDirPath() + "textures/background-default.png");
		AssetManager.getInstance().finishLoading();

		fillTable();
	}

	private void fillTable() {
		StyleHelper helper = StyleHelper.getInstance();

		ImageButton image = new ImageButton(
				helper.getDrawable("widgets/level-solved"));
		Label message = new Label(won ? _("level_solved") : _("level_failed"),
				helper.getBlackLabelStyle(50));
		ImageButton next = new ImageButton(
				helper.getImageButtonStyleRound("widgets/icon-next"));
		ImageButton levelOverview = new ImageButton(
				helper.getImageButtonStyleRound("widgets/icon-levels"));
		ImageButton replay = new ImageButton(
				helper.getImageButtonStyleRound("widgets/icon-reset"));
		ImageButton home = new ImageButton(
				helper.getImageButtonStyleRound("widgets/icon-home"));
		ImageButton achievements = new ImageButton(
				helper.getDrawable("widgets/icon-trophy"));

		levelOverview.addListener(new LevelOverviewClickListener());
		next.addListener(new NextLevelClickListener());
		replay.addListener(new ReplayLevelClickListener());
		achievements.addListener(new ShowNewAchievementsListener());
		home.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showMainMenuScreen();
			}
		});

		// fit icons that are too large
		replay.getImageCell().pad(5);
		levelOverview.getImageCell().pad(7);

		table.pad(30);

		if (won) {
			table.add(image).colspan(5).size(300).expand();
			table.row();
			table.add(message).colspan(5);
			table.row();

			if (!achievementController.getLatestUnlockedAchievements()
					.isEmpty()) {
				table.add(achievements).left().size(150);
			}

			table.add(replay).size(100).bottom().space(30).expandX().right();
			table.add(levelOverview).size(100).bottom().space(30);
			table.add(home).size(100).bottom().space(30);
			table.add(next).size(150);
		} else {
			table.add(message).colspan(5).expand();
			table.row();
			if (!achievementController.getLatestUnlockedAchievements()
					.isEmpty()) {
				table.add(achievements).left().size(150);
			}
			table.add(levelOverview).size(100).bottom().space(30).expandX()
					.right();
			table.add(home).size(100).bottom().space(30);
			table.add(replay).size(150);
		}
	}

	private class LevelOverviewClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			game.showLevelOverviewScreen(packagesController
					.getLevelController(gameController.getLevel()
							.getPackageIndex()));
		}
	}

	private class NextLevelClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			int currentLevelIndex = gameController.getLevel().getLevelIndex();
			int currentPackageIndex = gameController.getLevel()
					.getPackageIndex();

			if (packagesController.getLevelPackages().size() < currentPackageIndex + 1
					|| (packagesController.getLevelPackages().size() == currentPackageIndex + 1 && LevelPackagesController
							.getPackageSize(currentPackageIndex) - 1 <= currentLevelIndex)) {
				game.showMainMenuScreen();
			} else if (LevelPackagesController
					.getPackageSize(currentPackageIndex) - 1 <= currentLevelIndex) {
				game.showLevelOverviewScreen(packagesController
						.getLevelController(currentPackageIndex + 1));
			} else {
				final Level nextLevel = packagesController.getLevelController(
						currentPackageIndex).getLevel(currentLevelIndex + 1);
				if (nextLevel.getUnlocked()) {
					final GameController newGameController = nextLevel
							.createGameController(game);
					newGameController.register(game.getStatisticController());
					game.showPlacementModeScreen(newGameController);
				} else {
					final Level currentLevel = gameController.getLevel();
					final GameController newGameController = currentLevel
							.createGameController(game);
					newGameController.register(game.getStatisticController());
					game.showPlacementModeScreen(newGameController);
				}
			}
		}
	}

	private class ReplayLevelClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			final Level currentLevel = gameController.getLevel();
			final GameController newGameController = currentLevel
					.createGameController(game);
			newGameController.register(game.getStatisticController());
			game.showPlacementModeScreen(newGameController);
		}
	}

	private class ShowNewAchievementsListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			List<Achievement> newAchievements = achievementController
					.getLatestUnlockedAchievements();
			for (Achievement achievement : newAchievements) {
				Dialog achievementDialog = new NewAchievementDialog(
						achievement, achievement.getIndex(), true);
				achievementDialog.show(stage);
			}
		}
	}

	@Override
	protected void showLogicalPredecessor() {
		game.showLevelOverviewScreen(game
				.getLevelPackagesController()
				.getLevelController(gameController.getLevel().getPackageIndex()));
	}
}
