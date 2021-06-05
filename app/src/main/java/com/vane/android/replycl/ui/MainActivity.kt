package com.vane.android.replycl.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.vane.android.replycl.R
import com.vane.android.replycl.data.EmailStore
import com.vane.android.replycl.databinding.ActivityMainBinding
import com.vane.android.replycl.ui.compose.ComposeFragmentDirections
import com.vane.android.replycl.ui.email.EmailFragmentArgs
import com.vane.android.replycl.ui.home.HomeFragmentDirections
import com.vane.android.replycl.ui.home.Mailbox
import com.vane.android.replycl.ui.nav.*
import com.vane.android.replycl.ui.search.SearchFragmentDirections
import com.vane.android.replycl.util.contentView
import kotlin.LazyThreadSafetyMode.*

class MainActivity : AppCompatActivity(),
    Toolbar.OnMenuItemClickListener,
    NavController.OnDestinationChangedListener,
    NavigationAdapter.NavigationAdapterListener {

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)
    private val bottomNavDrawer: BottomNavDrawerFragment by lazy(NONE) {
        supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as BottomNavDrawerFragment
    }

    // Keep track of the current Email being viewed, if any, in order to pass the correct email id
    // to ComposeFragment when this Activity's FAB is clicked.
    private var currentEmailId = -1L

    val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBottomNavigationAndFab()

    }

    private fun setUpBottomNavigationAndFab() {
        // Wrap binding.run to ensure ContentViewBindingDelegate is calling this Activity's
        // setContentView before accessing views
        binding.run {
            findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener(
                this@MainActivity
            )
        }

        // Set a custom animation for showing and hiding the FAB
        binding.fab.apply {
            setShowMotionSpecResource(R.animator.fab_show)
            setHideMotionSpecResource(R.animator.fab_hide)
            setOnClickListener {
                navigateToCompose()
            }
        }

        // Set up some State and Slide Actions for the bottomNavDrawer
        bottomNavDrawer.apply {
            addOnSlideAction(HalfClockwiseRotateSlideAction(binding.bottomAppBarChevron))
            addOnSlideAction(AlphaSlideAction(binding.bottomAppBarTitle, true))
            addOnStateChangedAction(ShowHideFabStateAction(binding.fab))
            addOnStateChangedAction(ChangeSettingsMenuStateAction { showSettings ->
                // Toggle between the current destination's BAB menu and the menu which should
                // be displayed when the BottomNavigationDrawer is open.
                binding.bottomAppBar.replaceMenu(
                    if (showSettings) {
                        R.menu.bottom_app_bar_settings_menu
                    } else {
                        getBottomAppBarMenuForDestination()
                    }
                )
            })
            addOnSandwichSlideAction(HalfCounterClockwiseRotateSlideAction(binding.bottomAppBarChevron))
            addNavigationListener(this@MainActivity)
        }

        // Set up the BottomAppBar menu
        binding.bottomAppBar.apply {
            setNavigationOnClickListener {
                bottomNavDrawer.toggle()
            }
            setOnMenuItemClickListener(this@MainActivity)
        }

        // Set up the BottomNavigationDrawer's open/close affordance
        binding.bottomAppBarContentContainer.setOnClickListener {
            bottomNavDrawer.toggle()
        }
    }


    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        // Set the currentEmail being viewed so when the FAB is pressed, the correct email
        // reply is created. In a real app, this should be done in a ViewModel but is done
        // here to keep things simple. Here we're also setting the configuration of the
        // BottomAppBar and FAB based on the current destination.
        when (destination.id) {
            R.id.homeFragment -> {
                currentEmailId = -1
                setBottomAppBarForHome(getBottomAppBarMenuForDestination(destination))
            }
            R.id.emailFragment -> {
                currentEmailId =
                    if (arguments == null) -1 else EmailFragmentArgs.fromBundle(arguments).emailId
                setBottomAppBarForEmail(getBottomAppBarMenuForDestination(destination))
            }
            R.id.composeFragment -> {
                currentEmailId = -1
                setBottomAppBarForCompose()
            }
            R.id.searchFragment -> {
                currentEmailId = -1
                setBottomAppBarForSearch()
            }
        }
    }


    /**
     * Helper function which returns the menu which should be displayed for the current
     * destination.
     *
     * Used both when the destination has changed, centralizing destination-to-menu mapping, as
     * well as switching between the alternate menu used when the BottomNavigationDrawer is
     * open and closed.
     */
    @MenuRes
    private fun getBottomAppBarMenuForDestination(destination: NavDestination? = null): Int {
        val dest = destination ?: findNavController(R.id.nav_host_fragment).currentDestination
        return when (dest?.id) {
            R.id.homeFragment -> R.menu.bottom_app_bar_home_menu
            R.id.emailFragment -> R.menu.bottom_app_bar_email_menu
            else -> R.menu.bottom_app_bar_home_menu
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_settings -> {
                bottomNavDrawer.close()
                showDarkThemeMenu()
            }
            R.id.menu_search -> navigateToSearch()
            R.id.menu_email_star -> {
                EmailStore.update(currentEmailId) { isStarred = !isStarred }
            }
            R.id.menu_email_delete -> {
                EmailStore.delete(currentEmailId)
                findNavController(R.id.nav_host_fragment).popBackStack()
            }
        }
        return true
    }


    private fun setBottomAppBarForHome(@MenuRes menuRes: Int) {
        binding.run {
            fab.setImageState(intArrayOf(-android.R.attr.state_activated), true)
            bottomAppBar.visibility = View.VISIBLE
            bottomAppBar.replaceMenu(menuRes)
            fab.contentDescription = getString(R.string.fab_compose_email_content_description)
            bottomAppBarTitle.visibility = View.VISIBLE
            bottomAppBar.performShow()
            fab.show()
        }
    }


    private fun setBottomAppBarForEmail(@MenuRes menuRes: Int) {
        binding.run {
            fab.setImageState(intArrayOf(android.R.attr.state_activated), true)
            bottomAppBar.visibility = View.VISIBLE
            bottomAppBar.replaceMenu(menuRes)
            fab.contentDescription = getString(R.string.fab_reply_email_content_description)
            bottomAppBarTitle.visibility = View.INVISIBLE
            bottomAppBar.performShow()
            fab.show()
        }
    }

    private fun setBottomAppBarForCompose() {
        hideBottomAppBar()
    }

    private fun setBottomAppBarForSearch() {
        hideBottomAppBar()
        binding.fab.hide()
    }

    private fun hideBottomAppBar() {
        binding.run {
            bottomAppBar.performHide()
            // Get a handle on the animator that hides the bottom app bar so we can wait to hide
            // the fab and bottom app bar until after it's exit animation finishes.
            bottomAppBar.animate().setListener(object : AnimatorListenerAdapter() {
                var isCanceled = false
                override fun onAnimationEnd(animation: Animator?) {
                    if (isCanceled) return

                    // Hide the BottomAppBar to avoid it showing above the keyboard
                    // when composing a new email.
                    bottomAppBar.visibility = View.GONE
                    fab.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    isCanceled = true
                }
            })
        }
    }

    private fun showDarkThemeMenu() {
        MenuBottomSheetDialogFragment
            .newInstance(R.menu.dark_theme_bottom_sheet_menu)
            .show(supportFragmentManager, null)
    }

    fun navigateToHome(@StringRes titleRes: Int, mailbox: Mailbox) {
        binding.bottomAppBarTitle.text = getString(titleRes)

        // TODO: Set up MaterialFadeThrough transition as exit transition.
        currentNavigationFragment?.apply {
            exitTransition = MaterialFadeThrough().apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }

        val directions = HomeFragmentDirections.actionGlobalHomeFragment()
        findNavController(R.id.nav_host_fragment).navigate(directions)
    }

    private fun navigateToCompose() {
        // TODO: Set up MaterialElevationScale transition as exit and reenter transitions.
        currentNavigationFragment?.apply {
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }

        val directions = ComposeFragmentDirections.actionGlobalComposeFragment(currentEmailId)
        findNavController(R.id.nav_host_fragment).navigate(directions)
    }

    private fun navigateToSearch() {
        // TODO: Set up MaterialSharedAxis transition as exit and reenter transitions.
        currentNavigationFragment?.apply {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }

        val directions = SearchFragmentDirections.actionGlobalSearchFragment()
        findNavController(R.id.nav_host_fragment).navigate(directions)
    }

    /**
     * Set this Activity's night mode based on a user's in-app selection.
     */
    private fun onDarkThemeMenuItemSelected(itemId: Int): Boolean {
        val nightMode = when (itemId) {
            R.id.menu_light -> AppCompatDelegate.MODE_NIGHT_NO
            R.id.menu_dark -> AppCompatDelegate.MODE_NIGHT_YES
            R.id.menu_battery_saver -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            R.id.menu_system_default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> return false
        }

        delegate.localNightMode = nightMode
        return true
    }

    override fun onNavMenuItemClicked(item: NavigationModelItem.NavMenuItem) {
        // Swap the list of emails for the given mailbox
        navigateToHome(item.titleRes, item.mailbox)
    }

    override fun onNavEmailFolderClicked(folder: NavigationModelItem.NavEmailFolder) {
        // Do nothing
    }
}