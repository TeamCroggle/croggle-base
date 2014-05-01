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
 * Breadth first bottum up visitor. Ensures that before dispatching a node, all
 * nodes which are in any lower level of the tree hierarchy have been traversed.
 * 
 */
public class BFBUVisitor extends StrategyBoardObjectVisitor {

	@Override
	protected void beginTraversal(BoardObject b) {
		if (b instanceof Parent) {
			Parent p = (Parent) b;
			LinkedList<Parent> parentqueue = new LinkedList<Parent>();
			LinkedList<BoardObject> queue = new LinkedList<BoardObject>();

			parentqueue.add(p);
			queue.add(p);
			while (!parentqueue.isEmpty()) {
				p = parentqueue.pop();
				for (InternalBoardObject child : p) {
					if (child instanceof Parent) {
						parentqueue.add((Parent) child);
					}
					queue.push(child);
				}
			}
			for (BoardObject element : queue) {
				element.accept(this);
			}
		} else {
			b.accept(this);
		}
	}

	@Override
	public final void visitEgg(Egg egg) {
		dispatchEgg(egg);
	}

	@Override
	public final void visitColoredAlligator(ColoredAlligator alligator) {
		dispatchColoredAlligator(alligator);
	}

	@Override
	public final void visitAgedAlligator(AgedAlligator alligator) {
		dispatchAgedAlligator(alligator);
	}

	@Override
	public final void visitBoard(Board board) {
		dispatchBoard(board);
	}
}
