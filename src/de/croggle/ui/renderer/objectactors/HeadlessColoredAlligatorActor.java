package de.croggle.ui.renderer.objectactors;

import de.croggle.game.board.ColoredAlligator;

class HeadlessColoredAlligatorActor extends ColoredAlligatorActor {

	HeadlessColoredAlligatorActor(ColoredAlligator alligator,
			boolean colorBlindEnabled) {
		super(alligator, colorBlindEnabled);
	}

	@Override
	protected void initialize(String foregroundPath, String maskPath,
			boolean colorBlindEnabled) {
	}
}
