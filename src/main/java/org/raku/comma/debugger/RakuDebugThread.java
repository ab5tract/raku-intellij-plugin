package org.raku.comma.debugger;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.raku.comma.debugger.event.RakuDebugEventBreakpointReached;
import org.raku.comma.debugger.event.RakuDebugEventBreakpointSet;
import org.raku.comma.debugger.event.RakuDebugEventStop;
import org.raku.comma.run.RakuDebuggableConfiguration;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;
import org.edument.moarvm.DebugEvent;
import org.edument.moarvm.EventType;
import org.edument.moarvm.RemoteInstance;
import org.edument.moarvm.types.ExecutionStack;
import org.edument.moarvm.types.Kind;
import org.edument.moarvm.types.Lexical.*;
import org.edument.moarvm.types.MoarThread;
import org.edument.moarvm.types.StackFrame;
import org.edument.moarvm.types.event.BreakpointNotification;
import org.edument.moarvm.types.event.StepCompletedNotification;
import org.jetbrains.annotations.NotNull;
import org.msgpack.value.Value;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RakuDebugThread extends Thread {
    private static final Logger LOG = Logger.getInstance(RakuDebugThread.class);
    public static final String DEBUG_THREAD_NAME = "Debugger events handler thread";
    private final XDebugSession mySession;
    private final ExecutionResult myExecutionResult;
    private RemoteInstance client;
    private static final Executor myExecutor = ConcurrencyUtil.newSingleThreadExecutor(DEBUG_THREAD_NAME);
    private final List<BreakpointActionRequest> breakpointQueue = new CopyOnWriteArrayList<>();
    private boolean ready = false;

    RakuDebugThread(XDebugSession session, ExecutionResult result) {
        super("RakuDebugThread");
        mySession = session;
        myExecutionResult = result;
    }

    @Override
    public void run() {
        try {
            RakuDebuggableConfiguration runConfiguration = (RakuDebuggableConfiguration) mySession.getRunProfile();
            if (runConfiguration != null) {
                client = RemoteInstance.connect(runConfiguration.getDebugPort()).get(10, TimeUnit.SECONDS);
                ready = true;
                sendBreakpoints();
                setEventHandler();
                if (runConfiguration.isStartSuspended())
                    mySession.positionReached(new RakuSuspendContext(getThreads(), 0, mySession, this));
                else
                    client.resume();
            }
        } catch (CancellationException | InterruptedException | TimeoutException | ExecutionException e) {
            // If the program didn't start properly, do not prompt an odd message
            // when we mis-interpret this situation
            if (mySession.isStopped()) {
                return;
            }
            Notification notification = new Notification("raku.debug.errors", "Connection error", "Could not connect to debug server",
                                                         NotificationType.ERROR);
            Notifications.Bus.notify(notification,  mySession.getProject());
        }
    }

    private void setEventHandler() {
        Subject<DebugEvent> events = client.getSubject();
        events.subscribeOn(Schedulers.newThread()).subscribe(event -> {
            if (event.getEventType() == EventType.BreakpointNotification) {
                RakuDebugThread thread = this;
                ConcurrencyUtil.newSingleThreadExecutor(DEBUG_THREAD_NAME).execute(() -> {
                    BreakpointNotification bpn = (BreakpointNotification) event;
                    RakuThreadDescriptor[] threads;
                    try {
                        // Need to suspend all the threads at the breakpoint, so we can
                        // get the thread list.
                        client.suspend().get();
                        threads = getThreads();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    int activeThreadIndex = 0;
                    for (int i = 0; i < threads.length; i++) {
                        if (threads[i].threadId() == bpn.getThread()) {
                            activeThreadIndex = i;
                            break;
                        }
                    }
                    RakuDebugEventBreakpointReached reachedEvent =
                            new RakuDebugEventBreakpointReached(threads, activeThreadIndex,
                                                                mySession, thread, bpn.getFile(), bpn.getLine());
                    myExecutor.execute(reachedEvent);
                });
            }
            else if (event.getEventType() == EventType.StepCompleted) {
                RakuDebugThread thread = this;
                ConcurrencyUtil.newSingleThreadExecutor(DEBUG_THREAD_NAME).execute(() -> {
                    boolean needsStep = false;
                    StepCompletedNotification scn = (StepCompletedNotification)event;
                    RakuThreadDescriptor[] threads;
                    try {
                        // Need to suspend all the threads at the breakpoint, so we can
                        // get the thread list.
                        client.suspend().get();
                        threads = getThreads();
                        // Skip dispatcher inner calls
                        if (threads.length > 0) {
                            RakuStackFrameDescriptor[] frames = threads[0].stackFrames();
                            if (frames.length > 0 && frames[0].getFile().getPath().contains("dispatchers.nqp")) {
                                needsStep = true;
                            }
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    int activeThreadIndex = 0;
                    for (int i = 0; i < threads.length; i++) {
                        if (threads[i].threadId() == scn.getThread()) {
                            activeThreadIndex = i;
                            break;
                        }
                    }
                    RakuDebugEventStop reachedEvent = new RakuDebugEventStop(threads,
                                                                             activeThreadIndex, mySession, thread);
                    myExecutor.execute(reachedEvent);
                    if (needsStep) {
                      // Needs a step, does step over!
                      mySession.stepOver(false);
                    }
                });
            }
            else {
                // An event kind this handler does not act on. Worth a trace
                // when following a debugger session, not worth printing to
                // the IDE's stdout on every event it ignores.
                LOG.debug("Unhandled debugger event: " + event.getClass().getName());
            }
        }, (error) -> {
            throw new RuntimeException(error);
        });
    }

    public void stopDebugThread() {
        StopProcessAction.stopProcess(myExecutionResult.getProcessHandler());
        ((ConsoleView)myExecutionResult.getExecutionConsole()).print("Disconnected\n", ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    public void resumeExecution() {
        try {
            client.resume().get();
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            LOG.error(e);
        }
    }

    public void pauseExecution() {
        try {
            client.suspend().get();
            RakuThreadDescriptor[] threads = getThreads();
            RakuDebugEventStop stopEvent = new RakuDebugEventStop(threads, 0, mySession, this);
            myExecutor.execute(stopEvent);
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            LOG.error(e);
        }
    }

    private RakuThreadDescriptor[] getThreads() throws ExecutionException, InterruptedException {
        // By now the program must be suspended
        List<MoarThread> threads = client.threadList().get();
        List<RakuThreadDescriptor> threadDescriptors = new ArrayList<>(threads.size());
        for (MoarThread thread : threads) {
            if (thread.suspended) {
                ExecutionStack stack = client.threadStackTrace(thread.threadId).get();
                threadDescriptors.add(new RakuThreadDescriptor(thread.threadId, thread.nativeId,
                                                               stackToFrames(thread, stack)));
            }
        }
        return threadDescriptors.toArray(new RakuThreadDescriptor[0]);
    }

    private RakuStackFrameDescriptor[] stackToFrames(MoarThread thread, ExecutionStack stack) {
        List<StackFrame> frames = stack.getFrames();
        RakuStackFrameDescriptor[] result = new RakuStackFrameDescriptor[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            StackFrame frame = frames.get(i);
            RakuLoadedFileDescriptor fileDescriptor = new RakuLoadedFileDescriptor(frame.getFile(), frame.getName());
            result[i] = new RakuStackFrameDescriptor(fileDescriptor, frame);
            int finalI = i;
            client.contextHandle(thread.threadId, i)
                    .thenApply(v -> client.contextLexicals(v).thenApply(lex -> {
                        result[finalI].setLexicals(convertLexicals(lex));
                        mySession.rebuildViews();
                        client.releaseHandle(new int[]{v});
                        return null;
                    }));
        }
        return result;
    }

    public void stepOver(int threadId) {
        try {
            client.stepOver(threadId).get();
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            LOG.error(e);
        }
    }

    public void stepInto(int threadId) {
        try {
            client.stepInto(threadId).get();
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            LOG.error(e);
        }
    }

    public void stepOut(int threadId) {
        try {
            client.stepOut(threadId, 1).get();
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            LOG.error(e);
        }
    }

    private RakuValueDescriptor[] convertLexicals(Map<String, Lexical> lex) {
        RakuValueDescriptor[] result = new RakuValueDescriptor[lex.size()];
        AtomicInteger i = new AtomicInteger();
        lex.forEach((k, v) -> {
            result[i.get()] = convertDescriptor(k, v);
            i.getAndIncrement();
        });
        return result;
    }

    @NotNull
    private RakuValueDescriptor convertDescriptor(String k, Lexical v) {
        RakuValueDescriptor descriptor;
        switch (v.getKind()) {
            case INT:
                descriptor = new RakuNativeValueDescriptor(k, "int",
                                                           String.valueOf(((IntValue) v).getValue()));
                break;
            case NUM:
                descriptor = new RakuNativeValueDescriptor(k, "num",
                                                           String.valueOf(((NumValue) v).getValue()));
                break;
            case STR:
                descriptor = new RakuNativeValueDescriptor(k, "str",
                                                           String.valueOf(((StrValue) v).getValue()));
                break;
            default:
                ObjValue ov = (ObjValue)v;
                String type = ov.getType();
                boolean concrete = ov.isConcrete();
                int handle = ov.getHandle();
                descriptor = new RakuObjectValueDescriptor(k, type, concrete, handle,
                                                           presentableDescriptionForType(ov, true));
                break;
        }
        return descriptor;
    }

    /* Some types receive special handling to show them in a nicer way. */
    private String presentableDescriptionForType(ObjValue ov, boolean recurse) {
        try {
            if (!ov.isConcrete())
                return null;
            int handle = ov.getHandle();
            switch (ov.getType()) {
                case "Bool": {
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsInt = attrs.get("Int");
                    if (attrsInt != null) {
                        Lexical value = attrsInt.get("$!value");
                        if (value != null && value.getKind() == Kind.INT)
                            return ((IntValue)value).getValue() == 0 ? "False" : "True";
                    }
                }
                case "Int": {
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsInt = attrs.get("Int");
                    if (attrsInt != null) {
                        Lexical value = attrsInt.get("$!value");
                        if (value != null && value.getKind() == Kind.INT)
                            return Integer.toString(((IntValue)value).getValue());
                    }
                    break;
                }
                case "Num": {
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsNum = attrs.get("Num");
                    if (attrsNum != null) {
                        Lexical value = attrsNum.get("$!value");
                        if (value != null && value.getKind() == Kind.NUM)
                            return Double.toString(((NumValue)value).getValue());
                    }
                    break;
                }
                case "Rat": {
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsRat = attrs.get("Rat");
                    if (attrsRat != null) {
                        Lexical nu = attrsRat.get("$!numerator");
                        Lexical de = attrsRat.get("$!denominator");
                        if (nu != null && nu.getKind() == Kind.OBJ && de != null && de.getKind() == Kind.OBJ) {
                            ObjValue ovNu = (ObjValue)nu;
                            ObjValue ovDe = (ObjValue)de;
                            String nuPres = presentableDescriptionForType(ovNu, false);
                            String dePres = presentableDescriptionForType(ovDe, false);
                            if (nuPres != null && dePres != null)
                                return Double.toString(Double.parseDouble(nuPres) / Double.parseDouble(dePres));
                        }
                    }
                    break;
                }
                case "Str": {
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsStr = attrs.get("Str");
                    if (attrsStr != null) {
                        Lexical value = attrsStr.get("$!value");
                        if (value != null && value.getKind() == Kind.STR)
                            return ((StrValue)value).getValue();
                    }
                    break;
                }
                case "Scalar": {
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsScalar = attrs.get("Scalar");
                    if (attrsScalar != null) {
                        Lexical value = attrsScalar.get("$!value");
                        if (value != null && value.getKind() == Kind.OBJ) {
                            ObjValue vov = (ObjValue)value;
                            String nested = presentableDescriptionForType(vov, recurse);
                            if (nested == null)
                                nested = defaultObjectRepresentation(vov);
                            return "$ = " + nested;
                        }
                    }
                    break;
                }
                case "List":
                case "Array": {
                    if (!recurse)
                        return defaultObjectRepresentation(ov);
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsList = attrs.get("List");
                    if (attrsList != null) {
                        Lexical reified = attrsList.get("$!reified");
                        Lexical todo = attrsList.get("$!todo");
                        if (reified != null && reified.getKind() == Kind.OBJ &&
                            todo != null && todo.getKind() == Kind.OBJ) {
                            boolean isArray = ov.getType().equals("Array");
                            boolean lazy = ((ObjValue)todo).isConcrete();
                            if (((ObjValue)reified).isConcrete()) {
                                List<Lexical> elements = client.getObjectPositionals(((ObjValue)reified).getHandle()).get();
                                int elems = elements.size();
                                List<String> elemsRendered = new ArrayList<>();
                                for (int i = 0; i < Math.min(elems, 5); i++) {
                                    ObjValue aov = (ObjValue)elements.get(i);
                                    String nested = presentableDescriptionForType(aov, false);
                                    if (nested == null)
                                        nested = defaultObjectRepresentation(aov);
                                    if (isArray && nested.startsWith("$ = "))
                                        nested = nested.substring(4);
                                    elemsRendered.add(nested);
                                }
                                if (lazy) {
                                    elemsRendered.add("...lazy...");
                                }
                                else if (elems > 5) {
                                    elemsRendered.add("...total " + elems + " elems");
                                }
                                String values = String.join(", ", ArrayUtil.toStringArray(elemsRendered));
                                return isArray ? "[" + values + "]" : "(" + values + ")";
                            }
                            else {
                                // Empty, or lazy and unevaluated.
                                if (lazy)
                                    return isArray ? "[...lazy...]" : "(...lazy...)";
                                else
                                    return isArray ? "[]" : "()";
                            }
                        }
                    }
                }
                case "Map":
                case "Hash": {
                    if (!recurse)
                        return defaultObjectRepresentation(ov);
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsMap = attrs.get("Map");
                    if (attrsMap != null) {
                        Lexical storage = attrsMap.get("$!storage");
                        if (storage != null && storage.getKind() == Kind.OBJ) {
                            boolean isHash = ov.getType().equals("Hash");
                            if (((ObjValue)storage).isConcrete()) {
                                Map<String, Lexical> elements = client.getObjectAssociatives(((ObjValue)storage).getHandle()).get();
                                int elems = elements.size();
                                List<String> elemsRendered = new ArrayList<>();
                                int i = 0;
                                for (String name : elements.keySet()) {
                                    ObjValue hov = (ObjValue)elements.get(name);
                                    String nested = presentableDescriptionForType(hov, false);
                                    if (nested == null)
                                        nested = defaultObjectRepresentation(hov);
                                    if (isHash && nested.startsWith("$ = "))
                                        nested = nested.substring(4);
                                    elemsRendered.add(name + " => " + nested);
                                    if (++i == 5)
                                        break;
                                }
                                if (elems > 5) {
                                    elemsRendered.add("...total " + elems + " elems");
                                }
                                String values = String.join(", ", ArrayUtil.toStringArray(elemsRendered));
                                return isHash ? "{" + values + "}" : "Map.new((" + values + "))";
                            }
                            else {
                                return isHash ? "{}" : "Map.new";
                            }
                        }
                    }
                    break;
                }
                case "Promise": {
                    Map<String, Map<String, Lexical>> attrs = client.getObjectAttributes(handle).get();
                    Map<String, Lexical> attrsPromise = attrs.get("Promise");
                    if (attrsPromise != null) {
                        Lexical status = attrsPromise.get("$!status");
                        Lexical result = attrsPromise.get("$!result");
                        if (status != null && status.getKind() == Kind.OBJ && result != null && result.getKind() == Kind.OBJ) {
                            Map<String, Map<String, Lexical>> statusAttrs =
                                client.getObjectAttributes(((ObjValue)status).getHandle()).get();
                            String statusString =
                                presentableDescriptionForType((ObjValue)statusAttrs.get("PromiseStatus").get("$!key"), false);
                            if (statusString != null) {
                                switch (statusString) {
                                    case "Planned":
                                        return "Promise.new (not yet kept or broken)";
                                    case "Kept":
                                    case "Broken": {
                                        String nested = presentableDescriptionForType((ObjValue)result, false);
                                        if (nested == null)
                                            nested = defaultObjectRepresentation((ObjValue)result);
                                        return "Promise." + statusString.toLowerCase(Locale.ENGLISH) + "(" + nested + ")";
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
            return null;
        }
        catch (Throwable t) {
            LOG.error(t);
            return null;
        }
    }

    @NotNull
    private static String defaultObjectRepresentation(ObjValue ov) {
        String type = ov.getType();
        return ov.isConcrete() ? type + ".new" : "(" + type + ")";
    }

    private void sendBreakpoints() {
        if (ready) {
            try {
                for (BreakpointActionRequest request : breakpointQueue) {
                    if (!request.isRemove) {
                        /* IDEA counts line numbers from 0, even though they
                         * are counted starting at 1 in the UI.
                         * MoarVM's debugserver likewise starts at 1, so we
                         * have to add 1 here to get everything right.
                         */
                        int realLine = client.setBreakpoint(request.file, request.line + 1, true, false).get();
                        /* The breakpoint line number we've received back from
                         * the debugserver has to be translated back.
                         */
                        realLine -= 1;
                        RakuDebugEventBreakpointSet setEvent =
                                new RakuDebugEventBreakpointSet(request.file, realLine);
                        setEvent.setDebugSession(mySession);
                        setEvent.setDebugThread(this);
                        myExecutor.execute(setEvent);
                    } else {
                        client.clearBreakpoint(request.file, request.line + 1).get();
                    }
                }
            } catch (CancellationException | InterruptedException | ExecutionException e) {
                LOG.error(e);
            }
            breakpointQueue.clear();
        }
    }

    public void queueBreakpoint(XLineBreakpoint<?> breakpoint, boolean isRemove) {
        breakpointQueue.add(new BreakpointActionRequest(isRemove, breakpoint));
        sendBreakpoints();
    }

    public void addObjectChildren(int handle, XCompositeNode node) {
        objectMetadata(handle)
            .thenAccept(metadata -> {
                Value attrFeatures = metadata.get("attr_features");
                if (attrFeatures != null && attrFeatures.asBooleanValue().getBoolean()) {
                    addObjectAttributes(handle, node);
                    return;
                }
                Value posFeatures = metadata.get("pos_features");
                if (posFeatures != null && posFeatures.asBooleanValue().getBoolean()) {
                    addPositionalElements(handle, node);
                    return;
                }
                Value assFeatures = metadata.get("ass_features");
                if (assFeatures != null && assFeatures.asBooleanValue().getBoolean()) {
                    addAssociativeElements(handle, node);
                    return;
                }
                node.addChildren(XValueChildrenList.EMPTY, true);
            });
    }

    private void addObjectAttributes(int handle, XCompositeNode node) {
        client.getObjectAttributes(handle)
              .thenAccept(classAttrs -> {
                  XValueChildrenList children = new XValueChildrenList();
                  for (Map.Entry<String, Map<String,Lexical>> classEntry : classAttrs.entrySet()) {
                        RakuValueDescriptor[] descriptors = convertLexicals(classEntry.getValue());
                        for (RakuValueDescriptor desc : descriptors) {
                            children.add(new RakuXAttributeValue(desc, this));
                        }
                    }
                    node.addChildren(children, true);
              });
    }

    private void addPositionalElements(int handle, XCompositeNode node) {
        node.addChildren(XValueChildrenList.EMPTY, true);
        client.getObjectPositionals(handle)
              .thenAccept(positional -> {
                  XValueChildrenList children = new XValueChildrenList();
                  for (int i = 0; i < positional.size(); i++) {
                      children.add(new RakuXAggregateValue(
                          convertDescriptor(Integer.toString(i), positional.get(i)),
                          this));
                  }
                  node.addChildren(children, true);
              });
    }

    private void addAssociativeElements(int handle, XCompositeNode node) {
        node.addChildren(XValueChildrenList.EMPTY, true);
        client.getObjectAssociatives(handle)
              .thenAccept(associative -> {
                  XValueChildrenList children = new XValueChildrenList();
                  for (RakuValueDescriptor desc : convertLexicals(associative))
                      children.add(new RakuXAggregateValue(desc, this));
                  node.addChildren(children, true);
              });
    }

    private CompletableFuture<Map<String, Value>> objectMetadata(int handle) {
        return client.getObjectMetadata(handle)
            .thenApply(metadataMessage -> {
                Map<String, Value> metadata = new HashMap<>();
                for (Map.Entry<Value, Value> entry : metadataMessage.asMapValue().entrySet())
                    metadata.put(entry.getKey().asStringValue().asString(), entry.getValue());
                return metadata;
            });
    }

    static class BreakpointActionRequest {
        final boolean isRemove;
        final String file;
        final int line;

        BreakpointActionRequest(boolean isRemove, XLineBreakpoint<?> bp) {
            this.isRemove = isRemove;
            this.file = bp.getFileUrl().substring(7);
            this.line = bp.getLine();
        }
    }
}
