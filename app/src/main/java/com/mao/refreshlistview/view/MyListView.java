package com.mao.refreshlistview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mao.refreshlistview.R;

import java.text.SimpleDateFormat;


/**
 * Created by 毛麒添 on 2016/12/16 0016.
 * 自定义带下拉刷新的ListView控件(继承已有控件实现自定义)
 */

public class MyListView extends ListView implements AbsListView.OnScrollListener{

    private View mHeadView;//头布局对象
    private View mFootView;//脚布局对象

    private float downY;//按下的坐标Y
    private float moveY;

    //设置头部的三个状态
    public static final int PULL_REFRESH =0;//下拉刷新
    public static final int RELEASE_REFRESH =1;//释放刷新刷新
    public static final int IS_REFRESH =2;//正在刷新刷新

    private int refreshState=PULL_REFRESH;//状态，默认是下拉刷新

    private int mHeadViewHeight;//头部的高度
    private int mFootiewHeight;//头部的高度

    private RotateAnimation upAnim;//下拉刷新动画

    private RotateAnimation downAnim;//下拉不做刷新动画

    private ImageView iv_arrow;//箭头图标对象
    private ProgressBar pb;
    private TextView tv_title;
    private TextView tv_desc_refresh_time;

    private int paddingmove;//设置给头部的宽度(整个控件的核心)

    private boolean isLoadMore=false;//是否正在加载更多

