package com.mingz.billing;

import android.view.View;
import com.mingz.billing.ui.MultilevelListView.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoidTest {
    @Test
    public void test() {
        A<String, ?> a1 = new A<>("A", null);
        A<String, ?> a2 = new A<>("B", null);
        List<A<?, ?>> list = new ArrayList<>();
        A<?, ?>[] arr = new A[] { a1, a2 };
        list.addAll(Arrays.asList(arr));
//        list.add(a1);
//        list.add(a2);
        A<Integer, ?> a = new A<>(2, list.toArray(new A[0]));
        System.out.println(a.data + ": " + a.data.getClass().getSimpleName());
        for (A<?, ?> a3 : a.arr) {
            System.out.println("\t" + a3.data + ": " + a3.data.getClass().getSimpleName());
        }

        /*StringData d1 = new StringData("A");
        StringData d2 = new StringData("B");
        List<Data<?, ?>> list = new ArrayList<>();
        list.add(d1);
        list.add(d2);
        //IntData d = new IntData(2, new Data[] { d1, d2 });
        IntData d = new IntData(2, list.toArray(new Data[0]));
        System.out.println(d.data + ": " + d.data.getClass().getSimpleName() + "  " + d.newViewHolder(null));
        d.loadingDataOnView(null);
        for (Data<?, ?> d3 : d.subordinateData) {
            System.out.println("\t" + d3.data + ": " + d3.data.getClass().getSimpleName() + "  " + d3.newViewHolder(null));
            System.out.print("\t");
            d3.loadingDataOnView(null);
        }*/
    }

    private static class A<T, K> {
        T data;
        A<?, ?>[] arr;

//        A(T data, A<?>... arr) {
//            this.data = data;
//            this.arr = arr;
//        }

        A(T data, A<?, ?>[] arr) {
            this.data = data;
            this.arr = arr;
        }
    }

    /*private static class IntData extends Data<Integer, Integer> {
        protected IntData(@Nullable Integer data, @Nullable Data<?, ?>[] subordinateData) {
            super(data, subordinateData);
        }

        @Override
        public Integer newViewHolder(@NotNull View view) {
            return 266;
        }

        @Override
        public void loadingDataOnView(@NotNull Integer integer) {
            Integer a = data;
            System.out.println("IntData");
        }
    }

    private static class StringData extends Data<String, String> {
        protected StringData(@Nullable String data) {
            super(data);
        }

        @Override
        public String newViewHolder(@NotNull View view) {
            return "BFF";
        }

        @Override
        public void loadingDataOnView(@NotNull String s) {
            System.out.println("StringData");
        }
    }*/
}
