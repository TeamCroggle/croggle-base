package de.croggle.game.board.operations;

import java.util.ArrayList;
import java.util.List;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;

/**
 * A visitor-based operation to determine the list of a BoardObject's parents.
 * Use the static <code>get</code> method(s) to receive the respective lists.
 */
public class GetParentHierarchy implements BoardObjectVisitor {

	private final List<Parent> parents;
	private Parent currentParent;

	private GetParentHierarchy(BoardObject b) {
		parents = new ArrayList<Parent>();
		/*
		 * See {@link #get(BoardObject)} description:
		 * 
		 * 
		 * if (b instanceof Parent) { currentParent = (Parent) b; } else
		 */
		if (b instanceof InternalBoardObject) {
			currentParent = ((InternalBoardObject) b).getParent();
		} else {
			currentParent = null;
		}
		while (currentParent != null) {
			parents.add(currentParent);
			currentParent.accept(this);
		}
	}

	/**
	 * Iterates the tree upwards and adds all parents of the given BoardObject
	 * to the end of a list, that will be returned. That implicates that the
	 * resulting list will have the topmost ancestor at its end. The given
	 * BoardObject itself will NOT be part of the list, so the list's first
	 * element will be the given BoardObject's parent.
	 * 
	 * @param b
	 *            the BoardObject whose parent hierarchy is to be determined
	 * @return the given BoardObject's parent hierarchy beginning with the b's
	 *         parent and ending with tree's root
	 */
	public static List<Parent> get(BoardObject b) {
		GetParentHierarchy getter = new GetParentHierarchy(b);
		return getter.parents;
	}

	/**
	 * Common behavior of all internal board objects.
	 * 
	 * @param ibo
	 *            the InternalBoardObject to be visited
	 */
	private void visitInternalBoardObject(InternalBoardObject ibo) {
		currentParent = ibo.getParent();
	}

	@Override
	public void visitEgg(Egg egg) {
		throw new IllegalStateException(
				"This should never happen: Egg is a parent");
	}

	@Override
	public void visitColoredAlligator(ColoredAlligator alligator) {
		visitInternalBoardObject(alligator);

	}

	@Override
	public void visitAgedAlligator(AgedAlligator alligator) {
		visitInternalBoardObject(alligator);
	}

	@Override
	public void visitBoard(Board board) {
		currentParent = null;
	}

}
