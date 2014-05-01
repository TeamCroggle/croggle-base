package de.croggle.game.board.operations;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;
import de.croggle.game.event.BoardEventMessenger;

/**
 * A visitor looking for aged alligators, which are not necessary because they
 * have only one child or no children at all.
 * 
 */
public class RemoveLonelyAgedAlligators extends DFBUVisitor {
	private final BoardEventMessenger boardMessenger;

	/**
	 * 
	 * @param boardMessenger
	 */
	private RemoveLonelyAgedAlligators(BoardEventMessenger boardMessenger) {
		this.boardMessenger = boardMessenger;
	}

	/**
	 * 
	 */
	private RemoveLonelyAgedAlligators() {
		boardMessenger = null;
	}

	/**
	 * Removes all old alligators which are not necessary.
	 * 
	 * @param family
	 *            the family in which old alligators should be removed
	 * @param boardMessenger
	 *            the messenger used for notifying listeners about removed
	 *            alligators
	 */
	public static void remove(BoardObject family,
			BoardEventMessenger boardMessenger) {
		RemoveLonelyAgedAlligators visitor = new RemoveLonelyAgedAlligators(boardMessenger);
		visitor.beginTraversal(family);
	}

	/**
	 * Removes all old alligators which are not necessary.
	 * 
	 * @param family
	 *            the family in which old alligators should be removed
	 */
	public static void remove(BoardObject family) {
		RemoveLonelyAgedAlligators visitor = new RemoveLonelyAgedAlligators();
		visitor.beginTraversal(family);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void dispatchColoredAlligator(ColoredAlligator alligator) {
		checkChildren(alligator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void dispatchAgedAlligator(AgedAlligator alligator) {
		checkChildren(alligator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void dispatchBoard(Board board) {
		checkChildren(board);
	}

	private void checkChildren(Parent p) {
		InternalBoardObject child;
		AgedAlligator aged;
		for (int i = 0; i < p.getChildCount();) {
			child = p.getChildAtPosition(i);
			if (child.getClass() == AgedAlligator.class
					&& ((AgedAlligator) child).getChildCount() <= 1) {
				aged = (AgedAlligator) child;
				if (aged.getChildCount() == 0) {
					p.removeChild(child);
					if (boardMessenger != null) {
						boardMessenger.notifyAgedAlligatorVanishes(aged, i);
					}
				} else {
					p.replaceChild(child, aged.getFirstChild());
					if (boardMessenger != null) {
						boardMessenger.notifyAgedAlligatorVanishes(aged, i);
					}
					i++;
				}
			} else {
				i++;
			}
		}
	}

}
