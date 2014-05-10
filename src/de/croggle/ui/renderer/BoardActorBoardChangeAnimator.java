package de.croggle.ui.renderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;

import de.croggle.data.AssetManager;
import de.croggle.game.Color;
import de.croggle.game.board.AgedAlligator;
import de.croggle.game.board.Board;
import de.croggle.game.board.ColoredAlligator;
import de.croggle.game.board.ColoredBoardObject;
import de.croggle.game.board.Egg;
import de.croggle.game.board.InternalBoardObject;
import de.croggle.game.board.operations.FlattenTree;
import de.croggle.game.event.BoardEventListener;
import de.croggle.ui.renderer.layout.ActorDelta;
import de.croggle.ui.renderer.layout.ActorLayout;
import de.croggle.ui.renderer.objectactors.AgedAlligatorActor;
import de.croggle.ui.renderer.objectactors.BoardObjectActor;
import de.croggle.ui.renderer.objectactors.BoardObjectActorFactory;
import de.croggle.ui.renderer.objectactors.ColoredAlligatorActor;
import de.croggle.ui.renderer.objectactors.ColoredBoardObjectActor;
import de.croggle.ui.renderer.objectactors.EggActor;

class BoardActorBoardChangeAnimator implements BoardEventListener {
	private final BoardActor b;

	private AnimationsFinishedListener finishedListener;

	private boolean firstRebuild = true;
	private final LinkedList<Animation> animationQueue;
	private final Pool<Animation> animationPool;
	private final PopAnimationAction popAction;

	private final Pool<RecolorAction> recolorPool;

	private final float ageAnimationDuration = 0.3f;
	private final float createAnimatonDuration = 0.3f;
	private final float recolorAnimationDuration = 0.3f;

	private final float flashDuration = 0.4f;
	private final float rotationDuration = 0.4f;
	private final float fadeOutDuration = 0.4f;
	private final float repositionAnimationDuration = 0.3f;
	private final float resizeAnimationDuration = 0.3f;

	private final float hatchAnimationDuration = 0.4f;

	private final float moveToEaterAnimationDuration = 0.4f;
	private final float openJawAnimationDuration = 0.0f; // TODO not supported
															// yet

	private float speedFactor = 1.f;

	public BoardActorBoardChangeAnimator(BoardActor b) {
		this.b = b;
		animationPool = new ReflectionPool<Animation>(Animation.class);
		recolorPool = new ReflectionPool<RecolorAction>(RecolorAction.class);
		popAction = new PopAnimationAction();
		animationQueue = new LinkedList<Animation>();
	}

	/**
	 * Visualizes the recoloring of an object on the board.
	 * 
	 * @param recoloredObject
	 *            the object that has been recolored
	 */
	@Override
	public void onObjectRecolored(ColoredBoardObject recoloredObject) {
		BoardObjectActor actor = b.getLayout().getActor(recoloredObject);
		if (actor != null) {
			/*
			 * TODO unnecessary "if" if recolor events were fired at the right
			 * moment
			 */
			final float duration = recolorAnimationDuration * speedFactor;

			ColoredBoardObjectActor cboa = (ColoredBoardObjectActor) actor;
			cboa.setMixin(cboa.getBackground());
			cboa.invalidate();
			RecolorAction action = recolorPool.obtain();
			action.set(duration);
			action.setActor(cboa);
			registerAnimationActions(duration, action);
		}
	}

