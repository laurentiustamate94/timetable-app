package mobile.computing.laurentiu.timetableapp.SQLite;

public class ScheduleDay {
    private String weekdayName;
    private String courseName;
    private String shortCourseName;
    private ScheduleDayActivityType activityType;
    private ScheduleDayParityType parityType;
    private String location;
    private short startHour;
    private short endHour;

    public ScheduleDay(String weekdayName,
                       String courseName,
                       String shortCourseName,
                       ScheduleDayActivityType activityType,
                       ScheduleDayParityType parityType,
                       String location,
                       short startHour,
                       short endHour) {
        this.weekdayName = weekdayName;
        this.courseName = courseName;
        this.shortCourseName = shortCourseName;
        this.activityType = activityType;
        this.parityType = parityType;
        this.location = location;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public String getWeekDayName() {
        return weekdayName;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getShortCourseName() {
        return shortCourseName;
    }

    public ScheduleDayActivityType getActivityType() {
        return activityType;
    }

    public ScheduleDayParityType getParityType() {
        return parityType;
    }

    public String getLocation() {
        return location;
    }

    public short getStartHour() {
        return startHour;
    }

    public short getEndHour() {
        return endHour;
    }
}
