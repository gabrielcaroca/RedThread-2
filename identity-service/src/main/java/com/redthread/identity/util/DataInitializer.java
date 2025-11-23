package com.redthread.identity.util;

import com.redthread.identity.model.Role;
import com.redthread.identity.model.User;
import com.redthread.identity.repository.RoleRepository;
import com.redthread.identity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(RoleRepository roleRepo, UserRepository userRepo) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) {
        System.out.println("Inicializando datos base...");

        ensureRole("CLIENTE", "Cliente");
        ensureRole("DESPACHADOR", "Despachador");
        ensureRole("ADMINISTRADOR", "Administrador");

        ensureAdmin();
        ensureCliente();
        ensureDespachador();

        System.out.println("Inicialización completa.");
    }

    private void ensureRole(String key, String name) {
        roleRepo.findByKey(key).orElseGet(() -> {
            Role r = new Role();
            r.setKey(key);
            r.setName(name);
            roleRepo.save(r);
            System.out.println("🔸 Rol creado: " + key);
            return r;
        });
    }

    private void ensureAdmin() {
        createUserIfNotExists(
                "admin@redthread.cl",
                "123456",
                "Admin",
                "ADMINISTRADOR"
        );
    }

    private void ensureCliente() {
        createUserIfNotExists(
                "cliente@redthread.cl",
                "123456",
                "Cliente General",
                "CLIENTE"
        );
    }

    private void ensureDespachador() {
        createUserIfNotExists(
                "despachador@redthread.cl",
                "123456",
                "Despachador General",
                "DESPACHADOR"
        );
    }

    private void createUserIfNotExists(String email, String rawPassword, String fullName, String roleKey) {
        userRepo.findByEmail(email).orElseGet(() -> {
            var role = roleRepo.findByKey(roleKey)
                    .orElseThrow(() -> new IllegalStateException("El rol " + roleKey + " no existe"));

            User u = new User();
            u.setEmail(email);
            u.setFullName(fullName);
            u.setPassword(encoder.encode(rawPassword));
            u.setRoles(Set.of(role));

            userRepo.save(u);
            System.out.println("👤 Usuario creado: " + email + " [" + roleKey + "]");
            return u;
        });
    }
}
