package de.croggle.ui.renderer.layout;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import com.badlogic.gdx.math.Vector2;

import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.BoardObject;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.Parent;
import de.croggle.game.board.operations.BoardObjectVisitor;
import de.croggle.game.board.operations.CreateWidthMap;
import de.croggle.ui.renderer.objectactors.AgedAlligatorActor;
import de.croggle.ui.renderer.objectactors.BoardObjectActor;
import de.croggle.ui.renderer.objectactors.ColoredAlligatorActor;
import de.croggle.ui.renderer.objectactors.EggActor;

abstract class ActorLayouter implements BoardObjectVisitor {
	/**
	 * Abstract method to influence where the layouter obtains its new
	 * {@link AgedAlligatorActor}s from before they get integrated into the
	 * layout. A basic layout fixer would return the already existent actor
	 * here, whereas a builder would provide a completely new one.
	 * 
	 * @param alligator
	 * @return
	 */
	protected abstract AgedAlligatorActor provideAgedAlligatorActor(
			AgedAlligator alligator);

	/**
	 * Abstract method to influence where the layouter obtains its new
	 * {@link ColoredAlligatorActor}s from before they get integrated into the
	 * layout. A basic layout fixer would return the already existent actor
	 * here, whereas a builder would provide a completely new one.
	 * 
	 * @param alligator
	 * @return
	 */
	protected abstract ColoredAlligatorActor provideColoredAlligatorActor(
			ColoredAlligator alligator);

	/**
	 * Abstract method to influence where the layouter obtains its new
	 * {@link EggActor}s from before they get integrated into the layout. A
	 * basic layout fixer would return the already existent actor here, whereas
	 * a builder would provide a completely new one.
	 * 
	 * @param egg
	 * @return
	 */
	protected abstract EggActor provideEggActor(Egg egg);

	/**
	 * Called after the given actor was layouted. Overwrite this in derived
	 * classes to be notified when this happens.
	 * 
	 * @param actor
	 */
	protected void notifyAgedAlligatorLayouted(AgedAlligatorActor actor) {
	}

	/**
	 * Called after the given actor was layouted. Overwrite this in derived
	 * classes to be notified when this happens.
	 * 
	 * @param actor
	 */
	protected void notifyColoredAlligatorLayouted(ColoredAlligatorActor actor) {
	}

	/**
	 * Called after the given actor was layouted. Overwrite this in derived
	 * classes to be notified when this happens.
	 * 
	 * @param actor
	 */
	protected void notifyEggLayouted(EggActor actor) {
	}

	/**
	 * Called after the given actor was layouted. Overwrite this in derived
	 * classes.
	 * 
	 * @param actor
	 */
	protected void notifyLayouted(BoardObjectActor actor) {
	}

	// settings
	/**
	 * The configuration used for adjusting the created layouts
	 */
	private final ActorLayoutConfiguration config;

	/**
	 * the board to operate on
	 */
	private final Board b;

	/**
	 * Map to access the width of any BoardObject occurring in the Board to
	 * build in O(1)
	 */
	protected final Map<BoardObject, Float> widthMap;

	private final Stack<ParentState> parents;
	private final Stack<ParentState> parentReverser;

	/**
	 * The current scaling of newly added BoardObjectActors
	 */
	private float scaling = 1;

	/**
	 * Where newly added BoardObjectActors will be placed
	 */
	private Vector2 currentPosition;

	ActorLayouter(Board b, ActorLayoutConfiguration config) {
		this.config = config;
		this.b = b;

		parents = new Stack<ParentState>();
		parentReverser = new Stack<ParentState>();

		widthMap = CreateWidthMap.create(b, config.getUniformObjectWidth(),
				config.getVerticalScaleFactor(), config.getHorizontalPadding());
	}

	@Override
	public void visitEgg(Egg egg) {
		EggActor a = provideEggActor(egg);
		float offsetx = (config.getUniformObjectWidth() - config.getEggWidth())
				/ 2 * getScaling();
		if (config.getHorizontalGrowth() == TreeGrowth.POS_NEG) {
			offsetx *= -1;
		}

		float h = config.getEggHeight() * getScaling();
		float y = currentPosition.y;
		if (config.getVerticalGrowth() == TreeGrowth.NEG_POS
				&& config.getRenderDirectionY() == TreeGrowth.POS_NEG) {
			y += h;
		} else if (config.getVerticalGrowth() == TreeGrowth.POS_NEG
				&& config.getRenderDirectionY() == TreeGrowth.NEG_POS) {
			y -= h;
		}

		a.setBounds(currentPosition.x + offsetx, y, config.getEggWidth()
				* getScaling(), h);
		notifyEggLayouted(a);
		notifyLayouted(a);
	}

	@Override
	public void visitColoredAlligator(ColoredAlligator alligator) {
		ColoredAlligatorActor a = provideColoredAlligatorActor(alligator);
		setParentActorBounds(a, alligator);
		notifyColoredAlligatorLayouted(a);
		notifyLayouted(a);
		parentReverser.push(new ParentState(alligator, currentPosition.cpy(),
				getScaling()));
	}

	@Override
	public void visitAgedAlligator(AgedAlligator alligator) {
		AgedAlligatorActor a = provideAgedAlligatorActor(alligator);
		setParentActorBounds(a, alligator);
		notifyAgedAlligatorLayouted(a);
		notifyLayouted(a);
		parentReverser.push(new ParentState(alligator, currentPosition.cpy(),
				getScaling()));
	}

