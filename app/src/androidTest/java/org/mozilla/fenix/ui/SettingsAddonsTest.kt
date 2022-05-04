/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import android.view.View
import androidx.test.espresso.IdlingRegistry
import androidx.test.uiautomator.UiSelector
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.R
import org.mozilla.fenix.customannotations.SmokeTest
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.FeatureSettingsHelper
import org.mozilla.fenix.helpers.HomeActivityTestRule
import org.mozilla.fenix.helpers.RecyclerViewIdlingResource
import org.mozilla.fenix.helpers.TestAssetHelper
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.fenix.helpers.ViewVisibilityIdlingResource
import org.mozilla.fenix.ui.robots.addonsMenu
import org.mozilla.fenix.ui.robots.homeScreen
import org.mozilla.fenix.ui.robots.mDevice
import org.mozilla.fenix.ui.robots.navigationToolbar
import java.io.File

/**
 *  Tests for verifying the functionality of installing or removing addons
 *
 */
class SettingsAddonsTest {
    private lateinit var mockWebServer: MockWebServer
    private var addonsListIdlingResource: RecyclerViewIdlingResource? = null
    private var addonContainerIdlingResource: ViewVisibilityIdlingResource? = null
    private val featureSettingsHelper = FeatureSettingsHelper()
    private val addonName = "uBlock Origin"

    @get:Rule
    val activityTestRule = HomeActivityTestRule()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
        // disabling the new homepage pop-up that interferes with the tests.
        featureSettingsHelper.setJumpBackCFREnabled(false)

        changeGeckoPrefs(
            mapOf(
                "extensions.logging.enabled" to true,
            )
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()

        if (addonsListIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(addonsListIdlingResource!!)
        }

        if (addonContainerIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(addonContainerIdlingResource!!)
        }

        // resetting modified features enabled setting to default
        featureSettingsHelper.resetAllFeatureFlags()
    }

    // Walks through settings add-ons menu to ensure all items are present
    // @Test
    // fun settingsAddonsItemsTest() {
    //     homeScreen {
    //     }.openThreeDotMenu {
    //     }.openSettings {
    //         verifyAdvancedHeading()
    //         verifyAddons()
    //     }.openAddonsManagerMenu {
    //         addonsListIdlingResource =
    //             RecyclerViewIdlingResource(activityTestRule.activity.findViewById(R.id.add_ons_list), 1)
    //         IdlingRegistry.getInstance().register(addonsListIdlingResource!!)
    //         verifyAddonsItems()
    //     }
    // }

    // Installs an add-on from the Add-ons menu and verifies the prompts
    @Test
    fun installAddonTest() {
        homeScreen {}
            .openThreeDotMenu {}
            .openAddonsManagerMenu {
                addonsListIdlingResource =
                    RecyclerViewIdlingResource(
                        activityTestRule.activity.findViewById(R.id.add_ons_list),
                        1
                    )
                IdlingRegistry.getInstance().register(addonsListIdlingResource!!)
                clickInstallAddon(addonName)
                verifyAddonPermissionPrompt(addonName)
                cancelInstallAddon()
                clickInstallAddon(addonName)
                acceptPermissionToInstallAddon()
                assumeFalse(mDevice.findObject(UiSelector().text("Failed to install $addonName")).waitForExists(waitingTimeShort))
                closeAddonInstallCompletePrompt(addonName, activityTestRule)
                verifyAddonIsInstalled(addonName)
                verifyEnabledTitleDisplayed()
            }
    }

    // Installs an addon, then uninstalls it
    @Test
    fun verifyAddonsCanBeUninstalled() {
        addonsMenu {
            installAddon()
            closeAddonInstallCompletePrompt(addonName, activityTestRule)
            IdlingRegistry.getInstance().unregister(addonsListIdlingResource!!)
        }.openDetailedMenuForAddon(addonName) {
            addonContainerIdlingResource = ViewVisibilityIdlingResource(
                activityTestRule.activity.findViewById(R.id.addon_container),
                View.VISIBLE
            )
            IdlingRegistry.getInstance().register(addonContainerIdlingResource!!)
        }.removeAddon {
            IdlingRegistry.getInstance().unregister(addonContainerIdlingResource!!)
            verifyAddonCanBeInstalled(addonName)
        }
    }

