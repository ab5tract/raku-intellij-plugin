package org.raku.comma.utils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.text.VersionComparatorUtil;
import org.raku.comma.sdk.RakuSdkUtil;
import org.raku.comma.services.project.RakuProjectSdkService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A thin wrapper around GeneralCommandLine
 * Features include:
 * * Adds Raku interpreter from Sdk set for the project passed to constructor
 * * Contains a shortcut for executing and gathering output of process
 * Warning: RakuCommandLine usage is *synchronous*. It means that it will block
 * for scripts that take a lot of time to execute and setting execution
 * into separate thread is on the caller side.
 */
public class RakuCommandLine extends GeneralCommandLine {
    private static final Logger LOG = Logger.getInstance(RakuCommandLine.class);

    public RakuCommandLine(Project project) throws ExecutionException {
        this(project.getService(RakuProjectSdkService.class).getSdkPath());
    }

    public RakuCommandLine(@Nullable String sdkHome) throws ExecutionException {
        if (sdkHome == null) throw new ExecutionException("No SDK for project");

        if (Paths.get(sdkHome).toFile().isFile()) {
            setExePath(sdkHome);
        } else {
            String rakuBinary = RakuSdkUtil.findRakuInSdkHome(sdkHome);
            if (rakuBinary == null) throw new ExecutionException("SDK is invalid");

            setExePath(rakuBinary);
        }
    }

    public RakuCommandLine(Project project, int debugPort) throws ExecutionException {
        List<String> parameters = populateDebugCommandLine(project, debugPort);
        if (parameters == null) {
            throw new ExecutionException("SDK is not valid for debugging");
        }
        setExePath(parameters.getFirst());
        addParameters(parameters.subList(1, parameters.size()));
    }

    @NotNull
    public List<String> executeAndRead() {
        return executeAndRead(null);
    }

    /**
     * Runs the process and returns everything it produced: stdout, stderr and
     * the exit code.
     *
     * Prefer this over {@link #executeAndRead(File)}, which can only say
     * "nothing came back" and cannot say why. Both streams are drained
     * concurrently, which also removes a hang: reading stdout alone deadlocks
     * a child that fills the stderr pipe buffer, and Rakudo emits compile-time
     * worries there.
     *
     * @param timeoutMs wall-clock limit, or 0 to wait indefinitely.
     */
    @NotNull
    public ProcessOutput executeAndCapture(@Nullable File scriptFile, int timeoutMs) {
        try {
            CapturingProcessHandler handler = new CapturingProcessHandler(this);
            return timeoutMs > 0 ? handler.runProcess(timeoutMs) : handler.runProcess();
        } catch (ExecutionException e) {
            LOG.warn(e);
            ProcessOutput failed = new ProcessOutput();
            failed.appendStderr(e.getMessage() == null ? "Could not start the process." : e.getMessage());
            failed.setExitCode(-1);
            return failed;
        } finally {
            deleteScriptFile(scriptFile);
        }
    }

    /**
     * Legacy convenience wrapper: stdout lines on success, an empty list on
     * any failure.
     *
     * The empty-list-on-failure contract is preserved because callers depend
     * on it, but the reason is no longer thrown away silently — it now reaches
     * the log. A caller that needs to tell the user what went wrong should use
     * {@link #executeAndCapture} instead.
     */
    @NotNull
    public List<String> executeAndRead(@Nullable File scriptFile) {
        ProcessOutput output = executeAndCapture(scriptFile, 0);
        if (output.isTimeout()) {
            LOG.warn("Timed out: " + getCommandLineString());
            return new ArrayList<>();
        }
        if (output.getExitCode() != 0) {
            LOG.warn("Exited " + output.getExitCode() + ": " + getCommandLineString()
                     + (output.getStderr().isEmpty() ? "" : "\nstderr: " + output.getStderr()));
            return new ArrayList<>();
        }
        return new LinkedList<>(output.getStdoutLines());
    }

    private static void deleteScriptFile(@Nullable File scriptFile) {
        if (scriptFile != null && !scriptFile.delete()) {
            LOG.warn("Could not delete script file: " + scriptFile.getAbsolutePath());
        }
    }

    @Nullable
    private static List<String> populateDebugCommandLine(Project project, int debugPort) {
        List<String> command = new ArrayList<>();
        String homePath = project.getService(RakuProjectSdkService.class).getSdkPath();
        if (homePath == null) return null;

        String versionString = RakuSdkUtil.versionString(homePath);
        if (versionString == null) return null;

        if (VersionComparatorUtil.compare(versionString, "v2019.07") >= 0) {
            String rakuBinary = RakuSdkUtil.findRakuInSdkHome(homePath);
            if (rakuBinary == null) return null;
            command.add(rakuBinary);
            command.add("--debug-port=" + debugPort);
            command.add("--debug-suspend");
        } else {
            Map<String, String> moarBuildConfiguration = project.getService(RakuProjectSdkService.class).getMoarBuildConfig();
            if (moarBuildConfiguration.isEmpty()) return null;

            String prefix = moarBuildConfiguration.getOrDefault("raku::prefix", null);
            if (prefix == null) {
                prefix = moarBuildConfiguration.getOrDefault("Raku::prefix", "");
            }
            command.add(Paths.get(prefix, "bin", "moar").toString());
            // Always start suspended so we have time to send breakpoints and event handlers.
            // If the option is disabled, we'll resume right after that.
            command.add("--debug-port=" + debugPort);
            command.add("--debug-suspend");
            command.add("--libpath=" + Paths.get(prefix, "share", "nqp", "lib"));
            command.add("--libpath=" + Paths.get(prefix, "share", "raku", "lib"));
            command.add("--libpath=" + Paths.get(prefix, "share", "raku", "runtime"));
            command.add(Paths.get(prefix, "share", "raku", "runtime", "raku.moarvm").toString());
        }
        return command;
    }
}
