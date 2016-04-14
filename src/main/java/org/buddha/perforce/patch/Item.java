package org.buddha.perforce.patch;

import org.buddha.perforce.patch.util.Mapping;
import org.buddha.perforce.patch.util.P4Manager;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import difflib.DiffUtils;
import difflib.Patch;
import java.io.IOException;
import java.util.List;
import org.buddha.perforce.patch.util.StringUtils;

/**
 * Representation of remote and local files and their contents. Can be used to
 * generate UnifiedDiff.
 *
 * @author buddha
 */
public class Item {

    private final String remotePath;
    private final String localPath;

    private final FileAction action;

    private boolean omitBorderSpaces = true;
    
    private final List<String> remoteContent;
    private final List<String> localContent;

    public Item(IFileSpec fileSpec, Mapping map, boolean omitBorderSpaces) throws IOException, ConnectionException, RequestException, AccessException {
        remotePath = fileSpec.getPath(FilePath.PathType.DEPOT).getPathString();
        localPath = map.findLocalPath(fileSpec.getPath(FilePath.PathType.DEPOT).getPathString());
        action = fileSpec.getAction();
        this.omitBorderSpaces = omitBorderSpaces;
        remoteContent = P4Manager.getRemoteContent(fileSpec);
        localContent = P4Manager.getLocalContent(fileSpec, map);
    }

    public List<String> getUnifiedDiff(int context) {
        Patch<String> diff;
        if(omitBorderSpaces)
            diff = DiffUtils.diff(remoteContent, localContent, new TrimEqualizer<String>());
        else
            diff = DiffUtils.diff(remoteContent, localContent);
        // purposefully setting local path also as remote path
        return DiffUtils.generateUnifiedDiff(remotePath, remotePath, remoteContent, diff, context);
    }

    public List<String> getUnifiedDiff() {
        return getUnifiedDiff(10000);
    }

    @Override
    public String toString() {
        return StringUtils.concatStrings(getUnifiedDiff(), System.lineSeparator());
    }

}
