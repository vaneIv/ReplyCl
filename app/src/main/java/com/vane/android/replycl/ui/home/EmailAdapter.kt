package com.vane.android.replycl.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.vane.android.replycl.data.Email
import com.vane.android.replycl.data.EmailDiffCallback
import com.vane.android.replycl.databinding.EmailItemLayoutBinding

class EmailAdapter(
    private val listener: EmailAdapterListener
) : ListAdapter<Email, EmailViewHolder>(EmailDiffCallback) {

    interface EmailAdapterListener {
        fun onEmailClicked(cardView: View, email: Email)
        fun onEmailLongPressed(email: Email): Boolean
        fun onEmailStarChanged(email: Email, newValue: Boolean)
        fun onEmailArchived(email: Email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        return EmailViewHolder(
            EmailItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}