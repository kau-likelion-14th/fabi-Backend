package likelion14th.lte.category.controller;

import likelion14th.lte.category.entity.Category;
import likelion14th.lte.category.repository.CategoryRepository;
import likelion14th.lte.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest {

    private CategoryRepository categoryRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        CategoryService service = new CategoryService(categoryRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(service)).build();
    }

    @Test
    void getCategoriesReturnsIdsAndNamesInRepositoryOrder() throws Exception {
        Category study = Category.create("공부");
        ReflectionTestUtils.setField(study, "id", 3L);
        Category exercise = Category.create("운동");
        ReflectionTestUtils.setField(exercise, "id", 1L);
        when(categoryRepository.findAllByOrderByCategoryNameAsc()).thenReturn(List.of(study, exercise));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("CATEGORY_2001"))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].categoryId").value(3))
                .andExpect(jsonPath("$.result[0].categoryName").value("공부"))
                .andExpect(jsonPath("$.result[1].categoryId").value(1))
                .andExpect(jsonPath("$.result[1].categoryName").value("운동"));

        verify(categoryRepository).findAllByOrderByCategoryNameAsc();
    }

    @Test
    void getCategoriesReturnsEmptyArrayWhenNoCategoriesExist() throws Exception {
        when(categoryRepository.findAllByOrderByCategoryNameAsc()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CATEGORY_2001"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isEmpty());
    }
}
