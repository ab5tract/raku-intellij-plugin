package org.raku.comma.sdk

import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.PlatformTestUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.project.RakuProjectKind
import java.util.concurrent.atomic.AtomicBoolean

/**
 * `reactToSdkIssue` is where the plugin decides whether an SDK problem is
 * worth interrupting someone over. Nothing in src/test mentioned it.
 */
class ReactToSdkIssueTest : CommaFixtureTestCase() {

    /**
     * The one-shot guard is a private static on RakuSdkUtil and is never
     * reset, so in a shared test JVM whichever test (or production code path)
     * notifies first silences every later caller. Both tests below would
     * otherwise be decided by run order rather than by the code under test.
     */
    private fun alreadyPromptedFlag(): AtomicBoolean =
        RakuSdkUtil::class.java.getDeclaredField("alreadyPrompted")
            .also { it.isAccessible = true }
            .get(null) as AtomicBoolean

    private fun notificationsDuring(body: () -> Unit): List<Notification> {
        val seen = java.util.Collections.synchronizedList(mutableListOf<Notification>())
        val listener = object : Notifications {
            override fun notify(notification: Notification) { seen.add(notification) }
        }
        val app = ApplicationManager.getApplication().messageBus.connect()
        val proj = project.messageBus.connect()
        app.subscribe(Notifications.TOPIC, listener)
        proj.subscribe(Notifications.TOPIC, listener)
        try {
            body()
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        } finally {
            app.disconnect()
            proj.disconnect()
        }
        return seen.toList()
    }

    fun testDoesNotNotifyWhenTheProjectHasNoRakuFiles() {
        emptyTheSourceRoots()
        assertFalse("precondition: the project must look non-Raku",
                    RakuProjectKind.hasRakuFiles(project))

        val flag = alreadyPromptedFlag()
        val saved = flag.get()
        flag.set(false)
        try {
            val seen = notificationsDuring {
                RakuSdkUtil.reactToSdkIssue(project, "Quiet SDK issue", "should not be shown")
            }
            assertTrue("a project with no Raku in it must not be interrupted about the Raku SDK, " +
                       "was: ${seen.map { it.title }}",
                       seen.none { it.title == "Quiet SDK issue" })
            assertFalse("and the one-shot guard must not have been spent on it", flag.get())
        } finally {
            flag.set(saved)
        }
    }

    // A null project cannot be attributed to one, and silencing that would
    // hide a genuine global SDK problem -- RakuFileHandler reports exactly
    // that way.
    fun testNotifiesWhenThereIsNoProjectToAttributeItTo() {
        val flag = alreadyPromptedFlag()
        val saved = flag.get()
        flag.set(false)
        try {
            val seen = notificationsDuring {
                RakuSdkUtil.reactToSdkIssue(null, "Global SDK issue", "cannot be attributed")
            }
            assertTrue("an SDK issue with no project must still be reported, was: " +
                       "${seen.map { it.title }}",
                       seen.any { it.title == "Global SDK issue" })
        } finally {
            flag.set(saved)
        }
    }
}
