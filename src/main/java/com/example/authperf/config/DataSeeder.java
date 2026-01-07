package com.example.authperf.config;

import com.example.authperf.model.User;
import com.example.authperf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() < 10000) {
            System.out.println("Seeding 10,000 users...");
            List<User> users = new ArrayList<>();
            for (int i = 1; i <= 10000; i++) {
                users.add(User.builder()
                        .username("user" + i)
                        .email("user" + i + "@example.com")
                        .role("USER")
                        .build());

                // Batch insert logic if needed, but saveAll works fine for 10k in H2/Postgres
                // usually
                if (i % 1000 == 0) {
                    userRepository.saveAll(users);
                    users.clear();
                    System.out.println("Seeded " + i + " users...");
                }
            }
            if (!users.isEmpty()) {
                userRepository.saveAll(users);
            }
            System.out.println("Data seeding completed.");
        }
    }
}
