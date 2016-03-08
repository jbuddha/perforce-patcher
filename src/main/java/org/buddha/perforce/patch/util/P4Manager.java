package org.buddha.perforce.patch.util;

import java.util.List;

import com.perforce.p4java.client.*;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.file.FileAction;
import static com.perforce.p4java.core.file.FileAction.ADD;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.*;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.server.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.prefs.BackingStoreException;
import static org.buddha.perforce.patch.util.PreferenceCache.getInstance;

/**
 * The Utility class for performing operations that connect with perforce
 * <p>
 * @author jbuddha
 */
public class P4Manager {

	private P4Manager() {
	}

	public static IServer server = null;

	private static String p4Port, username, password;
	private static PreferenceCache prefs = getInstance();

	public static synchronized IServer connect(String p4Port, String username, String password) throws Exception {
		if (server != null) {
			return server;
		}

		P4Manager.p4Port = p4Port;
		P4Manager.username = username;
		P4Manager.password = password;

		if (!p4Port.startsWith("p4java://")) {
			p4Port = "p4java://" + p4Port;
		}

		server = ServerFactory.getServer(p4Port, null);
		server.connect();
		server.setUserName(username);
		server.login(password);

		prefs.setP4port(P4Manager.p4Port);
		prefs.setUsername(P4Manager.username);
		prefs.setPassword(P4Manager.password);
		return server;
	}

	public static void disconnect() {
		try {
			if (server != null) {
				server.logout();
				server.disconnect();
			}
		} catch (ConnectionException | RequestException | AccessException | ConfigException e) {
			e.printStackTrace();
		}
	}

	public static IClient getClient(String workspace) throws ConnectionException, RequestException, AccessException, BackingStoreException {
		IClient client = P4Manager.server.getClient(workspace);
		if (client != null) {
			server.setCurrentClient(client);
			prefs.setWorkspace(workspace);
		}
		return client;
	}

	public static List<String> getRemoteContent(IFileSpec fileSpec) throws ConnectionException, RequestException, AccessException, IOException {
		if (fileSpec.getAction() == ADD) {
			return new ArrayList<>();
		}
		try (InputStream in = fileSpec.getContents(true);
			 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			List<String> lines = new ArrayList<>();
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		}
	}

	public static List<String> getLocalContent(IFileSpec fileSpec, Mapping map) throws FileNotFoundException, IOException {
		if (fileSpec.getAction() == FileAction.DELETE) {
			return new ArrayList<>();
		}

		String localPath = map.findLocalPath(fileSpec.getPath(FilePath.PathType.DEPOT).getPathString());
		File localFile = new File(localPath);

		if (!localFile.exists() || !localFile.canRead()) {
			return null;
		}

		try (InputStream in = new FileInputStream(localFile);
			 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			List<String> lines = new ArrayList<>();
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		}
	}

	public static List<IFileSpec> getChangelistFiles(int changeListId) throws ConnectionException, RequestException, AccessException, BackingStoreException {
		List<IFileSpec> changelistFiles = P4Manager.server.getChangelistFiles(changeListId);
		if (changelistFiles != null) {
			prefs.setChangelist(changeListId);
		}
		return changelistFiles;
	}

	public static String[] getWorkspaces() throws ConnectionException, RequestException, AccessException {
		List<IClientSummary> clients = P4Manager.server.getClients(username, null, 0);
		String[] clientArray = new String[clients.size()];
		int i = 0;
		for (IClientSummary client : clients) {
			clientArray[i++] = client.getName();
		}
		return clientArray;
	}

	public static Map<String, ArrayList<String>> getPendingChangeLists() throws Exception {
		List<IChangelistSummary> changelists = P4Manager.server.getChangelists(0, null, null, username, false, false, true, false);
		int i = 0;
		int[] array = new int[changelists.size()];
		Map<String, ArrayList<String>> changelistMap = new HashMap<>();
		for (IChangelistSummary changelist : changelists) {
			String cl = "" + changelist.getId();
			if (changelist.getDescription().length() > 0) {
				cl = cl + "-" + changelist.getDescription().trim();
			}
			if (changelistMap.containsKey(changelist.getClientId())) {
				changelistMap.get(changelist.getClientId()).add(cl);
			} else {
				ArrayList<String> list = new ArrayList<>();
				list.add(cl);
				changelistMap.put(changelist.getClientId(), list);
			}

		}
		return changelistMap;
	}
}
