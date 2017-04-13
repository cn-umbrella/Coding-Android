package net.coding.program.project.detail.wiki;

import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.event.EventRefresh;
import net.coding.program.model.ProjectObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_wiki_main)
public class WikiMainActivity extends BackActivity {

    @Extra
    ProjectObject project;

    @ViewById
    DrawerLayout drawerLayoutRoot;

    @ViewById
    ViewGroup drawerLayout;

    @ViewById
    WebView webView;

    List<Wiki> dataList = new ArrayList<>();
    AndroidTreeView treeViewBuilder;
    View treeView = null;

    NodeHolder selectNode = null;
    TreeNode firstTreeNode = null;

    MenuItem deleteAction;

    @AfterViews
    void initWikiMainActivity() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setActionBarTitle(project.name);

        onRefrush();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_wiki, menu);
        deleteAction = menu.findItem(R.id.action_delete);
        return super.onCreateOptionsMenu(menu);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventUpdate(EventRefresh event) {
        onRefrush();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NodeHolder node) {
        selectNode(node);
    }

    private void buildTree() {
        TreeNode root = TreeNode.root();
        addTreeNode(root, dataList);

        treeViewBuilder = new AndroidTreeView(this, root);
        treeViewBuilder.setDefaultViewHolder(NodeHolder.class);

        treeView = treeViewBuilder.getView();
        drawerLayout.addView(treeView);

        selectNode((NodeHolder) firstTreeNode.getViewHolder());
    }

    @Click(R.id.clickEdit)
    void onClickEdit() {
//        EventBus.getDefault().post(new EventRefresh(true));

    }

    @Click(R.id.clickPopDrawer)
    void onClickPopDrawer() {
        drawerLayoutRoot.openDrawer(GravityCompat.START);
    }

    @Click(R.id.clickHistory)
    void onClickHistory() {

    }

    private void onRefrush() {
        Network.getRetrofit(this)
                .getWikis(project.owner_user_name, project.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<List<Wiki>>(this) {
                    @Override
                    public void onSuccess(List<Wiki> data) {
                        super.onSuccess(data);

                        dataList.clear();
                        dataList.addAll(data);

                        if (treeView != null) {
                            drawerLayout.removeView(treeView);
                        }

                        buildTree();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    @OptionsItem(R.id.action_delete)
    void onActionDelete() {
        showDialog("", "删除的同时也会删除历史版本，请确认删除该 Wiki ?", (dialog, which) -> deleteSelectWiki(), null);
    }

    private void deleteSelectWiki() {
        Wiki wiki = selectNode.getNodeValue();
        Network.getRetrofit(this)
                .deleteWiki(project.owner_user_name, project.name, wiki.iid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Boolean>(this) {
                    @Override
                    public void onSuccess(Boolean data) {
                        super.onSuccess(data);

                        showProgressBar(false);

                        onRefrush();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);

                        showProgressBar(false);
                    }
                });
        showProgressBar(true);
    }

    private void selectNode(NodeHolder node) {
        if (selectNode == node) {
            return;
        }

        if (selectNode != null) {
            selectNode.select(false);
        }

        selectNode = node;

        if (selectNode != null) {
            selectNode.select(true);

            drawerLayoutRoot.closeDrawer(GravityCompat.START);

            Wiki selectWiki = selectNode.getNodeValue();
            Global.setWebViewContent(webView, "markdown.html", selectWiki.html);
        }
    }

    private void addTreeNode(TreeNode node, List<Wiki> wikis) {
        for (Wiki item : wikis) {
            TreeNode childNode = new TreeNode(item);
            node.addChild(childNode);
            if (firstTreeNode == null) {
                firstTreeNode = childNode;
            }

            addTreeNode(childNode, item.children);
        }
    }

}
