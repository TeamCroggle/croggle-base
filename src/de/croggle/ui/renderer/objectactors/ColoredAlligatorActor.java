package de.croggle.ui.renderer.objectactors;

import com.badlogic.gdx.scenes.scene2d.Action;

import de.croggle.game.board.ColoredAlligator;

/**
 * An actor used for representing a colored alligator.
 */
public class ColoredAlligatorActor extends ColoredBoardObjectActor {

	ColoredAlligatorActor(ColoredAlligator alligator, boolean colorBlindEnabled) {
		super(alligator, colorBlindEnabled, "coloredalligator/foreground",
				"coloredalligator/background");
	}

	/**
	 * Signals the actor to enter the eating rendering state. That is, an
	 * alligator with a specific color, mouth opened. Will initiate a transition
	 * animation from mouth closed to open if it was closed previously.
	 * 
	 * @param duration
	 *            The number of seconds to pass until the animation is done
	 * @return an action that will transition the actor, when executed, from
	 *         normal to eating state
	 */
	public Action enterEatingStateAction(float duration) {
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
	public ColoredAlligator getBoardObject() {
		return (ColoredAlligator) super.getBoardObject();
	}
}
