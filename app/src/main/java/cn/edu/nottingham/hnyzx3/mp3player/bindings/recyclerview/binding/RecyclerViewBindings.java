package cn.edu.nottingham.hnyzx3.mp3player.bindings.recyclerview.binding;


import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;

import cn.edu.nottingham.hnyzx3.mp3player.bindings.recyclerview.adapter.BindingRecyclerViewAdapter;
import cn.edu.nottingham.hnyzx3.mp3player.bindings.recyclerview.adapter.binder.ItemBinder;

/**
 * data binding attributes added for RecyclerView component
 * binds a collection of items to a RecyclerView
 */
public class RecyclerViewBindings {
    private static final int KEY_ITEMS = -123;

    /**
     * add "items" attribute to the RecyclerView
     */
    @BindingAdapter("items")
    public static <T> void setItems(RecyclerView recyclerView, Collection<T> items) {
        BindingRecyclerViewAdapter<T> adapter = (BindingRecyclerViewAdapter<T>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setItems(items);
        } else {
            recyclerView.setTag(KEY_ITEMS, items);
        }
    }

    /**
     * add "setItemViewBinder" attribute to the RecyclerView
     */
    @BindingAdapter("itemViewBinder")
    public static <T> void setItemViewBinder(RecyclerView recyclerView, ItemBinder<T> itemViewMapper) {
        recyclerView.setAdapter(new BindingRecyclerViewAdapter<>(itemViewMapper, (Collection<T>) recyclerView.getTag(KEY_ITEMS)));
    }
}