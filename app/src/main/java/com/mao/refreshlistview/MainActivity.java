package com.mao.refreshlistview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mao.refreshlistview.view.MyListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 下拉刷新ListView，底部加载
 */
public class MainActivity extends AppCompatActivity {

    private MyListView mylistview;
    private List<String> listData;
    private MyAadpter myAadpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mylistview= (MyListView) findViewById(R.id.mylistview);

        mylistview.setOnRefreshListen(new MyListView.onRefreshListen() {
            //下拉刷新
            @Override
            public void onRefresh() {
                //加载数据耗时操作开启子线程
                new Thread(){
                    @Override
                    public void run() {
                        //睡眠两秒
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        listData.add(0,"下拉刷新后加载的数据");
                        //主线程跟新UI
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //跟新数据适配器
                                myAadpter.notifyDataSetChanged();
                                //更新完成需要执行的操作
                                mylistview.refreshComplete();
                            }
                        });
                    }
                }.start();
            }

            //加载更多
            @Override
            public void onLoadMord() {
                //加载数据耗时操作开启子线程
                new Thread(){
                    @Override
                    public void run() {
                        //睡眠两秒
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        listData.add("加载更多的数据1");
                        listData.add("加载更多的数据2");
                        listData.add("加载更多的数据3");
                        listData.add("加载更多的数据4");
                        //主线程跟新UI
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //跟新数据适配器
                                myAadpter.notifyDataSetChanged();
                                //更新完成需要执行的操作
                                mylistview.refreshComplete();
                            }
                        });
                    }
                }.start();
            }
        });

        listData=new ArrayList<String>();
        for (int i=0;i<40;i++){
            listData.add("这是ListView的数据"+i);
        }
        //给listView添加布局文件必须要在设置适配器之前添加
        myAadpter = new MyAadpter();

        mylistview.setAdapter(myAadpter);
    }

    class MyAadpter extends BaseAdapter{

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setTextSize(20);
            textView.setTextColor(Color.GRAY);
            textView.setText(listData.get(position));
            return textView;
        }
    }
}
