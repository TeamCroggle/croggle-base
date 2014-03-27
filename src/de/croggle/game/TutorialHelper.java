package de.croggle.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class TutorialHelper {

	private ArrayList<Tutorial> tutorials;
	private static TutorialHelper instance;

	private TutorialHelper() {
		FileHandle handle = Gdx.files.internal("tutorial/tutorials.json");
		Json json = new Json();
		tutorials = json.fromJson(ArrayList.class, handle.readString());
	}

	public static void initialize() {
		instance = new TutorialHelper();
	}

	public static TutorialHelper getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Call initialize() first");
		}
		return instance;
	}

	public Tutorial getTutorial(String id) {
		for (Tutorial tut : tutorials) {
			if (tut.getId().equals(id)) {
				return tut;
			}
		}
		// return some dummy stuff so it won't crash
		return new Tutorial("", "tutorials/introduction.png",
				"Couldn't load tutorial");
	}

}
