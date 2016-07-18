package com.tencent.qcloud.suixinbo.views;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import com.tencent.TIMUserProfile;
import com.tencent.qcloud.suixinbo.R;
import com.tencent.qcloud.suixinbo.avcontrollers.QavsdkControl;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.InitBusinessHelper;
import com.tencent.qcloud.suixinbo.presenters.LoginHelper;
import com.tencent.qcloud.suixinbo.presenters.ProfileInfoHelper;
import com.tencent.qcloud.suixinbo.presenters.viewinface.ProfileView;
import com.tencent.qcloud.suixinbo.utils.SxbLog;
import com.tencent.qcloud.suixinbo.views.customviews.BaseFragmentActivity;

import java.util.List;

/**
 * 主界面
 */
public class HomeActivity extends BaseFragmentActivity implements ProfileView {
    private FragmentTabHost mTabHost;
    private LayoutInflater layoutInflater;
    private ProfileInfoHelper infoHelper;
    private LoginHelper mLoginHelper;
    private final Class fragmentArray[] = {FragmentLiveList.class, FragmentPublish.class, FragmentProfile.class};
    private int mImageViewArray[] = {R.drawable.tab_live, R.drawable.icon_publish, R.drawable.tab_profile};
    private String mTextviewArray[] = {"live", "publish", "profile"};
    private static final String TAG = HomeActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        SxbLog.i(TAG, "HomeActivity onStart");
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        layoutInflater = LayoutInflater.from(this);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.contentPanel);

        int fragmentCount = fragmentArray.length;
        for (int i = 0; i < fragmentCount; i++) {
            //为每一个Tab按钮设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            mTabHost.getTabWidget().setDividerDrawable(null);

        }
        mTabHost.getTabWidget().getChildTabViewAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                DialogFragment newFragment = InputDialog.newInstance();
//                newFragment.show(ft, "dialog");

                startActivity(new Intent(HomeActivity.this, PublishLiveActivity.class));

            }
        });

        // 检测是否需要获取头像
        if (TextUtils.isEmpty(MySelfInfo.getInstance().getAvatar())) {
            infoHelper = new ProfileInfoHelper(this);
            infoHelper.getMyProfile();
        }
    }

    @Override
    protected void onStart() {
        SxbLog.i(TAG, "HomeActivity onStart");
        super.onStart();
        if (QavsdkControl.getInstance().getAVContext() == null) {//retry
            InitBusinessHelper.initApp(getApplicationContext());
            SxbLog.i(TAG, "HomeActivity retry login");
            mLoginHelper = new LoginHelper(this);
            mLoginHelper.imLogin(MySelfInfo.getInstance().getId(), MySelfInfo.getInstance().getUserSig());
        }
    }

    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.tab_content, null);
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageResource(mImageViewArray[index]);
        return view;
    }

    @Override
    protected void onDestroy() {
        if (mLoginHelper != null)
            mLoginHelper.onDestory();
        SxbLog.i(TAG, "HomeActivity onDestroy");
        QavsdkControl.getInstance().stopContext();
        super.onDestroy();
    }

    @Override
    public void updateProfileInfo(TIMUserProfile profile) {
        SxbLog.i(TAG, "updateProfileInfo");
        if (null != profile) {
            MySelfInfo.getInstance().setAvatar(profile.getFaceUrl());
            if (!TextUtils.isEmpty(profile.getNickName())) {
                MySelfInfo.getInstance().setNickName(profile.getNickName());
            } else {
                MySelfInfo.getInstance().setNickName(profile.getIdentifier());
            }
        }
    }

    @Override
    public void updateUserInfo(int reqid, List<TIMUserProfile> profiles) {
    }
}
