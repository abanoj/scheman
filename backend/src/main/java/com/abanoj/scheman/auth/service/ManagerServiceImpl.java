package com.abanoj.scheman.auth.service;

import com.abanoj.scheman.auth.dto.ManagerResponseDto;
import com.abanoj.scheman.auth.dto.ManagerUpdateRequestDto;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private static final String NOT_FOUND_MESSAGE = "Not found manager with id: ";

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ManagerResponseDto> findAll(Pageable pageable) {
        return userRepository.findAllByRole(Role.MANAGER, pageable)
                .map(u -> new ManagerResponseDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.isEnabled()));
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerResponseDto findById(UUID id) {
        User user = findManager(id);
        return new ManagerResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.isEnabled());
    }

    @Override
    @Transactional
    public ManagerResponseDto update(UUID id, ManagerUpdateRequestDto dto) {
        User user = findManager(id);
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        userRepository.save(user);
        log.debug("Updated manager with id {}", id);
        return new ManagerResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.isEnabled());
    }

    @Override
    @Transactional
    public void enable(UUID id) {
        User user = findManager(id);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Manager enabled {}", user.getEmail());
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        User user = findManager(id);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Manager disabled {}", user.getEmail());
    }

    private User findManager(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE + id));
        if (user.getRole() != Role.MANAGER) {
            throw new ResourceNotFoundException(NOT_FOUND_MESSAGE + id);
        }
        return user;
    }
}
