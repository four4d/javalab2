package com.example.kim.life;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    final static int SELECT_MAP = 0;
    final static int SELECT_IMAGE = 1;

    // Database 관련 객체들
    SQLiteDatabase db;
    String dbName = "lifeLogger.db"; // name of Database;
    String tbCategory = "CATEGORY"; // name of Table;
    String tbCategoryCon = "CID integer primary key autoincrement, CNAME text not null";
    String tbDo = "DO";
    String tbDoCon = "DID integer primary key autoincrement," +
            "CID integer not null," +
            "LATITUDE double, LONGITUDE double, ADDRESS text," +
            "CONTENT text default ' ', PHOTO text, DATE text," +
            "FOREIGN KEY(CID) references CATEGORY(CID)";
    int dbMode = Context.MODE_PRIVATE;

    // 레이아웃 객체
    TextView mTvTime;
    TextView mTvLocation;
    EditText mEtDo;
    Button mBtMap;
    Button mBtCamera;
    Button mBtPhoto;
    Button mBtSave;
    Button mBtTimeline;
    Button mBtAnalysis;
    Button mBtConfig;
    Spinner mSpCategory;
    Context mContext;

    ArrayAdapter<String> categoryAdapter;
    ArrayList<String> categoryList;

    Geocoder gc;

    String addrName;
    double latitude;
    double longitude;

    int cid = 0;
    String photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 레이아웃 객체 초기화
        mTvTime = (TextView) findViewById(R.id.tx_time);
        mTvLocation = (TextView) findViewById(R.id.tx_location);
        mEtDo = (EditText) findViewById(R.id.et_do);
        mBtMap = (Button) findViewById(R.id.bt_map);
        mBtCamera = (Button) findViewById(R.id.bt_camera);
        mBtPhoto = (Button) findViewById(R.id.bt_photo);
        mBtSave = (Button) findViewById(R.id.bt_save);
        mBtTimeline = (Button) findViewById(R.id.bt_timeline);
        mBtAnalysis = (Button) findViewById(R.id.bt_analysis);
        mBtConfig = (Button) findViewById(R.id.bt_config);
        mSpCategory = (Spinner) findViewById(R.id.sp_category);

        mContext = this.getApplication().getApplicationContext();

        categoryList = new ArrayList<String>();
        categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        mSpCategory.setAdapter(categoryAdapter);
        mSpCategory.setOnItemSelectedListener(this);

        db = openOrCreateDatabase(dbName, dbMode, null);
        createTable(tbCategory, tbCategoryCon);
        createTable(tbDo, tbDoCon);

        //만약 앱이 최초 실행되어 DB에 레코드가 아무것도 없는 경우 최초 기본데이터 insert
        if (!checkData(tbCategory, "CID", 1)) {
            insertData(tbCategory, "NULL, '식사'");
            insertData(tbCategory, "NULL, '공부'");
            insertData(tbCategory, "NULL, '독서'");
            insertData(tbCategory, "NULL, '휴식'");
            insertData(tbCategory, "NULL, '취침'");
            insertData(tbCategory, "NULL, '운동'");
            insertData(tbCategory, "NULL, '커피'");
            insertData(tbCategory, "NULL, '영화'");
            insertData(tbCategory, "NULL, 'TV'");
            insertData(tbCategory, "NULL, '게임'");
            insertData(tbCategory, "NULL, '인터넷'");
        }

        selectAllCategory();
        selectAllDo();
        categoryAdapter.notifyDataSetChanged();


        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        final String now = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));

        mTvTime.setText(now.substring(8, 10) + "시 " + now.substring(10, 12) + "분");

        //시스템 위치 서비스에 접근하는 객체 생성
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gc = new Geocoder(this, Locale.KOREAN);

        //위치 파악을 위해 가장 적합한 provider를 찾아서 세팅.
        String locationProvider = locationManager.getBestProvider(new Criteria(), true);
        // getLastKnownLocation 메소드를 사용하려하니 자동으로 permission체크가 추가됨.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        // 수동으로 좌표 구하기.
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();

            //좌표로 주소 탐색 실시. 주소가 있다면 주소를 화면에 표기. 없다면 위도,경도 표기.
            if (searchLocation(latitude, longitude)) {
                mTvLocation.setText(addrName.substring(4));
                System.out.println(addrName);
                System.out.println(addrName.substring(4));
            } else {
                mTvLocation.setText("위도:" + latitude + "\n경도:" + longitude);
            }
        }

        // 지도확인 버튼 클릭시 지도화면 액티비티 호출.
        mBtMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(mContext, MapActivity.class);
                mapIntent.putExtra("latitude",latitude);
                mapIntent.putExtra("longitude",longitude);
                mapIntent.putExtra("addrName",addrName);
                startActivityForResult(mapIntent, SELECT_MAP);
            }
        });

        // 사진첨부 버튼 클릭시 사진 갤러리 호출.
        mBtPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                startActivityForResult(intent, SELECT_IMAGE);
            }
        });

        // 저장 버튼 클릭시 DO 테이블에 레코드 INSERT
        mBtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer sb = new StringBuffer();
                sb.append("null, ");
                sb.append(cid);
                sb.append(", ");
                sb.append(latitude);
                sb.append(", ");
                sb.append(longitude);
                sb.append(", '");
                sb.append(addrName.substring(4));
                sb.append("', '");
                sb.append(mEtDo.getText().toString());
                sb.append("', '");
                sb.append(photoUri);
                sb.append("', '");
                sb.append(now);
                sb.append("'");
                insertData(tbDo, sb.toString());
            }
        });

        // 타임라인 버튼 클릭시 타임라인 액티비티 호출.
        mBtTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TimelineActivity.class);
                startActivity(intent);
            }
        });
    }

    /*위도,경도로 주소 값 받아오는 함수.
    * 파라미터:double latitude 위도값 / double longitude 경도값
    * 리턴값 : boolean 주소 존재 여부. 주소 있을 시 true.*/
    private boolean searchLocation(double latitude, double longitude) {
        List<Address> addressList = null;
        boolean sw = false;

        try {
            addressList = gc.getFromLocation(latitude, longitude, 1);

            if(addressList != null) {
                Address outAddr = addressList.get(0);
                int addrCount = outAddr.getMaxAddressLineIndex() + 1;
                StringBuffer outAddrStr = new StringBuffer();
                for (int k = 0; k < addrCount ; k++) {
                    outAddrStr.append(outAddr.getAddressLine(k));
                }
                addrName = outAddrStr.toString();
                sw = true;
            }
        }catch (IOException ex) {
            Log.d("Geocoding", "Exception : " + ex.toString());
        }

        return sw;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if(resultCode == RESULT_OK && requestCode == SELECT_MAP) {
            //인텐트에서 넘어온 데이터(위도, 경도 값, 주소명)로 갱신.
            latitude = result.getDoubleExtra("retLatitude",latitude);
            longitude = result.getDoubleExtra("retLongitude",longitude);
            addrName = result.getStringExtra("retAddrName");
            mTvLocation.setText(addrName.substring(4));
        }
        if(resultCode == RESULT_OK && requestCode == SELECT_IMAGE) {
            //갤러리에서 선택한 이미지 반환 값이 있을 경우
            Uri image = result.getData();
            photoUri = image.toString();
            System.out.println("사진경로 : " + photoUri);
        }

    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if(position>=0) {
            cid = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // Table 생성
    public void createTable(String tableName, String content) {
        try {
            String sql = "create table " + tableName + "("+content+")";
            db.execSQL(sql);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d("Lab sqlite", "error: " + e);
        }
    }
    // Table 삭제
    public void removeTable(String tableName) {
        String sql = "drop table " + tableName;
        db.execSQL(sql);
    }
    // Data 추가
    public void insertData(String tableName, String values) {
        String sql = "insert into " + tableName + " values(" + values + ");";
        db.execSQL(sql);
    }
    // Data 읽기(꺼내오기)
    public void selectData(String tableName, String pkName, int index) {
        String sql = "select * from " + tableName + " where " + pkName +" = " + index + ";";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if (result.moveToFirst()) {
            int id = result.getInt(0);
            String name = result.getString(1);
//            Toast.makeText(this, "index= " + id + " name=" + name, Toast.LENGTH_LONG).show();
            Log.d("lab_sqlite", "\"index= \" + id + \" name=\" + name ");
        }
        result.close();
    }
    // 모든 Data 읽기 - CATEGORY 테이블
    public void selectAllCategory() {
        String sql = "select * from CATEGORY;";
        Cursor results = db.rawQuery(sql, null);
        results.moveToFirst();

        while (!results.isAfterLast()) {
            int cid = results.getInt(0);
            String cname = results.getString(1);
            Log.d("lab_sqlite", "index= " + cid + " name=" + cname);

            categoryList.add(cname);
            results.moveToNext();
        }
        results.close();
    }

    // 모든 Data 읽기 - CATEGORY 테이블
    public void selectAllDo() {
        System.out.println("db읽기 시작");
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

    }

    // 테이블의 레코드가 비어있는지 여부 체크 후 boolean 리턴.
    public boolean checkData(String tableName, String pkName, int index) {
        boolean sw = false;
        String sql = "select * from " + tableName + " where " + pkName +" = " + index + ";";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if (result.moveToFirst()) {
            sw = true;
        }
        return sw;
    }

}
