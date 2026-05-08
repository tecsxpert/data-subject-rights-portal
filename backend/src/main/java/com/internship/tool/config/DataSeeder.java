package com.internship.tool.config;

import com.internship.tool.entity.DsrRequest;
import com.internship.tool.entity.DsrRequest.*;
import com.internship.tool.entity.User;
import com.internship.tool.repository.DsrRequestRepository;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test") // never run during test builds
public class DataSeeder implements CommandLineRunner {

    private final UserRepository    userRepo;
    private final DsrRequestRepository dsrRepo;
    private final PasswordEncoder   encoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("DataSeeder: data already present, skipping.");
            return;
        }

        log.info("DataSeeder: seeding demo data…");
        seedUsers();
        seedDsrRequests();
        log.info("DataSeeder: done — 30 DSR records, 3 users.");
    }

    // ── USERS ─────────────────────────────────────────────────

    private void seedUsers() {
        userRepo.saveAll(List.of(
            User.builder().username("admin").email("admin@dsrportal.dev")
                .password(encoder.encode("Admin@123")).role("ADMIN").active(true).build(),
            User.builder().username("manager").email("manager@dsrportal.dev")
                .password(encoder.encode("Manager@123")).role("MANAGER").active(true).build(),
            User.builder().username("analyst").email("analyst@dsrportal.dev")
                .password(encoder.encode("Analyst@123")).role("USER").active(true).build()
        ));
    }

    // ── DSR REQUESTS ──────────────────────────────────────────

    private static final String[][] SEEDS = {
        // {subjectName, email, type, status, priority, desc, daysAgo, deadlineDays}
        {"Priya Sharma","priya.sharma@gmail.com","ACCESS","PENDING","HIGH",
         "Request to access all personal data held on account including purchase history and browsing data.","-3","14"},
        {"Rahul Gupta","rahul.g@outlook.com","ERASURE","IN_PROGRESS","CRITICAL",
         "Requesting complete deletion of all data pursuant to Article 17 GDPR. Account no longer in use.","-5","7"},
        {"Anjali Singh","anjali.s@yahoo.com","RECTIFICATION","COMPLETED","MEDIUM",
         "Date of birth and home address recorded incorrectly. Correct DOB is 1990-03-15.","-20","30"},
        {"Vikram Patel","v.patel@hotmail.com","PORTABILITY","PENDING","LOW",
         "Request for structured machine-readable copy of data to transfer to another provider.","-1","21"},
        {"Deepa Nair","deepa.nair@protonmail.com","RESTRICTION","IN_PROGRESS","HIGH",
         "Processing of my data must be restricted pending outcome of ongoing legal dispute.","-8","10"},
        {"Arjun Mehta","arjun.m@icloud.com","OBJECTION","PENDING","MEDIUM",
         "Object to use of personal data for direct marketing and automated profiling.","-2","14"},
        {"Sneha Reddy","sneha.r@gmail.com","ACCESS","COMPLETED","LOW",
         "Requesting confirmation of what categories of personal data are processed.","-30","45"},
        {"Kiran Kumar","kiran.k@gmail.com","ERASURE","REJECTED","HIGH",
         "Erasure request — rejected due to statutory retention obligation under tax law.","-15","20"},
        {"Meena Joshi","meena.j@outlook.com","ACCESS","PENDING","MEDIUM",
         "Need full data export including all communications logged since account creation 2021.","-1","30"},
        {"Suresh Pillai","suresh.p@gmail.com","RECTIFICATION","IN_PROGRESS","HIGH",
         "Middle name missing from all records. Legal name is Suresh Kumar Pillai.","-6","7"},
        {"Lakshmi Rao","lakshmi.r@rediffmail.com","PORTABILITY","PENDING","LOW",
         "Requesting JSON export of profile, preferences and interaction history.","-4","21"},
        {"Aditya Shah","aditya.sh@yahoo.com","RESTRICTION","COMPLETED","MEDIUM",
         "Restriction lifted after dispute resolved. Data processing may resume.","-25","30"},
        {"Pooja Verma","pooja.v@gmail.com","OBJECTION","IN_PROGRESS","HIGH",
         "Object to processing for research purposes under Article 21(2) GDPR.","-7","14"},
        {"Rohit Khanna","rohit.k@protonmail.com","ACCESS","PENDING","CRITICAL",
         "Suspected data breach — requesting immediate access to audit of data shared with third parties.","-1","3"},
        {"Sunita Iyer","sunita.i@gmail.com","ERASURE","PENDING","HIGH",
         "Child's data inadvertently collected. Requesting deletion per Article 8 GDPR.","-2","7"},
        {"Manish Tiwari","manish.t@outlook.com","ACCESS","COMPLETED","LOW",
         "Standard subject access request — fulfilled. Report sent via encrypted email.","-40","60"},
        {"Geeta Bhatt","geeta.b@icloud.com","RECTIFICATION","PENDING","MEDIUM",
         "Phone number on file is outdated. Correct number: +91-9876543210.","-3","21"},
        {"Nikhil Srivastava","nikhil.s@gmail.com","PORTABILITY","CANCELLED","LOW",
         "Requestor withdrew portability request; satisfied with in-app data download.","-10","30"},
        {"Ritu Malhotra","ritu.m@hotmail.com","ERASURE","IN_PROGRESS","HIGH",
         "Data subject deceased. Next-of-kin requesting erasure of all personal accounts.","-9","14"},
        {"Vivek Pandey","vivek.p@gmail.com","OBJECTION","PENDING","MEDIUM",
         "Object to processing for legitimate interests — privacy concern raised.","-2","21"},
        {"Kavita Desai","kavita.d@gmail.com","ACCESS","IN_PROGRESS","MEDIUM",
         "Requesting logs of all login events and device data linked to account.","-5","14"},
        {"Harish Saxena","harish.s@yahoo.com","RESTRICTION","PENDING","LOW",
         "Contesting accuracy of credit-scoring data — restriction required during review.","-1","30"},
        {"Preeti Nanda","preeti.n@protonmail.com","ERASURE","COMPLETED","HIGH",
         "Right to erasure exercised. All records purged from primary and backup systems.","-35","45"},
        {"Sanjay Bose","sanjay.b@gmail.com","ACCESS","PENDING","MEDIUM",
         "First SAR submitted. Requesting copy of all categories defined under DPDP Act.","-1","30"},
        {"Ananya Chatterjee","ananya.c@outlook.com","PORTABILITY","IN_PROGRESS","HIGH",
         "Switching healthcare provider — requesting structured portability of medical data.","-4","10"},
        {"Rajesh Bansal","rajesh.b@gmail.com","RECTIFICATION","PENDING","LOW",
         "Residential address needs updating following relocation.","-1","21"},
        {"Swati Aggarwal","swati.a@icloud.com","OBJECTION","REJECTED","MEDIUM",
         "Objection to processing not upheld — compelling legitimate grounds demonstrated.","-20","25"},
        {"Dilip Menon","dilip.m@gmail.com","ACCESS","PENDING","CRITICAL",
         "Whistleblower — requesting confirmation whether data shared with employer.","-1","3"},
        {"Poonam Ghosh","poonam.g@rediffmail.com","ERASURE","IN_PROGRESS","HIGH",
         "Requesting erasure post-account closure under DPDP Act Section 12.","-6","14"},
        {"Tarun Ahuja","tarun.a@gmail.com","RESTRICTION","COMPLETED","MEDIUM",
         "Restriction applied during accuracy dispute. Dispute resolved; now lifted.","-45","60"}
    };

    private void seedDsrRequests() {
        User admin   = userRepo.findByUsername("admin").orElseThrow();
        User manager = userRepo.findByUsername("manager").orElseThrow();
        Random rng   = new Random(42);

        for (String[] s : SEEDS) {
            int daysAgo      = Math.abs(Integer.parseInt(s[6]));
            int deadlineDays = Integer.parseInt(s[7]);
            LocalDateTime created = LocalDateTime.now().minusDays(daysAgo);
            LocalDate deadline    = LocalDate.now().plusDays(deadlineDays - daysAgo);

            Status status = Status.valueOf(s[3]);
            LocalDateTime resolvedAt = null;
            if (status == Status.COMPLETED || status == Status.REJECTED || status == Status.CANCELLED) {
                resolvedAt = created.plusDays(rng.nextInt(5) + 1);
            }

            DsrRequest r = DsrRequest.builder()
                    .subjectName(s[0])
                    .subjectEmail(s[1])
                    .requestType(RequestType.valueOf(s[2]))
                    .status(status)
                    .priority(Priority.valueOf(s[4]))
                    .description(s[5])
                    .deadlineDate(deadline)
                    .assignedTo(rng.nextBoolean() ? manager : null)
                    .createdBy(admin)
                    .resolvedAt(resolvedAt)
                    .aiDescription("AI-generated summary pending enrichment.")
                    .isAiFallback(false)
                    .build();

            // Manually set createdAt for realistic demo data
            DsrRequest saved = dsrRepo.save(r);
            // Use a native query update to backdate createdAt for realism
            dsrRepo.backdateCreatedAt(saved.getId(), created);
        }
    }
}
