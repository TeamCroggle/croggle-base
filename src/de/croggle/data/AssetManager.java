package de.croggle.data;

import static de.croggle.backends.BackendHelper.getAssetDirPath;

import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

import de.croggle.game.Color;
import de.croggle.util.PatternBuilder;

/**
 * Proxy class to enforce singleton pattern on libgdx' AssetManager. Needs to be
 * initialized before first usage using {@link AssetManager#initialize()}. This
 * is due to Android behaviour, leading to static variables surviving app
 * restarts, while the AssetManager's managed assets are lost.
 */
public class AssetManager extends com.badlogic.gdx.assets.AssetManager {
	/*
	 * Static initialization will cause problems on app resume static {
	 * assetManager = new AssetManager(); }
	 */
	private static AssetManager assetManager;
	private Pixmap uncoloredColor;
	private Pixmap uncoloredPattern;
	private final Pixmap[] colors;
	private final Pixmap[] patterns;
	private final Texture[] patternTextures;
	private final static String[] patternAssets = new String[] {
			"pattern_maze.png", "pattern_turbulence.png", "pattern_noise.png",
			"pattern_bagel.png", "pattern_donut.png",
			"pattern_left_top_right_bottom.png",
			"pattern_left_bottom_right_top.png", "pattern_lambda.png",
			"pattern_lambda_inv.png", "pattern_left_top_right_bottom_inv.png",
			"pattern_left_bottom_right_top_inv.png", "pattern_triangle.png",
			"pattern_zebra.png", "pattern_triangle_inverted.png",
			"pattern_zebra_inverted.png", "pattern_sierpinski.png",
			"pattern_sierpinski_inv.png", "pattern_dice.png",
			"pattern_dice_inverted.png", "pattern_oval.png",
			"pattern_oval_inverted.png" };
	private final static String patternAssetBase = getAssetDirPath()
			+ "textures/";
	private int nGeneratedAssets;

	private AssetManager() {
		colors = new Pixmap[Color.MAX_COLORS];
		patterns = new Pixmap[Color.MAX_COLORS];
		patternTextures = new Texture[Color.MAX_COLORS];
		buildColors();
		buildPatterns();
	}

	private void buildColors() {
		uncoloredColor = new Pixmap(1, 1, Pixmap.Format.RGB888);
		uncoloredColor.setColor(com.badlogic.gdx.graphics.Color.WHITE);
		uncoloredColor.fill();

		com.badlogic.gdx.graphics.Color[] reps = Color.getRepresentations();
		for (int i = 0; i < colors.length; i++) {
			colors[i] = new Pixmap(1, 1, Pixmap.Format.RGB888);
			colors[i].setColor(reps[i]);
			colors[i].fill();
		}
	}

	private void buildPatterns() {
		int n = 0;
		patterns[n++] = PatternBuilder.generateHorizontalLines(8, 4);
		patterns[n++] = PatternBuilder.generateVerticalLines(8, 4);
		patterns[n++] = PatternBuilder.generateCheckerboard(16, 8);
		patterns[n++] = PatternBuilder.generateCircle(256, false);
		patterns[n++] = PatternBuilder.generateRhombus(256, 160, 200, false);
		patterns[n++] = PatternBuilder.generateCircle(256, true);
		patterns[n++] = PatternBuilder.generateRhombus(256, 160, 200, true);
		patterns[n++] = PatternBuilder.generateFilled(1);
		patterns[n++] = PatternBuilder.generateTriangleStrip(256, 1);
		nGeneratedAssets = n;
	}

	public Texture getColorTexture(Color c) {
		if (c.equals(Color.uncolored())) {
			return new Texture(uncoloredColor);
		}
		return new Texture(colors[c.getId()]);
	}

	public Texture getPatternTexture(Color c) {
		if (c.equals(Color.uncolored())) {
			return new Texture(uncoloredColor, true);
		}
		Texture texture;

		if (patternTextures[c.getId()] != null) {
			texture = patternTextures[c.getId()];
		} else {
			int i = c.getId() - nGeneratedAssets;
			if (i < 0) {
				texture = new Texture(patterns[c.getId()], true);
				patterns[c.getId()].dispose();
				patterns[c.getId()] = null;
			} else if (i < patternAssets.length) {
				texture = assetManager.get(patternAssetBase + patternAssets[i],
						Texture.class);
			} else {
				if (patternTextures[0] != null) {
					texture = patternTextures[0];
				} else {
					texture = new Texture(patterns[0], true);
					patterns[0].dispose();
					patterns[0] = null;
					patternTextures[0] = texture;
				}
			}
			patternTextures[c.getId()] = texture;
		}
		// apparently texture size has to be a power of two for this to work
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		texture.setFilter(TextureFilter.MipMapLinearLinear,
				TextureFilter.Linear);
		return texture;
	}

	@Override
	public synchronized void dispose() {
		super.dispose();
		uncoloredColor.dispose();
		uncoloredPattern.dispose();
		for (Pixmap c : colors) {
			c.dispose();
		}
		for (Pixmap p : patterns) {
			if (p != null) {
				p.dispose();
			}
		}
		for (Texture t : patternTextures) {
			if (t != null) {
				t.dispose();
			}
		}
	}

	/**
	 * Returns the globally unique (singleton) libgdx
	 * {@link com.badlogic.gdx.assets.AssetManager} used to manage all assets.
	 * 
	 * @return the app's {@link com.badlogic.gdx.assets.AssetManager}
	 */
	public static AssetManager getInstance() {
		return assetManager;
	}

	public static void initialize() {
		AssetManager.assetManager = new AssetManager();
		final TextureParameter param = new TextureParameter();
		param.genMipMaps = true;
		for (String name : patternAssets) {
			assetManager.load(patternAssetBase + name, Texture.class, param);
		}
	}
}
