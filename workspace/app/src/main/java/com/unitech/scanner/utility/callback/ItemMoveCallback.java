package com.unitech.scanner.utility.callback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.weight.FormattingViewHolder;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/2/2 下午 03:47
 * 修改人:user
 * 修改時間:2021/2/2 下午 03:47
 * 修改備註:
 */

public class ItemMoveCallback  extends ItemTouchHelper.Callback{
    private final FormattingViewHolderItemTouchHelperContract mAdapter;

    public ItemMoveCallback(FormattingViewHolderItemTouchHelperContract adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }



    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {


        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof FormattingViewHolder) {
                FormattingViewHolder myViewHolder= (FormattingViewHolder) viewHolder;
                mAdapter.onRowSelected(myViewHolder);
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof FormattingViewHolder) {
                FormattingViewHolder myViewHolder= (FormattingViewHolder) viewHolder;
                mAdapter.onRowClear(myViewHolder);
            }

    }

    public interface FormattingViewHolderItemTouchHelperContract {

        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(FormattingViewHolder myFormattingViewHolder);
        void onRowClear(FormattingViewHolder myFormattingViewHolder);
    }


}