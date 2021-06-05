package com.vane.android.replycl.ui.compose

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Slide
import com.google.android.material.transition.MaterialContainerTransform
import com.vane.android.replycl.R
import com.vane.android.replycl.data.Account
import com.vane.android.replycl.data.AccountStore
import com.vane.android.replycl.data.Email
import com.vane.android.replycl.data.EmailStore
import com.vane.android.replycl.databinding.ComposeRecipientChipBinding
import com.vane.android.replycl.databinding.FragmentComposeBinding
import com.vane.android.replycl.util.themeColor
import kotlin.LazyThreadSafetyMode.*

/**
 * A [Fragment] which allows for the composition of a new email.
 */
class ComposeFragment : Fragment() {

    private lateinit var binding: FragmentComposeBinding

    private val args: ComposeFragmentArgs by navArgs()

    // The new email being composed.
    private val composeEmail: Email by lazy(NONE) {
        // Get the id of the email being replied to, if any, and either create an new empty email
        // or a new reply email.
        val id = args.replyToEmailId
        if (id == -1L) EmailStore.create() else EmailStore.createReplyTo(id)
    }

    // Handle closing an expanded recipient card when on back is pressed.
    private val closeRecipientCardOnBackPressed = object : OnBackPressedCallback(false) {
        var expandedChip: View? = null
        override fun handleOnBackPressed() {
            expandedChip?.let { collapseChip(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, closeRecipientCardOnBackPressed)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentComposeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.run {
            closeIcon.setOnClickListener { findNavController().navigateUp() }
            email = composeEmail

            composeEmail.nonUserAccountRecipients.forEach { addRecipientChip(it) }

            senderSpinner.adapter = ArrayAdapter(
                senderSpinner.context,
                R.layout.spinner_item_layout,
                AccountStore.getAllUserAccounts().map { it.email }
            )

            // TODO: Set up MaterialContainerTransform enterTransition and Slide returnTransition.
            //While we use the same transition class, the way we configure this instance will be
            // different since our FAB lives in MainActivity and our ComposeFragment is placed inside
            // our MainActivity navigation host container.

            //In addition to parameters used to configure our previous container transform,
            // startView and endView are being set manually here. Instead of using transitionName
            // attributes to let the Android Transition system know which views should be transformed,
            // you can specify these manually when necessary.
            enterTransition = MaterialContainerTransform().apply {
                startView = requireActivity().findViewById(R.id.fab)
                endView = emailCardView
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                scrimColor = Color.TRANSPARENT
                containerColor = requireContext().themeColor(R.attr.colorSecondary)
                endContainerColor = requireContext().themeColor(R.attr.colorSurface)
            }
            returnTransition = Slide().apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_medium).toLong()
                addTarget(R.id.email_card_view)
            }
        }
    }

    /**
     * Add a chip for the given [Account] to the recipients chip group.
     *
     * This method also sets up the ability for expanding/collapsing the chip into a recipient
     * address selection dialog.
     */
    private fun addRecipientChip(acnt: Account) {
        binding.recipientChipGroup.run {
            val chipBinding = ComposeRecipientChipBinding.inflate(
                LayoutInflater.from(context),
                this,
                false
            ).apply {
                account = acnt
                root.setOnClickListener {
                    // Bind the views in the expanded card view to this account's details when
                    // clicked and expand.
                    binding.focusedRecipient = acnt
                    expandChip(it)
                }
            }
            addView(chipBinding.root)
        }
    }

    /**
     * Expand the recipient [chip] into a popup with a list of contact addresses to choose from.
     */
    private fun expandChip(chip: View) {
        // Configure the analogous collapse transform back to the recipient chip. This should
        // happen when the card is clicked, any region outside of the card (the card's transparent
        // scrim) is clicked, or when the back button is pressed.
        binding.run {
            recipientCardView.setOnClickListener { collapseChip(chip) }
            recipientCardScrim.visibility = View.VISIBLE
            recipientCardScrim.setOnClickListener { collapseChip(chip) }
        }
        closeRecipientCardOnBackPressed.expandedChip = chip
        closeRecipientCardOnBackPressed.isEnabled = true

        // TODO: Set up MaterialContainerTransform beginDelayedTransition.
        binding.recipientCardView.visibility = View.VISIBLE
        // Using INVISIBLE instead of GONE ensures the chip's parent layout won't shift during
        // the transition due to chips being effectively removed.
        chip.visibility = View.INVISIBLE
    }

