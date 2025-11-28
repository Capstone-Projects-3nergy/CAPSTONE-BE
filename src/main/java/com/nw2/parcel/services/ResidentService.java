package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.ResidentListDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final UsersRepository usersRepository;

    public List<ResidentListDto> getAllResidents() {

        List<Users> residents = usersRepository.findByRoleAndStatus(
                Users.Role.RESIDENT,
                Users.Status.ACTIVE
        );

        return residents.stream()
                .map(u -> new ResidentListDto(
                        u.getUserId(),
                        u.getFirstName(),
                        u.getLastName(),
                        (u.getFirstName() + " " + u.getLastName()).trim(),
                        u.getRoomNumber(),
                        u.getEmail()
                ))
                .toList();
    }
}