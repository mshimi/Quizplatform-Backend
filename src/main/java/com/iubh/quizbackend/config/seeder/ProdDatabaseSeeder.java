package com.iubh.quizbackend.config.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@Profile("prod") // This runner will ONLY run when the 'prod' profile is active
@RequiredArgsConstructor
@Slf4j
public class ProdDatabaseSeeder implements CommandLineRunner {

    private final ModuleRepository moduleRepository;
    private final ChoiceQuestionRepository choiceQuestionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {



        // 1. Check if the database already has modules to prevent duplicates
        if (moduleRepository.count() > 0) {

//            List<ChoiceQuestion> questions =   choiceQuestionRepository.findAll();
//
//            questions.forEach(question -> {
//                question.setActive(true);
//            });
//
//            choiceQuestionRepository.saveAll(questions);

            log.info("Production database already contains modules. Skipping data seeding.");
            return;
        }

        log.info("Starting production database seeding for modules...");

        // 2. Load the JSON file from the classpath
        try (InputStream inputStream = new ClassPathResource("data/prod-modules.json").getInputStream()) {
            // 3. Deserialize the JSON into a list of Module objects
            List<Module> modulesToSave = objectMapper.readValue(inputStream, new TypeReference<>() {});



            // 4. Save all modules to the database in a single transaction
            moduleRepository.saveAll(modulesToSave);

            log.info("Successfully seeded {} modules into the production database.", modulesToSave.size());
        } catch (Exception e) {
            log.error("Failed to seed production modules from JSON file.", e);
        }
    }
}