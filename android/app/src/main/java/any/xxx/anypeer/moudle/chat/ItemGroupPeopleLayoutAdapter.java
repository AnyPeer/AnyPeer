package any.xxx.anypeer.moudle.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import any.xxx.anypeer.R;

public class ItemGroupPeopleLayoutAdapter extends BaseAdapter {

    private List<String> objects = new ArrayList<>();

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemGroupPeopleLayoutAdapter(Context context, List<String> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public String getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_group_people_layout, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews(getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(String object, ViewHolder holder) {
        holder.tvName.setText(object);
    }

    protected class ViewHolder {
        private ImageView ivHeader;
        private TextView tvName;

        public ViewHolder(View view) {
            ivHeader = view.findViewById(R.id.iv_header);
            tvName = view.findViewById(R.id.tv_name);
        }
    }
}

