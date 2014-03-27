package de.croggle.ui.screens;

import static de.croggle.backends.BackendHelper.getAssetDirPath;
import static de.croggle.data.LocalizationHelper._;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import de.croggle.AlligatorApp;
import de.croggle.backends.BackendHelper;
import de.croggle.data.AssetManager;
import de.croggle.data.persistence.Setting;
import de.croggle.data.persistence.SettingChangeListener;
import de.croggle.game.ColorController;
import de.croggle.game.MultipleChoiceGameController;
import de.croggle.game.Tutorial;
import de.croggle.game.TutorialHelper;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.IllegalBoardException;
import de.croggle.game.board.operations.CreateWidthMap;
import de.croggle.game.level.LevelPackage;
import de.croggle.game.level.LevelPackagesController;
import de.croggle.game.level.MultipleChoiceLevel;
import de.croggle.ui.StyleHelper;
import de.croggle.ui.actors.IngameMenuDialog;
import de.croggle.ui.actors.NotificationDialog;
import de.croggle.ui.renderer.BoardActor;
import de.croggle.ui.renderer.layout.ActorLayoutConfiguration;

/**
 * Screen which the player sees when entering Multiple choice levels.
 */
public class MultipleChoiceScreen extends AbstractScreen implements
		SettingChangeListener {

	private final MultipleChoiceGameController gameController;
	private BoardActor boardActor;
	private CheckBox checkboxes[];
	private final Dialog goalDialog;

	private boolean showDialogs = true;

	/**
	 * Creates the base screen of a multiple choice level, which is shown to the
	 * player upon entering a multiple choice level.
	 * 
	 * @param game
	 *            the backreference to the central game
	 * @param controller
	 *            the game controller responsible for the multiple choice level
	 */
	public MultipleChoiceScreen(AlligatorApp game,
			MultipleChoiceGameController controller) {
		super(game);
		gameController = controller;

		AssetManager assetManager = AssetManager.getInstance();
		assetManager.load(getAssetDirPath() + "textures/pack.atlas",
				TextureAtlas.class);
		goalDialog = new Dialog("", StyleHelper.getInstance().getDialogStyle());

		fillTable();
		final int packageIndex = gameController.getLevel().getPackageIndex();
		final LevelPackagesController packagesController = game
				.getLevelPackagesController();
		final LevelPackage pack = packagesController.getLevelPackages().get(
				packageIndex);
		setBackground(pack.getDesign());
		game.getSettingController().addSettingChangeListener(this);

		// load graphics for animation/tutorial
		if (gameController.getLevel().hasAnimation()) {
			List<String> animations = gameController.getLevel().getAnimation();
			for (String animation : animations) {
				assetManager.load(animation, Texture.class);
			}
		}
	}

	@Override
	protected void onShow() {
		BackendHelper.acquireWakeLock();

		gameController.setTimeStamp();
		gameController.enterPlacement();

		if (showDialogs) {
			showTutorial();
			showGoal();
			showDialogs = false;
		}

	}

	private void showGoal() {
		goalDialog.show(stage);
	}

	private void showTutorial() {
		if (gameController.getLevel().hasAnimation()) {
			TutorialHelper helper = TutorialHelper.getInstance();
			List<String> tutorials = gameController.getLevel().getAnimation();
			for (int i = tutorials.size() - 1; i >= 0; i--) {
				Tutorial tutorial = helper.getTutorial(tutorials.get(i));
				buildTutorialDialog(tutorial.getPicturePath(),
						_(tutorial.getText()));
			}
		}
	}

	@Override
	public void hide() {
		super.hide();
		BackendHelper.releaseWakeLock();
		gameController.updateTime();
		gameController.setTimeStamp();
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		checkZoom();
	}

	private void fillTable() {
		StyleHelper helper = StyleHelper.getInstance();

		Table leftTable = new Table();
		ImageButton menu = new ImageButton(
				helper.getImageButtonStyleRound("widgets/icon-menu"));
		Button goal = new ImageButton(
				helper.getImageButtonStyleRound("widgets/icon-goal"));
		ImageButton startSimulation = new ImageButton(StyleHelper.getInstance()
				.getImageButtonStyleRound("widgets/icon-next"));

		// add listeners
		menu.addListener(new MenuClickListener());
		startSimulation.addListener(new StartSimulationListener());

		final ColorController colorController = gameController
				.getColorController();
		final ActorLayoutConfiguration config = new ActorLayoutConfiguration();
		config.setColorController(colorController);

		MultipleChoiceLevel level = (MultipleChoiceLevel) gameController
				.getLevel();

		checkboxes = new CheckBox[3];
		Table answerTable = new Table();

		// dummy table for extended scrolling
		answerTable.add(new Table()).width(100).left();

		for (int i = 0; i < level.getAnswers().length; i++) {
			Board answer = level.getAnswers()[i];
			Table boardTable = new Table();
			Table pageTable = new Table();
			checkboxes[i] = new CheckBox("", helper.getCheckBoxStyle());
			boardActor = new BoardActor(answer, config);
			boardActor.setColorBlindEnabled(game.getSettingController()
					.getCurrentSetting().isColorblindEnabled());
			game.getSettingController().addSettingChangeListener(boardActor);
			boardActor.setZoomAndPanEnabled(false);

			boardTable.add(boardActor).expand().fill();
			pageTable.add(checkboxes[i]).top().center().pad(20, 0, 5, 0);
			pageTable.row();
			pageTable.add(boardTable).center().expand().fill();

			// calculate width of answer
			Map<BoardObject, Float> map = CreateWidthMap.create(answer);
			float width = map.get(answer);
			width *= config.getUniformObjectWidth();

			answerTable.add(pageTable).width(width).minWidth(270).expandY()
					.fillY().space(30);
		}
		// dummy table for extended scrolling
		answerTable.add(new Table()).width(300);

		ScrollPane scrollPane = new ScrollPane(answerTable);
		scrollPane.setScrollingDisabled(false, true);

		leftTable.pad(30);
		leftTable.defaults().space(30);
		leftTable.add(menu).size(100).top().left();
		leftTable.row();
		leftTable.add(goal).expand().size(100).top().left();
		leftTable.row();
		leftTable.add(startSimulation).size(200).bottom().right();

		table.stack(scrollPane, leftTable).expand().fill();

		// TODO remove Simulationbutton and checkboxes and add simulationbutton
		// on each page.
		onSettingChange(game.getSettingController().getCurrentSetting());

		// group checkboxes so only one can be checked at a time
		// (max check amount is one per default)
		ButtonGroup checkboxGroup = new ButtonGroup();
		checkboxGroup.setMinCheckCount(0);
		for (int i = 0; i < level.getAnswers().length; i++) {
			checkboxGroup.add(checkboxes[i]);
		}

		// prepare goalDialog
		BoardActor goalBoard = new BoardActor(gameController.getLevel()
				.getInitialBoard(), config);
		goalBoard.setZoomAndPanEnabled(true);
		goalBoard.setColorBlindEnabled(game.getSettingController()
				.getCurrentSetting().isColorblindEnabled());
		game.getSettingController().addSettingChangeListener(goalBoard);
		Table goalTable = new Table();
		goalTable.add(goalBoard).size(getViewportWidth() * 1.5f,
				getViewportHeight());
		goal.addListener(new GoalClickListener());
		TextButton okay = new TextButton(_("button_ok"),
				helper.getTextButtonStyle());
		okay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				goalDialog.hide();
			}
		});

		goalDialog.add(goalTable).width(getViewportWidth() - 250)
				.height(getViewportHeight());
		goalDialog.stack(okay).center().bottom().width(300).height(70).pad(20);

		Dialog.fadeDuration = 0f;
	}

	private void buildTutorialDialog(String animationPath, String text) {
		AssetManager manager = AssetManager.getInstance();
		StyleHelper helper = StyleHelper.getInstance();

		final Dialog tutorial = new Dialog("", StyleHelper.getInstance()
				.getDialogStyle());
		tutorial.clear();
		Dialog.fadeDuration = 0f;

		Table buttonTable = new Table();
		Table imageTable = new Table();
		Drawable drawable = new TextureRegionDrawable(new TextureRegion(
				manager.get(animationPath, Texture.class)));
		// used image button here because it keeps the ratio of the texture
		ImageButton tutorialImage = new ImageButton(drawable);
		ImageButton okay = new ImageButton(
				helper.getImageButtonStyleRound("widgets/icon-check"));
		Label label = new Label(text, helper.getBlackLabelStyle());
		label.setWrap(true);
		label.setAlignment(Align.center);

		okay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				tutorial.hide();
			}
		});

		buttonTable.add(okay).size(100).bottom().right().expand().pad(30);
		imageTable.add(tutorialImage).width(800).height(350);
		imageTable.row();
		imageTable.add(label).minHeight(100).width(600).left()
				.pad(0, 30, 30, 10);
		tutorial.stack(imageTable, buttonTable).height(500).width(800);
		tutorial.show(stage);
	}

	private void checkZoom() {
		// In a MC Level there is no zoom function
	}

	@Override
	public void onSettingChange(Setting setting) {
		// In a MC Level there is no zoom function

	}

	private class MenuClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			Dialog menuDialog = new IngameMenuDialog(game, gameController,
					MultipleChoiceScreen.this);
			menuDialog.show(stage);
		}
	}

	private class StartSimulationListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			super.clicked(event, x, y);
			int answer = -1;
			for (int i = 0; i < checkboxes.length; i++) {
				if (checkboxes[i].isChecked()) {
					if (answer == -1) {
						answer = i;
					} else {
						Dialog dialog = new NotificationDialog(
								_("multiple_choice_dialog"));
						dialog.show(stage);
						return;
					}
				}
			}
			if (answer == -1) {
				Dialog dialog = new NotificationDialog(
						_("multiple_choice_dialog"));
				dialog.show(stage);
				return;
			}
			gameController.setSelection(answer);
			try {
				game.showSimulationModeScreen(gameController);
			} catch (IllegalBoardException e) {
				// This can't happen in a MC Level
			}
		}
	}

	private class GoalClickListener extends ClickListener {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			showGoal();
		}
	}

	@Override
	protected void showLogicalPredecessor() {
		game.showLevelOverviewScreen(game
				.getLevelPackagesController()
				.getLevelController(gameController.getLevel().getPackageIndex()));
	}
}
