package com.redthread.order.service;

import com.redthread.order.dto.AddressReq;
import com.redthread.order.model.Address;
import com.redthread.order.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

  @Mock AddressRepository repo;
  @InjectMocks AddressService service;

  @Test
  void patch_updatesOnlyNonNull() {
    String userId = "u1";
    Address a = Address.builder().id(1L).userId(userId)
        .line1("L1").line2(null).city("C").state("S").zip("Z").country("CL").isDefault(false)
        .build();

    when(repo.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(a));
    when(repo.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

    AddressReq req = new AddressReq("NEW L1", "L2", "NEW C", "S", "Z", "CL", true);
    Address out = service.patch(userId, 1L, req);

    assertThat(out.getLine1()).isEqualTo("NEW L1");
    assertThat(out.getLine2()).isEqualTo("L2");
    assertThat(out.isDefault()).isTrue();
  }
}
