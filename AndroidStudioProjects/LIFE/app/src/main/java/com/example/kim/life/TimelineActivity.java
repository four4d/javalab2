package com.example.kim.life;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class TimelineActivity extends AppCompatActivity {

    // Database 관련 객체들
    SQLiteDatabase db;
    String dbName = "lifeLogger.db"; // name of Database;
    String tbCategory = "CATEGORY"; // name of Table;
    String tbDo = "DO";
    String tbDoCon = "DID integer primary key autoincrement," +
            "CID integer not null," +
            "LATITUDE double, LONGITUDE double, ADDRESS text, CONTENT text default ' ', PHOTO text, " +
            "FOREIGN KEY(CID) references CATEGORY(CID)";
    int dbMode = Context.MODE_PRIVATE;

    private ListView mListView = null;
    private ListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // // Database 생성 및 열기
        db = openOrCreateDatabase(dbName,dbMode,null);

        ArrayList<ListData> _lists = new ArrayList<>();

        _lists = selectAllDo();

        ListAdapter adapter = new ListAdapter(this, R.layout.listview_item, _lists);

        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    // 모든 Data 읽기 - CATEGORY 테이블
    public ArrayList<ListData> selectAllDo() {
        String sql = "select * from DO;";
        Cursor results = db.rawQuery(sql, null);
        results.moveToFirst();

        ArrayList<ListData> _lists = new ArrayList<>();

        while (!results.isAfterLast()) {
            int db_did = results.getInt(0);
            int db_cid = results.getInt(1);
            double db_latitude = results.getDouble(2);
            double db_longitude = results.getDouble(3);
            String db_address = results.getString(4);
            String db_content = results.getString(5);
            String db_photo = results.getString(6);
            String db_date = results.getString(7);
            Log.d("lab_sqlite", "index= " + db_did + " date=" + db_date);

            ListData s = new ListData(db_photo, db_content, db_date);
            _lists.add(s);
            results.moveToNext();
        }
        results.close();
        return _lists;
    }

}

