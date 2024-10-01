/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.location.gps.gnsslogger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A map fragment to show the computed least square position and the device computed position on
 * Google map.
 */
public class TencentMapFragment extends Fragment implements TencentLocationListener {
  private static final String TAG = "TencentMapFragment";
  private View rootView;
  private TencentLocationManager mLocationManager; // 声明一个腾讯定位管理器对象
  private com.tencent.tencentmap.mapsdk.maps.MapView mMapView; // 声明一个地图视图对象
  private TencentMap mTencentMap; // 声明一个腾讯地图对象
  private boolean isFirstLoc = true; // 是否首次定位

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.tencent_map_fragment, container, false);
    initLocation(); // 初始化定位服务
    initView(); // 初始化视图

    return rootView;
  }
  // 初始化视图
  private void initView() {
    RadioGroup rg_type = rootView.findViewById(R.id.rg_type);
    rg_type.setOnCheckedChangeListener((group, checkedId) -> {
      if (checkedId == R.id.rb_common) {
        mTencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL); // 设置普通地图
      } else if (checkedId == R.id.rb_satellite) {
        mTencentMap.setMapType(TencentMap.MAP_TYPE_SATELLITE); // 设置卫星地图
      }
    });
    CheckBox ck_traffic = rootView.findViewById(R.id.ck_traffic);
    ck_traffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
      mTencentMap.setTrafficEnabled(isChecked); // 是否显示交通拥堵状况
    });
  }

  // 初始化定位服务
  private void initLocation() {
    mMapView = rootView.findViewById(R.id.mapView);
    mTencentMap = mMapView.getMap(); // 获取腾讯地图对象
    mLocationManager = TencentLocationManager.getInstance(getContext());
    // 创建腾讯定位请求对象
    TencentLocationRequest request = TencentLocationRequest.create();
    request.setInterval(30000).setAllowGPS(true);
    request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);
    mLocationManager.requestLocationUpdates(request, this); // 开始定位监听
  }

  @Override
  public void onLocationChanged(TencentLocation location, int resultCode, String resultDesc) {
    if (resultCode == TencentLocation.ERROR_OK) { // 定位成功
      if (location != null && isFirstLoc) { // 首次定位
        isFirstLoc = false;
        // 创建一个经纬度对象
        com.tencent.tencentmap.mapsdk.maps.model.LatLng latLng = new com.tencent.tencentmap.mapsdk.maps.model.LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate update = com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mTencentMap.moveCamera(update); // 把相机视角移动到指定地点
        // 从指定图片中获取位图描述
        BitmapDescriptor bitmapDesc = com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
                .fromResource(R.drawable.icon_locate);
        com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions ooMarker = new com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions(latLng).draggable(false) // 不可拖动
                .visible(true).icon(bitmapDesc).snippet("这是您的当前位置");
        mTencentMap.addMarker(ooMarker); // 往地图添加标记
      }
    } else { // 定位失败
      Log.d(TAG, "定位失败，错误代码为"+resultCode+"，错误描述为"+resultDesc);
    }
  }

  @Override
  public void onStatusUpdate(String s, int i, String s1) {}


  @Override
  public void onResume() {
    super.onResume();
    mMapView.onResume();
    if (mTencentMap != null) {
      mTencentMap.clear();
    }
  }

  @Override
  public void onPause() {
    mMapView.onPause();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    mMapView.onDestroy();
    super.onDestroy();
  }

}
