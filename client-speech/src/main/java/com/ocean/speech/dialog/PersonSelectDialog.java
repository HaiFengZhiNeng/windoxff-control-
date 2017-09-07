package com.ocean.speech.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by dell on 2017/9/5.
 */

public class PersonSelectDialog implements DialogInterface.OnDismissListener, AdapterView.OnItemClickListener {

    private Context mContext;
    private Dialog dialog;
    private View customview;
    private String mtitle;
    private ControlPresenter.ClickListenerInterface clickListener;
    private TextView tv_man;
    private TextView tv_woman;
    private TextView tv_old;
    private TextView tv_child;


    private String[] data_string_who = null;
    private List<String> data_who = null;

    private String[] data_string_number = null;
    private List<String> data_who_number = null;

    private PersonSelectAdapter selectAdapter = null;


    public void setClickListener(ControlPresenter.ClickListenerInterface clickListener) {
        this.clickListener = clickListener;
    }

    public PersonSelectDialog(Context context, String title) {
        mContext = context;
        mtitle = title;
        this.theme = R.style.NewSettingDialog;
    }

    public void init() {
        dialog = new Dialog(mContext, theme);
        customview = LayoutInflater.from(mContext).inflate(R.layout.dialog_personselect_layout, null);
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


    /**
     * 初始化listView
     */
    @SuppressLint("ResourceAsColor")
    private void initView() {
        data_string_who = mContext.getResources().getStringArray(R.array.voicer_cloud_entries_man);
        data_who = Arrays.asList(data_string_who);
        data_string_number = mContext.getResources().getStringArray(R.array.voicer_cloud_values_man);
        data_who_number = Arrays.asList(data_string_number);
        ListView mListView = (ListView) customview.findViewById(R.id.lv_person);
        selectAdapter = new PersonSelectAdapter();
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(selectAdapter);
        selectAdapter.setBean(data_who);

        tv_man = (TextView) customview.findViewById(R.id.tv_man);
        tv_woman = (TextView) customview.findViewById(R.id.tv_women);
        tv_child = (TextView) customview.findViewById(R.id.tv_child);
        tv_old = (TextView) customview.findViewById(R.id.tv_old);


        tv_man.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("GG", "mRefreshImg");
                tv_man.setTextColor(ContextCompat.getColor(mContext, R.color.color_perosn_text));
                tv_woman.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_old.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_child.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                data_string_who = mContext.getResources().getStringArray(R.array.voicer_cloud_entries_man);
                data_string_number = mContext.getResources().getStringArray(R.array.voicer_cloud_values_man);
                data_who = Arrays.asList(data_string_who);
                selectAdapter.setBean(data_who);
            }
        });

        tv_woman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_man.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_woman.setTextColor(ContextCompat.getColor(mContext, R.color.color_perosn_text));
                tv_old.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_child.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                data_string_who = mContext.getResources().getStringArray(R.array.voicer_cloud_entries_woman);
                data_string_number = mContext.getResources().getStringArray(R.array.voicer_cloud_values_woman);
                data_who = Arrays.asList(data_string_who);
                selectAdapter.setBean(data_who);
            }
        });

        tv_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_man.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_woman.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_old.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_child.setTextColor(ContextCompat.getColor(mContext, R.color.color_perosn_text));
                data_string_who = mContext.getResources().getStringArray(R.array.voicer_cloud_entries_child);
                data_string_number = mContext.getResources().getStringArray(R.array.voicer_cloud_values_child);
                data_who = Arrays.asList(data_string_who);
                selectAdapter.setBean(data_who);
            }
        });

        tv_old.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_man.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_woman.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));
                tv_old.setTextColor(ContextCompat.getColor(mContext, R.color.color_perosn_text));
                tv_child.setTextColor(ContextCompat.getColor(mContext, R.color.color_person_select_no));

                data_string_who = mContext.getResources().getStringArray(R.array.voicer_cloud_entries_old);
                data_string_number = mContext.getResources().getStringArray(R.array.voicer_cloud_values_old);
                data_who = Arrays.asList(data_string_who);
                selectAdapter.setBean(data_who);
            }
        });

    }


    private class PersonSelectAdapter extends BaseAdapter {

        private List<String> newBean = new ArrayList<>();

        public void setBean(List<String> list) {
            this.newBean = list;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return newBean != null ? newBean.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return newBean != null ? newBean.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PersonSelectAdapter.ViewHolder vh;
            if (convertView == null) {
                vh = new PersonSelectAdapter.ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_scene_item, null);
                vh.tv = (TextView) convertView.findViewById(R.id.scene_item_btn);
                convertView.setTag(vh);
            } else {
                vh = (PersonSelectAdapter.ViewHolder) convertView.getTag();
            }
            vh.tv.setText(newBean.get(position));

            return convertView;
        }

        class ViewHolder {
            TextView tv;
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
     * dialog 消失
     */
    public void dismiss() {
        if (isShowing()) {
            dialog.dismiss();

        }
    }

    /**
     * Dialog主题
     */
    private int theme;

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        fullScreen();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        clickListener.sendPersonSelect(data_string_number[i]);

        dismiss();
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
