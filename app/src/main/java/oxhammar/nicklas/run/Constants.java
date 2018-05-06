package oxhammar.nicklas.run;

/**
 * Created by Nick on 2018-04-06.
 */

public class Constants {
    public interface ACTION{
        public static String LOCATION_ACTION = "oxhammar.nicklas.run.action.location";
        public static String STARTFOREGROUND_ACTION = "oxhammar.nicklas.run.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "oxhammar.nicklas.run.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 666;
    }
}
