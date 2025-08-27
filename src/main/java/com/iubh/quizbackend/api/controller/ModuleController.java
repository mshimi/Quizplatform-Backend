package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.ModuleDetailDto;
import com.iubh.quizbackend.api.dto.ModuleListItemDto;
import com.iubh.quizbackend.api.dto.ModuleSummaryDto;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    /**
     * GET /api/v1/modules : Get a paginated list of all modules.
     * Each module includes an 'isFollowed' flag for the current user.
     */
//    @GetMapping
//    public ResponseEntity<Page<ModuleListItemDto>> getAllModules(
//            @AuthenticationPrincipal User currentUser,
//            Pageable pageable) {
//        Page<ModuleListItemDto> modules = moduleService.getAllModulesForUser(currentUser, pageable);
//        return ResponseEntity.ok(modules);
//    }

    @GetMapping
    public ResponseEntity<Page<ModuleListItemDto>> getAllModules(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean isFollowed, // Matches 'followed' from your TS interface
            @AuthenticationPrincipal User currentUser,
            Pageable pageable) { // Spring automatically handles page, size, and sort parameters
        Page<ModuleListItemDto> modules = moduleService.searchModules(title, isFollowed, currentUser, pageable);
        return ResponseEntity.ok(modules);
    }

    /**
     * GET /api/v1/modules/followed : Get modules the current user is following.
     */
    @GetMapping("/followed")
    public ResponseEntity<Page<ModuleListItemDto>> getFollowedModules(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "4") Integer pageSize
            ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ModuleListItemDto> modules = moduleService.getFollowedModules(currentUser, pageable);
        return ResponseEntity.ok(modules);
    }

    /**
     * POST /api/v1/modules/{moduleId}/toggle-follow : Follow or unfollow a module.
     */
    @PostMapping("/{moduleId}/toggle-follow")
    public ResponseEntity<Map<String, Boolean>> toggleFollowModule(
            @PathVariable UUID moduleId,
            @AuthenticationPrincipal User currentUser) {
        boolean isFollowing = moduleService.toggleFollow(moduleId, currentUser);
        // Return the new status, which is useful for updating the UI
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleDetailDto> getModuleById(
            @PathVariable UUID moduleId,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        ModuleDetailDto moduleDetails = moduleService.getModuleDetailsById(moduleId, currentUser, pageable);
        return ResponseEntity.ok(moduleDetails);
    }


    @GetMapping("/summary")
    public ResponseEntity<List<ModuleSummaryDto>> getAllModuleSummaries() {
        List<ModuleSummaryDto> modules = moduleService.getAllModulesAsSummary();
        return ResponseEntity.ok(modules);
    }
}