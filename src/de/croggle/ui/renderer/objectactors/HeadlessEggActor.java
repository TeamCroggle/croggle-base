package de.croggle.ui.renderer.objectactors;

import de.croggle.game.board.Egg;

class HeadlessEggActor extends EggActor {

	HeadlessEggActor(Egg egg, boolean colorBlindEnabled) {
		super(egg, colorBlindEnabled);
	}

	@Override
	protected void initialize(String foregroundPath, String maskPath,
			boolean colorBlindEnabled) {
	}
}
