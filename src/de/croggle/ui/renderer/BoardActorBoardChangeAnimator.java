package de.croggle.ui.renderer;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Pool;

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
	private boolean firstRebuild = true;

	final float ageAnimationDuration = 0.3f;
	final float createAnimatonDuration = 0.3f;
	final float recolorAnimationDuration = 1.0f;

	final float flashDuration = 0.4f;
	final float rotationDuration = 0.4f;
	final float fadeOutDuration = 0.4f;
	final float repositionAnimationDuration = 0.3f;
	final float resizeAnimationDuration = 0.3f;

	final float hatchAnimationDuration = 0.4f;

	final float moveToEaterAnimationDuration = 0.4f;
	final float openJawAnimationDuration = 0.4f;

	public BoardActorBoardChangeAnimator(BoardActor b) {
		this.b = b;
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
			ColoredBoardObjectActor cboa = (ColoredBoardObjectActor) actor;
			cboa.setMixin(cboa.getBackground());
			cboa.addAction(new RecolorAction(recolorAnimationDuration));
			cboa.invalidate();
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
		eaterActor.enterEatingState(openJawAnimationDuration);
		final List<InternalBoardObject> eatenLst = FlattenTree
				.toList(eatenFamily);

		BoardObjectActor actor;
		// fade out the actors
		for (InternalBoardObject eaten : eatenLst) {
			actor = b.getLayout().getActor(eaten);
			MoveToAction moveAction = Actions.moveTo(
					eaterActor.getX() + eaterActor.getWidth()
							* eaterActor.getScaleX() / 2,
					eaterActor.getY() + eaterActor.getHeight()
							* eaterActor.getScaleY() / 2,
					moveToEaterAnimationDuration);
			actor.addAction(moveAction);
			ScaleToAction scaleAction = Actions.scaleTo(0, 0,
					moveToEaterAnimationDuration);
			actor.addAction(scaleAction);
		}

		eaterActor.setOrigin(eaterActor.getWidth() / 2,
				eaterActor.getHeight() / 2);
		eaterActor.addAction(Actions.rotateBy(180, rotationDuration));

		// Really remove the eaten actors from the layout
		b.addAction(new TemporalAction() {
			@Override
			protected void begin() {
				setDuration(moveToEaterAnimationDuration);
			}

			@Override
			protected void update(float percent) {
				// do nothing
			}

			@Override
			protected void end() {
				BoardObjectActor eatenActor;
				for (InternalBoardObject eaten : eatenLst) {
					eatenActor = b.getLayout().getActor(eaten);
					b.removeLayoutActor(eatenActor);
				}
				fixLayout();
			}
		});
	}

	/**
	 * Animates the removal of the given {@link InternalBoardObject} by fading
	 * it out. <br />
	 * Careful: Don't rely on this method to add newly created objects to the
	 * layout, since this would only occur after the animation time. During that
	 * time, the layout would be in an inconsistent state.
	 * 
	 * @param object
	 */
	private void removeObjectAnimated(final InternalBoardObject object,
			final float fadingtime) {
		BoardObjectActor ba = b.getLayout().getActor(object);
		ba.addAction(Actions.fadeOut(fadingtime));
		b.addAction(new TemporalAction() {
			@Override
			protected void begin() {
				setDuration(fadingtime);
			}

			@Override
			protected void update(float percent) {
				// do nothing
			}

			@Override
			protected void end() {
				BoardObjectActor actor = b.getLayout().getActor(object);
				b.removeLayoutActor(actor);
				List<ActorDelta> deltas = b.getLayout().getDeltasToFix();
				applyDeltasAnimated(deltas);
				Pool<ActorDelta> deltaPool = b.getLayout().getDeltaPool();
				for (ActorDelta delta : deltas) {
					deltaPool.free(delta);
				}
			}
		});
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
		gator.addAction(Actions.rotateBy(180, rotationDuration));
		removeObjectAnimated(alligator, fadeOutDuration);
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
		EggActor eggActor = (EggActor) b.getLayout().getActor(replacedEgg);
		eggActor.enterHatchingState(hatchAnimationDuration);
		List<ActorDelta> deltas = b.getLayout().getDeltasToFix();
		List<ActorDelta> creation = filterCreated(deltas, true);
		applyCreationDeltas(creation);
		Pool<ActorDelta> deltaPool = b.getLayout().getDeltaPool();
		for (ActorDelta delta : deltas) {
			deltaPool.free(delta);
		}
		for (ActorDelta delta : creation) {
			deltaPool.free(delta);
		}
		removeObjectAnimated(replacedEgg, fadeOutDuration);
		b.layoutSizeChanged();
	}

	private void applyDeltasAnimated(List<ActorDelta> deltas) {
		List<ActorDelta> created = filterCreated(deltas, true);
		for (ActorDelta delta : deltas) {
			applyDeltaAnimated(delta);
		}
		applyCreationDeltas(created);
	}

	private void applyDeltaAnimated(ActorDelta delta) {
		Actor actor = delta.getActor();
		if (delta.isxChanged()) {
			MoveToAction moveTo;
			if (delta.isyChanged()) {
				moveTo = Actions.moveTo(delta.getNewX(), delta.getNewY(),
						repositionAnimationDuration);
			} else {
				moveTo = Actions.moveTo(delta.getNewX(), actor.getY(),
						repositionAnimationDuration);
			}
			actor.addAction(moveTo);
		} else if (delta.isyChanged()) {
			MoveToAction moveTo = Actions.moveTo(actor.getX(), delta.getNewY(),
					repositionAnimationDuration);
			actor.addAction(moveTo);
		}

		if (delta.isWidthChanged()) {
			SizeToAction sizeTo;
			if (delta.isHeightChanged()) {
				sizeTo = Actions.sizeTo(delta.getNewWidth(),
						delta.getNewHeight(), resizeAnimationDuration);
			} else {
				sizeTo = Actions.sizeTo(delta.getNewWidth(), actor.getHeight(),
						resizeAnimationDuration);
			}
			actor.addAction(sizeTo);
		} else if (delta.isyChanged()) {
			SizeToAction sizeTo = Actions.sizeTo(actor.getWidth(),
					delta.getNewHeight(), resizeAnimationDuration);
			actor.addAction(sizeTo);
		}
	}

	private void applyCreationDeltas(final List<ActorDelta> deltas) {
		BoardObjectActor actor;
		for (ActorDelta delta : deltas) {
			actor = delta.getActor();

			// TODO why is this necessary? Workaround for #114
			if (actor instanceof ColoredBoardObjectActor) {
				((ColoredBoardObjectActor) actor).invalidate();
			}

			actor.setScale(0.f);
			b.addLayoutActor(actor);
			ScaleToAction scaleAction = Actions.scaleTo(1, 1,
					createAnimatonDuration);
			actor.addAction(scaleAction);
		}
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
		agedActor.addAction(Actions.alpha(1.f, ageAnimationDuration));
		b.addLayoutActor(agedActor);
		removeObjectAnimated(colored, fadeOutDuration);
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
		applyDeltasAnimated(deltas);
		Pool<ActorDelta> deltaPool = b.getLayout().getDeltaPool();
		for (ActorDelta delta : deltas) {
			deltaPool.free(delta);
		}
		b.layoutSizeChanged();
	}

	private class RecolorAction extends Action {
		private final float duration;
		private float total;

		public RecolorAction(float duration) {
			this.duration = duration;
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
}
