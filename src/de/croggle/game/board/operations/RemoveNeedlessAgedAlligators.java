package de.croggle.game.board.operations;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;
import de.croggle.game.event.BoardEventMessenger;

/**
 * Operation to remove aged allgiators which are not needed according to the
 * associativity of the lambda calculus. The implementation currently looks for
 * aged alligators, which are only preceded by unbound variables or nothing at
 * all.
 * 
 */
public class RemoveNeedlessAgedAlligators extends BFBUVisitor {
	private final BoardEventMessenger boardMessenger;

	private RemoveNeedlessAgedAlligators(Parent family,
			BoardEventMessenger boardMessenger) {
		this.boardMessenger = boardMessenger;
	}

	public static void remove(BoardObject family,
			BoardEventMessenger boardMessenger) {
		if (!(family instanceof Parent)) {
			return;
		}
		RemoveNeedlessAgedAlligators remover = new RemoveNeedlessAgedAlligators(
				(Parent) family, boardMessenger);
		remover.beginTraversal(family);
	}

	@Override
	public void dispatchColoredAlligator(ColoredAlligator alligator) {
		checkChildren(alligator);
	}

	@Override
	public void dispatchAgedAlligator(AgedAlligator alligator) {
		checkChildren(alligator);
	}

	@Override
	public void dispatchBoard(Board board) {
		checkChildren(board);
	}

	private void checkChildren(Parent p) {
		int firstNotEggPosition = 0;
		InternalBoardObject currentChild;
		// traverse all children
		while (firstNotEggPosition < p.getChildCount()) {
			// children must be eggs
			currentChild = p.getChildAtPosition(firstNotEggPosition);
			if (currentChild.getClass() != Egg.class) {
				break;
			}
			// and free
			if (Boundedness.isBound((Egg) currentChild)) {
				firstNotEggPosition = p.getChildCount();
				break;
			}
			firstNotEggPosition++;
		}
		if (firstNotEggPosition < p.getChildCount()) {
			InternalBoardObject firstNotEgg = p
					.getChildAtPosition(firstNotEggPosition);

			if (firstNotEgg.getClass() == AgedAlligator.class) {
				int i = 0;
				for (InternalBoardObject child : (AgedAlligator) firstNotEgg) {
					p.insertChild(child, firstNotEggPosition + i);
					i++;
				}
				p.removeChild(firstNotEgg);
				if (boardMessenger != null) {
					boardMessenger.notifyAgedAlligatorVanishes(
							(AgedAlligator) firstNotEgg, 0);
				}
			}
		}
	}
}
