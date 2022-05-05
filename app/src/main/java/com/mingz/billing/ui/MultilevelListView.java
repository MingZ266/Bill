package com.mingz.billing.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mingz.billing.R;

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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultilevelListView);
        try {
            adapter.atMostExpandOne = typedArray.getBoolean(
                    R.styleable.MultilevelListView_atMostExpandOne, false);
        } finally {
            typedArray.recycle();
        }
    }

    public void setData(Data<?, ?>[] data) {
        adapter.dataList = new ArrayList<>(Arrays.asList(data));
        adapter.recordExpand = null;
        adapter.notifyDataSetChanged();
    }

    public void setDataAndExpandAll(Data<?, ?>[] data) {
        adapter.dataList = new ArrayList<>(Arrays.asList(data));
        adapter.expandAll();
    }

    public void setAllowExpandFold(boolean allow) {
        adapter.allowExpandFold = allow;
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        adapter.listener = listener;
    }

    private static class ListViewAdapter extends BaseAdapter {
        private final Context context;
        private List<Data<?, ?>> dataList = null;
        private OnItemClickListener listener = null;
        private boolean allowExpandFold = true;
        private boolean atMostExpandOne = false;
        private int[] recordExpand = null;

        private ListViewAdapter(Context context) {
            this.context = context;
        }

        private void expandAll() {
            for (int i = 0; i < dataList.size(); i++) {
                Data<?, ?> goalData = dataList.get(i);
                Data<?, ?>[] children = goalData.subordinateData;
                if (children == null || goalData.isExpand) {
                    continue;
                }
                goalData.isExpand = true;
                dataList.addAll(i + 1, Arrays.asList(children));
            }
            notifyDataSetChanged();
        }

        private void onItemClick(int position) {
            Data<?, ?> goalData = dataList.get(position);
            if (allowExpandFold) {
                Data<?, ?>[] children = goalData.subordinateData;
                if (children != null) {
                    if (atMostExpandOne) {
                        // 展开可能改变条目位置，折叠不会
                        if (recordExpand == null) {
                            recordExpand = new int[1];
                            recordExpand[0] = -1;
                        }
                        int level = goalData.getLevel(); // 该条目所处层级
                        if (goalData.isExpand) {
                            goalData.isExpand = false;
                            foldAll(position + 1, children);
                            // 该层级索引置-1，后续层级记录移除
                            recordExpand = copyIfNeed(recordExpand, level + 1);
                            recordExpand[level] = -1;
                        } else {
                            goalData.isExpand = true;
                            int index = recordExpand[level]; // 该层级已展开条目的索引
                            if (index >= 0) {
                                // 该层级已有一个条目展开，将其折叠
                                Data<?, ?> foldGoal = dataList.get(index);
                                foldGoal.isExpand = false;
                                int count = foldAll(index + 1, foldGoal.subordinateData);
                                if (position > index) {
                                    // 折叠展开的条目后，此条目的位置改变了
                                    position -= count;
                                }
                            }
                            // 展开该条目
                            dataList.addAll(position + 1, Arrays.asList(children));
                            // 记录该层级展开条目的索引，下一层级索引置-1，移除下一层级之后的记录
                            recordExpand = copyIfNeed(recordExpand, level + 2);
                            recordExpand[level] = position;
                            recordExpand[level + 1] = -1;
                        }
                    } else {
                        // 展开、折叠不会改变这个条目的位置
                        if (goalData.isExpand) {
                            goalData.isExpand = false;
                            foldAll(position + 1, children);
                        } else {
                            goalData.isExpand = true;
                            dataList.addAll(position + 1, Arrays.asList(children));
                        }
                    }
                    notifyDataSetChanged();
                } // else: 不改变条目位置
            } // else: 不改变条目位置
            if (listener != null) {
                listener.onItemClick(goalData, position);
            }
        }

        private int[] copyIfNeed(int[] src, int newLength) {
            if (src.length == newLength) {
                return src;
            }
            return Arrays.copyOf(src, newLength);
        }

        /**
         * 折叠所有孩子节点.
         *
         * @param position 第一个孩子节点的位置
         * @param children 将被折叠的孩子节点
         * @return 被折叠的条目数
         */
        private int foldAll(int position, Data<?, ?>[] children) {
            if (children == null) {
                return 0;
            }
            int count = 0;
            for (Data<?, ?> data : children) {
                if (data.isExpand) {
                    count += foldAll(position + 1, data.subordinateData);
                    data.isExpand = false;
                }
                dataList.remove(position);
                count++;
            }
            return count;
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
            int resId = data.getResId();
            if (convertView == null || (viewHolder = convertView.getTag(resId)) == null) {
                convertView = View.inflate(context, resId, null);
                viewHolder = data.newViewHolder(convertView);
                convertView.setTag(resId, viewHolder);
            }
            data.loadingDataOnView(context, viewHolder, position);
            return convertView;
        }
    }

    public static abstract class Data<T, ViewHolder> {
        /**
         * 该节点使用的数据.
         */
        @Nullable
        public final T data;

        /**
         * 隶属于该节点的数据.
         */
        @Nullable
        public final Data<?, ?>[] subordinateData;

        private boolean isExpand = false;

        protected Data(@Nullable T data) {
            this(data, null);
        }

        protected Data(@Nullable T data, @Nullable Data<?, ?>[] subordinateData) {
            this.data = data;
            this.subordinateData = subordinateData;
        }

        /**
         * 该级条目使用的视图资源id.
         */
        @LayoutRes
        protected abstract int getResId();

        /**
         * 该级条目所处的层级，从0开始.
         */
        @IntRange(from = 0)
        protected abstract int getLevel();

        /**
         * 该节点是否展开了.
         */
        public boolean getIsExpand() {
            return isExpand;
        }

        /**
         * 生成新的ViewHolder实例.
         * 并将视图中的组件通过一系列{@link View#findViewById(int)}
         * 绑定到ViewHolder类中的成员.
         *
         * @param view {@link #getResId()}对应的视图
         * @return 生成的ViewHolder实例
         */
        @NonNull
        protected abstract ViewHolder newViewHolder(@NonNull View view);

        /**
         * 通过{@link #data}为ViewHolder类中的视图对象加载数据.
         *
         * @param viewHolder 与视图绑定的ViewHolder实例
         */
        protected abstract void loadingDataOnView(@NonNull Context context, @NonNull Object viewHolder,
                                                  int position);
    }

    public interface OnItemClickListener {
        /**
         * 当条目被点击时调用.
         * @param data 该条目使用的数据
         * @param position 该条目的一维等价位置
         */
        void onItemClick(@NonNull Data<?, ?> data, int position);
    }
}
