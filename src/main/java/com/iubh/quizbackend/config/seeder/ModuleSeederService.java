package com.iubh.quizbackend.config.seeder;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleSeederService {

    private final ModuleRepository moduleRepository;

    public List<Module> seedModules() {
        List<Module> modules = List.of(
                Module.builder().title("Grundlagen der Programmierung").description("Einführung in die prozedurale und objektorientierte Programmierung mit Java.").build(),
                Module.builder().title("Algorithmen und Datenstrukturen").description("Analyse von Algorithmen, Komplexitätstheorie und Implementierung gängiger Datenstrukturen.").build(),
                Module.builder().title("Datenbanken I").description("Grundlagen relationaler Datenbanken, SQL und Datenmodellierung.").build(),
                Module.builder().title("Betriebssysteme und Rechnernetze").description("Konzepte von Betriebssystemen, Prozessmanagement und Grundlagen der Netzwerkkommunikation.").build(),
                Module.builder().title("Software Engineering").description("Methoden und Techniken für den professionellen Softwareentwicklungsprozess.").build()
        );
        return moduleRepository.saveAll(modules);
    }
}