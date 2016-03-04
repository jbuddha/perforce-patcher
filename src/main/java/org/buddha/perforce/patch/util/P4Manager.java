package org.buddha.perforce.patch.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.perforce.p4java.client.*;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.file.FileAction;
import static com.perforce.p4java.core.file.FileAction.ADD;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.*;
import com.perforce.p4java.impl.generic.client.ClientOptions;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.server.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.buddha.perforce.patch.Config;
import static java.util.prefs.Preferences.userNodeForPackage;
import org.buddha.perforce.patch.fx.MainApp;

public class P4Manager {

    private P4Manager() {
    }

    public static void remember(boolean value) {
        remember = value;
    }

    public static IServer server = null;
    public static boolean remember = true;

    private static String p4Port, username, password;
    private static Preferences prefs = userNodeForPackage(MainApp.class);

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

        if (remember) {
            if (P4Manager.p4Port != null) {
                prefs.put(Config.P4PORT_KEY, P4Manager.p4Port);
            }
            if (P4Manager.username != null) {
                prefs.put(Config.P4USER_KEY, P4Manager.username);
            }
            if (P4Manager.password != null) {
                prefs.put(Config.P4PASSWORD_KEY, P4Manager.password);
            }

            prefs.sync();
        }
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

    public static ClientView getDepotView(String workspace) throws ConnectionException, RequestException, AccessException {
        IClient client = P4Manager.server.getClient(workspace);
        return client.getClientView();
    }

    public static IClient getClient(String workspace) throws ConnectionException, RequestException, AccessException, BackingStoreException {
        IClient client = P4Manager.server.getClient(workspace);
        if (client != null) {
            server.setCurrentClient(client);
            if (remember) {
                prefs.put(Config.P4CLIENT_KEY, workspace);
                prefs.sync();
            }
        }
        return client;
    }

    public static synchronized boolean isWorkspacePresent(String workspace) throws ConnectionException, RequestException, AccessException {
        if (null == P4Manager.server.getClientTemplate(workspace)) {
            return true;
        }
        return false;
    }

    public static synchronized void createCopyWorkspace(String refWorkspaceName, String newWorkspaceName, String root)
            throws ConnectionException, RequestException, AccessException, UnknownHostException {
        if (isWorkspacePresent(refWorkspaceName) && !isWorkspacePresent(newWorkspaceName)) {
            ClientView refView = P4Manager.getDepotView(refWorkspaceName);
            IClient newClient = new Client();
            newClient.setName(newWorkspaceName);
            newClient.setRoot(root);
            newClient.setServer(P4Manager.server);
            newClient.setOwnerName(P4Manager.server.getUserName());
            newClient.setHostName(InetAddress.getLocalHost().getHostName());
            IClientOptions newClientOptions = new ClientOptions(true, false, false, false, false, true);
            newClient.setOptions(newClientOptions);
            //P4Manager.server.setCurrentClient(newClient);
            ClientView newView = new ClientView();
            List<IClientViewMapping> refMapping = refView.getEntryList();
            for (IClientViewMapping mapping : refMapping) {
                ClientViewMapping newMap = new ClientViewMapping();
                newMap.setLeft(mapping.getLeft());
                newMap.setRight(mapping.getRight().replace(refWorkspaceName, newWorkspaceName));
                newMap.setType(mapping.getType());
                newView.addEntry(newMap);
            }
            newClient.setClientView(newView);
            P4Manager.server.createClient(newClient);
        }
    }

    public static synchronized void syncFile(IClient workspace, String file, boolean forceSync) throws ConnectionException, RequestException, AccessException {
        P4Manager.server.setCurrentClient(workspace);
        workspace.sync(FileSpecBuilder.makeFileSpecList(Arrays.asList(file)), forceSync, false, false, false);
    }

    public static synchronized void addViewToWorkspace(IClient workspace, ClientViewMapping newMapping)
            throws ConnectionException, RequestException, AccessException {
        workspace.getClientView().addEntry(newMapping);
        P4Manager.server.updateClient(workspace);
    }

    public static void addViewToWorkspace(String workspace, String leftPath, String rightPath, EntryType type)
            throws ConnectionException, RequestException, AccessException {
        ClientViewMapping mapping = new ClientViewMapping();
        mapping.setLeft(leftPath);
        mapping.setRight(rightPath);
        mapping.setType(type);
        addViewToWorkspace(P4Manager.server.getClient(workspace), mapping);
    }

    public static synchronized void checkOutFile(IClient workspace, String file)
            throws ConnectionException, RequestException, AccessException {
        P4Manager.server.setCurrentClient(workspace);
        IChangelist CL = new Changelist();
        CL.setServer(P4Manager.server);
        CL = workspace.createChangelist(CL);
        workspace.editFiles(FileSpecBuilder.makeFileSpecList(Arrays.asList(file)), false, false, CL.getId(), null);
    }

    public static synchronized void checkOutFile(IClient workspace, String file, int clId)
            throws ConnectionException, RequestException, AccessException {
        P4Manager.server.setCurrentClient(workspace);
        IChangelist CL = new Changelist();
        CL.setId(clId);
        CL.setServer(P4Manager.server);
        workspace.editFiles(FileSpecBuilder.makeFileSpecList(Arrays.asList(file)), false, false, CL.getId(), null);
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
            if (remember) {
                prefs.putInt(Config.P4CHANGELIST_KEY, changeListId);
                prefs.sync();
            }
        }
        return changelistFiles;
    }
}
