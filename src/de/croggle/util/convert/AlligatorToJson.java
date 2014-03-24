package de.croggle.util.convert;

import java.util.Iterator;

import de.croggle.backends.BackendHelper;
import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;
import de.croggle.game.board.operations.BoardObjectVisitor;

/**
 * Helper class to format a given tree of BoardObjects as json.
 * 
 */
public class AlligatorToJson implements BoardObjectVisitor {

	private StringBuilder result;
	private int depth;

	private AlligatorToJson() {
		result = new StringBuilder();
		depth = 0;
	}

	/**
	 * Performs the conversion between alligator constellations and their json
	 * formatted string representations.
	 * 
	 * @param b
	 *            the BoardOobject to be converted
	 * @return the converted json string
	 */
	public static String convert(BoardObject b) {
		AlligatorToJson converter = new AlligatorToJson();
		return converter.toJson(b);
	}

	private String toJson(BoardObject b) {
		result = new StringBuilder();
		depth = 0;
		b.accept(this);
		return result.toString();
	}

	@Override
	public void visitEgg(Egg egg) {
		println('{');
		depth++;
		println("\"type\" : \"egg\",");
		printThree("\"color\" : ", egg.getColor().getId(), ',');
		printThree("\"movable\" : ", egg.isMovable(), ',');
		printThree("\"removable\" : ", egg.isRemovable(), ',');
		printTwo("\"recolorable\" : ", egg.isRecolorable());
		depth--;
		if (egg.getParent() == null || egg.getParent().isLastChild(egg)) {
			println('}');
		} else {
			indent();
			print('}');
		}
	}

	@Override
	public void visitColoredAlligator(ColoredAlligator alligator) {
		println('{');
		depth++;
		println("\"type\" : \"colored alligator\",");
		printThree("\"color\" : ", alligator.getColor().getId(), ',');
		printThree("\"movable\" : ", alligator.isMovable(), ',');
		printThree("\"removable\" : ", alligator.isRemovable(), ',');
		printThree("\"recolorable\" : ", alligator.isRecolorable(), ',');
		indent();
		print("\"children\" : ");
		printChildren(alligator);
		depth--;
		if (alligator.getParent().isLastChild(alligator)) {
			println('}');
		} else {
			indent();
			print('}');
		}
	}

	@Override
	public void visitAgedAlligator(AgedAlligator alligator) {
		println('{');
		depth++;
		println("\"type\" : \"aged alligator\",");
		printThree("\"movable\" : ", alligator.isMovable(), ',');
		printThree("\"removable\" : ", alligator.isRemovable(), ',');
		indent();
		print("\"children\" : ");
		printChildren(alligator);
		depth--;
		if (alligator.getParent().isLastChild(alligator)) {
			println('}');
		} else {
			indent();
			print('}');
		}
	}

	@Override
	public void visitBoard(Board board) {
		println('{');
		depth++;
		indent();
		print("\"families\" : ");
		printChildren(board);
		depth--;
		println('}');
	}

	private void printChildren(Parent p) {
		printChildren(p, false, true);
	}

	private void printChildren(Parent p, boolean indentBefore,
			boolean breakAfter) {
		if (p.getChildCount() < 1) {
			if (indentBefore) {
				indent();
				print("[]");
			} else {
				print("[]");
			}
			if (breakAfter) {
				newLine();
			}
		} else {
			if (indentBefore) {
				println('[');
			} else {
				print('[');
				newLine();
			}
			depth++;
			Iterator<InternalBoardObject> i = p.iterator();
			while (i.hasNext()) {
				i.next().accept(this);
				if (i.hasNext()) {
					print(',');
					newLine();
				}
			}
			depth--;
			if (breakAfter) {
				println(']');
			} else {
				indent();
				print(']');
			}
		}
	}

	private void println(String line) {
		indent();
		result.append(line);
		newLine();
	}

	private void println(char c) {
		indent();
		result.append(c);
		newLine();
	}

	private void print(char c) {
		result.append(c);
	}

	private void print(String s) {
		result.append(s);
	}

	private void print(int i) {
		result.append(i);
	}

	private void print(boolean b) {
		result.append(b);
	}

	private void indent() {
		for (int i = 0; i < depth; i++) {
			result.append('\t');
		}
	}

	private void newLine() {
		print(BackendHelper.lineSeparator);
	}

	private void printTwo(String prefix, int value) {
		indent();
		print(prefix);
		print(value);
		newLine();
	}

	private void printTwo(String prefix, boolean value) {
		indent();
		print(prefix);
		print(value);
		newLine();
	}

	private void printThree(String prefix, int value, char postfix) {
		indent();
		print(prefix);
		print(value);
		print(postfix);
		newLine();
	}

	private void printThree(String prefix, boolean value, char postfix) {
		indent();
		print(prefix);
		print(value);
		print(postfix);
		newLine();
	}
}
