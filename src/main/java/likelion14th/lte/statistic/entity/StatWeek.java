package likelion14th.lte.statistic.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stat_week")
public class StatWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private WeekEnum week;

    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statistic_id", nullable = false)
    private Statistic statistic;

    public static StatWeek create(Statistic statistic, WeekEnum week) {
        StatWeek statWeek = new StatWeek();
        statWeek.statistic = statistic;
        statWeek.week = week;
        statWeek.count = 0;
        return statWeek;
    }

    public void increaseCount() {
        this.count++;
    }
}
