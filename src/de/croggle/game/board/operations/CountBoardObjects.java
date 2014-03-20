package de.croggle.game.board.operations;

import java.util.Iterator;
import java.util.Stack;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;

/**
 * A visitor for counting the number of objects in a family.
 */
public class CountBoardObjects implements BoardObjectVisitor {
	private int count;
	private final boolean countBoard;
	private final boolean countEgg;
	private final boolean countAgedAlligator;
	private final boolean countColoredAlligator;

	private final Stack<Iterator<InternalBoardObject>> iterators;

	/**
	 * Initializes the BoardObject counter with 0 BoardObjects counted.
	 */
	private CountBoardObjects(BoardObject b, boolean countBoard,
			boolean countEgg, boolean countAgedAlligator,
			boolean countColoredAlligator) {
		count = 0;
		this.countBoard = countBoard;
		this.countEgg = countEgg;
		this.countAgedAlligator = countAgedAlligator;
		this.countColoredAlligator = countColoredAlligator;

		iterators = new Stack<Iterator<InternalBoardObject>>();
		b.accept(this);
		while (!iterators.empty()) {
			if (!iterators.peek().hasNext()) {
				iterators.pop();
			} else {
				iterators.peek().next().accept(this);
			}
		}
	}

	/**
	 * Count the number of objects in a family.
	 * 
	 * @param family
	 *            the family whose members should be counted
	 * @return the number of family members
	 */
	public static int count(BoardObject family) {
		return CountBoardObjects.count(family, true, true, true, true);
	}

	public static int count(BoardObject family, boolean countBoard,
			boolean countEgg, boolean countAgedAlligator,
			boolean countColoredAlligator) {
		CountBoardObjects counter = new CountBoardObjects(family, countBoard,
				countEgg, countAgedAlligator, countColoredAlligator);
		return counter.count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEgg(Egg egg) {
		if (countEgg) {
			count++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitColoredAlligator(ColoredAlligator alligator) {
		if (countColoredAlligator) {
			count++;
		}
		iterators.push(alligator.iterator());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitAgedAlligator(AgedAlligator alligator) {
		if (countAgedAlligator) {
			count++;
		}
		iterators.push(alligator.iterator());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitBoard(Board board) {
		if (countBoard) {
			count++;
		}
		iterators.push(board.iterator());
	}

}