    @SmokeTest
    @Test
    // Installs uBlock add-on and checks that the app doesn't crash while loading pages with trackers
    fun noCrashWithAddonInstalledTest() {
        // setting ETP to Strict mode to test it works with add-ons
        activityTestRule.activity.settings().setStrictETP()

        val trackingProtectionPage =
            TestAssetHelper.getEnhancedTrackingProtectionAsset(mockWebServer)

        addonsMenu {
            installAddon()
            closeAddonInstallCompletePrompt(addonName, activityTestRule)
            IdlingRegistry.getInstance().unregister(addonsListIdlingResource!!)
        }.goBack {
        }.openNavigationToolbar {
        }.enterURLAndEnterToBrowser(trackingProtectionPage.url) {
           verifyPageContent(trackingProtectionPage.content)
        }
    }

    @SmokeTest
    @Test
    fun useAddonsInPrivateModeTest() {
        val testPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        homeScreen {
        }.togglePrivateBrowsingMode()
        addonsMenu {
            installAddon()
            selectAllowInPrivateBrowsing(/*, activityTestRule*/)
            closeAddonInstallCompletePrompt(addonName, activityTestRule)
            IdlingRegistry.getInstance().unregister(addonsListIdlingResource!!)
        }.goBack {}
        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.url) {
        }.openThreeDotMenu {
            openAddonsSubList()
            verifyAddonAvailableInMainMenu(addonName)
        }
    }

    private fun installAddon() {
        homeScreen {
        }.openThreeDotMenu {
        }.openAddonsManagerMenu {
            addonsListIdlingResource =
                RecyclerViewIdlingResource(
                    activityTestRule.activity.findViewById(R.id.add_ons_list),
                    1
                )
           IdlingRegistry.getInstance().register(addonsListIdlingResource!!)
            clickInstallAddon(addonName)
            verifyAddonPermissionPrompt(addonName)
            acceptPermissionToInstallAddon()
            assumeFalse(mDevice.findObject(UiSelector().text("Failed to install $addonName")).waitForExists(waitingTimeShort))
        }
    }

    /**
     * Change any Gecko preferences. Similar to manually updating them through "about:config".
     * Each call of this method will reset any previous override.
     * Accepts any number of preferences to override with any type of values.
     *
     * Example usage:
     * ```
     * changeGeckoPrefs(
     *   mapOf(
     *     "extensions.logging.enabled" to true,
     *     "extensions.blocklist.detailsURL" to "DefaultOverriden",
     *     "extensions.autoDisableScopes" to 99999
     *   )
     * )
     * ```
     * ![Geckoview documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/automation.html)
     */
    private fun changeGeckoPrefs(prefs: Map<String, Any>) {
        // Change any gecko preferences with the help of a special file placed in a special location.
        // The "data/local/tmp" location GeckoView uses by default seems to need a more cumbersome approach for accessing.

        val newline = "\n"
        val indent = "  "
        val separator = ": "
        val preferences = StringBuilder(prefs.size)
        // Need to iterate and add each entry to our text to avoid printing a list with brackets and commas.
        prefs.forEach {
            preferences.append(indent + it.key + separator + it.value + newline)
        }
        val textToWrite = "prefs:$newline$preferences"

        // Use the "echo" unix command to write our needed configuration.
        val writeCommand = listOf("echo", textToWrite)

        val geckoViewConfigFilePath =
            "/data/local/tmp/" +
                activityTestRule.activity.packageName +
                "-geckoview-config.yaml"

        // Actually execute "echo" with the needed configuration in the provided file.
        ProcessBuilder(writeCommand).apply {
            redirectOutput(File(geckoViewConfigFilePath))
            start()
        }
    }
}
