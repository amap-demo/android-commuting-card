package com.amap.demo.commutingcard.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.demo.commutingcard.R;
import com.amap.demo.commutingcard.util.AMapUtil;

public class BusView extends ConstraintLayout {

    private TextView busTimeDes;
    private TextView busLineDes;
    private TextView firstStationDes;
    private BusPath busPath;

    public BusView(Context context) {
        super(context);
    }

    public BusView(Context context, AttributeSet attrs) {
        super(context,attrs);
        initView(context);
    }

    public BusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.bus_layout, this);
        busTimeDes      = findViewById(R.id.bus_time_des);
        busLineDes      = findViewById(R.id.bus_line_des);
        firstStationDes = findViewById(R.id.first_station_des);
    }

    public void setPath(BusPath busPath) {
        if (busPath == null) {
            return;
        }
        this.busPath = busPath;
        String time = AMapUtil.times(busPath.getDuration() + System.currentTimeMillis()/1000);

        long hour = busPath.getDuration()/3600;
        long minute = (busPath.getDuration()%3600)/60;
        String formate = hour > 0 ? "全程%d小时%d分钟" : "全程%d分钟";
        String spendDuration = hour > 0 ? String.format(formate, hour, minute) : String.format(formate, minute);
        busTimeDes.setText(spendDuration + " 预计" + time + "到达");

        //duration.setText(spendDuration);
        busPath.getSteps();
        BusStep busStep = busPath.getSteps().get(0);
        RouteBusLineItem routeBusLineItem = busStep.getBusLines().get(0);
        busLineDes.setText(routeBusLineItem.getBusLineName());

        routeBusLineItem.getDepartureBusStation().getBusStationName();
        firstStationDes.setText(routeBusLineItem.getDepartureBusStation().getBusStationName());
        //busStep
    }
}