    /**
     * Collapse the recipient card back into its [chip] form.
     */
    private fun collapseChip(chip: View) {
        // Remove the scrim view and on back pressed callbacks
        binding.recipientCardScrim.visibility = View.GONE
        closeRecipientCardOnBackPressed.expandedChip = null
        closeRecipientCardOnBackPressed.isEnabled = false

        // TODO: Set up MaterialContainerTransform beginDelayedTransition.
        chip.visibility = View.VISIBLE
        binding.recipientCardView.visibility = View.INVISIBLE
    }
}

//class ComposeFragment : Fragment() {
//
//    private lateinit var binding: FragmentComposeBinding
//
//    private val args: ComposeFragmentArgs by navArgs()
//
//    // The new email being composed.
//    private val composeEmail: Email by lazy(NONE) {
//        // Get the id of the email being replied to, if any, and either create an new empty email
//        // or a new reply email.
//        val id = args.replyToEmailId
//        if (id == -1L) EmailStore.create() else EmailStore.createReplyTo(id)
//    }
//
//    // Handle closing an expanded recipient card when on back is pressed.
//    private val closeRecipientCardOnBackPressed = object : OnBackPressedCallback(false) {
//        var expandedChip: View? = null
//        override fun handleOnBackPressed() {
//            expandedChip?.let { collapseChip(it) }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        requireActivity().onBackPressedDispatcher.addCallback(this, closeRecipientCardOnBackPressed)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentComposeBinding.inflate(inflater, container, false)
//
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        binding.run {
//            closeIcon.setOnClickListener { findNavController().navigateUp() }
//            email = composeEmail
//
//            composeEmail.nonUserAccountRecipients.forEach { addRecipientChip(it) }
//
//            senderSpinner.adapter = ArrayAdapter(
//                senderSpinner.context,
//                R.layout.spinner_item_layout,
//                AccountStore.getAllUserAccounts().map { it.email }
//            )
//
//            // TODO: Set up MaterialContainerTransform enterTransition and Slide returnTransition.
//        }
//    }
//
//    /**
//     * Add a chip for the given [Account] to the recipients chip group.
//     *
//     * This method also sets up the ability for expanding/collapsing the chip into a recipient
//     * address selection dialog.
//     */
//    private fun addRecipientChip(acnt: Account) {
//        binding.recipientChipGroup.run {
//            val chipBinding = ComposeRecipientChipBinding.inflate(
//                LayoutInflater.from(context),
//                this,
//                false
//            ).apply {
//                account = acnt
//                root.setOnClickListener {
//                    // Bind the views in the expanded card view to this account's details when
//                    // clicked and expand.
//                    binding.focusedRecipient = acnt
//                    expandChip(it)
//                }
//            }
//            addView(chipBinding.root)
//        }
//    }
//
//    /**
//     * Expand the recipient [chip] into a popup with a list of contact addresses to choose from.
//     */
//    private fun expandChip(chip: View) {
//        // Configure the analogous collapse transform back to the recipient chip. This should
//        // happen when the card is clicked, any region outside of the card (the card's transparent
//        // scrim) is clicked, or when the back button is pressed.
//        binding.run {
//            recipientCardView.setOnClickListener { collapseChip(chip) }
//            recipientCardScrim.visibility = View.VISIBLE
//            recipientCardScrim.setOnClickListener { collapseChip(chip) }
//        }
//        closeRecipientCardOnBackPressed.expandedChip = chip
//        closeRecipientCardOnBackPressed.isEnabled = true
//
//        // TODO: Set up MaterialContainerTransform beginDelayedTransition.
//        binding.recipientCardView.visibility = View.VISIBLE
//        // Using INVISIBLE instead of GONE ensures the chip's parent layout won't shift during
//        // the transition due to chips being effectively removed.
//        chip.visibility = View.INVISIBLE
//    }
//
//    /**
//     * Collapse the recipient card back into its [chip] form.
//     */
//    private fun collapseChip(chip: View) {
//        // Remove the scrim view and on back pressed callbacks
//        binding.recipientCardScrim.visibility = View.GONE
//        closeRecipientCardOnBackPressed.expandedChip = null
//        closeRecipientCardOnBackPressed.isEnabled = false
//
//        // TODO: Set up MaterialContainerTransform beginDelayedTransition.
//        chip.visibility = View.VISIBLE
//        binding.recipientCardView.visibility = View.INVISIBLE
//    }
//}