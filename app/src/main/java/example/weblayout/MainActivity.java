package example.weblayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://3w.huanqiu.com/a/de583b/7JtkyGE2nVC?agt=20&group_id=6646502052568498695&app=");
        mRecyclerView = findViewById(R.id.recyclerview);
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(new RecyclerView.Adapter() {
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return new RecyclerView.ViewHolder(new TextView(parent.getContext())) {
                        @Override
                        public String toString() {
                            return super.toString();
                        }
                    };
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                    ((TextView) holder.itemView).setText(String.valueOf(position));
                }

                @Override
                public int getItemCount() {
                    return 100;
                }
            });

        }


//        RecyclerView recyclerView = findViewById(R.id.recyclerview);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(new RecyclerView.Adapter() {
//            @NonNull
//            @Override
//            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//                return new RecyclerView.ViewHolder(new TextView(viewGroup.getContext())) {
//                    @Override
//                    public String toString() {
//                        return super.toString();
//                    }
//                };
//            }
//
//            @Override
//            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
//                Log.i("@@", "@@@@@@@:" + i);
//                ((TextView) viewHolder.itemView).setText(String.valueOf(i));
//            }
//
//            @Override
//            public int getItemCount() {
//                return 100;
//            }
//        });
//        ListView listView = new ListView(this);
//        listView.setAdapter(new BaseAdapter() {
//            @Override
//            public int getCount() {
//                return 1000;
//            }
//
//            @Override
//            public Object getItem(int position) {
//                return null;
//            }
//
//            @Override
//            public long getItemId(int position) {
//                return 0;
//            }
//
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                TextView textView;
//                if ((textView = (TextView) convertView) == null) {
//                    textView = new TextView(parent.getContext());
//                }
//                textView.setText(String.valueOf(position));
//                return textView;
//            }
//        });
//        webView.addView(listView,new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }
}