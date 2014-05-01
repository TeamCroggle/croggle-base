package de.croggle.game.board.operations.validation;

abstract class AbstractBoardValidator {
	protected final boolean validateObjectUncolored;
	protected final boolean validateAgedAlligatorChildless;
	protected final boolean validateColoredAlligatorChildless;
	protected final boolean validateEmptyBoard;

	public AbstractBoardValidator(BoardErrorType[] errorTypes) {
		boolean validateObjectUncolored = false;
		boolean validateAgedAlligatorChildless = false;
		boolean validateColoredAlligatorChildless = false;
		boolean validateEmptyBoard = false;
		for (BoardErrorType t : errorTypes) {
			switch (t) {
			case AGEDALLIGATOR_CHILDLESS: {
				validateAgedAlligatorChildless = true;
				break;
			}
			case COLOREDALLIGATOR_CHILDLESS: {
				validateColoredAlligatorChildless = true;
				break;
			}
			case OBJECT_UNCOLORED: {
				validateObjectUncolored = true;
				break;
			}
			case EMPTY_BOARD: {
				validateEmptyBoard = true;
				break;
			}
			default: {
				throw new IllegalStateException("This should never happen");
			}
			}
		}
		this.validateAgedAlligatorChildless = validateAgedAlligatorChildless;
		this.validateColoredAlligatorChildless = validateColoredAlligatorChildless;
		this.validateEmptyBoard = validateEmptyBoard;
		this.validateObjectUncolored = validateObjectUncolored;
	}
}
