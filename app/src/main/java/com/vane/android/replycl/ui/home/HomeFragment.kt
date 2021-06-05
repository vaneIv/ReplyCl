package com.vane.android.replycl.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.vane.android.replycl.R
import com.vane.android.replycl.data.Email
import com.vane.android.replycl.data.EmailStore
import com.vane.android.replycl.databinding.FragmentHomeBinding
import com.vane.android.replycl.ui.MainActivity
import com.vane.android.replycl.ui.MenuBottomSheetDialogFragment
import com.vane.android.replycl.ui.nav.NavigationModel

/**
 * A [Fragment] that displays a list of emails.
 */
class HomeFragment : Fragment(), EmailAdapter.EmailAdapterListener {

    private val args: HomeFragmentArgs by navArgs()

    private lateinit var binding: FragmentHomeBinding

    private val emailAdapter = EmailAdapter(this)

    // An on back pressed callback that handles replacing any non-Inbox HomeFragment with inbox
    // on back pressed.
    private val nonInboxOnBackCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            NavigationModel.setNavigationMenuItemChecked(NavigationModel.INBOX_ID)
            (requireActivity() as MainActivity)
                .navigateToHome(R.string.navigation_inbox, Mailbox.INBOX)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Set up MaterialFadeThrough enterTransition.
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Set up postponed enter transition.
        // Postpone enter transitions to allow shared element transitions to run.
        // https://github.com/googlesamples/android-architecture-components/issues/495
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        // Only enable the on back callback if this home fragment is a mailbox other than Inbox.
        // This is to make sure we always navigate back to Inbox before exiting the app.
        nonInboxOnBackCallback.isEnabled = args.mailbox != Mailbox.INBOX
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            nonInboxOnBackCallback
        )

        binding.recyclerView.apply {
            val itemTouchHelper = ItemTouchHelper(ReboundingSwipeActionCallback())
            itemTouchHelper.attachToRecyclerView(this)
            adapter = emailAdapter
        }
        binding.recyclerView.adapter = emailAdapter

        EmailStore.getEmails(args.mailbox).observe(viewLifecycleOwner) {
            emailAdapter.submitList(it)
        }
    }

    override fun onEmailClicked(cardView: View, email: Email) {
        // Set exit and reenter transitions here as opposed to in onCreate because these transitions
        // will be set and overwritten on HomeFragment for other navigation actions.
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val emailCardDetailTransitionName = getString(R.string.email_card_detail_transition_name)
        val extras = FragmentNavigatorExtras(cardView to emailCardDetailTransitionName)
        val directions = HomeFragmentDirections.actionHomeFragmentToEmailFragment(email.id)
        findNavController().navigate(directions, extras)
    }

    override fun onEmailLongPressed(email: Email): Boolean {
        MenuBottomSheetDialogFragment
            .newInstance(R.menu.email_bottom_sheet_menu)
            .show(parentFragmentManager, null)

        return true
    }

    override fun onEmailStarChanged(email: Email, newValue: Boolean) {
        EmailStore.update(email.id) { isStarred = newValue }
    }

    override fun onEmailArchived(email: Email) {
        EmailStore.delete(email.id)
    }


}