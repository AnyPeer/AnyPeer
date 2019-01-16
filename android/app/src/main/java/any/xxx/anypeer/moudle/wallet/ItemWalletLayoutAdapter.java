package any.xxx.anypeer.moudle.wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import any.xxx.anypeer.R;
import any.xxx.anypeer.util.AnyWallet;

public class ItemWalletLayoutAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<AnyWallet.TransactionItem> mTransactions;

    ItemWalletLayoutAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        if (AnyWallet.getInstance() != null) {
            mTransactions = AnyWallet.getInstance().getAllTransactionItems();
        }
    }

    @Override
    public int getCount() {
        if (mTransactions != null) {
            return mTransactions.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mTransactions != null) {
            return mTransactions.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_wallet_layout, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews(getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(Object object, ViewHolder holder) {
        try {
            AnyWallet.TransactionItem item = (AnyWallet.TransactionItem) object;
            if (item != null) {
                String asset;
                if (item.mInAmount > 0) {
                    holder.tvPayTitle.setText(R.string.wallet_inamount);
                    asset = String.format(Locale.US, "%.4f %s", AnyWallet.TOELA(item.mInAmount), AnyWallet.ELA);
                    holder.tvPayAsset.setText(asset);
                }
                else if (item.mOutAmount > 0) {
                    holder.tvPayTitle.setText(R.string.wallet_outamount);
                    asset = String.format(Locale.US, "%.4f %s", AnyWallet.TOELA(item.mOutAmount), AnyWallet.ELA);
                    holder.tvPayAsset.setText(asset);
                }

                holder.tvPayTime.setText(item.mTransactionTime);
                holder.tvPayStatus.setText(item.mStatus);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected class ViewHolder {
        private TextView tvPayTitle;
        private TextView tvPayTime;
        private TextView tvPayAsset;
        private TextView tvPayStatus;

        ViewHolder(View view) {
            tvPayTitle = view.findViewById(R.id.tv_pay_title);
            tvPayTime = view.findViewById(R.id.tv_pay_time);
            tvPayAsset = view.findViewById(R.id.tv_pay_asset);
            tvPayStatus = view.findViewById(R.id.tv_pay_status);
        }
    }
}
