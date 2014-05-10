package de.croggle.backends;

import com.badlogic.gdx.Gdx;

import de.croggle.backends.sqlite.ContentValues;
import de.croggle.backends.sqlite.DatabaseHelper;

public abstract class BackendHelper {

	protected static BackendHelper backend;

	private static void initialize(BackendHelper backend) {
		if (BackendHelper.backend != null) {
			Gdx.app.log("Backedhelper",
					"Warning: BackendHelper instantiated more than once");
		}
		BackendHelper.backend = backend;
	}

	static {
		lineSeparator = System.getProperty("line.separator");
	}
	public static final String lineSeparator;

	public static boolean acquireWakeLock() {
		return backend.wakelockAcquire();
	}

	protected abstract boolean wakelockAcquire();

	public static boolean releaseWakeLock() {
		return backend.wakelockRelease();
	}

	protected abstract boolean wakelockRelease();

	public static DatabaseHelper getNewDatabaseHelper() {
		return backend.instantiateDatabaseHelper();
	}

	protected abstract DatabaseHelper instantiateDatabaseHelper();

	public static ContentValues getNewContentValues() {
		return backend.instantiateContentValues();
	}

	protected abstract ContentValues instantiateContentValues();

	public static String getAssetDirPath() {
		return backend.assetDirPath();
	}

	protected abstract String assetDirPath();

	public void set() {
		BackendHelper.initialize(this);
	}
}
