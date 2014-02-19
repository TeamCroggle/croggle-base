package de.croggle.backends.sqlite;

public interface Cursor {
	boolean moveToFirst();

	boolean moveToNext();

	int getColumnIndex(String name);

	String getString(int columnIndex);

	int getInt(int columnIndex);

	float getFloat(int columnIndex);
}