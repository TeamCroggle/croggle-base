package de.croggle.game.board.operations;

import java.util.Stack;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;

/**
 * Depth first, top down visitor. Ensures that for each child node all parent
 * nodes have been dispatched before.
 * 
 */
public abstract class DFTDVisitor extends StrategyBoardObjectVisitor {
	private final Stack<BoardObject> stack = new Stack<BoardObject>();

	@Override
	protected final void beginTraversal(BoardObject b) {
		stack.clear();
		resetCancelation();

		stack.add(b);
		while (!stack.isEmpty() && !isCanceled()) {
			stack.pop().accept(this);
		}
	}

	private final void visitParent(Parent p) {
		for (InternalBoardObject child : p) {
			stack.add(child);
		}
	}

	@Override
	public final void visitAgedAlligator(AgedAlligator alligator) {
		dispatchAgedAlligator(alligator);
		visitParent(alligator);
	}

	@Override
	public final void visitBoard(Board board) {
		dispatchBoard(board);
		visitParent(board);

	}

	@Override
	public final void visitColoredAlligator(ColoredAlligator alligator) {
		dispatchColoredAlligator(alligator);
		visitParent(alligator);

	}

	@Override
	public final void visitEgg(Egg egg) {
		dispatchEgg(egg);
	}

}
