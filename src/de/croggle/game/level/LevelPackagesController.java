package de.croggle.game.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import de.croggle.AlligatorApp;
import de.croggle.backends.BackendHelper;

/**
 * Controls the overview over the different level packages.
 */
public class LevelPackagesController {

	private final AlligatorApp game;
	private List<LevelPackage> levelPackages;

	/**
	 * Creates a new controller with no packages attached.
	 * 
	 * @param game
	 *            the backreference to the central game object
	 */
	public LevelPackagesController(AlligatorApp game) {
		this.game = game;
		this.initialiseLevelPackages();
	}

	/**
	 * Gets the level controller which is responsible for handling the levels
	 * within the level package.
	 * 
	 * @param packageId
	 *            the Id of the chosen LevelPackage
	 * @return the level controller one must use to handle the levels within the
	 *         level package
	 */
	public LevelController getLevelController(int packageId) {
		return new LevelController(packageId, game);
	}

	/**
	 * Gets the levelPackages of the game.
	 * 
	 * @return a List of levelPackages
	 */
	public List<LevelPackage> getLevelPackages() {
		return levelPackages;
	}

	/**
	 * Method to initialize the levelPackages from the assets.
	 * 
	 */
	private void initialiseLevelPackages() {
		levelPackages = new ArrayList<LevelPackage>();

		FileHandle handle = Gdx.files.internal(BackendHelper.getAssetDirPath()
				+ "json/levels");
		FileHandle[] possiblePackageDirs = handle.list();
		for (FileHandle dir : possiblePackageDirs) {
			if (!dir.isDirectory()) {
				continue;
			}
			String name = dir.name();
			if (name.length() != 2) {
				continue;
			}
			if (name.matches("[0-9][0-9]")) {
				try {
					if (dir.child("package.json").exists()) {
						levelPackages.add(this.loadPackage(Integer
								.parseInt(name)));
					}
				} catch (GdxRuntimeException ex) {
					/*
					 * Ignore. Just in case, libGdx' documentation of is telling
					 * the truth (which it isn't) and an exception is thrown in
					 * dir.child, because it is internal an non-existent
					 */
				}
			}
		}
		Collections.sort(levelPackages, new Comparator<LevelPackage>() {

			@Override
			public int compare(LevelPackage o1, LevelPackage o2) {
				return (int) Math.signum(o1.getLevelPackageId()
						- o2.getLevelPackageId());
			}
		});
	}

	/**
	 * Returns the number of levels contained in a package.
	 * 
	 * @param packageIndex
	 *            of the Level Package
	 * @return the number of levels contained in the package.
	 */
	public static int getPackageSize(int packageIndex) {
		FileHandle dirHandle = Gdx.files.internal(BackendHelper
				.getAssetDirPath()
				+ "json/levels/"
				+ String.format("%02d", packageIndex));
		return dirHandle.list().length - 1;
	}

	/**
	 * @param PackageIndex
	 *            of the Level Package which should be loaded.
	 * @return the Level Package belonging to the given index.
	 */
	private LevelPackage loadPackage(int packageIndex) {
		FileHandle handle = Gdx.files.internal(BackendHelper.getAssetDirPath()
				+ "json/levels/" + String.format("%02d", packageIndex)
				+ "/package.json");
		JsonReader reader = new JsonReader();
		JsonValue de_croggle = reader.parse(handle.readString());
		JsonValue json = de_croggle.child().getChild("packages");

		String animationPath = null;
		String animation = json.getString("animation");
		Boolean hasAnimation = !animation.equals("");
		if (hasAnimation) {
			animationPath = BackendHelper.getAssetDirPath() + animation;
		}

		LevelPackage levelPackage = new LevelPackage(packageIndex,
				json.getString("name"), json.getString("description"),
				BackendHelper.getAssetDirPath() + json.getString("banner"),
				hasAnimation, animationPath, BackendHelper.getAssetDirPath()
						+ json.getString("design"));
		return levelPackage;
	}

}
