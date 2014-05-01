package de.croggle.game.board.operations;

import java.util.LinkedList;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;

/**
 * Breadth first top down visitor. Visits all elements starting with the given
 * root, whereby higher elements in the tree will always be dispatched earlier
 * than lower elements. Ensures for each level of nodes in the tree hierarchy
 * all levels above have already been dispatched.
 * 
 * 
 */
public abstract class BFTDVisitor extends StrategyBoardObjectVisitor {
	private final LinkedList<BoardObject> queue = new LinkedList<BoardObject>();

	@Override
	protected void beginTraversal(BoardObject b) {
		queue.clear();
		resetCancelation();

		queue.add(b);
		while (!queue.isEmpty() && !isCanceled()) {
			queue.pop().accept(this);
		}
	}

	private void visitParent(Parent p) {
		for (InternalBoardObject child : p) {
			queue.add(child);
		}
	}

	@Override
	public void visitAgedAlligator(AgedAlligator alligator) {
		dispatchAgedAlligator(alligator);
		// called after dispatching, as operations may decide to remove children
		visitParent(alligator);
	}

	@Override
	public void visitBoard(Board board) {
		dispatchBoard(board);
		visitParent(board);
	}

	@Override
	public void visitColoredAlligator(ColoredAlligator alligator) {
		dispatchColoredAlligator(alligator);
		visitParent(alligator);
	}

	@Override
	public void visitEgg(Egg egg) {
		dispatchEgg(egg);
	}
}
