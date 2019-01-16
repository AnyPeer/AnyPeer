package any.xxx.anypeer.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.gyf.barlibrary.ImmersionBar;

import any.xxx.anypeer.R;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorPrimary).init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ImmersionBar.with(this).destroy();
    }
}
