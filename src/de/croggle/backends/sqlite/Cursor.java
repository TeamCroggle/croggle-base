package de.croggle.backends.sqlite;

public interface Cursor {
	boolean moveToFirst();

	boolean moveToNext();

	int getColumnIndex(String name);

	String getString(int index);

	int getInt(int columnIndex);

	float getFloat(int column);
}