	/**
	 * Visualizes the process of one alligator eating another and its children,
	 * or just an egg, on the board.
	 * 
	 * @param eater
	 *            the alligator which eats the other alligator
	 * @param eatenFamily
	 *            the family which is eaten by the other alligator
	 */
	@Override
	public void onEat(final ColoredAlligator eater,
			final InternalBoardObject eatenFamily, int eatenParentPosition) {
		ColoredAlligatorActor eaterActor = ((ColoredAlligatorActor) b
				.getLayout().getActor(eater));
		final float eaterX = eaterActor.getX();
		final float eaterY = eaterActor.getY();
		final float eaterWidth = eaterActor.getWidth();
		final float eaterHeight = eaterActor.getHeight();
		final float eaterScaleX = eaterActor.getScaleX();
		final float eaterScaleY = eaterActor.getScaleY();
		final float moveDuration = moveToEaterAnimationDuration * speedFactor;
		final float openJawDuration = openJawAnimationDuration * speedFactor;
		eaterActor.setOrigin(eaterWidth / 2, eaterHeight / 2);
		// don't forget this animation when summing up durations later
		Action eat = eaterActor.enterEatingStateAction(openJawDuration);
		final List<InternalBoardObject> eatenLst = FlattenTree
				.toList(eatenFamily);

		final ArrayList<Action> actions = new ArrayList<Action>();

		BoardObjectActor actor;
		for (InternalBoardObject eaten : eatenLst) {
			actor = b.getLayout().getActor(eaten);
			MoveToAction move = Actions.moveTo(eaterX + eaterWidth / 2
					- eaterWidth * eaterScaleX / 2, eaterY + eaterHeight / 2
					- eaterHeight * eaterScaleY / 2, moveDuration);
			// move.setActor(actor);

			ScaleToAction scale = Actions.scaleTo(0, 0, moveDuration);
			// scale.setActor(actor);

			RemoveObjectAction remove = new RemoveObjectAction();
			remove.set(this);
			// remove.setActor(actor);

			Action scaleThenRemove = Actions.sequence(scale, remove);

			Action all = Actions.parallel(move, scaleThenRemove);

			Action delayedAll = Actions.delay(openJawDuration, all);
			delayedAll.setActor(actor);

			actions.add(delayedAll);
		}
		registerAnimationActions(openJawDuration, eat);
		registerAnimationActions(moveDuration, actions.toArray(new Action[0]));
		// not used since eating alligators age before they die
		// Action eaterDies = Actions.rotateBy(180, rotationDuration);
		// eaterDies.setActor(eaterActor);
		// registerAnimationActions(rotationDuration, eaterDies);
	}

	/**
	 * Creates an Action that animates the removal of the given
	 * {@link InternalBoardObject} by fading it out. <br />
	 * Careful: Don't rely on this method to add newly created objects to the
	 * layout, since this would only occur after the animation time. During that
	 * time, the layout would be in an inconsistent state.
	 * 
	 * @param object
	 * @param fadingtime
	 *            Number of seconds the object's Actor representation is to be
	 *            faded out
	 */
	private Action removeObjectAction(final InternalBoardObject object,
			final float fadingtime) {
		BoardObjectActor ba = b.getLayout().getActor(object);
		Action fadeout = Actions.fadeOut(fadingtime);
		RemoveObjectAction remove = new RemoveObjectAction();
		remove.set(this);
		Action result = Actions.sequence(fadeout, remove);
		result.setActor(ba);
		return result;
	}

	/**
	 * Visualizes the disappearance of an aged alligator on the board.
	 * 
	 * @param alligator
	 *            the alligator which disappeared
	 */
	@Override
	public void onAgedAlligatorVanishes(AgedAlligator alligator,
			int positionInParent) {
		BoardObjectActor gator = b.getLayout().getActor(alligator);
		gator.setOrigin(gator.getWidth() / 2, gator.getHeight() / 2);

		final float rotationDuration = this.rotationDuration * speedFactor;
		final float fadeOutDuration = this.fadeOutDuration * speedFactor;

		Action rotate = Actions.rotateBy(180, rotationDuration);
		Action remove = removeObjectAction(alligator, fadeOutDuration);
		Action sequence = Actions.sequence(rotate, remove);
		sequence.setActor(gator);
		registerAnimationActions(rotationDuration + fadeOutDuration, sequence);
	}

	/**
	 * Completely rebuilds the board as it is seen on the screen.
	 * 
	 * @param board
	 *            the board that is going to replace the board that was seen
	 *            previously
	 */
	@Override
	public final void onBoardRebuilt(Board board) {
		if (firstRebuild) {
			firstRebuild = false;
		} else {
			flash();
		}

		b.clearWorld();
		b.setLayout(ActorLayout.create(board, b.getLayoutConfiguration()));
		for (BoardObjectActor actor : b.getLayout()) {
			b.addToWorld(actor);
		}
		b.updateListeners();
	}

