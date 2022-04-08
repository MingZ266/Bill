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
        private Data<?, ?>[] srcData = null;
        private List<DstData<?, ?>> dstData = null;

        private ListViewAdapter(Context context) {
            this.context = context;
        }

        private void updateData(Data<?, ?>[] data) {
            srcData = data;
            dstData = new ArrayList<>();
            for (int i = 0; i < srcData.length; i++) {
                srcData[i].index = new int[] { i };
                dstData.add(srcData[i]);
            }
            notifyDataSetChanged();
        }

        private void onItemClick(int position) {
            Data<?, ?> goalData = findFromIndex(dstData.get(position).index);
            Data<?, ?>[] children = goalData.subordinateData;
            if (children == null) {
                myLog.v(position + ": 没有孩子节点，不更改视图");
                return;
            }
            if (goalData.isExpand) {
                goalData.isExpand = false;
                foldAll(position + 1, children);
            } else {
                goalData.isExpand = true;
                for (int i = 0; i < children.length; i++) {
                    Data<?, ?> child = children[i];
                    // 记录位置
                    child.index = new int[goalData.index.length + 1];
                    System.arraycopy(goalData.index, 0, child.index, 0, goalData.index.length);
                    child.index[goalData.index.length] = i;
                    dstData.add(++position, child);
                }
            }
            notifyDataSetChanged();
        }

        private Data<?, ?> findFromIndex(int[] index) {
            Data<?, ?> result = null;
            Data<?, ?>[] subordinateData = srcData;
            for (int i : index) {
                if (subordinateData == null) {
                    throw new Error("索引记录异常");
                }
                result = subordinateData[i];
                subordinateData = result.subordinateData;
            }
            return result;
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
            myLog.v("getItemId: " + position);
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = null;
            myLog.v("getView: " + position + "  isNull: " + (convertView == null));
            DstData<?, ?> data = dstData.get(position);
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

    private static abstract class DstData<T, ViewHolder> {
        @LayoutRes
        private final int resId;

        @Nullable
        protected final T data;

        /**
         * 记录该节点在树中的位置.
         */
        int[] index = null;

        /**
         * 该节点是否展开了.
         */
        boolean isExpand = false;

        private DstData(@LayoutRes int resId, @Nullable T data) {
            this.resId = resId;
            this.data = data;
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

    public static abstract class Data<T, ViewHolder> extends DstData<T, ViewHolder> {
        @Nullable
        protected final Data<?, ?>[] subordinateData;

        protected Data(@LayoutRes int resId, @Nullable T data) {
            this(resId, data, null);
        }

        protected Data(@LayoutRes int resId, @Nullable T data, @Nullable Data<?, ?>[] subordinateData) {
            super(resId, data);
            this.subordinateData = subordinateData;
        }
    }
}
