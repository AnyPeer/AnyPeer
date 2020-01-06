package any.xxx.anypeer.moudle.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.RankBean;

public class ItemRankLayoutAdapter extends BaseAdapter {

    private List<RankBean> objects;

    private Context context;
    private LayoutInflater layoutInflater;
    private float mTotal;

    ItemRankLayoutAdapter(Context context, List<RankBean> objects, float total) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
        mTotal = total;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public RankBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_rank_layout, null);
            convertView.setTag(new ViewHolder(convertView));
        }

        initializeViews(getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private final static int LIMIT = 24;
    private void initializeViews(RankBean object, ViewHolder holder, int position) {
        try {
            holder.tvName.setText(object.getNickname());
            holder.tvRank.setText(object.getRank());
            if (Integer.parseInt(object.getRank()) > LIMIT) {
                holder.tvRank.setTextColor(context.getResources().getColor(R.color.white_blue));
            }
            else {
                holder.tvRank.setTextColor(context.getResources().getColor(R.color.orange1));
            }
            holder.tvValue.setText(context.getString(R.string.value) + object.getValue());
//        holder.tvNum.setText(context.getString(R.string.num) + object.getEstRewardPerYear());

            float percent = Float.parseFloat(object.getValue()) / mTotal * 100;
            holder.tvNum.setText(String.format(Locale.US, "%s %.4f%s", context.getString(R.string.num), percent, "%"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (position == 0) {
            holder.div.setVisibility(View.VISIBLE);
        } else {
            holder.div.setVisibility(View.GONE);
        }
    }

    protected class ViewHolder {
        private TextView tvName;
        private TextView tvRank;
        private TextView tvValue;
        private TextView tvNum;
        private View div;

        ViewHolder(View view) {
            tvName = view.findViewById(R.id.tv_name);
            tvRank = view.findViewById(R.id.tv_rank);
            tvValue = view.findViewById(R.id.tv_value);
            tvNum = view.findViewById(R.id.tv_num);
            div = view.findViewById(R.id.div);
        }
    }
}
