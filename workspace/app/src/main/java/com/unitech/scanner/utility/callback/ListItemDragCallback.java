package com.unitech.scanner.utility.callback;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/2/2 下午 03:41
 * 修改人:user
 * 修改時間:2021/2/2 下午 03:41
 * 修改備註:
 */

public  interface ListItemDragCallback {
    void requestDrag(RecyclerView.ViewHolder viewHolder);
}
