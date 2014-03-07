package de.croggle.backends.sqlite;

import de.croggle.data.persistence.manager.AchievementManager;
import de.croggle.data.persistence.manager.LevelProgressManager;
import de.croggle.data.persistence.manager.ProfileManager;
import de.croggle.data.persistence.manager.SettingManager;
import de.croggle.data.persistence.manager.StatisticManager;

/**
 * This class is responsible for creating and managing the database with its
 * different tables.
 */
public abstract class DatabaseHelper {

	/**
	 * The version number of the database.
	 */
	public static final int DATABASE_Version = 3;

	/**
	 * The name of the database.
	 */
	public static final String DATABASE_NAME = "persistenceDB";

	public abstract Database getWritableDatabase();

	public abstract void close();

	public final void onCreate(Database db) {
		db.execSQL(AchievementManager.CREATE_TABLE);
		db.execSQL(LevelProgressManager.CREATE_TABLE);
		db.execSQL(ProfileManager.CREATE_TABLE);
		db.execSQL(SettingManager.CREATE_TABLE);
		db.execSQL(StatisticManager.CREATE_TABLE);
	}

	public final void onUpgrade(Database db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + AchievementManager.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + LevelProgressManager.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ProfileManager.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SettingManager.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + StatisticManager.TABLE_NAME);

		onCreate(db);
	}
}
