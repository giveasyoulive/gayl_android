/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui.robots

import android.app.NotificationManager
import android.content.Context
import androidx.test.uiautomator.By.text
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTime
import org.mozilla.fenix.helpers.TestHelper
import org.mozilla.fenix.helpers.TestHelper.appName
import org.mozilla.fenix.helpers.ext.waitNotNull

class NotificationRobot {

    fun verifySystemNotificationExists(notificationMessage: String) {
        val notification = UiSelector().text(notificationMessage)
        var notificationFound = mDevice.findObject(notification).waitForExists(waitingTime)

        while (!notificationFound) {
            scrollToEnd()
            notificationFound = mDevice.findObject(notification).waitForExists(waitingTime)
        }

        assertTrue(notificationFound)
    }

    fun clearNotifications() {
        if (clearButton.exists()) {
            clearButton.click()
        } else {
            scrollToEnd()
            if (clearButton.exists()) {
                clearButton.click()
            } else if (notificationTray().exists()) {
                mDevice.pressBack()
            }
        }
    }

    fun cancelAllShownNotifications() {
        cancelAll()
    }

    fun verifySystemNotificationGone(notificationMessage: String) {
        mDevice.waitNotNull(
            Until.gone(text(notificationMessage)),
            waitingTime
        )

        assertFalse(
            mDevice.findObject(
                UiSelector().text(notificationMessage)
            ).exists()
        )
    }

    fun verifyPrivateTabsNotification() {
        mDevice.waitNotNull(Until.hasObject(text("Close private tabs")), waitingTime)
        assertPrivateTabsNotification()
    }

    fun clickMediaNotificationControlButton(action: String) {
        mediaSystemNotificationButton(action).waitForExists(waitingTime)
        mediaSystemNotificationButton(action).click()
    }

    fun clickDownloadNotificationControlButton(action: String) {
        try {
            // double check if notification actions are viewable by checking for action existence; otherwise scroll again
            while (!downloadSystemNotificationButton(action).exists()) {
                scrollToEnd()
                notificationTray().ensureFullyVisible(downloadSystemNotificationButton(action))
            }

            // assertTrue(downloadSystemNotificationButton(action).waitForExists(waitingTime))
            downloadSystemNotificationButton(action).click()
        } catch (e: UiObjectNotFoundException) {
            if (expandNotificationButton.contentDescription.equals("Expand"))
                expandNotificationButton.click()
            downloadSystemNotificationButton(action).click()
        }
        // API 30 Bug? Sometimes a click doesn't register, try again
        try {
            assertTrue(downloadSystemNotificationButton(action).waitUntilGone(waitingTime))
        } catch (e: AssertionError) {
            downloadSystemNotificationButton(action).click()
        }
    }

    fun verifyMediaSystemNotificationButtonState(action: String) {
        assertTrue(mediaSystemNotificationButton(action).waitForExists(waitingTime))
    }

    fun verifyDownloadSystemNotificationButtonState(action: String) {
        assertTrue(downloadSystemNotificationButton(action).waitForExists(waitingTime))
    }

    fun expandDownloadNotificationMessage() {
        while (!notificationHeader.exists()) {
            scrollToEnd()
            notificationTray().ensureFullyVisible(notificationHeader)
        }

        if (expandNotificationButton.contentDescription.equals("Expand")) {
            // expand the notification
            notificationHeader.click()
        }
    }

    class Transition {

        fun clickClosePrivateTabsNotification(interact: HomeScreenRobot.() -> Unit): HomeScreenRobot.Transition {
            NotificationRobot().verifySystemNotificationExists("Close private tabs")
            closePrivateTabsNotification().click()

            HomeScreenRobot().interact()
            return HomeScreenRobot.Transition()
        }
    }
}

fun notificationShade(interact: NotificationRobot.() -> Unit): NotificationRobot.Transition {
    NotificationRobot().interact()
    return NotificationRobot.Transition()
}

private fun assertPrivateTabsNotification() {
    mDevice.findObject(UiSelector().text("Firefox Preview (Private)")).exists()
    mDevice.findObject(UiSelector().text("Close private tabs")).exists()
}

private fun closePrivateTabsNotification() =
    mDevice.findObject(UiSelector().text("Close private tabs"))

private fun downloadSystemNotificationButton(action: String) =
    mDevice.findObject(
        UiSelector()
            .resourceId("android:id/action0")
            .textContains(action)
    )

private fun mediaSystemNotificationButton(action: String) =
    mDevice.findObject(
        UiSelector()
            .resourceId("com.android.systemui:id/action0")
            .descriptionContains(action)
    )

private fun notificationTray() = UiScrollable(
    UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller")
).setAsVerticalList()

private val notificationHeader =
    mDevice.findObject(
        UiSelector()
            .resourceId("android:id/app_name_text")
            .text(appName)
    )

private fun scrollToEnd() {
    notificationTray().scrollToEnd(1)
}

private val clearButton = mDevice.findObject(UiSelector().resourceId("com.android.systemui:id/dismiss_text"))

private fun cancelAll() {
    val notificationManager: NotificationManager =
        TestHelper.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()
}

private val expandNotificationButton =
    notificationHeader
        .getFromParent(
            UiSelector().resourceId("android:id/expand_button")
        )
