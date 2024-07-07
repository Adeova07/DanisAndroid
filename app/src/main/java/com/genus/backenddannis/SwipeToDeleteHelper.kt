package com.genus.backenddannis

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.data.dao.PendapatanDao
import com.genus.backenddannis.data.dao.PengeluaranDao
import com.genus.backenddannis.data.adapter.CombineAdapter
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import kotlinx.coroutines.launch

class SwipeToDeleteHelper(
    private val adapter: CombineAdapter,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val pendapatanDao: PendapatanDao,
    private val pengeluaranDao: PengeluaranDao,
    private val listener: OnSwipeListener
) : ItemTouchHelper.SimpleCallback(
    0, // Tidak ada operasi drag-and-drop yang diperbolehkan
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Izinkan swipe ke kiri dan kanan
) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Tidak ada operasi drag-and-drop yang diperlukan
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Dapatkan posisi item yang di-swipe
        val position = viewHolder.adapterPosition

        // Hapus item dari database
        val item = adapter.transactionList[position]
        if (item is Pendapatan) {
            lifecycleScope.launch {
                pendapatanDao.delete(item)
                // Hapus item dari adapter setelah penghapusan dari database
                listener.onSwipe(position)
            }
        } else if (item is Pengeluaran) {
            lifecycleScope.launch {
                pengeluaranDao.delete(item)
                // Hapus item dari adapter setelah penghapusan dari database
                listener.onSwipe(position)
            }
        }
    }

    interface OnSwipeListener {
        fun onSwipe(position: Int)
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
