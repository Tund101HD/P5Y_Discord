package me.tund.database;

public class SquadMember {

        private int user_no = 0;
        private long discord_id = 0;
        private String in_game_name = "no_name";
        private boolean[] brs = {false, false, false, false, false, false, false, false, false, false};
        private String preferred_unit = "both";
        private double preferred_br = 13.0d;
        private int activity = 0;
        private double kd = 0.1f;
        private boolean replace = true;
        private int trainings = 0;
        private int cookies = 0;


        public SquadMember(int user_no, long discord_id, String in_game_name, boolean[] brs, String preferred_unit, double preferred_br, int activity, int trainings, int cookies, double kd, boolean replace) {
            this.user_no = user_no;
            this.discord_id = discord_id;
            this.in_game_name = in_game_name;
            this.brs = brs;
            this.preferred_unit = preferred_unit;
            this.preferred_br = preferred_br;
            this.activity = activity;
            this.kd = kd;
            this.replace = replace;
            this.trainings = trainings;
            this.cookies = cookies;
        }


        public SquadMember() {}




    public String getPreferred_unit() {
        return preferred_unit;
    }

    public void setPreferred_unit(String preferred_unit) {
        this.preferred_unit = preferred_unit;
    }

    public double getPreferred_br() {
        return preferred_br;
    }

    public void setPreferred_br(float preferred_br) {
        this.preferred_br = preferred_br;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public double getKd() {
        return kd;
    }

    public void setKd(float kd) {
        this.kd = kd;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public int getTrainings() {
        return trainings;
    }

    public void setTrainings(int trainings) {
        this.trainings = trainings;
    }

    public int getCookies() {
        return cookies;
    }

    public void setCookies(int cookies) {
        this.cookies = cookies;
    }

    public int getUser_no() {
        return user_no;
    }

    public void setUser_no(int user_no) {
        this.user_no = user_no;
    }

    public long getDiscord_id() {
        return discord_id;
    }

    public void setDiscord_id(long discord_id) {
        this.discord_id = discord_id;
    }

    public boolean[] getBrs() {
        return brs;
    }

    public void setBrs(boolean[] brs) {
        this.brs = brs;
    }

    public String getIn_game_name() {
        return in_game_name;
    }

    public void setIn_game_name(String in_game_name) {
        this.in_game_name = in_game_name;
    }
}
