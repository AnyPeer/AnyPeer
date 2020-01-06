package any.xxx.anypeer.moudle.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.HeightBean;
import any.xxx.anypeer.bean.RankBean;
import any.xxx.anypeer.bean.RankListBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RankFragment extends Fragment {

    private View mLayout;
    private ListView mRankList;
    private boolean isLoading;
    private TextView mClickLoadNodeInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mLayout == null) {
            final Activity context = this.getActivity();
            if (context == null) {
                return null;
            }

            mLayout = context.getLayoutInflater().inflate(R.layout.fragment_rank, null);
            mRankList = mLayout.findViewById(R.id.lv);
            mClickLoadNodeInfo = mLayout.findViewById(R.id.clickload);
            mClickLoadNodeInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isLoading) {
                        isLoading = true;
                        mClickLoadNodeInfo.setText(R.string.loading);
                        getHeight();
                    }
                }
            });
        } else {
            ViewGroup parent = (ViewGroup) mLayout.getParent();
            if (parent != null) {
                parent.removeView(mLayout);
            }
        }

        return mLayout;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && !isLoading) {
            isLoading = true;
            getHeight();
        }
    }

    private final int ERROR_MSG = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case ERROR_MSG: {
                    String errorMsg = (String) message.obj;
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                    mClickLoadNodeInfo.setText(R.string.load_failed);
                    break;
                }
            }
        }
    };

    private void getHeight() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://blockchain.elastos.org/api/v1/newblock/")
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
                Message msg = new Message();
                msg.what = ERROR_MSG;
                msg.obj = e.getMessage();
                mHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isLoading = false;
                if (response.isSuccessful()) {
                    String json = response.body().string();
//                    Log.d("Rank", json);

                    Gson gson = new Gson();
                    HeightBean heightBean = gson.fromJson(json, HeightBean.class);
                    if (heightBean != null) {
                        getRank(heightBean.getHeight());
                    }
                }
            }
        });
    }

    private void getRank(String height) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api-wallet-ela.elastos.org/api/1/dpos/rank/height/" + height)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
                Message msg = new Message();
                msg.what = ERROR_MSG;
                msg.obj = e.getMessage();
                mHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    isLoading = false;
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        Log.d("Rank", json);
                        Gson gson = new Gson();
                        RankListBean rankListBean = gson.fromJson(json, RankListBean.class);
                        if (rankListBean != null) {
                            Log.d("Rank", rankListBean.getResult().size() + "");
                            getActivity().runOnUiThread(() -> {
                                RankBean me = null;
                                List<RankBean> rankBeans = rankListBean.getResult();

                                float total = 0;
                                Iterator<RankBean> iterator = rankBeans.iterator();
                                int rank = 1;
                                while (iterator.hasNext()) {
                                    RankBean rankBean = iterator.next();
                                    try {
                                        total += Float.parseFloat(rankBean.getValue());
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if (rankBean.getNickname().equals(getString(R.string.app_name))) {
                                        me = new RankBean();
                                        me.setNickname(rankBean.getNickname());
                                        me.setRank(rankBean.getRank());
                                        me.setValue(rankBean.getValue());
                                        me.setEstRewardPerYear(rankBean.getEstRewardPerYear());

                                        if (rankBean.isValid()) {
                                            rankBean.setRank(Integer.toString(rank++));
                                        }
                                    }
                                    else {
                                        if (!rankBean.isValid()) {
                                            iterator.remove();
                                        }
                                        else {
                                            rankBean.setRank(Integer.toString(rank++));
                                        }
                                    }
                                }

                                if (me != null) {
                                    rankBeans.add(0, me);
                                }
                                ItemRankLayoutAdapter itemRankLayoutAdapter = new ItemRankLayoutAdapter(getActivity(), rankBeans, total);
                                mRankList.setAdapter(itemRankLayoutAdapter);
                                mRankList.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