	private void flash() {
		Image flash = new Image(AssetManager.getInstance().getColorTexture(
				Color.uncolored()));
		flash.setFillParent(true);
		b.addToActor(flash);
		flash.validate();
		flash.addAction(Actions.alpha(0.f, flashDuration));
		flash.addAction(Actions.delay(flashDuration, Actions.removeActor()));
	}

	/**
	 * Visualizes the process of replacing an egg within a family with the
	 * family the protecting alligator has eaten.
	 * 
	 * @param replacedEgg
	 *            the hatching egg
	 * @param bornFamily
	 *            the family that hatches from that egg
	 */
	@Override
	public void onHatched(Egg replacedEgg, InternalBoardObject bornFamily) {
		final float hatchDuration = hatchAnimationDuration * speedFactor;
		final float fadeDuration = fadeOutDuration * speedFactor;

		EggActor eggActor = (EggActor) b.getLayout().getActor(replacedEgg);
		Action hatch = eggActor.enterHatchingStateAction(hatchDuration);
		List<ActorDelta> deltas = b.getLayout().getDeltasToFix();
		List<ActorDelta> creation = filterCreated(deltas, true);
		List<Action> creations = applyCreationDeltas(creation);
		float creationTime = creations.isEmpty() ? 0
				: ((TemporalAction) creations.get(0)).getDuration();
		Pool<ActorDelta> deltaPool = b.getLayout().getDeltaPool();
		for (ActorDelta delta : deltas) {
			deltaPool.free(delta);
		}
		for (ActorDelta delta : creation) {
			deltaPool.free(delta);
		}

		Action remove = removeObjectAction(replacedEgg, fadeDuration);
		Action hatchThenRemove = Actions.sequence(hatch, remove);
		hatchThenRemove.setActor(eggActor);

		creations.add(hatchThenRemove);

		registerAnimationActions(
				Math.max(hatchDuration + fadeDuration, creationTime),
				creations.toArray(new Action[0]));
		b.layoutSizeChanged();
	}

	private void applyDeltasAnimated(List<ActorDelta> deltas) {
		final float repositionDuration = repositionAnimationDuration
				* speedFactor;
		final float resizeDuration = resizeAnimationDuration * speedFactor;

		final List<ActorDelta> created = filterCreated(deltas, true);
		final ArrayList<Action> actions = new ArrayList<Action>();
		for (ActorDelta delta : deltas) {
			actions.add(applyDeltaAnimated(delta));
		}
		final List<Action> creations = applyCreationDeltas(created);
		actions.addAll(creations);
		final float creationDuration = creations.isEmpty() ? 0
				: ((TemporalAction) creations.get(0)).getDuration();
		final float duration = Math.max(creationDuration,
				Math.max(repositionDuration, resizeDuration));

		registerAnimationActions(duration, actions.toArray(new Action[0]));
	}

	private void applyDeltasAnimatedImmediately(List<ActorDelta> deltas) {
		final List<ActorDelta> created = filterCreated(deltas, true);
		Action action;
		for (ActorDelta delta : deltas) {
			action = applyDeltaAnimated(delta);
			action.getActor().addAction(action);
		}
		final List<Action> creations = applyCreationDeltas(created);
		for (Action creation : creations) {
			creation.getActor().addAction(creation);
		}
	}

	private Action applyDeltaAnimated(ActorDelta delta) {
		final float repositionDuration = repositionAnimationDuration
				* speedFactor;
		final float resizeDuration = resizeAnimationDuration * speedFactor;

		Actor actor = delta.getActor();
		final ParallelAction result = new ParallelAction();
		if (delta.isxChanged()) {
			MoveToAction moveTo;
			if (delta.isyChanged()) {
				moveTo = Actions.moveTo(delta.getNewX(), delta.getNewY(),
						repositionDuration);
			} else {
				moveTo = Actions.moveTo(delta.getNewX(), actor.getY(),
						repositionDuration);
			}
			result.addAction(moveTo);
		} else if (delta.isyChanged()) {
			MoveToAction moveTo = Actions.moveTo(actor.getX(), delta.getNewY(),
					repositionDuration);
			result.addAction(moveTo);
		}

		if (delta.isWidthChanged()) {
			SizeToAction sizeTo;
			if (delta.isHeightChanged()) {
				sizeTo = Actions.sizeTo(delta.getNewWidth(),
						delta.getNewHeight(), resizeDuration);
			} else {
				sizeTo = Actions.sizeTo(delta.getNewWidth(), actor.getHeight(),
						resizeDuration);
			}
			result.addAction(sizeTo);
		} else if (delta.isyChanged()) {
			SizeToAction sizeTo = Actions.sizeTo(actor.getWidth(),
					delta.getNewHeight(), resizeDuration);
			result.addAction(sizeTo);
		}
		result.setActor(actor);
		return result;
	}

