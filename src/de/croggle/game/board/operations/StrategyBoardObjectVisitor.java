package de.croggle.game.board.operations;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;

/**
 * A visitor for traversing trees of BoardObjects. It visits a node at first and
 * then each of it's children from left to right.
 */
public abstract class StrategyBoardObjectVisitor implements BoardObjectVisitor {
	private boolean canceled = false;

	protected boolean isCanceled() {
		return canceled;
	}

	protected void cancelTraversal() {
		canceled = true;
	}

	protected void resetCancelation() {
		canceled = false;
	}

	protected abstract void beginTraversal(BoardObject b);

	protected void dispatchEgg(Egg egg) {

	}

	protected void dispatchColoredAlligator(ColoredAlligator alligator) {

	}

	protected void dispatchAgedAlligator(AgedAlligator alligator) {

	}

	protected void dispatchBoard(Board board) {

	}
}
