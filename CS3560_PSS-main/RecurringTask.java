import java.util.ArrayList;
import java.util.List;

public class RecurringTask extends Task {
    int EndDate;
    int Frequency;
    int StartDate;

    public RecurringTask(String name, String type, int startDate, float time, float duration, int endDate, int frequency) {
        super(name, type, startDate, time, duration);
        EndDate = endDate;
        Frequency = frequency;
        StartDate = startDate;
    }

    public int getEndDate() {
        return EndDate;
    }

    public void setEndDate(int endDate) {
        this.EndDate = endDate;
    }

    public int getFrequency() {
        return Frequency;
    }

    public void setFrequency(int frequency) {
        this.Frequency = frequency;
    }

    public List<Integer> getAllOccurrenceDates() {
        List<Integer> occurrenceDates = new ArrayList<>();
        int currentDate = StartDate;

        // Generate occurrence dates
        while (currentDate <= EndDate) {
            occurrenceDates.add(currentDate);
            currentDate = getNextDate(currentDate);
        }

        return occurrenceDates;
    }

    private int getNextDate(int currentDate) {
        // Extract year, month, and day components from currentDate
        int year = currentDate / 10000;
        int month = (currentDate % 10000) / 100;
        int day = currentDate % 100;

        // Increment the date by the specified amount
        day += Frequency;
        while (day > daysInMonth(year, month)) {
            day = day - daysInMonth(year, month);
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }

        // Recreate the integer representation of the next date
        return year * 10000 + month * 100 + day;
    }

    private int daysInMonth(int year, int month) {
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
            default:
                return 31;
        }
    }
}