	/**
	 * 
	 * @param deltas
	 * @return the number of seconds needed to perform the animations
	 */
	private List<Action> applyCreationDeltas(final List<ActorDelta> deltas) {
		BoardObjectActor actor;
		final ArrayList<Action> actions = new ArrayList<Action>();
		for (ActorDelta delta : deltas) {
			actor = delta.getActor();

			// TODO why is this necessary? Workaround for #114
			if (actor instanceof ColoredBoardObjectActor) {
				((ColoredBoardObjectActor) actor).invalidate();
			}

			final float duration = createAnimatonDuration * speedFactor;

			actor.setScale(0.f);
			b.addLayoutActor(actor);
			ScaleToAction scale = Actions.scaleTo(1, 1, duration);
			scale.setActor(actor);

			actions.add(scale);
		}
		return actions;
	}

	/**
	 * Finds and returns all deltas indicating an object creation in the given
	 * list in a separate list. If "remove" is true, the deltas found to be
	 * creation deltas are removed from the initial deltas list.
	 * 
	 * @param deltas
	 * @param remove
	 * @return
	 */
	private List<ActorDelta> filterCreated(List<ActorDelta> deltas,
			boolean remove) {
		List<ActorDelta> created = new ArrayList<ActorDelta>();
		int deltaCount = deltas.size();
		for (int i = 0; i < deltaCount; i++) {
			ActorDelta delta = deltas.get(i);
			if (delta.isCreated()) {
				created.add(delta);
				if (remove) {
					deltas.remove(i);
					deltaCount--;
					i--;
				}
			}
		}
		return created;
	}

	@Override
	public void onAge(ColoredAlligator colored, AgedAlligator aged) {
		BoardObjectActor coloredActor = b.getLayout().getActor(colored);
		AgedAlligatorActor agedActor = BoardObjectActorFactory
				.instantiateAgedAlligatorActor(aged);
		agedActor.setSize(coloredActor.getWidth(), coloredActor.getHeight());
		agedActor.setPosition(coloredActor.getX(), coloredActor.getY());
		agedActor.setColor(1.f, 1.f, 1.f, 0.f);
		b.addLayoutActor(agedActor);

		final float duration = ageAnimationDuration * speedFactor;

		Action add = Actions.alpha(1.f, duration);
		add.setActor(agedActor);
		Action remove = removeObjectAction(colored, duration);
		remove.setActor(coloredActor);
		registerAnimationActions(duration, add, remove);
	}

	@Override
	public void onObjectPlaced(InternalBoardObject placed) {
		fixLayout();
	}

	@Override
	public void onObjectRemoved(InternalBoardObject removed) {
		BoardObjectActor removedActor = b.getLayout().getActor(removed);
		if (removedActor != null) {
			b.removeLayoutActor(removedActor);
			fixLayout();
		}
	}

	@Override
	public void onObjectMoved(InternalBoardObject moved) {
		fixLayout();
	}

	void fixLayout() {
		List<ActorDelta> deltas = b.getLayout().getDeltasToFix();
		applyDeltasAnimatedImmediately(deltas);
		Pool<ActorDelta> deltaPool = b.getLayout().getDeltaPool();
		for (ActorDelta delta : deltas) {
			deltaPool.free(delta);
		}
		b.layoutSizeChanged();
	}

