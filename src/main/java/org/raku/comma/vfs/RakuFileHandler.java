package org.raku.comma.vfs;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.impl.ArchiveHandler;
import org.raku.comma.sdk.RakuSdkUtil;
import org.raku.comma.services.application.RakuSdkStore;
import org.raku.comma.services.application.RakuSdkStoreEntry;
import org.raku.comma.utils.RakuCommandLine;
import org.raku.comma.utils.RakuUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RakuFileHandler extends ArchiveHandler {
    private final Pattern pathPattern = Pattern.compile("/?(-?\\d+?):(.+?)");
    private final String myPath;
    private static final Map<String, String> packagesCache = new HashMap<>();
    private final Logger LOG = Logger.getInstance(RakuFileHandler.class);

    public RakuFileHandler(@NotNull String path) {
        super(path);
        myPath = path;
    }

    @NotNull
    @Override
    protected Map<String, EntryInfo> createEntriesMap() {
        HashMap<String, EntryInfo> entries = new HashMap<>();

        var rakuSdkStore = ApplicationManager.getApplication().getService(RakuSdkStore.class);
        RakuSdkStoreEntry sdk = null;

        try {
            sdk = rakuSdkStore.getSdks().getLast();
        } catch (Exception ignored) {
            LOG.warn("There is no sdk available in RakuSdkStore");
            return entries; // empty
        }

        String sdkHome = rakuSdkStore.getSdks().getLast().getPath();
        String sdkVersion = rakuSdkStore.getSdks().getLast().getVersion();

        try {
            Matcher matcher = pathPattern.matcher(myPath);
            if (! matcher.matches()) return Map.of();
            List<String> providesList = executeLocateScript(sdkHome, matcher.group(2));

            EntryInfo root = new EntryInfo("", true, 0L, 1L, null);
            entries.put("", root);

            for (String provideItem : providesList) {
                String[] provideInfo = provideItem.split("=");
                packagesCache.put(provideInfo[0], provideInfo[1]);
                entries.put(provideInfo[0] + ".pm6", new EntryInfo(provideInfo[0] + ".pm6", false, 1L, 1L, root));
            }
        } catch (ExecutionException e) {
            LOG.warn(e);
            return entries;
        }

        return entries;
    }

    @NotNull
    private static List<String> executeLocateScript(String sdkHome, String argument) throws ExecutionException {
        File locateScript = RakuUtils.getResourceAsFile("zef/locate.raku");
        if (locateScript == null) {
            throw new ExecutionException("Resource bundle is corrupted: locate script is missing");
        }
        RakuCommandLine cmd;
        try {
            cmd = new RakuCommandLine(sdkHome);
        } catch (ExecutionException e) {
            RakuSdkUtil.reactToSdkIssue(null, "Cannot use current Raku SDK", e.getMessage(), e);
            throw e;
        }
        cmd.setWorkDirectory(System.getProperty("java.io.tmpdir"));
        cmd.addParameters(locateScript.getAbsolutePath());
        cmd.addParameter(argument);
        return cmd.executeAndRead(locateScript);
    }

    @Override
    public byte @NotNull [] contentsToByteArray(@NotNull String relativePath) {
        getEntriesMap();
        try {
            String packageName = relativePath.substring(0, relativePath.length() - 4);
            if (packagesCache.containsKey(packageName)) {
                return Files.readAllBytes(Paths.get(packagesCache.get(packageName)));
            }
        } catch (IOException e) {
            LOG.warn(e);
        }
        return new byte[0];
    }
}
