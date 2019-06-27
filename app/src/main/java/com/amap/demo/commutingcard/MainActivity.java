package com.amap.demo.commutingcard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;

import com.amap.api.services.route.DriveRouteResult;

import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.core.AMapException;
import com.amap.demo.commutingcard.overlay.BusRouteOverlay;
import com.amap.demo.commutingcard.overlay.DrivingRouteOverlay;

import com.amap.demo.commutingcard.util.ToastUtil;
import com.amap.api.services.route.DrivePath;
import com.amap.demo.commutingcard.view.BusView;
import com.amap.demo.commutingcard.view.DriveView;


public class MainActivity extends Activity implements RouteSearch.OnRouteSearchListener, View.OnClickListener{
    private AMap aMap;
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private final int ROUTE_TYPE_DRIVE = 1;
    private final int ROUTE_TYPE_BUS = 2;
    private LatLonPoint startPoint = new LatLonPoint(39.903588,116.47357);//起点，39.942295,116.335891
    private LatLonPoint endPoint = new LatLonPoint(39.993253,116.473195);//终点，39.995576,116.481288

    private Button changQueryTypeBtn;
    private Button chooseStartPointBtn;
    private Button chooseEndPointBtn;
    private Button gotoNaviBtn;
    private TextView startPointTv;
    private TextView endPointTv;
    private String currentCityName = "北京";
    private final String CHANGE_TO_DRIVE = "切为驾车";
    private final String CHANGE_TO_BUS = "切为公交";

    private DriveView driveView;
    private BusView busView;

    private int routeType = ROUTE_TYPE_DRIVE;
    public static final String PN_GAODE_MAP = "com.autonavi.minimap";// 高德地图包名
    private static final int REQUEST_CODE = 1;
    private static final String TYPE_START = "start";
    private static final String TYPE_END = "end";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mContext    = this.getApplicationContext();
        mapView     = (MapView) findViewById(R.id.map);
        driveView   = findViewById(R.id.drive_view);
        busView     = findViewById(R.id.bus_view);
        startPointTv= findViewById(R.id.start_point);
        endPointTv  = findViewById(R.id.end_point);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.getUiSettings().setZoomControlsEnabled(false);
        }

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        registerListener();

        changQueryTypeBtn   = findViewById(R.id.change_query_type);
        chooseEndPointBtn   = findViewById(R.id.choose_end_point);
        chooseStartPointBtn = findViewById(R.id.choose_start_point);
        gotoNaviBtn         = findViewById(R.id.drive_go);


        changQueryTypeBtn.setOnClickListener(this);
        chooseStartPointBtn.setOnClickListener(this);
        chooseEndPointBtn.setOnClickListener(this);
        gotoNaviBtn.setOnClickListener(this);

        changQueryTypeBtn.setText(CHANGE_TO_BUS);
        routeType = ROUTE_TYPE_DRIVE;
        startPointTv.setText("起点:国家广告产业园");
        endPointTv.setText("终点:首开广场");
    }

    /**
     * 注册监听
     */
    private void registerListener() {


    }

    /**
     * 开始搜索路径规划方案
     */

    public void searchRouteResult(int routeType, int mode) {
        if (startPoint == null) {
            ToastUtil.show(mContext, "定位中，稍后再试...");
            return;
        }
        if (endPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                startPoint, endPoint);
        if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        } else if (routeType == ROUTE_TYPE_BUS) {
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, mode,
                    currentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mDriveRouteResult = result;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    if(drivePath == null) {
                        return;
                    }
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            mContext, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (changQueryTypeBtn != null) {
                                changQueryTypeBtn.setText(CHANGE_TO_BUS);
                            }
                            if (driveView != null) {
                                driveView.setVisibility(View.VISIBLE);
                                driveView.setPath(drivePath);
                            }

                            if(busView != null) {
                                busView.setVisibility(View.GONE);
                            }
                        }
                    });

                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }


    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    final BusPath busPath = result.getPaths().get(0);
                    if (busPath == null) {
                        return;
                    }

                    BusRouteOverlay busrouteOverlay = new BusRouteOverlay(this, aMap,
                            result.getPaths().get(0), result.getStartPos(),
                            result.getTargetPos());
                    busrouteOverlay.removeFromMap();
                    busrouteOverlay.addToMap();
                    busrouteOverlay.zoomToSpan();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (changQueryTypeBtn != null) {
                                changQueryTypeBtn.setText(CHANGE_TO_DRIVE);
                            }
                            if(busView != null) {
                                busView.setVisibility(View.VISIBLE);
                                busView.setPath(busPath);
                            }
                            if (driveView != null) {
                                driveView.setVisibility(View.GONE);
                            }
                        }
                    });

                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }



    @Override
    public void onWalkRouteSearched(WalkRouteResult var1, int var2) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult var1, int var2) {

    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_query_type:
                routeType = routeType  == ROUTE_TYPE_DRIVE ? ROUTE_TYPE_BUS : ROUTE_TYPE_DRIVE;

                searchRouteResult(routeType, RouteSearch.DrivingDefault);
                break;
            case R.id.choose_start_point:
                choosePoint(TYPE_START);
                break;
            case R.id.choose_end_point:
                choosePoint(TYPE_END);
                break;
            case R.id.drive_go:
                gotoNavi();
                break;

            default:
                break;
        }
    }

    private void gotoNavi() {

        String uriString = null;
        StringBuilder builder = new StringBuilder("amapuri://route/plan?sourceApplication=maxuslife");
        if (startPoint != null) {
            builder.append("&sname=").append("")
                    .append("&slat=").append(startPoint.getLatitude())
                    .append("&slon=").append(startPoint.getLongitude());
        }
        builder.append("&dlat=").append(endPoint.getLatitude())
                .append("&dlon=").append(endPoint.getLongitude())
                .append("&dname=").append("")
                .append("&dev=0")
                .append("&t=0");
        uriString = builder.toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(PN_GAODE_MAP);
        intent.setData(Uri.parse(uriString));
        this.startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {//如果结果码等于RESULT_OK
            if (requestCode == REQUEST_CODE) {

                String place = data.getExtras().getString("place");
                double lon = data.getExtras().getDouble("lon");
                double lat = data.getExtras().getDouble("lat");
                String type = data.getExtras().getString("type");
                if (TYPE_START.equals(type)) {

                    startPoint.setLatitude(lat);
                    startPoint.setLongitude(lon);

                    startPointTv.setText("起点:" + place);

                } else if (TYPE_END.equals(type)) {
                    endPoint.setLatitude(lat);
                    endPoint.setLongitude(lon);
                    endPointTv.setText("终点:" + place);
                }

                searchRouteResult(routeType, RouteSearch.DrivingDefault);
            }
        }
    }

    private void choosePoint (String type) {
        Intent intent = new Intent(this, GeocodeSearcheActivity.class);
        intent.putExtra("type", type);
        startActivityForResult(intent, REQUEST_CODE);
    }

}
