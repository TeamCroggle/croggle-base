package de.croggle.game.board.operations;

import java.util.Iterator;
import java.util.Stack;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;

/**
 * Depth first, bottom up visitor. Ensures that for every dispatched parent node
 * all child nodes have been already dispatched.
 * 
 */
public abstract class DFBUVisitor extends StrategyBoardObjectVisitor {
	private final Stack<State> stack = new Stack<State>();

	@Override
	protected final void beginTraversal(BoardObject b) {
		if (b instanceof Parent) {
			Parent p = (Parent) b;
			stack.clear();
			resetCancelation();

			stack.add(new State(p));
			InternalBoardObject next;
			while (!stack.isEmpty() && !isCanceled()) {
				while (stack.peek().it.hasNext() && !isCanceled()) {
					next = stack.peek().it.next();
					if (next instanceof Parent) {
						stack.push(new State((Parent) next));
					} else {
						next.accept(this);
					}
				}
				stack.pop().p.accept(this);
			}
		} else {
			dispatchEgg((Egg) b);
		}
	}

	@Override
	public final void visitAgedAlligator(AgedAlligator alligator) {
		dispatchAgedAlligator(alligator);
	}

	@Override
	public final void visitBoard(Board board) {
		dispatchBoard(board);
	}

	@Override
	public final void visitColoredAlligator(ColoredAlligator alligator) {
		dispatchColoredAlligator(alligator);
	}

	@Override
	public final void visitEgg(Egg egg) {
		dispatchEgg(egg);
	}

	private class State {
		public State(Parent p) {
			this.p = p;
			it = p.iterator();
		}

		public final Parent p;
		public final Iterator<InternalBoardObject> it;
	}
}
