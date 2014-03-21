package de.croggle.ui.actors;

import static de.croggle.data.LocalizationHelper._;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.croggle.ui.NotificationCloseListener;
import de.croggle.ui.StyleHelper;

/**
 * A simple dialog with only a message and a "close" button to display.
 * 
 */
public class NotificationDialog extends Dialog {
	
	List<NotificationCloseListener> listeners = new ArrayList<NotificationCloseListener>();

	public NotificationDialog(String msg) {
		super("", StyleHelper.getInstance().getDialogStyle());

		StyleHelper helper = StyleHelper.getInstance();

		Label message = new Label(msg, helper.getBlackLabelStyle());
		message.setWrap(true);
		message.setAlignment(Align.center);

		TextButton okay = new TextButton(_("button_ok"),
				helper.getTextButtonStyle());

		okay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				NotificationDialog.this.hide();
				updateListeners();
			}
		});

		clear();
		add(message).width(500).center().pad(100).row();
		add(okay).center().width(300).height(70);
		padBottom(30);
		layout();
	}
	
	public void registerListener(NotificationCloseListener listener) {
		listeners.add(listener);
	}
	
	public void unregisterListener(NotificationCloseListener listener) {
		listeners.remove(listener);
	}
	
	private void updateListeners() {
		for (NotificationCloseListener listener : listeners) {
			listener.onNotificationClose();
		}
	}

}
