package de.croggle.ui.renderer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import de.croggle.game.Color;
import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.ui.renderer.objectactors.AgedAlligatorActor;
import de.croggle.ui.renderer.objectactors.BoardObjectActor;
import de.croggle.ui.renderer.objectactors.BoardObjectActorFactory;
import de.croggle.ui.renderer.objectactors.ColoredAlligatorActor;
import de.croggle.ui.renderer.objectactors.EggActor;

/**
 * This is a helper class for all classes who want to take part in the
 * {@link DragAndDrop dragging and dropping} of {@link BoardObjectActor}s.
 * Therefore it instantiates and offers the caller three Actors (normal
 * dragging, valid drag target, invalid drag target) that are necessary to
 * register drag sources. The class also takes care of convenience features like
 * the automatic panning of the BoardActor's world when the user drags to the
 * edge of the {@link BoardActor}.
 * 
 */
class BoardObjectActorDragging {

	private final com.badlogic.gdx.graphics.Color validColor = new com.badlogic.gdx.graphics.Color(
			0, 1, 0, 1);
	private final com.badlogic.gdx.graphics.Color invalidColor = new com.badlogic.gdx.graphics.Color(
			1, 0, 0, 1);

	private final float autoPanBorderWidth;
	private final float autoPanBorderHeight;

	protected final BoardActor b;

	protected final ColoredAlligatorActor coloredDragActor;
	protected final EggActor eggDragActor;
	protected final AgedAlligatorActor agedDragActor;

	protected final ColoredAlligatorActor coloredValidDragActor;
	protected final EggActor eggValidDragActor;
	protected final AgedAlligatorActor agedValidDragActor;

	protected final ColoredAlligatorActor coloredInvalidDragActor;
	protected final EggActor eggInvalidDragActor;
	protected final AgedAlligatorActor agedInvalidDragActor;

	public BoardObjectActorDragging(BoardActorLayoutEditing editing) {
		b = editing.getBoardActor();

		// TODO maybe bind the value somehow to BoardActor size changes
		autoPanBorderWidth = Math.min(b.getWidth() / 2, 150);
		autoPanBorderHeight = Math.min(b.getHeight() / 2, 100);

		boolean colorBlind = b.getLayout().getLayoutConfiguration()
				.isColorBlindEnabled();

		{
			coloredDragActor = BoardObjectActorFactory
					.instantiateColoredAlligatorActor(new ColoredAlligator(
							false, false, Color.uncolored(), false), colorBlind);
			coloredDragActor.addAction(new AutoPanAction());

			eggDragActor = BoardObjectActorFactory.instantiateEggActor(new Egg(
					false, false, Color.uncolored(), false), colorBlind);
			eggDragActor.addAction(new AutoPanAction());

			agedDragActor = BoardObjectActorFactory
					.instantiateAgedAlligatorActor(new AgedAlligator(false,
							false));
			agedDragActor.addAction(new AutoPanAction());
		}

		{
			coloredValidDragActor = BoardObjectActorFactory
					.instantiateColoredAlligatorActor(
							coloredDragActor.getBoardObject(), colorBlind);
			coloredValidDragActor.setColor(validColor);
			coloredValidDragActor.addAction(new AutoPanAction());

			eggValidDragActor = BoardObjectActorFactory.instantiateEggActor(
					eggDragActor.getBoardObject(), colorBlind);
			eggValidDragActor.setColor(validColor);
			eggValidDragActor.addAction(new AutoPanAction());

			agedValidDragActor = BoardObjectActorFactory
					.instantiateAgedAlligatorActor(agedDragActor
							.getBoardObject());
			agedValidDragActor.setColor(validColor);
			agedValidDragActor.addAction(new AutoPanAction());
		}

		{
			coloredInvalidDragActor = BoardObjectActorFactory
					.instantiateColoredAlligatorActor(
							coloredDragActor.getBoardObject(), colorBlind);
			coloredInvalidDragActor.setColor(invalidColor);
			coloredInvalidDragActor.addAction(new AutoPanAction());

			eggInvalidDragActor = BoardObjectActorFactory.instantiateEggActor(
					eggDragActor.getBoardObject(), colorBlind);
			eggInvalidDragActor.setColor(invalidColor);
			eggInvalidDragActor.addAction(new AutoPanAction());

			agedInvalidDragActor = BoardObjectActorFactory
					.instantiateAgedAlligatorActor(agedDragActor
							.getBoardObject());
			agedInvalidDragActor.setColor(invalidColor);
			agedInvalidDragActor.addAction(new AutoPanAction());
		}
	}

