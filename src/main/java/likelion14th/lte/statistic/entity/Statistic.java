package likelion14th.lte.statistic.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "statistic")
public class Statistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statisticId;

    private int streak;

    private int monthPercent;

    @OneToMany(mappedBy = "statistic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatWeek> statWeeks = new ArrayList<>();

    public static Statistic create() {
        Statistic statistic = new Statistic();
        statistic.streak = 0;
        statistic.monthPercent = 0;
        statistic.initializeWeeks();
        return statistic;
    }

    private void initializeWeeks() {
        for (WeekEnum week : WeekEnum.values()) {
            statWeeks.add(StatWeek.create(this, week));
        }
    }

    public WeekEnum getMostTodoWeek() {
        return statWeeks.stream()
                .max(Comparator.comparingInt(StatWeek::getCount))
                .map(StatWeek::getWeek)
                .orElse(WeekEnum.MON);
    }

    public void updateStreak(boolean success) {
        this.streak = success ? this.streak + 1 : 0;
    }

    public void increaseWeekCount(DayOfWeek dayOfWeek) {
        statWeeks.stream()
                .filter(statWeek -> statWeek.getWeek().toDayOfWeek() == dayOfWeek)
                .findFirst()
                .ifPresent(StatWeek::increaseCount);
    }

    public void updateMonthPercent(long completedCount, long uncompletedCount) {
        long totalCount = completedCount + uncompletedCount;
        this.monthPercent = totalCount == 0 ? 0 : (int) (completedCount * 100 / totalCount);
    }
}
