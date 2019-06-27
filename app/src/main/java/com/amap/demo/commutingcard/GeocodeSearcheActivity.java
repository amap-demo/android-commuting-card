package com.amap.demo.commutingcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;


import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.demo.commutingcard.util.AMapUtil;
import com.amap.demo.commutingcard.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class GeocodeSearcheActivity extends Activity implements GeocodeSearch.OnGeocodeSearchListener, TextWatcher, Inputtips.InputtipsListener, View.OnClickListener {
    private String keyWord = "";// 要输入的poi搜索关键字
    private GeocodeSearch geocoderSearch;
    private AutoCompleteTextView searchText;// 输入搜索关键字
    private Button go;
    private String type = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_search);
        searchText = (AutoCompleteTextView) findViewById(R.id.keyWord);
        go         = findViewById(R.id.go);
        searchText.addTextChangedListener(this);// 添加文本输入框监听事件
        go.setOnClickListener(this);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        init();
    }

    /**
     * 开始进行poi搜索
     */
    protected void init() {
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    public void getLatlon(final String name) {

        GeocodeQuery query = new GeocodeQuery(name, "010");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);

                Intent intent = new Intent();
                intent.putExtra("place", result.getGeocodeQuery().getLocationName());
                intent.putExtra("lon", address.getLatLonPoint().getLongitude());
                intent.putExtra("lat", address.getLatLonPoint().getLatitude());
                intent.putExtra("type", type);
                setResult(RESULT_OK, intent);//设置resultCode
                finish();
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, "北京");
            Inputtips inputTips = new Inputtips(GeocodeSearcheActivity.this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }
    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.route_inputs, listString);
            searchText.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }

    @Override
    public void onClick(View v) {
        keyWord = AMapUtil.checkEditText(searchText);
        getLatlon(keyWord);
    }
}
