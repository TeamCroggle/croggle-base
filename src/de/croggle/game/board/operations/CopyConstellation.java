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
 * A class to copy a given expression of {@link BoardObject}s non-recursively,
 * thus preventing stack overflow exceptions.
 * 
 */
public class CopyConstellation implements BoardObjectVisitor {

	private final Stack<Iterator<InternalBoardObject>> iterators;
	private final Stack<Parent> parents;
	private Parent copy;

	private CopyConstellation(Parent p) {
		iterators = new Stack<Iterator<InternalBoardObject>>();
		parents = new Stack<Parent>();

		if (p.getClass() == AgedAlligator.class) {
			copy = shallowCopy((AgedAlligator) p);
		} else if (p.getClass() == Board.class) {
			copy = shallowCopy((Board) p);
		} else if (p.getClass() == ColoredAlligator.class) {
			copy = shallowCopy((ColoredAlligator) p);
		} else {
			throw new UnsupportedOperationException("Unknown Board object: "
					+ p.getClass());
		}

		parents.push(copy);
		iterators.push(p.iterator());
		while (!iterators.empty()) {
			if (!iterators.peek().hasNext()) {
				iterators.pop();
				parents.pop();
			} else {
				iterators.peek().next().accept(this);
			}
		}
	}

	public static Egg copy(Egg e) {
		return new Egg(e.isMovable(), e.isRemovable(), e.getColor(),
				e.isRecolorable());
	}

	private static AgedAlligator shallowCopy(AgedAlligator a) {
		return new AgedAlligator(a.isMovable(), a.isRemovable());
	}

	private static ColoredAlligator shallowCopy(ColoredAlligator c) {
		return new ColoredAlligator(c.isMovable(), c.isRemovable(),
				c.getColor(), c.isRecolorable());
	}

	private static Board shallowCopy(Board b) {
		return new Board();
	}

	public static AgedAlligator copy(AgedAlligator a) {
		CopyConstellation copier = new CopyConstellation(a);

		return (AgedAlligator) copier.copy;
	}

	public static ColoredAlligator copy(ColoredAlligator c) {
		CopyConstellation copier = new CopyConstellation(c);

		return (ColoredAlligator) copier.copy;
	}

	public static Board copy(Board b) {
		CopyConstellation copier = new CopyConstellation(b);

		return (Board) copier.copy;
	}

	@Override
	public void visitEgg(Egg egg) {
		parents.peek().addChild(copy(egg));
	}

	@Override
	public void visitColoredAlligator(ColoredAlligator alligator) {
		ColoredAlligator copy = shallowCopy(alligator);
		parents.peek().addChild(copy);
		parents.push(copy);
		iterators.push(alligator.iterator());
	}

	@Override
	public void visitAgedAlligator(AgedAlligator alligator) {
		AgedAlligator copy = shallowCopy(alligator);
		parents.peek().addChild(copy);
		parents.push(copy);
		iterators.push(alligator.iterator());
	}

	@Override
	public void visitBoard(Board board) {
		throw new IllegalStateException(
				"Copy is not prepared to find a Board as a child");
	}

}
