package likelion14th.lte.category.config;

import likelion14th.lte.category.entity.Category;
import likelion14th.lte.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements ApplicationRunner {
    private static final List<String> DEFAULT_CATEGORY_NAMES = List.of("운동", "동아리", "공부", "집안일");

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String categoryName : DEFAULT_CATEGORY_NAMES) {
            categoryRepository.findByCategoryName(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.create(categoryName)));
        }
    }
}
