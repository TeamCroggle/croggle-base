package de.croggle.game;

public class Tutorial {

	private String id;
	private String picturePath;
	private String text;

	public Tutorial(String id, String picturePath, String text) {
		this.id = id;
		this.picturePath = picturePath;
		this.text = text;
	}

	public Tutorial() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
