package com.preethi.android.crustappandroid;

public class PathRequest extends APIRequest {
    private String mBusStop1;
    private String mBusStop2;
    private APIResponseHandler mHandler;

    public PathRequest(String busStop1, String busStop2, APIResponseHandler handler) {
        mBusStop1 = busStop1;
        mBusStop2 = busStop2;
        mHandler = handler;
    }

    @Override
    protected String getUrl() {
        return "http://ec2-54-84-146-121.compute-1.amazonaws.com:8080/beacons/crust/stops/" + mBusStop1 + "/" + mBusStop2;
    }

    @Override
    protected APIResponseHandler getResponseHandler() {
        return mHandler;
    }
}