	/**
	 * Registers a set of given {@link Action}s to the animator's Action queue.
	 * To be able to register the actions at the {@link BoardObjectActor} they
	 * belong to it is necessary that the Actions have their respective Actor
	 * set via setActor.
	 * 
	 * @param actions
	 *            The actions to be enqueued to this animator's animation queue.
	 *            Must have their {@link Actor}s set for proper registering.
	 * @param duration
	 *            The time needed to pass so that a new animation can be
	 *            started, in seconds
	 */
	private void registerAnimationActions(float duration, Action... actions) {
		Animation anim = animationPool.obtain();
		anim.set(duration, actions);
		animationQueue.add(anim);

		// if there are no ongoing actions, we have to trigger the first one
		// manually
		if (animationQueue.size() == 1) {
			popAnimationActions();
		}
	}

	/**
	 * Pops the topmost set of Actions from the animation queue and registers
	 * all actions in it to their respective Actors. Also sets a timer to
	 * automatically pop the next set of animation actions after the current set
	 * is finished.
	 */
	private void popAnimationActions() {
		if (popAction.ended) {
			if (!animationQueue.isEmpty()) {
				Animation anim = animationQueue.remove(0);
				for (Action a : anim.actions) {
					a.getActor().addAction(a);
				}

				popAction.reset(); // causes .ended to be set to false
				popAction.setDuration(anim.duration);
				this.b.removeAction(popAction);
				this.b.addAction(popAction);
				animationPool.free(anim);
			} else {
				if (finishedListener != null) {
					finishedListener.finished();
				}
			}
		}
	}

	public void setAnimationsFinishedLitener(AnimationsFinishedListener listener) {
		this.finishedListener = listener;
	}

	public void setAnimationSpeed(float speed) {
		this.speedFactor = 1 / speed; // invert, since speed=2 means half the
										// time
	}

	private static class RecolorAction extends Action {
		private float duration;
		private float total;

		public void set(float duration) {
			super.reset();
			this.duration = duration;
			total = 0;
		}

		@Override
		public void setActor(Actor actor) {
			if (actor != null) {
				if (!(actor instanceof ColoredBoardObjectActor)) {
					throw new RuntimeException(actor.getClass().getSimpleName());
				}
				((ColoredBoardObjectActor) actor).setMixinBlending(0.f);
			}
			super.setActor(actor);
		}

		@Override
		public boolean act(float delta) {
			if (total < duration) {
				ColoredBoardObjectActor actor = (ColoredBoardObjectActor) getActor();
				total += delta;
				if (total + delta >= duration) {
					actor.setMixinBlending(1.f);
					return true;
				} else {
					float blending = actor.getMixinBlending();
					blending += delta / duration;
					actor.setMixinBlending(blending);
					return false;
				}
			}
			return true;
		}

	}

	private class PopAnimationAction extends TemporalAction {
		private boolean ended = true;

		@Override
		public void reset() {
			super.reset();
			ended = false;
		}

		@Override
		protected void update(float percent) {
		}

		@Override
		protected void end() {
			ended = true;
			BoardActorBoardChangeAnimator.this.popAnimationActions();
		}
	}

	private static class Animation {
		public Action[] actions;
		public float duration;

		public void set(float duration, Action[] actions) {
			this.actions = actions;
			this.duration = duration;
		}
	}

	private static class RemoveObjectAction extends Action {

		private boolean done = false;

		private BoardActorBoardChangeAnimator animator;

		public void set(BoardActorBoardChangeAnimator animator) {
			super.reset();
			this.animator = animator;
			done = false;
		}

		@Override
		public boolean act(float timDelta) {
			if (!done) {
				done = true;
				BoardActor board = animator.b;
				ActorLayout layout = board.getLayout();
				BoardObjectActor actor = layout
						.getActor(((BoardObjectActor) getActor())
								.getBoardObject());
				board.removeLayoutActor(actor);
				List<ActorDelta> deltas = layout.getDeltasToFix();
				animator.applyDeltasAnimated(deltas);
				Pool<ActorDelta> deltaPool = layout.getDeltaPool();
				for (ActorDelta delta : deltas) {
					deltaPool.free(delta);
				}
			}
			return true;
		}

	}
}
