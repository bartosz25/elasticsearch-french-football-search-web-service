package com.waitingforcode.elasticsearch.config;

public final class SearchDictionary {

    public static final String GROUP_BY_SEASON = "group_by_season";
    public static final String GROUP_BY_ROUND = "group_by_round";
    public static final String GROUP_BY_TEAMS = "group_by_teams";
    public static final String GOALS = "goals";
    public static final String SEASON = "season";
    public static final String SCORES = "scores";
    public static final String PLACE = "place";
    public static final String NAME = "name";
    public static final String WINS = "wins";
    public static final String DRAWS = "draws";
    public static final String LOSSES = "losses";
    public static final String SCORED = "scored";
    public static final String CONCEDED = "conceded";
    public static final String TEAM = "team";
    public static final String POINTS = "points";
    public static final String GAMES = "games";

    // Filters, aggregations
    public static final String HOME_ADVERSARY_GOALS = "host_adversary_goals";
    public static final String AWAY_ADVERSARY_GOALS = "guest_adversary_goals";
    public static final String HOME_GOALS = "scores_home";
    public static final String AWAY_GOALS = "scores_away";

    // Used in queries
    public static final String HOST_TEAM = "hostTeam";
    public static final String GUEST_TEAM = "guestTeam";
    public static final String ROUND = "round";
    public static final String HOST_GOALS = "hostGoals";
    public static final String GUEST_GOALS = "guestGoals";

    private SearchDictionary() {
        // prevent init
    }

}
