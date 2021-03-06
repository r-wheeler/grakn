/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.test.migration.csv;

import ai.grakn.Grakn;
import ai.grakn.GraknSession;
import ai.grakn.migration.csv.CSVMigrator;
import ai.grakn.test.EngineContext;
import ai.grakn.util.GraphLoader;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static ai.grakn.test.migration.MigratorTestUtils.assertPetGraphCorrect;
import static ai.grakn.test.migration.MigratorTestUtils.assertPokemonGraphCorrect;
import static ai.grakn.test.migration.MigratorTestUtils.getFile;
import static ai.grakn.test.migration.MigratorTestUtils.load;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CSVMigratorMainTest {
    private GraknSession factory;
    private String keyspace;

    private final String dataFile = getFile("csv", "pets/data/pets.csv").getAbsolutePath();
    private final String templateFile = getFile("csv", "pets/template.gql").getAbsolutePath();

    @ClassRule
    public static final EngineContext engine = EngineContext.startInMemoryServer();

    @Rule
    public final SystemOutRule sysOut = new SystemOutRule().enableLog();

    @Rule
    public final SystemErrRule sysErr = new SystemErrRule().enableLog();

    @Before
    public void setup(){
        keyspace = GraphLoader.randomKeyspace();
        factory = Grakn.session(engine.uri(), keyspace);

        load(factory, getFile("csv", "pets/schema.gql"));
    }

    @Test
    public void runningCSVMigrationFromScript_PetDataMigratedCorrectly(){
        runAndAssertDataCorrect("-u", engine.uri(), "-input", dataFile, "-template", templateFile, "-keyspace", keyspace);
    }

    @Test
    public void usingTabsAsSeparatorInCSVMigratorScript_PetDataMigratedCorrectly(){
        String tsvFile = getFile("csv", "pets/data/pets.tsv").getAbsolutePath();
        runAndAssertDataCorrect("-u", engine.uri(), "-input", tsvFile, "-template", templateFile, "-separator", "\t", "-keyspace", keyspace);
    }

    @Test
    public void usingSpacesAsSeparatorInCSVMigratorScript_PetDataMigratedCorrectly(){
        String tsvFile = getFile("csv", "pets/data/pets.spaces").getAbsolutePath();
        runAndAssertDataCorrect("-u", engine.uri(), "-input", tsvFile, "-template", templateFile, "-separator", " ", "-keyspace", keyspace);
    }

    @Test
    public void usingSingleQuotesForStringInCSVMigratorScript_PetDataMigratedCorrectly(){
        String quoteFile = getFile("csv", "pets/data/pets.singlequotes").getAbsolutePath();
        runAndAssertDataCorrect("-u", engine.uri(), "-input", quoteFile, "-template", templateFile, "-quote", "\'", "-keyspace", keyspace);
    }

    @Test
    public void usingNullsInTemplateInCSVMigratorScript_PetDataMigratedCorrectly(){
        String nullTemplate = getFile("csv", "pets/template-null.gql").getAbsolutePath();
        runAndAssertDataCorrect("-u", engine.uri(), "-input", dataFile, "-template", nullTemplate, "-keyspace", keyspace, "-null", "");
    }

    @Test
    public void specifyingIncorrectURIInCSVMigratorScript_ErrorIsPrintedToSystemErr(){
        run("csv", "-input", dataFile, "-template", templateFile, "-uri", "localhost:" + engine.uri().substring(1), "-keyspace", keyspace);

        assertThat(sysErr.getLog(), containsString("Could not connect to Grakn Engine. Have you run 'grakn.sh start'?"));
    }

    @Test
    public void usingPropertiesFileInCSVMigratorScript_PetDataMigratedCorrectly(){
        load(factory, getFile("csv", "multi-file/schema.gql"));
        String configurationFile = getFile("csv", "multi-file/migration.yaml").getAbsolutePath();
        run("csv", "-u", engine.uri(), "-config", configurationFile, "-keyspace", keyspace);

        assertPokemonGraphCorrect(factory);
    }

    @Test
    public void csvMigratorCalledWithNoArgs_HelpMessagePrintedToSystemOut(){
        run();

        assertThat(sysOut.getLog(), containsString("usage: migration.sh"));
    }

    @Test
    public void csvMigratorCalledWithNoTemplate_ErrorIsPrintedToSystemErr(){
        run("-input", dataFile, "-u", engine.uri());
        assertThat(sysErr.getLog(), containsString("Template file missing (-t)"));
    }

    @Test
    public void csvMigratorCalledWithInvalidTemplateFile_ErrorIsPrintedToSystemErr(){
        run("-input", dataFile + "wrong", "-template", templateFile + "wrong", "-u", engine.uri());
        assertThat(sysErr.getLog(), containsString("Cannot find file"));
    }

    @Test
    public void csvMigratorCalledWithUnknownArgument_ErrorIsPrintedToSystemErr(){
        run("-whale", "");
        assertThat(sysErr.getLog(), containsString("Unrecognized option: -whale"));
    }

    private void run(String... args){
        CSVMigrator.main(args);
    }

    private void runAndAssertDataCorrect(String... args){
        run(args);

        assertPetGraphCorrect(factory);
    }
}
