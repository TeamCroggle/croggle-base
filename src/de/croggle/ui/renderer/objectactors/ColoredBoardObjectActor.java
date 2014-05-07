package de.croggle.ui.renderer.objectactors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
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
	private static final String vertexShaderLoc = "shader/ColoredBoardObject.vert";
	private static final String fragmentShaderLoc = "shader/ColoredBoardObject.frag";
	private static final String blendFragmentShaderLoc = "shader/ColoredBoardObject-Blendin.frag";

	private TextureRegion mask;
	private TextureRegion foreground;
	private final Mesh mesh;
	private Texture background;
	private Texture mixin;
	private final float[] vertices;
	private final ShaderProgram shader;
	private final ShaderProgram shaderBlendin;
	/**
	 * with how much alpha background will be drawn, as opposed to mixin, which
	 * is drawn with 1 - mixinBlending
	 */
	private float mixinBlending = 1.f;
	private boolean valid = false;
	private boolean colorBlindEnabled = false;

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
		mesh = new Mesh(false, 4, 4, new VertexAttribute(Usage.Position, 2,
				"a_position"), new VertexAttribute(Usage.ColorPacked, 4,
				"a_color"), new VertexAttribute(Usage.TextureCoordinates, 2,
				"a_texCoordMask"), new VertexAttribute(
				Usage.TextureCoordinates, 2, "a_texCoordForeground"),
				new VertexAttribute(Usage.TextureCoordinates, 2,
						"a_texCoordBackground"));
		vertices = new float[(2 + 1 + 2 + 2 + 2) * 4];
		short[] indices = new short[] { 1, 2, 0, 3 };
		mesh.setIndices(indices);
		shader = new ShaderProgram(Gdx.files.internal(BackendHelper
				.getAssetDirPath() + vertexShaderLoc),
				Gdx.files.internal(BackendHelper.getAssetDirPath()
						+ fragmentShaderLoc));
		shaderBlendin = new ShaderProgram(Gdx.files.internal(BackendHelper
				.getAssetDirPath() + vertexShaderLoc),
				Gdx.files.internal(BackendHelper.getAssetDirPath()
						+ blendFragmentShaderLoc));
		if (!shader.isCompiled()) {
			throw new IllegalArgumentException("Error compiling shader: "
					+ shader.getLog());
		}
		if (!shaderBlendin.isCompiled()) {
			throw new IllegalArgumentException(
					"Error compiling shaderBlendin: " + shaderBlendin.getLog());
		}

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
	private void validate() {
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

	/**
	 * Sets the way the actor is rendered. If true, render patterns for color
	 * blind
	 * 
	 * @param enabled
	 *            whether to render in color blind mode or not
	 */
	public void setColorBlindEnabled(boolean enabled) {
		if (enabled == colorBlindEnabled) {
			return;
		} else {
			colorBlindEnabled = enabled;
			invalidate();
		}
	}

	/**
	 * Whether color blind mode is enabled for this actor or not
	 * 
	 * @return the display setting for color blindness mode
	 */
	public boolean getColorBlindEnabled() {
		return colorBlindEnabled;
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
		if (!valid) {
			validate();
		}

		batch.end();

		calculateVertices(parentAlpha);
		mesh.setVertices(vertices);
		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(4);

		Matrix4 combined = new Matrix4(batch.getProjectionMatrix()).mul(batch
				.getTransformMatrix());
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glDepthMask(false);
		if (mixinBlending < 0.99f && mixin != null) {

			background.bind(1);
			foreground.getTexture().bind(2);
			mixin.bind(3);
			// bind to texture unit 0 last for spritebatch
			mask.getTexture().bind(0);

			shaderBlendin.begin();
			shaderBlendin.setUniformMatrix("u_projTrans", combined);
			shaderBlendin.setUniformi("u_mask", 0);
			shaderBlendin.setUniformi("u_background", 1);
			shaderBlendin.setUniformi("u_foreground", 2);
			shaderBlendin.setUniformi("u_blendin", 3);
			shaderBlendin.setUniformf("u_blendin_priority", mixinBlending);
			mesh.render(shaderBlendin, GL20.GL_TRIANGLE_STRIP, 0, 4);
			shaderBlendin.end();
		} else {
			background.bind(1);
			foreground.getTexture().bind(2);
			// bind to texture unit 0 last for spritebatch
			mask.getTexture().bind(0);

			shader.begin();
			shader.setUniformMatrix("u_projTrans", combined);
			shader.setUniformi("u_mask", 0);
			shader.setUniformi("u_background", 1);
			shader.setUniformi("u_foreground", 2);
			mesh.render(shader, GL20.GL_TRIANGLE_STRIP, 0, 4);
			shader.end();
		}

		batch.begin();
	}

	private void calculateVertices(float parenAlpha) {
		float x = getX();
		float y = getY();
		float originX = getOriginX();
		float originY = getOriginY();
		float width = getWidth();
		float height = getHeight();
		float scaleX = getScaleX();
		float scaleY = getScaleY();
		float rotation = getRotation();

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		// lower left, upper left, upper right, lower right
		float x1; // negative
		float y1; // negative
		float x2; // negative
		float y2; // positive
		float x3; // positive
		float y3; // positive
		float x4; // positive
		float y4; // negative

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		float mask_u = mask.getU();
		float mask_v = mask.getV();
		float mask_u2 = mask.getU2();
		float mask_v2 = mask.getV2();

		float foreground_u = foreground.getU();
		float foreground_v = foreground.getV();
		float foreground_u2 = foreground.getU2();
		float foreground_v2 = foreground.getV2();

		float n = 10;
		float background_u = 0;
		float background_v = 0;
		float background_u2 = n;
		float background_v2 = n * width / height;

		float color = getColor().toFloatBits();
		int idx = 0;
		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = mask_u;
		vertices[idx++] = mask_v2;
		vertices[idx++] = foreground_u;
		vertices[idx++] = foreground_v2;
		vertices[idx++] = background_u;
		vertices[idx++] = background_v;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = mask_u;
		vertices[idx++] = mask_v;
		vertices[idx++] = foreground_u;
		vertices[idx++] = foreground_v;
		vertices[idx++] = background_u;
		vertices[idx++] = background_v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = mask_u2;
		vertices[idx++] = mask_v;
		vertices[idx++] = foreground_u2;
		vertices[idx++] = foreground_v;
		vertices[idx++] = background_u2;
		vertices[idx++] = background_v2;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = mask_u2;
		vertices[idx++] = mask_v2;
		vertices[idx++] = foreground_u2;
		vertices[idx++] = foreground_v2;
		vertices[idx++] = background_u2;
		vertices[idx++] = background_v;
	}

	/**
	 * Returns the value of how much alpha the current background texture will
	 * have, as opposed to the set {@link #setMixin(Texture) mixin}, which will
	 * be drawn with one minus this value as alpha value.
	 * 
	 * @return the alpha value used to blend the current and the mixin texture
	 *         together
	 */
	public float getMixinBlending() {
		return mixinBlending;
	}

	/**
	 * Sets the value determining the alpha channel used to blend the current
	 * background and the so-called {@link #setMixin(Texture) mixin}. The
	 * current background will be rendered with <code>blending</code>, and the
	 * mixin with <code>1 - blending</code>. This will only work if there is a
	 * previous mixin!
	 * 
	 * @param blending
	 *            the alpha value to be used for drawing the current background,
	 *            and 1-blending for the mixin.
	 */
	public void setMixinBlending(float blending) {
		mixinBlending = blending;
	}

	/**
	 * Returns the {@link Texture} currently used as background for this
	 * {@link ColoredBoardObjectActor}
	 * 
	 * @return the {@link Actor}'s background texture
	 */
	public Texture getBackground() {
		return background;
	}

	/**
	 * Returns the Texture to be mixed with the background. See
	 * {@link #setMixinBlending(float)} for how to control the blending
	 * 
	 * @return the Texture to be mixed with the background
	 */
	public Texture getMixin() {
		return mixin;
	}

	/**
	 * Sets the Texture to be mixed/blended with the Actor's background.
	 * 
	 * @param mixin
	 *            the texture to be blended with the background
	 */
	public void setMixin(Texture mixin) {
		this.mixin = mixin;
	}
}
