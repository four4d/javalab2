package com.example.kim.life;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity {

    private GoogleMap map;
    Button mBtReturn;
    Button mBtGohere;
    Button mBtFind;
    EditText mEtFind;
    TextView mTvHere;
    Context mContext;

    double latitude;
    double longitude;
    boolean autoSw = false;
    String addrName;

    Intent in;

    Geocoder gc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mBtReturn = (Button) findViewById(R.id.bt_return);
        mBtGohere = (Button) findViewById(R.id.bt_gohere);
        mBtFind = (Button) findViewById(R.id.bt_find);
        mEtFind = (EditText) findViewById(R.id.et_find);
        mTvHere = (TextView) findViewById(R.id.tx_here);
        mContext = this.getApplication().getApplicationContext();

        //Id가 map인 Fragment에서 구글맵을 가져와 map에 세팅.
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        //제가 테스트용으로 쓰는 스마트폰의 버젼이 2.3.6 진저브레드라고 FragmentManager가 지원하지 않아
        //검색하여 Support.v4라는 라이브러리가 있다는 것을 알게되었고 SupportFragmentManager를 활용하였습니다.

        //시스템 위치 서비스에 접근하는 객체 생성
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gc = new Geocoder(this, Locale.KOREAN);
        //위치가 변할 때 LocationManager로부터 공지를 받고, 해당하는 상황의 메소드를 실행하는 LocationListener객체 생성
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //위치가 변할 때, 맵 업데이트. 자동모드일때는 바로 갱신. 수동모드일 경우 아무동작하지 않음.
                if (autoSw) {
                    latitude = location.getLatitude();  //현재 위치 위도 값 세트.
                    longitude = location.getLongitude(); //현재 위치 경도 값 세트.
                    updateMap(latitude, longitude);
                    updateAddrText(latitude, longitude);
                }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                //provider의 상태가 변할 때 자동으로 호출 됨.
                alertStatus(provider);
            }

            public void onProviderEnabled(String provider) {
                //사용자에 의해 provider가 사용 가능하게 될 때 자동으로 호출 됨.
                alertProvider(provider);
            }

            public void onProviderDisabled(String provider) {
                //사용자에 의해 provider가 사용불가능하게 될 때 자동으로 호출 됨.
                checkProvider(provider);
            }
        };

        in = getIntent();
        addrName = in.getStringExtra("addrName");
        latitude = in.getDoubleExtra("latitude",latitude);
        longitude = in.getDoubleExtra("longitude",longitude);
        updateMap(latitude, longitude);
        mTvHere.setText(addrName);

        //위치 파악을 위한 provider를 NETWORK_PROVIDER 또는 GPS_PROVIDER로 직접 설정할 때는 아래 코드로.
        //String locationProvider = LocationManager.NETWORK_PROVIDER;

        //위치 파악을 위해 가장 적합한 provider를 찾아서 세팅.
        final String locationProvider = locationManager.getBestProvider(new Criteria(), true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);

        // 현재위치로 버튼 클릭 시 현재위치로 이동. 자동 위치 갱신 다시 On
        mBtGohere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSw = true;
                // 수동으로 좌표 구하기.
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                if (lastKnownLocation != null) {
                    latitude = lastKnownLocation.getLatitude();
                    longitude = lastKnownLocation.getLongitude();
                    updateMap(latitude, longitude);
                    updateAddrText(latitude, longitude);
                }
            }
        });

        // 주소명칭으로 찾기 버튼 클릭시 주소 또는 명칭 검색어로 좌표 검색
        mBtFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSw = false;
                String findStr = mEtFind.getText().toString();
                updateAddrText(findStr);
                updateMap(latitude, longitude);
            }
        });

        // 돌아가기 버튼 클릭시 현재 액티비티 종료하여 이전 화면으로.
        mBtReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrunResult();
            }
        });

    }

    private void retrunResult() {
        in = new Intent();
        in.putExtra("retLatitude",latitude);
        in.putExtra("retLongitude",longitude);
        in.putExtra("retAddrName",addrName);
        this.setResult(this.RESULT_OK,in);
        this.finish();
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

    /*주소 또는 명칭으로 좌표 값 받아오는 함수.
    * 파라미터:String findStr
    * 리턴값 : boolean 주소 존재 여부. 주소 있을 시 true.*/
    private boolean searchLocation(String findStr) {
        List<Address> addressList = null;
        boolean sw = false;

        try {
            addressList = gc.getFromLocationName(findStr, 1);

            if(addressList != null) {
                Address outAddr = addressList.get(0);
                int addrCount = outAddr.getMaxAddressLineIndex() + 1;
                StringBuffer outAddrStr = new StringBuffer();
                for (int k = 0; k < addrCount ; k++) {
                    outAddrStr.append(outAddr.getAddressLine(k));
                }
                addrName = outAddrStr.toString();
                latitude = outAddr.getLatitude();
                longitude = outAddr.getLongitude();
                sw = true;
            }
        }catch (IOException ex) {
            Log.d("Geocoding", "Exception : " + ex.toString());
        }
        return sw;
    }

    private void updateAddrText(double latitude, double longitude) {
        //좌표로 주소 탐색 실시. 주소가 있다면 주소를 화면에 표기. 없다면 위도,경도 표기.
        if (searchLocation(latitude, longitude)) {
            mTvHere.setText(addrName.substring(4));
        } else {
            mTvHere.setText("위도:" + latitude + "\n경도:" + longitude);
        }
    }

    private void updateAddrText(String findStr) {
        //주소,명칭으로 탐색 실시. 주소가 있다면 주소를 화면에 표기. 없다면 위도,경도 표기.
        if (searchLocation(findStr)) {
            mTvHere.setText(addrName.substring(4));
        } else {
            mTvHere.setText("위도:" + latitude + "\n경도:" + longitude);
        }
    }

    public void updateMap(double latitude, double longitude) {
        final LatLng LOC = new LatLng(latitude, longitude);  //해당 위도, 경도 값으로 좌표를 표현하는 객체 생성.

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LOC, 16)); //해당 좌표를 중심으로 하는 지도로 이동.
        Marker mk = map.addMarker(new MarkerOptions().position(LOC).title("현재위치"));
        mk.showInfoWindow();
    }

    public void checkProvider(String provider) {
        Toast.makeText(this, provider+"에 의한 위치서비스가 꺼져 있습니다. 켜주세요...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void alertProvider(String provider) {
        Toast.makeText(this, provider + "서비스가 켜졌습니다.", Toast.LENGTH_LONG).show();
    }

    public void alertStatus(String provider) {
        Toast.makeText(this, "위치서비스가 " + provider + "로 변경되었습니다.", Toast.LENGTH_LONG).show();
    }
}
