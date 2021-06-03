package com.vane.android.replycl.ui.nav

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.vane.android.replycl.databinding.NavDividerItemLayoutBinding
import com.vane.android.replycl.databinding.NavEmailFolderItemLayoutBinding
import com.vane.android.replycl.databinding.NavMenuItemLayoutBinding

sealed class NavigationViewHolder<T : NavigationModelItem>(
    view: View
) : RecyclerView.ViewHolder(view) {

    abstract fun bind(navItem: T)

    class NavMenuItemViewHolder(
        private val binding: NavMenuItemLayoutBinding,
        private val listener: NavigationAdapter.NavigationAdapterListener
    ) : NavigationViewHolder<NavigationModelItem.NavMenuItem>(binding.root) {

        override fun bind(navItem: NavigationModelItem.NavMenuItem) {
            binding.run {
                navMenuItem = navItem
                navListener = listener
                executePendingBindings()
            }
        }
    }

    class NavDividerViewHolder(
        private val binding: NavDividerItemLayoutBinding
    ) : NavigationViewHolder<NavigationModelItem.NavDivider>(binding.root) {

        override fun bind(navItem: NavigationModelItem.NavDivider) {
            binding.navDivider = navItem
            binding.executePendingBindings()
        }
    }

    class EmailFolderViewHolder(
        private val binding: NavEmailFolderItemLayoutBinding,
        private val listener: NavigationAdapter.NavigationAdapterListener
    ) : NavigationViewHolder<NavigationModelItem.NavEmailFolder>(binding.root) {

        override fun bind(navItem: NavigationModelItem.NavEmailFolder) {
            binding.run {
                navEmailFolder = navItem
                navListener = listener
                executePendingBindings()
            }
        }
    }
}