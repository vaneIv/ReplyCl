package com.vane.android.replycl.ui.email

import androidx.recyclerview.widget.GridLayoutManager
import com.vane.android.replycl.R
import com.vane.android.replycl.ui.common.EmailAttachmentAdapter
import kotlin.random.Random

class EmailAttachmentGridAdapter(
    private val spans: Int
) : EmailAttachmentAdapter() {

    /**
     * A [GridLayoutManager.SpanSizeLookup] which randomly assigns a span count to each item
     * in this adapter.
     */
    val variableSpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

        private var indexSpanCounts: List<Int> = emptyList()

        override fun getSpanSize(position: Int): Int {
            return indexSpanCounts[position]
        }

        private fun generateSpanForItems(count: Int): List<Int> {
            val list = mutableListOf<Int>()

            var rowSpansOccupied = 0
            repeat(count) {
                val size = Random.nextInt(1, spans + 1 - rowSpansOccupied)
                rowSpansOccupied += size
                if (rowSpansOccupied >= 3) rowSpansOccupied = 0
                list.add(size)
            }

            return list
        }

        override fun invalidateSpanIndexCache() {
            super.invalidateSpanIndexCache()
            indexSpanCounts = generateSpanForItems(itemCount)
        }
    }

    override fun getLayoutIdForPosition(position: Int): Int =
        R.layout.email_attachment_grid_item_layout

}