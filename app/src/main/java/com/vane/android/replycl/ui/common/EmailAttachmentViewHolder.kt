package com.vane.android.replycl.ui.common

import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.vane.android.replycl.data.EmailAttachment

/**
 * Generic RecyclerView.ViewHolder which is able to bind layouts which expose a variable
 * for an [EmailAttachment].
 */
class EmailAttachmentViewHolder(
    private val binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(attachment: EmailAttachment) {
        binding.run {
            setVariable(BR.emailAttachment, attachment)
            executePendingBindings()
        }
    }
}