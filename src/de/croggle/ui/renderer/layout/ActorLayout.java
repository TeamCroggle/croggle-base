package de.croggle.ui.renderer.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;

import de.croggle.game.board.Board;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.ui.renderer.BoardActor;
import de.croggle.ui.renderer.objectactors.BoardObjectActor;

/**
 * A class encapsulating all information that is involved in layouting actors to
 * represent a board. That is:
 * <ul>
 * <li>the layout itself (a collection of {@link BoardActor}s)</li>
 * <li>the {@link Board} on which the layout is based</li>
 * <li>a map to lookup the actor representing a certain
 * {@link InternalBoardObject} in estimated O(1)</li>
 * <li>the {@link ActorLayoutConfiguration configuration} the layout builder
 * used to layout the actors</li>
 * <li>useful {@link ActorLayoutStatistics statistics} generated during the
 * layout process, like e.g. the total width</li>
 * </ul>
 */
public class ActorLayout implements Iterable<BoardObjectActor> {
	private final Map<InternalBoardObject, BoardObjectActor> layout;
	private final Board b;
	private final ActorLayoutStatistics statistics;
	private final ActorLayoutConfiguration config;

	private final Pool<ActorDelta> deltaPool;

	public static ActorLayout create(Board b, ActorLayoutConfiguration config) {
		return ActorLayoutBuilder.build(b, config);
	}

	/**
	 * 
	 * @param layout
	 * @param config
	 * @param statistics
	 */
	ActorLayout(Map<InternalBoardObject, BoardObjectActor> layout, Board b,
			ActorLayoutConfiguration config) {
		this.layout = layout;
		this.b = b;
		this.config = config;
		// TODO maybe tweak the initial capacity value (currently random)
		deltaPool = new ReflectionPool<ActorDelta>(ActorDelta.class, 40);
		statistics = new ActorLayoutStatistics(this);
	}

	/**
	 * 
	 * @return
	 */
	public Collection<BoardObjectActor> getActors() {
		return layout.values();
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	public BoardObjectActor getActor(InternalBoardObject object) {
		return layout.get(object);
	}

	/**
	 * 
	 * @param b
	 * @return
	 */
	public InternalBoardObject getBoardObjectByActor(BoardObjectActor b) {
		return b.getBoardObject();
	}

	/**
	 * 
	 * @return
	 */
	public ActorLayoutStatistics getLayoutStatistics() {
		return statistics;
	}

	/**
	 * 
	 * @return
	 */
	public ActorLayoutConfiguration getLayoutConfiguration() {
		return config;
	}

	public boolean removeActor(BoardObjectActor actor) {
		if (actor == null) {
			return false;
		}
		return layout.remove(actor.getBoardObject()) != null;
	}

	public boolean hasActor(BoardObjectActor actor) {
		return layout.get(actor.getBoardObject()) == actor;
	}

	/**
	 * 
	 * @param actor
	 * @return true, if the actor was added using its associated BoardObject as
	 *         key, false if it wasn't added due to a previous mapping of a
	 *         {@link BoardObjectActor} to the associated
	 *         {@link InternalBoardObject}
	 */
	public boolean addActor(BoardObjectActor actor) {
		if (getActor(actor.getBoardObject()) == null) {
			layout.put(actor.getBoardObject(), actor);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Iterator<BoardObjectActor> iterator() {
		return layout.values().iterator();
	}

	/**
	 * Calculates which actors need to be altered and in which way to restore
	 * the layout after this layout's board has changed. Afterwards, it returns
	 * the results of the calculation but leaves it to the caller to apply them.
	 * This allows the code rendering this layout to apply animations on the
	 * changes to be made.
	 * 
	 * @return
	 */
	public List<ActorDelta> getDeltasToFix() {
		return ActorLayoutFixer.getDeltas(this, b);
	}

	/**
	 * Returns the {@link Board} instance this layout is representing.
	 * 
	 * @return the {@link Board} that is represented by this layout
	 */
	public Board getBoard() {
		return b;
	}

	public int size() {
		return layout.size();
	}

	public float getHeightInPixels() {
		if (statistics == null) {
			throw new IllegalStateException(
					"Cannot calculate height without statistics");
		}
		return statistics.getHeightMap().get(getBoard());
	}

	/**
	 * Fixes the layout by instantly applying all necessary
	 * {@link ActorLayout#getDeltasToFix() deltas} and returns the newly added
	 * {@link BoardObjectActor}s.
	 * 
	 * TODO also take care of removed objects. But don't know how to return them
	 * 
	 * @return
	 */
	public List<BoardObjectActor> fix() {
		List<BoardObjectActor> added = new ArrayList<BoardObjectActor>();
		List<ActorDelta> deltas = getDeltasToFix();
		for (ActorDelta delta : deltas) {
			BoardObjectActor actor = delta.getActor();
			if (delta.isCreated()) {
				added.add(actor);
				addActor(actor);
			} else {
				if (delta.isxChanged()) {
					if (delta.isyChanged()) {
						actor.setPosition(delta.getNewX(), delta.getNewY());
					} else {
						actor.setPosition(delta.getNewX(), actor.getY());
					}
				} else if (delta.isyChanged()) {
					actor.setPosition(actor.getX(), delta.getNewY());
				}

				if (delta.isWidthChanged()) {
					if (delta.isHeightChanged()) {
						actor.setSize(delta.getNewWidth(), delta.getNewHeight());
					} else {
						actor.setSize(delta.getNewWidth(), actor.getHeight());
					}
				} else if (delta.isyChanged()) {
					actor.setSize(actor.getWidth(), delta.getNewHeight());
				}
			}
			deltaPool.free(delta);
		}
		return added;
	}

	public int getHeightInActors() {
		// variables:
		// h = board height
		// u = uniform object height
		// p = vertical padding
		// s = vertical scale factor
		// Initial formula: h = sum i=0 to (n-1) ((u + p) * s^i)
		// Wolfram Alpha: h = (s^n - 1) * (u + p) / (s - 1)
		// => n = log (h * (s - 1) / (u + p) + 1) / log(s)
		float h = getHeightInPixels();
		float s = config.getVerticalScaleFactor();
		float u = config.getUniformObjectHeight();
		float p = config.getVerticalPadding();
		float result = Math.round(Math.log(h * (s - 1) / (u + p) + 1)
				/ Math.log(s));
		return (int) result;
	}

	/**
	 * Calculates and returns the scale that was applied on a board actor.
	 * 
	 * @return
	 */
	public float getMinimumScale() {
		// variables:
		// h = board height
		// u = uniform object height
		// p = vertical padding
		// s = vertical scale factor
		// Initial formula: h = sum i=0 to (n-1) ((u + p) * s^i)
		// Wolfram Alpha: h = (s^n - 1) * (u + p) / (s - 1)
		// => s^n = h * (s - 1) / (u + p) + 1
		float h = getHeightInPixels();
		float s = config.getVerticalScaleFactor();
		float u = config.getUniformObjectHeight();
		float p = config.getVerticalPadding();
		float result = h * (s - 1.f) / (u + p) + 1.f;
		getHeightInActors();
		return result;
	}

	public Pool<ActorDelta> getDeltaPool() {
		return deltaPool;
	}
}
