package com.vane.android.replycl.ui.nav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.vane.android.replycl.data.Account
import com.vane.android.replycl.data.AccountDiffCallback
import com.vane.android.replycl.databinding.AccountItemLayoutBinding

/**
 * An adapter which holds a list of selectable accounts owned by the current user.
 */
class AccountAdapter(
    private val listener: AccountAdapterListener
) : ListAdapter<Account, AccountViewHolder>(AccountDiffCallback) {

    interface AccountAdapterListener {
        fun onAccountClicked(account: Account)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {

        return AccountViewHolder(
            AccountItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}