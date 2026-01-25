package com.online.library.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Architecture Tests")
public class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.online.library");
    }

    @Test
    @DisplayName("Kontrolery nie powinny zależeć bezpośrednio od encji")
    void controllers_should_not_depend_on_entities() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controllers..")
                .should().dependOnClassesThat().resideInAPackage("..entities..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Serwisy powinny być w pakiecie services")
    void services_should_reside_in_services_package() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*ServiceImpl")
                .should().resideInAPackage("..services.impl..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repozytoria powinny być w pakiecie repositories")
    void repositories_should_reside_in_repositories_package() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Repository")
                .should().resideInAPackage("..repositories..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Encje powinny być w pakiecie entities")
    void entities_should_reside_in_entities_package() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Entity")
                .should().resideInAPackage("..entities..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTOs powinny być w pakiecie dto")
    void dtos_should_reside_in_dto_package() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Dto")
                .should().resideInAPackage("..dto..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Kontrolery powinny zależeć tylko od serwisów (nie od repozytoriów)")
    void controllers_should_only_depend_on_services_not_repositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controllers..")
                .should().dependOnClassesThat().resideInAPackage("..repositories..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repozytoria nie powinny zależeć od serwisów")
    void repositories_should_not_depend_on_services() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repositories..")
                .should().dependOnClassesThat().resideInAPackage("..services..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Architektura warstwowa powinna być przestrzegana")
    void layered_architecture_should_be_respected() {
        ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controllers").definedBy("..controllers..")
                .layer("Services").definedBy("..services..")
                .layer("Repositories").definedBy("..repositories..")
                .layer("Domain").definedBy("..domain..")
                .layer("Config").definedBy("..config..")
                .layer("Exceptions").definedBy("..exceptions..")
                .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
                .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Services")
                .whereLayer("Repositories").mayOnlyBeAccessedByLayers("Services", "Domain");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Klasy serwisów powinny mieć adnotację @Service")
    void service_classes_should_be_annotated_with_service() {
        ArchRule rule = classes()
                .that().resideInAPackage("..services.impl..")
                .and().haveSimpleNameEndingWith("ServiceImpl")
                .should().beAnnotatedWith(org.springframework.stereotype.Service.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repozytoria Spring Data powinny być interfejsami")
    void repository_interfaces_should_be_in_repositories_package() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repositories..")
                .and().haveSimpleNameEndingWith("Repository")
                .should().beInterfaces();

        rule.check(importedClasses);
    }
}