	@Override
	public void visitBoard(Board board) {
		Parent p = board;
		Iterator<InternalBoardObject> it = p.iterator();
		if (config.getHorizontalGrowth() == TreeGrowth.NEG_POS) {
			while (it.hasNext()) {
				InternalBoardObject child = it.next();
				if (config.getRenderDirectionX() == TreeGrowth.POS_NEG) {
					currentPosition.x += widthMap.get(child);
				}
				child.accept(this);
				if (config.getRenderDirectionX() == TreeGrowth.NEG_POS) {
					currentPosition.x += widthMap.get(child);
				}
				if (it.hasNext()) {
					currentPosition.x += config.getHorizontalPadding();
				}
			}
		} else {
			while (it.hasNext()) {
				InternalBoardObject child = it.next();
				if (config.getRenderDirectionX() == TreeGrowth.NEG_POS) {
					currentPosition.x -= widthMap.get(child);
				}
				child.accept(this);
				if (config.getRenderDirectionX() == TreeGrowth.POS_NEG) {
					currentPosition.x -= widthMap.get(child);
				}
				if (it.hasNext()) {
					currentPosition.x -= config.getHorizontalPadding();
				}
			}
		}
	}

	/**
	 * Place a ParentActor in the middle of the horizontal space allocated for
	 * it and its children
	 * 
	 * @param p
	 */
	private void setParentActorBounds(BoardObjectActor p, Parent parent) {
		double totalWidth = widthMap.get(parent);
		float w = config.getUniformObjectWidth() * getScaling();
		float h = config.getUniformObjectHeight() * getScaling();
		if (p.getClass() == AgedAlligatorActor.class) {
			w = config.getAgedAlligatorWidth() * getScaling();
			h = config.getAgedAlligatorHeight() * getScaling();
		} else if (p.getClass() == ColoredAlligatorActor.class) {
			w = config.getColoredAlligatorWidth() * getScaling();
			h = config.getColoredAlligatorHeight() * getScaling();
		}
		float offset = ((float) totalWidth - w) / 2.f;
		if (config.getHorizontalGrowth() == TreeGrowth.POS_NEG) {
			offset *= -1;
		}
		float y = currentPosition.y;
		if (config.getVerticalGrowth() == TreeGrowth.NEG_POS
				&& config.getRenderDirectionY() == TreeGrowth.POS_NEG) {
			y += h;
		} else if (config.getVerticalGrowth() == TreeGrowth.POS_NEG
				&& config.getRenderDirectionY() == TreeGrowth.NEG_POS) {
			y -= h;
		}

		p.setBounds(currentPosition.x + offset, y, w, h);
	}

	private void layoutChildren(Parent p) {
		Vector2 initialPosition = currentPosition.cpy();

		// move currentPosition one level down
		float h = config.getUniformObjectHeight() * getScaling();
		if (config.getVerticalGrowth() == TreeGrowth.NEG_POS) {
			currentPosition.y += h + config.getVerticalPadding() * getScaling();
		} else {
			currentPosition.y -= h + config.getVerticalPadding() * getScaling();
		}

		// iterate over the children and layout them depending on the horizontal
		// grow direction
		goDeeper();
		// used for having children still centered if smaller than parent
		float childrenWidth = 0;
		Iterator<InternalBoardObject> it = p.iterator();
		while (it.hasNext()) {
			childrenWidth += widthMap.get(it.next());
			if (it.hasNext()) {
				childrenWidth += getScaling() * config.getHorizontalPadding();
			}
		}

		it = p.iterator();
		if (config.getHorizontalGrowth() == TreeGrowth.NEG_POS) {
			currentPosition.x += (widthMap.get(p) - childrenWidth) / 2;
			while (it.hasNext()) {
				InternalBoardObject child = it.next();
				if (config.getRenderDirectionX() == TreeGrowth.POS_NEG) {
					currentPosition.x += widthMap.get(child);
				}
				child.accept(this);
				if (config.getRenderDirectionX() == TreeGrowth.NEG_POS) {
					currentPosition.x += widthMap.get(child);
				}
				if (it.hasNext()) {
					currentPosition.x += getScaling()
							* config.getHorizontalPadding();
				}
			}
		} else {
			currentPosition.x -= (widthMap.get(p) - childrenWidth) / 2;
			while (it.hasNext()) {
				InternalBoardObject child = it.next();
				if (config.getRenderDirectionX() == TreeGrowth.NEG_POS) {
					currentPosition.x -= widthMap.get(child);
				}
				child.accept(this);
				if (config.getRenderDirectionX() == TreeGrowth.POS_NEG) {
					currentPosition.x -= widthMap.get(child);
				}
				if (it.hasNext()) {
					currentPosition.x -= getScaling()
							* config.getHorizontalPadding();
				}
			}
		}
		goHigher();

		currentPosition = initialPosition;
	}

	private float getScaling() {
		return scaling;
	}

	/**
	 * Enter the next level inside the syntax tree
	 */
	private void goDeeper() {
		scaling *= config.getVerticalScaleFactor();
	}

	/**
	 * Leave the current level inside the syntax tree
	 */
	private void goHigher() {
		scaling /= config.getVerticalScaleFactor();
	}

	protected ActorLayoutConfiguration getConfig() {
		return config;
	}

	/**
	 * To be called before accessing this {@link ActorLayouter layouter's}
	 * results.
	 */
	protected void doLayout() {
		currentPosition = config.getTreeOrigin().cpy();
		visitBoard(b);
		reverseParents();
		while (!parents.isEmpty()) {
			ParentState current = parents.pop();
			currentPosition = current.position;
			scaling = current.scale;
			layoutChildren(current.parent);
			reverseParents();
		}
	}

	private void reverseParents() {
		while (!parentReverser.isEmpty()) {
			parents.push(parentReverser.pop());
		}
	}

	private static class ParentState {
		public ParentState(Parent p, Vector2 position, float scale) {
			parent = p;
			this.position = position;
			this.scale = scale;
		}

		public Parent parent;
		public float scale;
		public Vector2 position;
	}
}
