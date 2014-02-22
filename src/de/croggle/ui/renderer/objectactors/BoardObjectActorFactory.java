package de.croggle.ui.renderer.objectactors;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.operations.BoardObjectVisitor;

public class BoardObjectActorFactory {
	private BoardObjectActorFactory() {

	}

	public static enum BoardObjectActorType {
		EGG, AGED_ALLIGATOR, COLORED_ALLIGATOR
	}

	public static BoardObjectActorType getType(BoardObjectActor a) {
		return getType(a.getBoardObject());
	}

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

	public static BoardObjectActor createActor(InternalBoardObject o,
			boolean colorBlindEnabled) {
		switch (getType(o)) {
		case EGG: {
			return new EggActor((Egg) o, colorBlindEnabled);
		}
		case AGED_ALLIGATOR: {
			return new AgedAlligatorActor((AgedAlligator) o);
		}
		case COLORED_ALLIGATOR: {
			return new ColoredAlligatorActor((ColoredAlligator) o,
					colorBlindEnabled);
		}
		default:
			throw new IllegalStateException("This should never happen");
		}
	}

	public static BoardObjectActor copyActor(BoardObjectActor a,
			boolean copyBoardObject) {
		BoardObjectActor result;
		InternalBoardObject ibo = copyBoardObject ? a.getBoardObject().copy()
				: a.getBoardObject();
		switch (getType(a)) {
		case EGG: {
			EggActor ea = (EggActor) a;
			EggActor res = new EggActor((Egg) ibo, ea.colorBlindEnabled);
			result = res;
			break;
		}
		case AGED_ALLIGATOR: {
			AgedAlligatorActor aa = (AgedAlligatorActor) a;
			AgedAlligatorActor res = new AgedAlligatorActor((AgedAlligator) ibo);
			result = res;
			break;
		}
		case COLORED_ALLIGATOR: {
			ColoredAlligatorActor ca = (ColoredAlligatorActor) a;
			ColoredAlligatorActor res = new ColoredAlligatorActor(
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
}
