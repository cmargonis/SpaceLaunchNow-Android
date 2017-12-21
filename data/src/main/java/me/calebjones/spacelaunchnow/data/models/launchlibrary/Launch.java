
package me.calebjones.spacelaunchnow.data.models.launchlibrary;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import me.calebjones.spacelaunchnow.data.models.realm.RealmStr;

public class Launch extends RealmObject {

    private String name;
    private Date windowstart;
    private Date windowend;
    private Date net;
    private String holdreason;
    private String failreason;
    private String hashtag;
    private String vidURL;
    private RealmList<RealmStr> vidURLs;
    private RealmList<RealmStr> infoURLs;

    @PrimaryKey
    private Integer id;
    private Integer probability;
    private Integer wsstamp;
    private Integer westamp;
    private Integer netstamp;
    private Integer launchTimeStamp;
    private Integer locationid;

    private long widgetRefresh;

    private Integer status;

    private Integer inhold;
    private Integer tbdtime;
    private Integer tbddate;
    private Integer eventID;

    private Location location;
    private LSP lsp;
    private Rocket rocket;

    private Date startDate;
    private Date endDate;

    private boolean notifiable = false;
    private boolean userToggledNotifiable = false;

    private boolean isNotifiedDay = false;
    private boolean isNotifiedHour = false;
    private boolean isNotifiedTenMinute = false;
    private boolean isNextLaunch = false;

    private boolean syncCalendar = false;
    private boolean userToggledCalendar = false;

    public boolean isUserToggledNotifiable() {
        return userToggledNotifiable;
    }

    public void setUserToggledNotifiable(boolean userToggledNotifiable) {
        this.userToggledNotifiable = userToggledNotifiable;
    }

    public boolean isNotifiable() {
        return notifiable;
    }

    public void setNotifiable(boolean notifiable) {
        this.notifiable = notifiable;
    }

    public boolean isUserToggledCalendar() {
        return userToggledCalendar;
    }

    public void setUserToggledCalendar(boolean userToggledCalendar) {
        this.userToggledCalendar = userToggledCalendar;
    }

    private RealmList<Mission> missions;

    public long getWidgetRefresh() {
        return widgetRefresh;
    }

    public void setWidgetRefresh(long widgetRefresh) {
        this.widgetRefresh = widgetRefresh;
    }

    public boolean isNextLaunch() {
        return isNextLaunch;
    }

    public void setNextLaunch(boolean nextLaunch) {
        isNextLaunch = nextLaunch;
    }

    public boolean isNotifiedDay() {
        return isNotifiedDay;
    }

    public void setNotifiedDay(boolean notifiedDay) {
        isNotifiedDay = notifiedDay;
    }

    public boolean getIsNotifiedTenMinute() {
        return isNotifiedTenMinute;
    }

    public void setIsNotifiedTenMinute(boolean isNotifiedTenMinute) {
        this.isNotifiedTenMinute = isNotifiedTenMinute;
    }

    public boolean getIsNotifiedHour() {
        return isNotifiedHour;
    }

    public void setIsNotifiedHour(boolean isNotifiedHour) {
        this.isNotifiedHour = isNotifiedHour;
    }

    public boolean getIsNotifiedDay() {
        return isNotifiedDay;
    }

    public void setIsNotifiedDay(boolean isNotifiedDay) {
        this.isNotifiedDay = isNotifiedDay;
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer calendarID) {
        this.eventID = calendarID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getFailreason() {
        return failreason;
    }

    public void setFailreason(String failreason) {
        this.failreason = failreason;
    }

    public String getHoldreason() {
        return holdreason;
    }

    public void setHoldreason(String holdreason) {
        this.holdreason = holdreason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getWindowstart() {
        return windowstart;
    }

    public void setWindowstart(Date windowstart) {
        this.windowstart = windowstart;
    }

    public Date getWindowend() {
        return windowend;
    }

    public void setWindowend(Date windowend) {
        this.windowend = windowend;
    }

    public Date getNet() {
        return net;
    }

    public void setNet(Date net) {
        this.net = net;
    }

    public Integer getWsstamp() {
        return wsstamp;
    }

    public void setWsstamp(Integer wsstamp) {
        this.wsstamp = wsstamp;
    }

    public Integer getWestamp() {
        return westamp;
    }

    public void setWestamp(Integer westamp) {
        this.westamp = westamp;
    }

    public Integer getNetstamp() {
        return netstamp;
    }

    public void setNetstamp(Integer netstamp) {
        this.netstamp = netstamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getInhold() {
        return inhold;
    }

    public void setInhold(Integer inhold) {
        this.inhold = inhold;
    }

    public Integer getTbdtime() {
        return tbdtime;
    }

    public void setTbdtime(Integer tbdtime) {
        this.tbdtime = tbdtime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Rocket getRocket() {
        return rocket;
    }

    public void setRocket(Rocket rocket) {
        this.rocket = rocket;
    }

    public RealmList<Mission> getMissions() {
        return missions;
    }

    public void setMissions(RealmList<Mission> missions) {
        this.missions = missions;
    }

    public String getVidURL() {
        return vidURL;
    }

    public void setVidURL(String vidURL) {
        this.vidURL = vidURL;
    }

    public RealmList<RealmStr> getVidURLs() {
        return vidURLs;
    }

    public void setVidURLs(RealmList<RealmStr> mVidURls) {
        this.vidURLs = mVidURls;
    }

    public RealmList<RealmStr> getInfoURLs() {
        return infoURLs;
    }

    public void setInfoURLs(RealmList<RealmStr> infoURLs) {
        this.infoURLs = infoURLs;
    }

    public boolean syncCalendar() {
        return syncCalendar;
    }

    public void setSyncCalendar(boolean syncCalendar) {
        this.syncCalendar = syncCalendar;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date date) {
        this.startDate = date;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void resetNotifiers() {

        isNotifiedDay = false;
        isNotifiedHour = false;
        isNotifiedTenMinute = false;
    }

    public Integer getLaunchTimeStamp() {
        return launchTimeStamp;
    }

    public void setLaunchTimeStamp(Integer launchTimeStamp) {
        this.launchTimeStamp = launchTimeStamp;
    }

    public int getLocationid() {
        return locationid;
    }

    public void setLocationid(int locationid) {
        this.locationid = locationid;
    }

    public String getUrl() {
        return String.format("https://spacelaunchnow.me/launch/%s/", id);
    }

    public LSP getLsp() {
        return lsp;
    }

    public void setLsp(LSP lsp) {
        this.lsp = lsp;
    }

    public Integer getTbddate() {
        return tbddate;
    }

    public void setTbddate(Integer tbddate) {
        this.tbddate = tbddate;
    }
}
