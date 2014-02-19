package de.croggle.backends.sqlite;

public interface ContentValues {
	void put(String key, float value);

	void put(String key, String value);

	void put(String key, int value);

	void put(String key, boolean value);

	/**
	 * Returns the object backing this {@link ContentValues}. Actually, this
	 * method should rather be package private, as only implementations of the
	 * concrete backends need it. But this is obviously not possible. Don't use
	 * it though!
	 * 
	 * Usage example: The concrete Android {@link Database} implementation uses
	 * it to get the Android's ContentValues out of croggle's ContentValues to
	 * apply it with the help of Android's SQLiteOpenHelper.
	 * 
	 * @return the object backing this {@link ContentValues}
	 */
	Object get();
}
