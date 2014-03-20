package de.croggle.ui.renderer.objectactors;

import com.badlogic.gdx.scenes.scene2d.Actor;

import de.croggle.AlligatorApp;
import de.croggle.data.persistence.Setting;
import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.operations.BoardObjectVisitor;

/**
 * Helper class providing easy to use functionality for creating new or cloning
 * existing {@link BoardObjectActor}s. Using this class primarily helps to avoid
 * reoccurring type checks of the actors' InternalBoardObjects
 * 
 */
public class BoardObjectActorFactory {
	private static boolean headlessActors = false;

	private BoardObjectActorFactory() {
	}

	/**
	 * An enumeration representing the different types of concrete
	 * {@link BoardObjectActor}s
	 * 
	 */
	public static enum BoardObjectActorType {
		EGG, AGED_ALLIGATOR, COLORED_ALLIGATOR
	}

	/**
	 * Performs the same task as {@link #getType(InternalBoardObject)}, just
	 * wrapping the {@link BoardObjectActor#getBoardObject()} method to call it.
	 * 
	 * @param a
	 *            a {@link BoardObjectActor} whose type is to be found out
	 * @return a {@link BoardObjectActorType} matching the given
	 *         {@link InternalBoardObject}
	 * 
	 * @see #getType(InternalBoardObject)
	 */
	public static BoardObjectActorType getType(BoardObjectActor a) {
		return getType(a.getBoardObject());
	}

	/**
	 * Small utility function to find out the {@link BoardObjectActorType type}
	 * a {@link BoardObjectActor} should have regarding a given
	 * {@link InternalBoardObject}.
	 * 
	 * @param o
	 *            the {@link InternalBoardObject} for which the proper
	 *            {@link BoardObjectActorType} is to be found out
	 * @return a {@link BoardObjectActorType} matching the given
	 *         {@link InternalBoardObject}
	 */
	public static BoardObjectActorType getType(InternalBoardObject o) {
		final BoardObjectActorType result[] = new BoardObjectActorType[1];
		BoardObjectVisitor visitor = new BoardObjectVisitor() {
			@Override
			public void visitEgg(Egg egg) {
				result[0] = BoardObjectActorType.EGG;
			}

			@Override
			public void visitColoredAlligator(ColoredAlligator alligator) {
				result[0] = BoardObjectActorType.COLORED_ALLIGATOR;
			}

			@Override
			public void visitBoard(Board board) {
				// Just ignore
			}

			@Override
			public void visitAgedAlligator(AgedAlligator alligator) {
				result[0] = BoardObjectActorType.AGED_ALLIGATOR;
			}
		};
		o.accept(visitor);

		return result[0];
	}

	public static ColoredAlligatorActor instantiateColoredAlligatorActor(
			ColoredAlligator alligator, boolean colorBlindEnabled) {
		if (AlligatorApp.DEBUG && headlessActors) {
			return new HeadlessColoredAlligatorActor(alligator,
					colorBlindEnabled);
		} else {
			return new ColoredAlligatorActor(alligator, colorBlindEnabled);
		}
	}

	public static AgedAlligatorActor instantiateAgedAlligatorActor(
			AgedAlligator alligator) {
		if (AlligatorApp.DEBUG && headlessActors) {
			return new HeadlessAgedAlligatorActor(alligator);
		} else {
			return new AgedAlligatorActor(alligator);
		}
	}

	public static EggActor instantiateEggActor(Egg egg,
			boolean colorBlindEnabled) {
		if (AlligatorApp.DEBUG && headlessActors) {
			return new HeadlessEggActor(egg, colorBlindEnabled);
		} else {
			return new EggActor(egg, colorBlindEnabled);
		}
	}

	/**
	 * Creates a new {@link BoardObjectActor} representing and matching the
	 * actual type of the given {@link InternalBoardObject}. It can be of type
	 * {@link AgedAlligatorActor}, {@link ColoredAlligatorActor} or
	 * {@link EggActor}
	 * 
	 * @param o
	 *            the {@link InternalBoardObject} to create a representation for
	 * @param colorBlindEnabled
	 *            whether the created {@link Actor} is supposed to be drawing
	 *            itself in {@link Setting#isColorblindEnabled() color blind
	 *            mode} or not
	 * @return a {@link BoardObjectActor} suitable to represent the given
	 *         {@link InternalBoardObject}
	 */
	public static BoardObjectActor createActorFor(InternalBoardObject o,
			boolean colorBlindEnabled) {
		switch (getType(o)) {
		case EGG: {
			return instantiateEggActor((Egg) o, colorBlindEnabled);
		}
		case AGED_ALLIGATOR: {
			return instantiateAgedAlligatorActor((AgedAlligator) o);
		}
		case COLORED_ALLIGATOR: {
			return instantiateColoredAlligatorActor((ColoredAlligator) o,
					colorBlindEnabled);
		}
		default:
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Copies given {@link BoardObjectActor}, no matter of which type. The
	 * {@link InternalBoardObject} used by the original Actor can be either
	 * chosen to be copied or to be shared between both actors, resulting in a
	 * shallow copy.
	 * 
	 * @param a
	 *            the {@link BoardObjectActor} to be copied
	 * @param copyBoardObject
	 *            whether or not to copy the {@link InternalBoardObject}
	 *            represented by a
	 * @return a copy of the given {@link BoardObjectActor}
	 */
	public static BoardObjectActor copyActor(BoardObjectActor a,
			boolean copyBoardObject) {
		BoardObjectActor result;
		InternalBoardObject ibo = copyBoardObject ? a.getBoardObject().copy()
				: a.getBoardObject();
		switch (getType(a)) {
		case EGG: {
			EggActor ea = (EggActor) a;
			EggActor res = instantiateEggActor((Egg) ibo, ea.colorBlindEnabled);
			result = res;
			break;
		}
		case AGED_ALLIGATOR: {
			AgedAlligatorActor res = instantiateAgedAlligatorActor((AgedAlligator) ibo);
			result = res;
			break;
		}
		case COLORED_ALLIGATOR: {
			ColoredAlligatorActor ca = (ColoredAlligatorActor) a;
			ColoredAlligatorActor res = instantiateColoredAlligatorActor(
					(ColoredAlligator) ibo, ca.colorBlindEnabled);
			result = res;
			break;
		}
		default:
			throw new IllegalStateException("This should never happen");
		}
		result.setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
		result.setScale(a.getScaleX(), a.getScaleY());
		result.setColor(a.getColor());
		return result;
	}

	public static void setActorsHeadless(boolean headless) {
		BoardObjectActorFactory.headlessActors = headless;
	}
}
