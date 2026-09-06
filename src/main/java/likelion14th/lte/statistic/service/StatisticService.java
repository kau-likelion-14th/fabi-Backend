package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.entity.TodoDate;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticService {
    private static final int STATISTIC_BATCH_SIZE = 500;

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public StatisticResponse getStatistic(Long userId) {
        User user = getUserOrThrow(userId);
        return StatisticResponse.from(user.getStatistic());
    }

    @Transactional
    public void updateStatistic(Long userId) {
        User user = getUserOrThrow(userId);
        Statistic statistic = user.getStatistic();
        LocalDate day = LocalDate.now().minusDays(1);

        List<TodoDate> dayTodoDates = todoDateRepository.findAllByTodo_User_IdAndDate(userId, day);
        boolean hasCompletedTodo = dayTodoDates.stream().anyMatch(TodoDate::isCompleted);
        boolean hasUncompletedTodo = dayTodoDates.stream().anyMatch(todoDate -> !todoDate.isCompleted());
        boolean success = hasCompletedTodo && !hasUncompletedTodo;

        statistic.updateStreak(success);
        if (success) {
            statistic.increaseWeekCount(day.getDayOfWeek());
        }

        LocalDate start = day.minusDays(30);
        List<TodoDate> monthTodoDates = todoDateRepository.findAllByTodo_User_IdAndDateBetween(userId, start, day);
        long completedCount = monthTodoDates.stream().filter(TodoDate::isCompleted).count();
        long uncompletedCount = monthTodoDates.size() - completedCount;
        statistic.updateMonthPercent(completedCount, uncompletedCount);
    }

    @Transactional
    public void updateAllStatistics() {
        int page = 0;
        Page<User> users;

        do {
            users = userRepository.findAll(PageRequest.of(page, STATISTIC_BATCH_SIZE));
            users.forEach(user -> updateStatistic(user.getId()));
            entityManager.flush();
            entityManager.clear();
            page++;
        } while (users.hasNext());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }
}
