package de.croggle.backends.sqlite;

/**
 * This class is responsible for creating and managing the database with its
 * different tables.
 */
public interface DatabaseHelper {

	/**
	 * The version number of the database.
	 */
	public static final int DATABASE_Version = 2;

	/**
	 * The name of the database.
	 */
	public static final String DATABASE_NAME = "persistenceDB";

	Database getWritableDatabase();

	void close();
}
