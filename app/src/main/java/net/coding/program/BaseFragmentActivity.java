package net.coding.program;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.CustomDialog;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.umeng.UmengFragmentActivity;
import net.coding.program.user.UserDetailActivity_;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cc191954 on 14-8-16.
 * 封装了图片下载并缓存
 */
public class BaseFragmentActivity extends UmengFragmentActivity implements NetworkCallback {

    protected LayoutInflater mInflater;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    private ProgressDialog mProgressDialog;

    private NetworkImpl networkImpl;

    protected FootUpdate mFootUpdate = new FootUpdate();

    protected boolean progressBarIsShowing() {
        return mProgressDialog.isShowing();
    }

    protected View.OnClickListener mOnClickUser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String globalKey = (String) v.getTag();

            Intent intent = new Intent(BaseFragmentActivity.this, UserDetailActivity_.class);
            intent.putExtra("globalKey", globalKey);
            startActivity(intent);
        }
    };

    protected void showErrorMsg(int code, JSONObject json) {
        if (code == NetworkImpl.NETWORK_ERROR) {
            showButtomToast("连接服务器失败，请检查网络或稍后重试");
        } else {
            String msg = Global.getErrorMsg(json);
            if (!msg.isEmpty()) {
                showButtomToast(msg);
            }
        }
    }

    protected ImageLoadTool getImageLoad() {
        return imageLoadTool;
    }

    protected boolean isLoadingLastPage(String tag) {
        return networkImpl.isLoadingLastPage(tag);
    }

    protected boolean isLoadingFirstPage(String tag) {
        return networkImpl.isLoadingFirstPage(tag);
    }

    protected void showProgressBar(boolean show) {
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkImpl = new NetworkImpl(this, this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        mInflater = getLayoutInflater();
        initSetting();

        UnreadNotify.update(this);
    }

    protected void initSetting() {
        networkImpl.initSetting();
    }

    protected void showDialog(String msg, DialogInterface.OnClickListener clickOk) {
        Dialog dialog = new AlertDialog.Builder(this).
                setTitle(msg)
                .setPositiveButton("确定", clickOk)
                .setNegativeButton("取消", null)
                .show();

        CustomDialog.dialogTitleLineColor(this, dialog);
    }

    protected void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk) {
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", clickOk)
                .setNegativeButton("取消", null)
                .show();

        CustomDialog.dialogTitleLineColor(this, dialog);
    }

    public void dialogTitleLineColor(Dialog dialog) {
        CustomDialog.dialogTitleLineColor(this, dialog);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
    }

    protected void getNextPageNetwork(String url, final String tag) {
        networkImpl.getNextPageNetwork(url, tag);
    }

    protected void postNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Post);
    }

    protected void postNetwork(String url, RequestParams params, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, params, tag, dataPos, data, NetworkImpl.Request.Post);
    }

    protected void deleteNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Delete);
    }

    protected void deleteNetwork(String url, final String tag, String id) {
        networkImpl.loadData(url, null, tag, -1, id, NetworkImpl.Request.Delete);
    }

    protected void putNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Put);
    }

    @Override
    public void getNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Get);
    }

    protected void getNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Get);
    }

    protected void showButtomToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void showMiddleToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void iconfromNetwork(ImageView view, String url) {
        imageLoadTool.loadImage(view, Global.makeSmallUrl(view, url));
    }

    /**
     * 载入动画
     */
    private DialogUtil.LoadingPopupWindow mDialogProgressPopWindow = null;

    private void initDialogLoading() {
        if (mDialogProgressPopWindow == null) {
            PopupWindow.OnDismissListener onDismissListener = new PopupWindow.OnDismissListener() {
                public void onDismiss() {
                    hideProgressDialog();
                }
            };

            mDialogProgressPopWindow = DialogUtil.initProgressDialog(this, onDismissListener);
        }
    }

    public void showDialogLoading(String title) {
        initDialogLoading();
        DialogUtil.showProgressDialog(this, mDialogProgressPopWindow, title);
    }

    public void showDialogLoading() {
        showDialogLoading("");
    }

    public void hideProgressDialog() {
        if (mDialogProgressPopWindow != null) {
            DialogUtil.hideDialog(mDialogProgressPopWindow);
        }
    }

}
