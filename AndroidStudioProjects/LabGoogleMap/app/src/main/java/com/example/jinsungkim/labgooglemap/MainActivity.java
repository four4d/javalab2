package com.example.jinsungkim.labgooglemap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {

    //static final LatLng SEOUL = new LatLng(37.56, 126.97);
    //실습 따라할 때 사용한 코드 부분은 주석처리 하였습니다.
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Id가 map인 Fragment에서 구글맵을 가져와 map에 세팅.
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        //제가 테스트용으로 쓰는 스마트폰의 버젼이 2.3.6 진저브레드라고 FragmentManager가 지원하지 않아
        //검색하여 Support.v4라는 라이브러리가 있다는 것을 알게되었고 SupportFragmentManager를 활용하였습니다.

        //시스템 위치 서비스에 접근하는 객체 생성
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //위치가 변할 때 LocationManager로부터 공지를 받고, 해당하는 상황의 메소드를 실행하는 LocationListener객체 생성
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //위치가 변할 때, 맵 업데이트
                updateMap(location);
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

        //위치 파악을 위한 provider를 NETWORK_PROVIDER 또는 GPS_PROVIDER로 직접 설정할 때는 아래 코드로.
        //String locationProvider = LocationManager.NETWORK_PROVIDER;

        //위치 파악을 위해 가장 적합한 provider를 찾아서 세팅.
        String locationProvider = locationManager.getBestProvider(new Criteria(), true);

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

        //Marker seoul = map.addMarker(new MarkerOptions().position(SEOUL).title("Seoul"));
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 15));
        //map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    public void updateMap(Location location) {
        double latitude = location.getLatitude();  //현재 위치 위도 값 세트.
        double longitude = location.getLongitude(); //현재 위치 경도 값 세트.
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
