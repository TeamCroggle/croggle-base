package de.croggle.game.board.operations;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;

/**
 * A visitor for finding a colored alligator which can eat a family next to it.
 */
public class FindEating extends DFTDVisitor {
	private ColoredAlligator eater;

	/**
	 * Creates a new visitor with no found alligator.
	 */
	private FindEating() {
		eater = null;
	}

	/**
	 * Search the top-leftmost colored alligator which can eat a family next to
	 * it. In this case, "left" is preferred over "top", i.e. in a term ()The
	 * eaten family can be retrieved by calling
	 * <code>eater.getParent().getNextChild(eater)</code> where "eater" is the
	 * returned ColoredAlligator.
	 * 
	 * @param board
	 *            the board in which colored alligators should be searched
	 * @return the eating alligator if one was found, otherwise null
	 */
	public static ColoredAlligator findEater(Board board) {
		FindEating finder = new FindEating();
		finder.beginTraversal(board);
		return finder.eater;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispatchColoredAlligator(ColoredAlligator alligator) {
		dispatchParent(alligator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispatchAgedAlligator(AgedAlligator alligator) {
		dispatchParent(alligator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispatchBoard(Board board) {
		dispatchParent(board);
	}

	private void dispatchParent(Parent p) {
		if (p.getChildCount() > 1) {
			InternalBoardObject firstChild = p.getFirstChild();
			if (firstChild.getClass() == ColoredAlligator.class) {
				eater = (ColoredAlligator) firstChild;
				cancelTraversal();
			}
		}
	}
}
