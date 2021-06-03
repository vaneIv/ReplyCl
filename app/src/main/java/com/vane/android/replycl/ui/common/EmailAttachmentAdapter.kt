package com.vane.android.replycl.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vane.android.replycl.data.EmailAttachment

/**
 * Generic RecyclerView.Adapter to display [EmailAttachment]s.
 */
abstract class EmailAttachmentAdapter : RecyclerView.Adapter<EmailAttachmentViewHolder>() {

    private var list: List<EmailAttachment> = emptyList()

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = getLayoutIdForPosition(position)

    fun submitList(attachment: List<EmailAttachment>) {
        list = attachment
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailAttachmentViewHolder {
        return EmailAttachmentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewType,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EmailAttachmentViewHolder, position: Int) {
        holder.bind(list[position])
    }

    /**
     * Clients should implement this method to determine what layout is inflated for a given
     * position. The layout must include a data parameter named 'emailAttachment' with a type
     * of [EmailAttachment].
     */
    abstract fun getLayoutIdForPosition(position: Int): Int
}