package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.UserDTO;
import com.aradosavljevic.hr_service.application.request.user.UserCreateRequest;
import com.aradosavljevic.hr_service.application.request.user.UserUpdateRequest;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserDTO> getAll(Pageable pageable);

    UserDTO getById(Long id);

    UserDTO create(UserCreateRequest request);

    UserDTO update(Long id, UserUpdateRequest request);

    void delete(Long id);
}
