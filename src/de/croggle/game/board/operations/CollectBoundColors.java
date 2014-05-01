package de.croggle.game.board.operations;

import java.util.HashSet;
import java.util.Set;

import de.croggle.game.Color;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;

/**
 * A visitor for collecting all the colors of alligators in a family. This is
 * equivalent to the set of variables which are bound in a given subterm.
 */
public class CollectBoundColors extends DFTDVisitor {
	private final Set<Color> boundColors;

	private CollectBoundColors() {
		boundColors = new HashSet<Color>();
	}

	/**
	 * Returns the set of colors of alligators in the given family.
	 * 
	 * @param family
	 *            the family to examine
	 * @return the set of bound colors
	 */
	public static Color[] collect(BoardObject family) {
		final CollectBoundColors visitor = new CollectBoundColors();
		visitor.beginTraversal(family);
		return visitor.boundColors
				.toArray(new Color[visitor.boundColors.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void dispatchColoredAlligator(ColoredAlligator alligator) {
		boundColors.add(alligator.getColor());
	}
}
