/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buddha.tests;

import static com.perforce.p4java.core.file.FileAction.ADD;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.IServer;
import java.io.InputStream;
import java.util.List;
import org.buddha.perforce.patch.P4Manager;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author buddha
 */
public class PerforceTests {
	
	@Before
	public void before(){
		
	}
	
	@Test
	public void helloPerforce() throws Exception
	{
		IServer server = P4Manager.connect("","","");
		System.out.println(P4Manager.isWorkspacePresent("jbuddha-elixir-ocp-main"));
		List<IFileSpec> files = server.getChangelistFiles(1248686);
		for(IFileSpec fileSpec: files) {
			if(fileSpec.getAction() == ADD)
			{
				//System.out.println("NEW FILE: " + fileSpec.getLocalPathString());
				InputStream in = fileSpec.getContents(true);
//				List<String> lines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
//											.lines()
//											.map(Object::toString)
//											.collect(Collectors.toList());
//				System.out.println(lines.toString());
				in.close();
			}
		}
		//System.out.println(files.get(0).getContents(true));
		P4Manager.disconnect();
	}
}
