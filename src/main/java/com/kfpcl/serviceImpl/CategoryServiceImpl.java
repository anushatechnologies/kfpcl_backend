package com.kfpcl.serviceImpl;

import com.kfpcl.dto.CategoryDto;
import com.kfpcl.entity.Category;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CategoryDto mapToDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getStatus().name()
        );
    }
}
