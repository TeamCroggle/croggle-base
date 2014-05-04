package de.croggle.ui.renderer.objectactors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.croggle.backends.BackendHelper;
import de.croggle.data.AssetManager;
import de.croggle.game.board.ColoredBoardObject;

/**
 * Parent class for all {@link BoardObjectActor}s representing
 * {@link ColoredBoardObject}s. Therefore it provides the ability to draw in
 * three steps:
 * <ol>
 * <li>Applying an alpha mask</li>
 * <li>draw actual color</li>
 * <li>draw a foreground picture</li>
 * </ol>
 */
public abstract class ColoredBoardObjectActor extends BoardObjectActor {

	private TextureRegion mask;
	private TextureRegion foreground;
	private Texture background;
	private boolean valid = false;
	boolean colorBlindEnabled = false;

	/**
	 * Create a new ColoredBoardObject using the color from the given object and
	 * the textures indicated by the given paths
	 * 
	 * @param object
	 *            the object to be represented by this {@link Actor}
	 * @param colorBlindEnabled
	 *            whether to render patterns instead of colors
	 * @param foregroundPath
	 *            the path indicating the image containing the texture of the
	 *            foreground (relative to the
	 *            {@link BackendHelper#getAssetDirPath() asset directory})
	 * @param maskPath
	 *            the path indicating the image containing the mask texture
	 *            (relative to the {@link BackendHelper#getAssetDirPath() asset
	 *            directory})
	 */
	public ColoredBoardObjectActor(ColoredBoardObject object,
			boolean colorBlindEnabled, String foregroundPath, String maskPath) {
		super(object);
		initialize(foregroundPath, maskPath, colorBlindEnabled);
	}

	/**
	 * Initializer method to set up mask, background and foreground textures.
	 * Protected so headless actor versions (for testing purposes) can override
	 * the texture creation.
	 * 
	 * @param foregroundPath
	 * @param maskPath
	 * @param colorBlindEnabled
	 */
	protected void initialize(String foregroundPath, String maskPath,
			boolean colorBlindEnabled) {
		AssetManager assetManager = AssetManager.getInstance();
		TextureAtlas tex;
		try {
			tex = assetManager.get(BackendHelper.getAssetDirPath()
					+ "textures/pack.atlas", TextureAtlas.class);
		} catch (GdxRuntimeException ex) {
			throw new IllegalStateException(
					"Could not access atlas containing necessary textures. Make sure it is loaded before instantiating BoardObjectActors.");
		}
		mask = tex.findRegion(maskPath);
		foreground = tex.findRegion(foregroundPath);
		this.colorBlindEnabled = colorBlindEnabled;
		this.setWidth(foreground.getRegionWidth());
		this.setHeight(foreground.getRegionHeight());

		validate();
	}

	/**
	 * Updates the color/pattern texture of this {@link ColoredBoardObject}. The
	 * Actor is automatically validated next time it is rendered.
	 */
	public void validate() {
		if (colorBlindEnabled) {
			background = AssetManager.getInstance().getPatternTexture(
					((ColoredBoardObject) getBoardObject()).getColor());
		} else {
			background = AssetManager.getInstance().getColorTexture(
					((ColoredBoardObject) getBoardObject()).getColor());
		}
		valid = true;
	}

	/**
	 * Invalidates this actor, causing it to refresh its background texture
	 * before it is rendered the next time.
	 */
	public void invalidate() {
		valid = false;
	}

	public void setColorBlindEnabled(boolean enabled) {
		if (enabled == colorBlindEnabled) {
			return;
		} else {
			colorBlindEnabled = enabled;
			invalidate();
		}
	}

	private void drawAlphaMask(SpriteBatch batch) {
		// prevent batch from drawing buffered stuff here
		batch.flush();
		// disable RGB color, only enable ALPHA to the frame buffer
		Gdx.gl.glColorMask(false, false, false, true);

		// change the blending function for our alpha map
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);

		// draw alpha mask sprite(s)
		batch.draw(mask, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());

		// flush the batch to the GPU
		batch.flush();
		// reset the color mask
		Gdx.gl.glColorMask(true, true, true, true);
		// reset blend function
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void drawBackground(SpriteBatch batch) {
		if (!valid) {
			validate();
		}
		// now that the buffer has our alpha, we simply draw the sprite with the
		// mask applied
		batch.setBlendFunction(GL20.GL_DST_ALPHA, GL20.GL_ONE_MINUS_DST_ALPHA);

		// The scissor test is optional, but it depends
		// Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		// Gdx.gl.glScissor((int) getX(), (int) getY(), (int)
		// Math.ceil(getWidth()), (int) Math.ceil(getHeight()));

		// draw our background to be masked
		final int n = 10; // number of patterns horizontally
		final float width = getWidth();
		final float height = getHeight();
		batch.draw(background, getX(), getY(), getOriginX(), getOriginY(),
				width, height, getScaleX(), getScaleY(), getRotation(), 0, 0,
				background.getWidth() * n, (int) (background.getWidth() * (n
						* height / width)), false, false);

		batch.flush();
		// disable scissor before continuing
		// Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		// reset blend function
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void drawForeground(SpriteBatch batch) {
		batch.draw(foreground, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());

		// flush the batch to the GPU
		batch.flush();
	}

	/**
	 * Draws the actor. The sprite batch is configured to draw in he parent's
	 * coordinate system.
	 * 
	 * @param batch
	 *            The sprite batch specifies where to draw into.
	 * @param parentAlpha
	 *            the parent's alpha value
	 */
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		Color c = batch.getColor();
		Color col = getColor();
		batch.setColor(col.r, col.g, col.b, col.a * parentAlpha);

		// draw the alpha mask
		drawAlphaMask(batch);

		// draw background
		drawBackground(batch);

		// draw our foreground elements
		drawForeground(batch);

		// restore alpha value that was removed by mask
		Gdx.gl.glColorMask(false, false, false, true);
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
		batch.draw(background, getX(), getY(), getWidth() * getScaleX(),
				getHeight() * getScaleY());
		batch.flush();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glColorMask(true, true, true, true);
		batch.setColor(c);
	}
}
