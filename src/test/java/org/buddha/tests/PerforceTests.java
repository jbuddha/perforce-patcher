package org.buddha.tests;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.server.IServer;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.buddha.perforce.patch.util.Mapping;
import org.buddha.perforce.patch.util.P4Manager;
import org.buddha.perforce.patch.Config;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jbuddha
 */
public class PerforceTests {

    IServer server;
    IClient client;

    @Before
    public void before() throws Exception {
        server = P4Manager.connect(Config.P4PORT, Config.P4USER, Config.P4PASSWORD);
        server.setCurrentClient(server.getClient(Config.P4CLIENT));
        client = server.getClient(Config.P4CLIENT);
    }

    @Test
    @Ignore
    public void getFiles() throws Exception {

        List<IFileSpec> files = server.getChangelistFiles(Config.P4CHANGELIST);
        for (IFileSpec fileSpec : files) {
            System.out.println(fileSpec.getPath(FilePath.PathType.DEPOT));

            try (InputStream in = fileSpec.getContents(true);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                List<String> lines = new ArrayList<>();
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
                for (String l : lines) {
                    System.out.println(l);
                }
            }
        }
        P4Manager.disconnect();
    }

    @Test
    @Ignore
    public void getFileSpecList() throws Exception {

        try {
            String home = client.getRoot();
            Mapping map = new Mapping(client);
            List<IClientViewMapping> mappings = client.getClientView().getEntryList();
            for (IClientViewMapping mapping : mappings) {
                String localPath = map.findLocalPath(mapping.getLeft().replace("/...", "") + "/projspec.xml");
                assertTrue(localPath.startsWith(home));

                assertTrue(localPath.endsWith("/projspec.xml"));
            }
        } finally {
            P4Manager.disconnect();
        }
    }

    @Test
    public void mappingLeftTests() {
        try {
            String home = client.getRoot();
            Mapping map = new Mapping(client);
            Assert.assertEquals("//road/devtools/main/phase3-projspec-toolsmodule.xml",
                    map.findLeft("//road/devtools/main/phase3-projspec-toolsmodule.xml"));
            Assert.assertEquals("//product/BCC/main/",
                    map.findLeft("//product/BCC/main/"));
            Assert.assertEquals("//product/BCC/main/",
                    map.findLeft("//product/BCC/main/src/cim/test.txt"));
        } finally {
            P4Manager.disconnect();
        }
    }

    @Test
    public void mappingRightTests() {
        try {
            String home = client.getRoot();
            Mapping map = new Mapping(client);
            Assert.assertEquals(home + "/devtools/projspec.xml",
                    map.findRight("//road/devtools/main/phase3-projspec-toolsmodule.xml"));
            Assert.assertEquals(home + "/BCC/",
                    map.findRight("//product/BCC/main/"));
            Assert.assertEquals(home + "/BCC/src/cim/test.txt",
                    map.findRight("//product/BCC/main/src/cim/test.txt"));
        } finally {
            P4Manager.disconnect();
        }
    }
}
