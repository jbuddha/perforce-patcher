package org.buddha.perforce.patch.util;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static java.util.prefs.Preferences.userNodeForPackage;
import org.buddha.perforce.patch.fx.MainApp;

/**
 * Utility class for storing and retrieving preferences cached locally
 * <p>
 * @author jbuddha
 */
public class PreferenceCache {

	private static final PreferenceCache cache = new PreferenceCache();
	private static Preferences prefs;

	private String p4port, username, password, workspace;
	private int changelist;

	private final String P4PORT_KEY = "P4PORT";
	private final String P4USER_KEY = "P4USER";
	private final String P4PASSWORD_KEY = "P4PASSWORD";
	private final String P4WORKSPACE_KEY = "P4WORKSPACE";
	private final String P4CHANGELIST_KEY = "P4CHANGELIST";

	private boolean persist = true;

	private PreferenceCache() {
		prefs = userNodeForPackage(MainApp.class);
		changelist = prefs.getInt(P4CHANGELIST_KEY, 0);
		username = prefs.get(P4USER_KEY, "");
		password = prefs.get(P4PASSWORD_KEY, "");
		workspace = prefs.get(P4WORKSPACE_KEY, "");
		p4port = prefs.get(P4PORT_KEY, "");
	}

	public static PreferenceCache getInstance() {
		return cache;
	}

	public void persist(boolean persist) {
		this.persist = persist;
	}

	public static Preferences getPrefs() {
		return prefs;
	}

	public static void setPrefs(Preferences prefs) {
		PreferenceCache.prefs = prefs;
	}

	public String getP4port() {
		return p4port;
	}

	public void setP4port(String p4port) throws BackingStoreException {
		if (p4port != null) {
			this.p4port = p4port;
			if (persist) {
				prefs.put(P4PORT_KEY, p4port);
				prefs.sync();
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) throws BackingStoreException {
		if (username != null) {
			this.username = username;
			if (persist) {
				prefs.put(P4USER_KEY, username);
				prefs.sync();
			}
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) throws BackingStoreException {
		if (password != null) {
			this.password = password;
			if(persist) {
				prefs.put(P4PASSWORD_KEY, password);
				prefs.sync();
			}
		}
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) throws BackingStoreException {
		if (workspace != null) {
			this.workspace = workspace;
			if(persist) {
				prefs.put(P4WORKSPACE_KEY, workspace);
				prefs.sync();
			}
		}
	}

	public int getChangelist() {
		return changelist;
	}

	public void setChangelist(int changelist) throws BackingStoreException {
		if (changelist > 0) {
			this.changelist = changelist;
			if(persist) {
				prefs.putInt(P4CHANGELIST_KEY, changelist);
				prefs.sync();
			}
		}
	}

}