	/**
	 * This action gets automatically added to all drag actors, so when the user
	 * moves them to the edge of the BoardActor, the BoardActor will pan to show
	 * the parts of the world beyond the edge.
	 */
	private class AutoPanAction extends Action {

		// only act once in %divider% milliseconds
		private final float divider = 0.01f;

		private final float xDistance = 2;
		private final float yDistance = 2;

		private float timePassed;

		@Override
		public boolean act(float delta) {
			if (timePassed >= divider) {
				timePassed = 0;
				Actor a = getActor();
				float x = a.getX();
				float y = a.getY();
				float maxx = x + a.getWidth();
				float maxy = y + a.getHeight();
				float w = b.getWidth();
				float h = b.getHeight();
				Vector2 pMin = new Vector2(x, y);
				Vector2 pMax = new Vector2(maxx, maxy);
				pMin = b.stageToLocalCoordinates(pMin);
				pMax = b.stageToLocalCoordinates(pMax);
				if (pMin.x <= autoPanBorderWidth) {
					if (pMax.x < w - autoPanBorderWidth) {
						b.panActorDistance(xDistance, 0);
					}
				} else if (pMax.x >= w - autoPanBorderWidth) {
					b.panActorDistance(-xDistance, 0);
				}

				if (pMin.y <= autoPanBorderHeight) {
					if (pMax.y < h - autoPanBorderHeight) {
						b.panActorDistance(0, yDistance);
					}
				} else if (pMax.y >= h - autoPanBorderHeight) {
					b.panActorDistance(0, -yDistance);
				}
			}
			timePassed += delta;

			// never end
			return false;
		}
	}

	public BoardObjectActor getDragActor(final BoardObjectActor a) {
		BoardObjectActor result;
		switch (BoardObjectActorFactory.getType(a)) {
		case AGED_ALLIGATOR: {
			result = agedDragActor;
			break;
		}
		case COLORED_ALLIGATOR: {
			result = coloredDragActor;
			break;
		}
		case EGG: {
			result = eggDragActor;
			break;
		}
		default:
			throw new IllegalStateException("This should never happen");

		}
		result.setSize(a.getWidth(), a.getHeight());
		result.setScale(b.getZoom());

		return result;
	}

	public BoardObjectActor getValidDragActor(final BoardObjectActor a) {
		BoardObjectActor result;
		switch (BoardObjectActorFactory.getType(a)) {
		case AGED_ALLIGATOR: {
			result = agedValidDragActor;
			break;
		}
		case COLORED_ALLIGATOR: {
			result = coloredValidDragActor;
			break;
		}
		case EGG: {
			result = eggValidDragActor;
			break;
		}
		default:
			throw new IllegalStateException("This should never happen");

		}
		result.setSize(a.getWidth(), a.getHeight());
		result.setScale(b.getZoom());

		return result;
	}

	public BoardObjectActor getInvalidDragActor(final BoardObjectActor a) {
		BoardObjectActor result;
		switch (BoardObjectActorFactory.getType(a)) {
		case AGED_ALLIGATOR: {
			result = agedInvalidDragActor;
			break;
		}
		case COLORED_ALLIGATOR: {
			result = coloredInvalidDragActor;
			break;
		}
		case EGG: {
			result = eggInvalidDragActor;
			break;
		}
		default:
			throw new IllegalStateException("This should never happen");

		}
		result.setSize(a.getWidth(), a.getHeight());
		result.setScale(b.getZoom());

		return result;
	}
}
