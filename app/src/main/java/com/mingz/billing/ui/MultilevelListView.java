package com.mingz.billing.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.annotation.LayoutRes;
import com.mingz.billing.utils.MyLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultilevelListView extends ListView {
    //private final MyLog myLog = new MyLog(this);
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

    private static class ListViewAdapter extends BaseAdapter {
        private final MyLog myLog = new MyLog(this);
        private final Context context;
        @LayoutRes
        private int[] lastIds = null;
        private List<Data<?, ?>> dstData = null;

        private ListViewAdapter(Context context) {
            this.context = context;
        }

        private void updateData(Data<?, ?>[] data) {
            dstData = new ArrayList<>(Arrays.asList(data));
            notifyDataSetChanged();
        }

        private void onItemClick(int position) {
            Data<?, ?> goalData = dstData.get(position);
            Data<?, ?>[] children = goalData.subordinateData;
            if (children == null) {
                return;
            }
            recordResId();
            if (goalData.isExpand) {
                goalData.isExpand = false;
                foldAll(position + 1, children);
            } else {
                goalData.isExpand = true;
                dstData.addAll(position + 1, Arrays.asList(children));
            }
            notifyDataSetChanged();
        }

        private void recordResId() {
            int size = dstData.size();
            if (lastIds == null || lastIds.length < size) {
                lastIds = new int[size];
            }
            for (int i = 0; i < size; i++) {
                lastIds[i] = dstData.get(i).resId;
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
                dstData.remove(position);
            }
        }

        @Override
        public int getCount() {
            if (dstData == null) {
                return 0;
            }
            return dstData.size();
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
            Data<?, ?> data = dstData.get(position);
            if (lastIds != null && position < lastIds.length) {
                myLog.v("dataResId: " + data.resId + "  lastId: " + lastIds[position]);
                if (data.resId != lastIds[position]) {
                    convertView = null;
                }
            }
            myLog.v("getView: " + position + "  isNull: " + (convertView == null));
            Object viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context, data.resId, null);
                viewHolder = data.newViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = convertView.getTag();
            }
            data.loadingDataOnView(viewHolder);
            return convertView;
        }
    }

    public static abstract class Data<T, ViewHolder> {
        @LayoutRes
        private final int resId;

        @Nullable
        protected final T data;

        @Nullable
        protected final Data<?, ?>[] subordinateData;

        /**
         * 该节点是否展开了.
         */
        private boolean isExpand = false;

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
        @NotNull
        protected abstract ViewHolder newViewHolder(@NotNull View view);

        /**
         * 通过{@link #data}为ViewHolder类中的视图对象加载数据.
         *
         * @param viewHolder 与视图绑定的ViewHolder实例
         */
        protected abstract void loadingDataOnView(@NotNull Object viewHolder);
    }
}
