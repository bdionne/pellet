package com.clarkparsia.pellet.protege;

import java.util.Objects;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

/**
 *
 * @author Evren Sirin
 */
public class PelletReasonerPreferences {
    private static String KEY = "com.clarkparsia.pellet.remote";
    private static PelletReasonerPreferences INSTANCE;

    public static synchronized PelletReasonerPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PelletReasonerPreferences();
        }
        return INSTANCE;
    }

    private final Preferences prefs = PreferencesManager.getInstance().getApplicationPreferences(KEY);

    private int explanationCount;

    private boolean updated = false;

    private PelletReasonerPreferences() {
			explanationCount = prefs.getInt("explanationCount", 0);
		}

	public boolean save() {
		if (!updated) {
			return false;
		}

		updated = false;
		
		prefs.putInt("explanationCount", explanationCount);

		return true;
	}

    private void update(Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            updated = true;
        }
    }


    public int getExplanationCount() {
        return explanationCount;
    }

    public void setExplanationCount(int theExplanationCount) {
        update(explanationCount, theExplanationCount);
        explanationCount = theExplanationCount;
    }
}