    public MyListView(Context context) {
        super(context);
        init();
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    //初始化头布局，脚布局
    public void init(){
        //初始化头布局
        initHeadView();
        //初始化脚布局
        initFootView();
        //初始化箭头动画
        initAnimation();

        setOnScrollListener(this);
    }

    /**
     * 初始化脚布局
     */
    private void initFootView() {
        mFootView = View.inflate(getContext(), R.layout.listview_foot_item, null);
        //提前手动测量宽高
        mFootView.measure(0,0);
        //获取控件的高度
        mFootiewHeight= mFootView.getMeasuredHeight();

        //设置给头部的padding高度为负，让其隐藏
        mFootView.setPadding(0,-mFootiewHeight,0,0);//这是内边距，可以隐藏当前头控件

        addFooterView(mFootView);
    }

    //初始化箭头动画
    private void initAnimation() {
        //往上旋转
        upAnim = new RotateAnimation(0f,-180f,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f
                );
        upAnim.setDuration(500);
        upAnim.setFillAfter(true);

        //向下旋转
        downAnim = new RotateAnimation(-180f,-360f,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f
        );
        downAnim.setDuration(500);
        downAnim.setFillAfter(true);
    }

    /**
     * 初始化头布局
     */
    private void initHeadView() {
        mHeadView=View.inflate(getContext(), R.layout.listview_head_item,null);
        //获取布局中的控件
        iv_arrow = (ImageView) mHeadView.findViewById(R.id.iv_arrow);
        pb = (ProgressBar) mHeadView.findViewById(R.id.pb);
        tv_title = (TextView) mHeadView.findViewById(R.id.tv_title);
        tv_desc_refresh_time = (TextView) mHeadView.findViewById(R.id.tv_desc_refresh_time);
        //提前手动测量宽高
        mHeadView.measure(0,0);
        //获取到测量后的高度
        mHeadViewHeight = mHeadView.getMeasuredHeight();

        //设置给头部的padding高度为负，让其隐藏
        mHeadView.setPadding(0,-mHeadViewHeight,0,0);//这是内边距，可以隐藏当前头控件

        addHeaderView(mHeadView);
    }

    //触摸监听事件

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /**
         * 触碰屏幕，记录这个值，滑动屏幕，记录这个值，将滑动的距离减去刚开始按下的值就是隐藏
         * 头部需要展现的距离，这时候是下拉刷新状态，当头部的距离大于或等于0的时候表示头部已
         * 经完全展示，这时候就需要改变状态为释放刷新，
         */
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                //按下的坐标Y
                downY = ev.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                //移动的实时距离坐标
                moveY = ev.getY();

                //如果是正在刷新的状态，则不进行下面操作，而是执行父类的方法
                if(refreshState==IS_REFRESH){
                    return super.onTouchEvent(ev);
                }

                //偏移量
                float offset=moveY-downY;
                //当偏移量大于0，并且当前第一个可见头目的索引是0才显示头部
                if(offset > 0 && getFirstVisiblePosition()==0){
                    //手指滑动的距离，设置给头部让其显示
                    paddingmove = (int) (-mHeadViewHeight+offset);
                    mHeadView.setPadding(0, paddingmove,0,0);
                    if(paddingmove >=0 && refreshState!= RELEASE_REFRESH){//隐藏头部完全显示
                        //改变状态成释放状态
                        Log.w("毛麒添", "onTouchEvent: "+refreshState );
                        refreshState=RELEASE_REFRESH;
                        updateHeader();//根据状态更新头部信息

                    }else if(paddingmove <0 && refreshState!= PULL_REFRESH){//隐藏头部不完全显示
                        Log.w("毛麒添", "onTouchEvent: "+refreshState );
                        refreshState=PULL_REFRESH;
                        updateHeader();//根据状态更新头部信息

                    }

                    return true;//事件被消费
                }

                break;
            case MotionEvent.ACTION_UP://手指抬起
                //根据刚刚操作的作态做响应
                if(refreshState==PULL_REFRESH){
                    /**
                     * 如果paddingmove<0 隐藏头部没有完全显示，说明正在下拉，但是没有拉完去，
                     * 没有执行跟新操作，则继续隐藏头部
                     */
                    mHeadView.setPadding(0,-mHeadViewHeight,0,0);

                }else  if(refreshState==RELEASE_REFRESH){
                    //如果paddingmove>=0 隐藏头部完全显示，则执行正在刷新,隐藏头部显示完全
                    mHeadView.setPadding(0,0,0,0);
                    //状态变为正在刷新
                    refreshState=IS_REFRESH;
                    updateHeader();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void updateHeader() {
        switch (refreshState){
            case PULL_REFRESH://下拉刷新状态
                //开始动画，更改标题
                iv_arrow.startAnimation(downAnim);
                tv_title.setText("下拉刷新");

                break;
            case RELEASE_REFRESH: //释放刷新状态
                //开始动画，更改标题
                iv_arrow.startAnimation(upAnim);
                tv_title.setText("释放刷新");

                break;
            case IS_REFRESH://正在刷新状态
                //箭头清除动画
                iv_arrow.clearAnimation();
                tv_title.setText("正在刷新.....");
                pb.setVisibility(VISIBLE);
                iv_arrow.setVisibility(INVISIBLE);

                //如果是正在刷新状态，告诉外部调用监听方法正在刷新
                if(listener!=null){
                    listener.onRefresh();//告知调用者此时可以发送网络请求加载更多数据
                }
                break;

        }
    }

    //调用者下拉刷新完成需要执行的操作
    public void refreshComplete() {
        if(isLoadMore){//加载更多
            mFootView.setPadding(0,-mFootiewHeight,0,0);
            isLoadMore=false;

        }else {//下拉刷新
            refreshState=PULL_REFRESH;
            pb.setVisibility(View.INVISIBLE);
            iv_arrow.setVisibility(View.VISIBLE);
            tv_title.setText("下拉刷新");
            mHeadView.setPadding(0,-mHeadViewHeight,0,0);//隐藏头部

            //设置此时跟新操作的时间
            String time=getNowTime();
            tv_desc_refresh_time.setText("最后更新时间："+time);
        }

    }

    //获取当前时间并格式化
    private String getNowTime() {
        long timeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(timeMillis);
    }

    //ListView状态更新的时候调用
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(isLoadMore){
            return;//已经正在加载，则不能重复下拉加载
        }
        //不管是更新什么状态，最后都是为空闲状态
        //显示加载更多
          if(scrollState==SCROLL_STATE_IDLE && getLastVisiblePosition()>=(getCount()-1)){
              isLoadMore=true;
              mFootView.setPadding(0,0,0,0);
              //显示在最后一条位置
              setSelection(getCount());

              //告诉外部调用现在是加载更多
              if(listener!=null){
                  listener.onLoadMord();
              }
          }
    }

    //滑动的时候调用
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    //回调接口
    public interface onRefreshListen{
        void onRefresh();//下拉刷新
        void onLoadMord();//加载更多
    }

    private onRefreshListen listener;
    //监听回调方法
    public void setOnRefreshListen(onRefreshListen listener) {
         this.listener=listener;
    }
}
