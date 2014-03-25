package de.croggle.game.board.operations;

import java.util.LinkedList;
import java.util.Stack;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;
import de.croggle.game.event.BoardEventMessenger;

public class RemoveUselessAgedAlligators implements BoardObjectVisitor {
	private final BoardEventMessenger boardMessenger;
	private final LinkedList<Parent> parents;
	private final Stack<Parent> bottomUpStack;

	private RemoveUselessAgedAlligators(Parent family,
			BoardEventMessenger boardMessenger) {
		this.boardMessenger = boardMessenger;
		parents = new LinkedList<Parent>();
		bottomUpStack = new Stack<Parent>();
		parents.add(family);
		for (int i = 0; i < parents.size(); i++) {
			parents.get(i).accept(this);
		}
		while (!bottomUpStack.isEmpty()) {
			checkChildren(bottomUpStack.pop());
		}
	}

	public static void remove(BoardObject family,
			BoardEventMessenger boardMessenger) {
		if (!(family instanceof Parent)) {
			return;
		}
		new RemoveUselessAgedAlligators((Parent) family, boardMessenger);
	}

	@Override
	public void visitEgg(Egg egg) {
	}

	@Override
	public void visitColoredAlligator(ColoredAlligator alligator) {
		parents.add(alligator);
		bottomUpStack.push(alligator);
	}

	@Override
	public void visitAgedAlligator(AgedAlligator alligator) {
		parents.add(alligator);
		bottomUpStack.push(alligator);
	}

	@Override
	public void visitBoard(Board board) {
		parents.add(board);
		bottomUpStack.push(board);
	}

	private void checkChildren(Parent p) {
		int firstNotEggPosition = 0;
		InternalBoardObject currentChild;
		// traverse all children
		while (firstNotEggPosition < p.getChildCount()) {
			// children must be eggs
			currentChild = p.getChildAtPosition(firstNotEggPosition);
			if (currentChild.getClass() != Egg.class) {
				break;
			}
			// and free
			if (Boundedness.isBound((Egg) currentChild)) {
				firstNotEggPosition = p.getChildCount();
				break;
			}
			firstNotEggPosition++;
		}
		if (firstNotEggPosition < p.getChildCount()) {
			InternalBoardObject firstNotEgg = p
					.getChildAtPosition(firstNotEggPosition);

			if (firstNotEgg.getClass() == AgedAlligator.class) {
				int i = 0;
				for (InternalBoardObject child : (AgedAlligator) firstNotEgg) {
					p.insertChild(child, firstNotEggPosition + i);
					i++;
				}
				p.removeChild(firstNotEgg);
				if (boardMessenger != null) {
					boardMessenger.notifyAgedAlligatorVanishes(
							(AgedAlligator) firstNotEgg, 0);
				}
			}
		}
	}
}
