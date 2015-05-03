package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.os.Handler;
import java.util.logging.LogRecord;

import pt.ulisboa.tecnico.cmov.airdesk.wifidirect.termite.util.AsyncService;

/**
 * Created by ashansa on 5/3/15.
 */
public class AirDeskService extends AsyncService {

    @Override
    public AsyncServiceInfo createHandler() {
        AsyncServiceInfo info = new AsyncServiceInfo();
        info.mHandler = new AirDeskServiceHandler();
        return info;
    }

    public void broadcastTagSubscription(String[] subscribedTags) {
        Intent intent = new Intent();
        intent.setAction(Constants.SUBSCRIBED_TO_TAGS);
        intent.putExtra(Constants.TAGS, subscribedTags);
        sendBroadcast(intent);
    }

    public class AirDeskServiceHandler extends Handler {

    }
}
