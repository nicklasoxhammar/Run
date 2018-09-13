package oxhammar.nicklas.run;

/**
 * Created by Nick on 2018-04-06.
 */

public class Constants {
    public interface ACTION {
        String LOCATION_ACTION = "oxhammar.nicklas.run.action.location";
        String STARTFOREGROUND_ACTION = "oxhammar.nicklas.run.action.startforeground";
        String STOPFOREGROUND_ACTION = "oxhammar.nicklas.run.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 666;
    }
}
