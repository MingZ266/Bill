package com.mingz.billing.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对于使用同一Layout资源的条目必须设置同一ViewHolder类.
 */
public class MultilevelListView extends ListView {
    private final ListViewAdapter adapter;

    public MultilevelListView(Context context) {
        this(context, null);
    }

    public MultilevelListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        adapter = new ListViewAdapter(context);
        setAdapter(adapter);
        setOnItemClickListener((parent, view, position, id) -> adapter.onItemClick(position));
    }

    public void setData(Data<?, ?>[] data) {
        adapter.updateData(data);
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        adapter.listener = listener;
    }

    private static class ListViewAdapter extends BaseAdapter {
        private final Context context;
        private List<Data<?, ?>> dataList = null;
        private OnItemClickListener listener = null;

        private ListViewAdapter(Context context) {
            this.context = context;
        }

        private void updateData(Data<?, ?>[] data) {
            dataList = new ArrayList<>(Arrays.asList(data));
            notifyDataSetChanged();
        }

        private void onItemClick(int position) {
            Data<?, ?> goalData = dataList.get(position);
            Data<?, ?>[] children = goalData.subordinateData;
            if (children != null) {
                if (goalData.isExpand) {
                    goalData.isExpand = false;
                    foldAll(position + 1, children);
                } else {
                    goalData.isExpand = true;
                    dataList.addAll(position + 1, Arrays.asList(children));
                }
                notifyDataSetChanged();
            }
            if (listener != null) {
                // 不论展开、收起还是都不做，都不会改变这个条目的位置
                listener.onItemClick(goalData);
            }
        }

        /**
         * 折叠所有孩子节点.
         *
         * @param position 第一个孩子节点的位置
         * @param children 将被折叠的孩子节点
         */
        private void foldAll(int position, Data<?, ?>[] children) {
            if (children == null) {
                return;
            }
            for (Data<?, ?> data : children) {
                if (data.isExpand) {
                    foldAll(position + 1, data.subordinateData);
                    data.isExpand = false;
                }
                dataList.remove(position);
            }
        }

        @Override
        public int getCount() {
            if (dataList == null) {
                return 0;
            }
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Data<?, ?> data = dataList.get(position);
            Object viewHolder;
            if (convertView == null || (viewHolder = convertView.getTag(data.resId)) == null) {
                convertView = View.inflate(context, data.resId, null);
                viewHolder = data.newViewHolder(convertView);
                convertView.setTag(data.resId, viewHolder);
            }
            data.loadingDataOnView(context, viewHolder);
            return convertView;
        }
    }

    public static abstract class Data<T, ViewHolder> {
        @LayoutRes
        private final int resId;

        @Nullable
        public final T data;

        @Nullable
        public final Data<?, ?>[] subordinateData;

        /**
         * 该节点是否展开了.
         */
        protected boolean isExpand = false;

        protected Data(@LayoutRes int resId, @Nullable T data) {
            this(resId, data, null);
        }

        protected Data(@LayoutRes int resId, @Nullable T data, @Nullable Data<?, ?>[] subordinateData) {
            this.resId = resId;
            this.data = data;
            this.subordinateData = subordinateData;
        }

        /**
         * 生成新的ViewHolder实例.
         * 并将视图中的组件通过一系列{@link View#findViewById(int)}
         * 绑定到ViewHolder类中的成员.
         *
         * @param view {@link #resId}对应的视图
         * @return 生成的ViewHolder实例
         */
        @NonNull
        protected abstract ViewHolder newViewHolder(@NonNull View view);

        /**
         * 通过{@link #data}为ViewHolder类中的视图对象加载数据.
         *
         * @param viewHolder 与视图绑定的ViewHolder实例
         */
        protected abstract void loadingDataOnView(@NonNull Context context, @NonNull Object viewHolder);
    }

    public interface OnItemClickListener {
        void onItemClick(@NonNull Data<?, ?> data);
    }
}
