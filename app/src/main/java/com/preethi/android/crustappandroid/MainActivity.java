package com.preethi.android.crustappandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, BeaconConsumer, RangeNotifier{
    private Integer mClosestBeacon = 0;
    private Double mClosestBeaconDistance = Double.MAX_VALUE;
    private BeaconManager mBeaconManager;
    private TextToSpeech engine;
    private Integer mCurrentDestinationBusStop = 0;
    private double pitch=1.3f;
    private double speed=0.8f;
    int cIndex = 0;
    String text = "Welcome to V T A";
    String[] mainScreenText = {"Welcome to V T A"," Select the Bus No","Bus Number 60","Bus Number 181","Bus Number 66"};
    private APIRequest.APIResponseHandler mClosestBusStopResponseHandler;
    private APIRequest.APIResponseHandler mPathResponseHandler;

    private static class ClosestBusStopResponseHandler implements APIRequest.APIResponseHandler {
        private WeakReference<MainActivity> mMainActivityRef;

        public ClosestBusStopResponseHandler(MainActivity activity) {
            mMainActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleResponse(String response) {
            if (mMainActivityRef.get() == null) {
                return;
            }
            if (response.equals(APIRequest.ERROR)) {
                Toast.makeText(mMainActivityRef.get(), "Some error occurred", Toast.LENGTH_SHORT).show();
                return;
            } else if (mMainActivityRef.get().mCurrentDestinationBusStop == 0) {
                Toast.makeText(mMainActivityRef.get(), "Destination Bus Stop not set", Toast.LENGTH_SHORT).show();
                return;
            }
            String sourceBusStop = response.replace("\n", "");
            new PathRequest(sourceBusStop,
                    mMainActivityRef.get().mCurrentDestinationBusStop.toString(),
                    mMainActivityRef.get().mPathResponseHandler).execute();
        }
    }

    private static class PathResponseHandler implements APIRequest.APIResponseHandler {
        private WeakReference<MainActivity> mMainActivityRef;

        public PathResponseHandler(MainActivity activity) {
            mMainActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleResponse(String response) {
            if (mMainActivityRef.get() == null) {
                return;
            }
            Toast.makeText(mMainActivityRef.get(), response, Toast.LENGTH_SHORT).show();
            mMainActivityRef.get().speakBusInformation(response);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        verifyLocationsPermissions(this);

        mClosestBusStopResponseHandler = new ClosestBusStopResponseHandler(this);
        mPathResponseHandler = new PathResponseHandler(this);

        if (savedInstanceState != null) {
            mClosestBeacon = savedInstanceState.getInt("closest_beacon");
            mClosestBeaconDistance = savedInstanceState.getDouble("closest_beacon_distance");
            mCurrentDestinationBusStop = savedInstanceState.getInt("current_destination");
        }
    }

    public static void verifyLocationsPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        mBeaconManager.bind(this);
        Log.d("In on Resume", "Speech");
        engine = new TextToSpeech(this, this);
       // speakBusInformation();
    }

    @Override
    protected void onPause(){

        if(engine != null)
        {
            engine.stop();
            engine.shutdown();
        }

        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("closest_beacon", mClosestBeacon);
        outState.putDouble("closest_beacon_distance", mClosestBeaconDistance);
        outState.putInt("current_destination", mCurrentDestinationBusStop);
    }

    @Override
    public void onInit(int status) {
        Log.d("Speech", "OnInit - Status ["+status+"]");

        if (status == TextToSpeech.SUCCESS) {
            Log.d("Speech", "Success!");
            engine.setLanguage(Locale.US);
            engine.setPitch((float) pitch);
            engine.setSpeechRate((float) speed);
            speech();

        }
    }

    private void speakBusInformation(String speakNow) {

           // String speakNow = mainScreenText[cIndex];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21(speakNow);
            } else {
                ttsUnder20(speakNow);
            }

        }


    public void bus1Action(View view) {
        if (mClosestBeacon == 0) {
            speakBusInformation("Cannot Detect Any Beacon Close to you");
            return;
        }
        mCurrentDestinationBusStop = 73;
        speakBusInformation("73");
        new ClosestBusStopRequest(mClosestBeacon.toString(), mClosestBusStopResponseHandler).execute();
        //speakBusInformation("Bus number 73 San Jose");
    }

    public void bus2Action(View view) {
        if (mClosestBeacon == 0) {
            speakBusInformation("Cannot Detect Any Beacon Close to you");
            return;
        }
        speakBusInformation("323");
        mCurrentDestinationBusStop = 323;
        new ClosestBusStopRequest(mClosestBeacon.toString(), mClosestBusStopResponseHandler).execute();
        //speakBusInformation("Bus number 323 Fremont");
    }

    public void bus3Action(View view) {
        if (mClosestBeacon == 0) {
            speakBusInformation("Cannot Detect Any Beacon Close to you");
            return;
        }
        speakBusInformation("66");
        mCurrentDestinationBusStop = 66;
        new ClosestBusStopRequest(mClosestBeacon.toString(), mClosestBusStopResponseHandler).execute();
        //speakBusInformation("Bus number 66 Great Mall");
    }

    private void speech() {
        engine.setPitch((float) pitch);
        engine.setSpeechRate((float) speed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21("Welcome to V T A Select the Bus Number");
        } else {
            ttsUnder20("Welcome to V T A Select the Bus Number");
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    public void onDestroy() {
        if (engine != null) {
            engine.stop();
            engine.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon: beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                Identifier namespaceId = beacon.getId1();
                Identifier instanceId = beacon.getId2();
                Log.d("RangingActivity", "I see a beacon transmitting namespace id: " + namespaceId +
                        " and instance id: " + instanceId +
                        " approximately " + beacon.getDistance() + " meters away.");
                String instanceHexValue = instanceId.toHexString();
                if (beacon.getDistance() < mClosestBeaconDistance) {
                    mClosestBeacon = Integer.parseInt(instanceHexValue.substring(instanceHexValue.length() - 4, instanceHexValue.length()), 16);
                    mClosestBeaconDistance = beacon.getDistance();
                    mBeaconManager.setRangeNotifier(null);
                }
            }
        }
    }
}
