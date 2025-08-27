package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.ChangeRequestCountsDto;
import com.iubh.quizbackend.api.dto.ChoiceQuestionDto;
import com.iubh.quizbackend.api.dto.ModuleDetailDto;
import com.iubh.quizbackend.api.dto.ModuleListItemDto;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.ChoiceQuestionMapper;
import com.iubh.quizbackend.mapper.ModuleMapper;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.UserRepository;
import com.iubh.quizbackend.repository.specification.ModuleSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final ModuleMapper moduleMapper;
    private final ChoiceQuestionRepository choiceQuestionRepository; // Inject new repository
    private final ChoiceQuestionMapper choiceQuestionMapper; // Inject question mapper


    /**
     * Gets a paginated list of all modules, with a flag indicating if the current user follows each one.
     */
    @Transactional(readOnly = true)
    public Page<ModuleListItemDto> getAllModulesForUser(User currentUser, Pageable pageable) {
        Page<Module> modulesPage = moduleRepository.findAll(pageable);
        Set<UUID> followedModuleIds = getFollowedModuleIds(currentUser);

        return modulesPage.map(module -> {
            ModuleListItemDto dto = moduleMapper.toListItemDto(module);
            dto.setIsFollowed(followedModuleIds.contains(module.getId()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<ModuleListItemDto> searchModules(String title, Boolean isFollowed, User currentUser, Pageable pageable) {
        // Start with a base specification that does nothing (returns all)
        Specification<Module> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (title != null && !title.isBlank()) {
            spec = spec.and(ModuleSpecifications.titleContains(title));
        }

        if (isFollowed != null) {
            if (isFollowed) {
                spec = spec.and(ModuleSpecifications.isFollowedBy(currentUser.getId()));
            } else {
                spec = spec.and(ModuleSpecifications.isNotFollowedBy(currentUser.getId()));
            }
        }
        Page<Module> modulesPage = moduleRepository.findAll(spec, pageable);

        // Map to DTOs and set the 'isFollowed' flag for the response
        Set<UUID> followedModuleIds = getFollowedModuleIds(currentUser);
        return modulesPage.map(module -> {
            ModuleListItemDto dto = moduleMapper.toListItemDto(module);
            dto.setIsFollowed(followedModuleIds.contains(module.getId()));
            return dto;
        });
    }

    /**
     * Gets a paginated list of modules that the current user is following.
     */
    @Transactional(readOnly = true)
    public Page<ModuleListItemDto> getFollowedModules(User currentUser, Pageable pageable) {
        Page<Module> modulesPage = moduleRepository.findByFollowers_Id(currentUser.getId(), pageable);
        return modulesPage.map(module -> {
            ModuleListItemDto dto = moduleMapper.toListItemDto(module);
            dto.setIsFollowed(true); // By definition, all modules in this list are followed
            return dto;
        });
    }

    /**
     * Toggles the follow status of a module for the current user.
     * If the user follows the module, they will unfollow it, and vice-versa.
     *
     * @return The new follow status (true if now following, false otherwise).
     */
    @Transactional
    public boolean toggleFollow(UUID moduleId, User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + currentUser.getId()));

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + moduleId));

        // Use the Set's efficient 'contains' check. This works because Module has a proper equals/hashCode.
        if (user.getFollowedModules().contains(module)) {
            user.unfollowModule(module);
            return false; // User is no longer following
        } else {
            user.followModule(module);
            return true; // User is now following
        }
    }

    /**
     * Gets a detailed view of a single module, including a paginated list of its questions.
     *
     * @param moduleId    The ID of the module to retrieve.
     * @param currentUser The currently authenticated user, for context (e.g., isFollowed).
     * @param pageable    Pagination information for the questions list.
     * @return A detailed DTO of the module.
     */
    @Transactional(readOnly = true)
    public ModuleDetailDto getModuleDetailsById(UUID moduleId, User currentUser, Pageable pageable) {
        // 1. Fetch the module entity
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + moduleId));

        // 2. Fetch the paginated list of questions for this module
        Page<ChoiceQuestion> questionsPage = choiceQuestionRepository.findByModule_Id(moduleId, pageable);

        // 3. Map the page of question entities to a page of DTOs
        Page<ChoiceQuestionDto> questionsDtoPage = questionsPage.map(e -> {

           log.info(e.getAnswers().size() + " Entity : answers found");
            ChoiceQuestionDto dto = choiceQuestionMapper.toDto(e);
            log.info(dto.getAnswers().size() + " Dto :  answers found");
            dto.setChangeRequestCounts(
                    ChangeRequestCountsDto.builder()
                            .total(e.getTotalChangeRequests())
                            .answerChange(e.getAnswerChangeRequests())
                            .questionTextChange(e.getQuestionTextChangeRequests())
                            .duplicationChange(e.getDuplicationChangeRequests())
                            .deletionRequest(e.getDeletionRequests())
                            .build()
            );
            return dto;
        });

        // 4. Assemble the final detailed DTO
        ModuleDetailDto detailDto = new ModuleDetailDto();
        detailDto.setId(module.getId());
        detailDto.setTitle(module.getTitle());
        detailDto.setDescription(module.getDescription());
        detailDto.setNumberOfQuestions(module.getNumberOfChoiceQuestions());
        detailDto.setLikeCount(module.getLikeCount());
        detailDto.setQuestions(questionsDtoPage);


        // 5. Determine if the current user is following this module
        Set<UUID> followedModuleIds = getFollowedModuleIds(currentUser);
        detailDto.setIsFollowed(followedModuleIds.contains(moduleId));

        return detailDto;
    }

    private Set<UUID> getFollowedModuleIds(User user) {
        // This check is important because the user object from the security context might not have collections loaded.
        // Fetching from the repository ensures we have the complete, managed entity.
        return userRepository.findById(user.getId())
                .map(User::getFollowedModules)
                .orElse(Set.of())
                .stream()
                .map(Module::getId)
                .collect(Collectors.toSet());
    }
}