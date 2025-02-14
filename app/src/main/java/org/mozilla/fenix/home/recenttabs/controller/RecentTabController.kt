/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.recenttabs.controller

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.navigation.NavController
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.tabs.TabsUseCases.SelectTabUseCase
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.ext.inProgressMediaTab
import org.mozilla.fenix.home.HomeFragmentDirections
import org.mozilla.fenix.home.recenttabs.RecentTab
import org.mozilla.fenix.home.recenttabs.interactor.RecentTabInteractor

/**
 * An interface that handles the view manipulation of the recent tabs in the Home screen.
 */
interface RecentTabController {

    /**
     * @see [RecentTabInteractor.onRecentTabClicked]
     */
    fun handleRecentTabClicked(tabId: String)

    /**
     * @see [RecentTabInteractor.onRecentSearchGroupClicked]
     */
    fun handleRecentSearchGroupClicked(tabId: String)

    /**
     * @see [RecentTabInteractor.onRecentTabShowAllClicked]
     */
    fun handleRecentTabShowAllClicked()

    /**
     * @see [RecentTabInteractor.onRemoveRecentTab]
     */
    fun handleRecentTabRemoved(tab: RecentTab.Tab)
}

/**
 * The default implementation of [RecentTabController].
 *
 * @param selectTabUseCase [SelectTabUseCase] used selecting a tab.
 * @param navController [NavController] used for navigation.
 */
class DefaultRecentTabsController(
    private val selectTabUseCase: SelectTabUseCase,
    private val navController: NavController,
    private val metrics: MetricController,
    private val store: BrowserStore,
    private val appStore: AppStore,
) : RecentTabController {

    override fun handleRecentTabClicked(tabId: String) {
        if (tabId == store.state.inProgressMediaTab?.id) {
            metrics.track(Event.OpenInProgressMediaTab)
        } else {
            metrics.track(Event.OpenRecentTab)
        }

        selectTabUseCase.invoke(tabId)
        navController.navigate(R.id.browserFragment)
    }

    override fun handleRecentTabShowAllClicked() {
        dismissSearchDialogIfDisplayed()
        metrics.track(Event.ShowAllRecentTabs)
        navController.navigate(HomeFragmentDirections.actionGlobalTabsTrayFragment())
    }

    override fun handleRecentSearchGroupClicked(tabId: String) {
        metrics.track(Event.JumpBackInGroupTapped)
        navController.navigate(
            HomeFragmentDirections.actionGlobalTabsTrayFragment(
                focusGroupTabId = tabId
            )
        )
    }

    override fun handleRecentTabRemoved(tab: RecentTab.Tab) {
        appStore.dispatch(AppAction.RemoveRecentTab(tab))
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun dismissSearchDialogIfDisplayed() {
        if (navController.currentDestination?.id == R.id.searchDialogFragment) {
            navController.navigateUp()
        }
    }
}
