package likelion14th.lte.category.service;

import likelion14th.lte.category.dto.response.CategoryResponse;
import likelion14th.lte.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderByCategoryNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
