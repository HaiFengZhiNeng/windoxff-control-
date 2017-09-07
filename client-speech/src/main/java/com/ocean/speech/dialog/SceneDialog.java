package com.ocean.speech.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ocean.speech.R;
import com.ocean.speech.control.ControlPresenter;
import com.ocean.speech.control.InterfaceBean;

import java.util.ArrayList;


public class SceneDialog implements DialogInterface.OnDismissListener, AdapterView.OnItemClickListener {
    private Context mContext;
    private String mtitle;
    private Dialog dialog;
    private View customview;
    private ControlPresenter.ClickListenerInterface clickListener;

    private ArrayList<InterfaceBean> data = null;

    /**
     * Dialog主题
     */
    private int theme;


    public void setClickListener(ControlPresenter.ClickListenerInterface clickListener) {
        this.clickListener = clickListener;
    }

    public SceneDialog(Context context, String title) {
        mContext = context;
        mtitle = title;
        this.theme = R.style.NewSettingDialog;
    }

    public void init() {
        dialog = new Dialog(mContext, theme);
        customview = LayoutInflater.from(mContext).inflate(R.layout.dialog_interface_layout, null);
        dialog.setContentView(customview);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setOnDismissListener(this);

        DisplayMetrics m = mContext.getResources().getDisplayMetrics();
        int screenWidth = Math.min(m.widthPixels, m.heightPixels);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        dialog.getWindow().setGravity(Gravity.RIGHT | Gravity.TOP);
        params.x = 100; // 新位置X坐标
        params.y = 50; // 新位置Y坐标
        params.width = (int) (screenWidth * 1.1);
        params.height = (int) (screenWidth * 0.8);
        dialog.getWindow().setAttributes(params);
        initView();
    }

    private InterfaceAdapter adapter = null;

    private void initView() {
        adapter = new InterfaceAdapter();
        ListView mListView = (ListView) customview.findViewById(R.id.interface_list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);

        ImageView mRefreshImg = (ImageView) customview.findViewById(R.id.refresh_img);
        mRefreshImg.setVisibility(View.GONE);
        mRefreshImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.sendRefreshData();
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (clickListener != null)
            clickListener.sendScene(data.get(position).getId());

        dismiss();
    }

    public void setData(ArrayList<InterfaceBean> interfaceBeen) {
        data = interfaceBeen;
        if (adapter != null)
            adapter.setBeen(interfaceBeen);
    }

    private class InterfaceAdapter extends BaseAdapter {

        ArrayList<InterfaceBean> been = null;

        public void setBeen(ArrayList<InterfaceBean> been) {
            this.been = been;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return been != null ? been.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return been != null ? been.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_scene_item, null);
                vh.tv = (TextView) convertView.findViewById(R.id.scene_item_btn);
                vh.scene_item = (TextView) convertView.findViewById(R.id.scene_item);

                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.tv.setText(been.get(position).getId() + "");
            vh.scene_item.setText(been.get(position).getContent() + "");
            return convertView;
        }

        class ViewHolder {
            TextView tv;
            TextView scene_item;
        }
    }

    /**
     * 显示
     */
    public void show() {
        if (dialog == null) {
            init();
        }
        dialog.show();
        fullScreen();
    }

    /**
     * 是否显示
     *
     * @return 是否显示
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    /**
     * dialog 小时
     */
    public void dismiss() {
        if (isShowing()) {
            dialog.dismiss();

        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        fullScreen();
    }

    /**
     * 全屏显示方法
     */
    public void fullScreen() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }
}
