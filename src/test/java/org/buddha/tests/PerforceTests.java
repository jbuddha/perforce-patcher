/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buddha.tests;

import static com.perforce.p4java.core.file.FileAction.ADD;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.server.IServer;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.buddha.perforce.patch.P4Manager;
import org.buddha.temp.TempConfig;
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
		IServer server = P4Manager.connect(TempConfig.P4PORT,TempConfig.P4USER,TempConfig.P4PASSWORD);
		//System.out.println(P4Manager.isWorkspacePresent("jbuddha"));
		
		server.setCurrentClient(server.getClient(TempConfig.P4CLIENT));
		List<IFileSpec> files = server.getChangelistFiles(18169);
		for(IFileSpec fileSpec: files) {
			
			System.out.println(fileSpec.getPath(FilePath.PathType.DEPOT));
			System.out.println(fileSpec.getPath(FilePath.PathType.CLIENT));
			System.out.println(fileSpec.getPath(FilePath.PathType.LOCAL));
			System.out.println("fileSpec.getPath(FilePath.PathType.ORIGINAL) = " + fileSpec.getPath(FilePath.PathType.ORIGINAL));
			System.out.println("fileSpec.getPath(FilePath.PathType.UNKNOWN) = " + fileSpec.getPath(FilePath.PathType.UNKNOWN));
			
			InputStream in = fileSpec.getContents(true);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line;
			List<String> lines = new ArrayList<>();
			while((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			
			System.out.println(lines.toString());
			bufferedReader.close();
			in.close();
			
//			if(fileSpec.getAction() == ADD)
//			{
				//System.out.println("NEW FILE: " + fileSpec.getLocalPathString());
//				InputStream in = fileSpec.getContents(true);
//				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
//				String line;
//				List<String> lines = new ArrayList<>();
//				while((line = bufferedReader.readLine()) != null) {
//					lines.add(line);
//				}
////				List<String> lines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
////											.lines()
////											.map(Object::toString)
////											.collect(Collectors.toList());
////				System.out.println(lines.toString());
//				bufferedReader.close();
//				in.close();
//			}
		}
		//System.out.println(files.get(0).getContents(true));
		P4Manager.disconnect();
	}
}
