package com.redthread.identity.service;

import com.redthread.identity.dto.AddressDto;
import com.redthread.identity.model.Address;
import com.redthread.identity.model.User;
import com.redthread.identity.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository repo;
    private final UserService userService;

    public AddressService(AddressRepository repo, UserService userService) {
        this.repo = repo; this.userService = userService;
    }

    public List<Address> listMine() {
        User u = userService.getCurrentUserEntity();
        return repo.findByUserId(u.getId());
    }

    @Transactional
    public Address create(AddressDto dto) {
        User u = userService.getCurrentUserEntity();
        Address a = new Address();
        a.setUser(u);
        a.setLine1(dto.getLine1());
        a.setLine2(dto.getLine2());
        a.setCity(dto.getCity());
        a.setState(dto.getState());
        a.setZip(dto.getZip());
        a.setCountry(dto.getCountry());
        a.setDefault(dto.isDefault());
        if (a.isDefault()) unsetOthers(u);
        return repo.save(a);
    }

    @Transactional
    public Address update(Long id, AddressDto dto) {
        User u = userService.getCurrentUserEntity();
        Address a = repo.findByIdAndUser(id, u).orElseThrow();
        a.setLine1(dto.getLine1());
        a.setLine2(dto.getLine2());
        a.setCity(dto.getCity());
        a.setState(dto.getState());
        a.setZip(dto.getZip());
        a.setCountry(dto.getCountry());
        a.setDefault(dto.isDefault());
        if (a.isDefault()) unsetOthers(u, id);
        return repo.save(a);
    }

    public void delete(Long id) {
        User u = userService.getCurrentUserEntity();
        Address a = repo.findByIdAndUser(id, u).orElseThrow();
        repo.delete(a);
    }

    private void unsetOthers(User u) {
        for (Address x : repo.findByUserId(u.getId())) { x.setDefault(false); }
    }
    private void unsetOthers(User u, Long keepId) {
        for (Address x : repo.findByUserId(u.getId())) { if (!x.getId().equals(keepId)) x.setDefault(false); }
    }
}
