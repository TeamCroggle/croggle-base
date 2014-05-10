package de.croggle.ui.renderer.objectactors;

import com.badlogic.gdx.scenes.scene2d.Action;

import de.croggle.game.board.Egg;

/**
 * An actor used for representing an egg.
 */
public class EggActor extends ColoredBoardObjectActor {

	EggActor(Egg egg, boolean colorBlindEnabled) {
		super(egg, colorBlindEnabled, "egg/foreground", "egg/background");
	}

	/**
	 * Returns an Action that transitions between a normal egg and one with a
	 * broken shell within the given time.
	 * 
	 * @param duration
	 *            The time it takes to transition,in seconds
	 */
	public Action enterHatchingStateAction(float duration) {
		Action result = new Action() {
			@Override
			public boolean act(float delta) {
				return true;
			}
		};
		result.setActor(this);
		return result;
	}

	@Override
	public Egg getBoardObject() {
		return (Egg) super.getBoardObject();
	}
}
