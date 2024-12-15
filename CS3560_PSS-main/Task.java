import java.util.Scanner;

public class Task {
    public static Scanner scanner = new Scanner(System.in);

    String Name;
    String Type;
    int Date;
    float StartTime;
    float Duration;

    public Task(String name, String type, int date, float time, float duration) {
        Name = name;
        Type = type;
        Date = date;
        StartTime = time;
        Duration = duration;
    }

    // Getters
    public String getName() {
        return Name;
    }

    public String getType() {
        return Type;
    }

    public int getDate() {
        return Date;
    }

    public float getStartTime() {
        return StartTime;
    }

    public float getDuration() {
        return Duration;
    }

    // Setters
    public void setName(String Name) {
        this.Name = Name;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public void setDate(int Date) {
        this.Date = Date;
    }

    public void setStartTime(float StartTime) {
        this.StartTime = StartTime;
    }

    public void setDuration(float Duration) {
        this.Duration = Duration;
    }
}

