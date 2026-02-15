package com.tradebridge.backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.springframework.stereotype.Service;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.tradebridge.backend")
class RepositoryAccessRulesTest {

    @ArchTest
    static final ArchRule repositories_should_only_be_used_by_services = noClasses()
            .that().resideInAPackage("com.tradebridge.backend..")
            .and().areNotInterfaces()
            .and().areNotAnnotatedWith(Service.class)
            .and().resideOutsideOfPackage("..persistence..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");
}
