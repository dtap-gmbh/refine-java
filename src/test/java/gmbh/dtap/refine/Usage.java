package gmbh.dtap.refine;

import gmbh.dtap.refine.api.*;
import gmbh.dtap.refine.client.JsonEngine;
import gmbh.dtap.refine.client.JsonOperation;
import gmbh.dtap.refine.client.KeyValueUploadOptions;
import gmbh.dtap.refine.fluent.RefineExecutor;

import java.io.File;
import java.util.List;

/**
 * Sample usage of the Refine API Java client.
 *
 * @since 0.1.0
 */
public class Usage {

   private String url = "http://localhost:3333/";
   private File file = new File("src/test/resources/addresses.csv");

   private void createAndDeleteProject() throws Exception {
      try (RefineClient client = RefineClients.create(url)) {
         RefineProjectLocation location = client.createProject("Addresses", file);
         client.deleteProject(location.getId());
      }
   }

   private void createAndDeleteExtendedProject() throws Exception {
      try (RefineClient client = RefineClients.create(url)) {
         RefineProjectLocation location = client.createProject("Addresses", file,
               UploadFormat.SEPARATOR_BASED, KeyValueUploadOptions.create("separator", ","));
         client.deleteProject(location.getId());
      }
   }

   private void createProjectAndExportRows() throws Exception {
      try (RefineClient client = RefineClients.create(url)) {
         RefineProjectLocation location = client.createProject("Addresses", file);
         client.exportRows(location.getId(), JsonEngine.from("{\"facets\":[]}"), ExportFormat.HTML, System.out);
         client.deleteProject(location.getId());
      }
   }

   private void createProjectAndGetMetadata() throws Exception {
      try (RefineClient client = RefineClients.create(url)) {
         RefineProjectLocation location1 = client.createProject("Addresses1", file);
         RefineProjectLocation location2 = client.createProject("Addresses2", file);
         List<RefineProject> projects = client.allProjectMetadata();
         System.out.println(projects);
         client.deleteProject(location1.getId());
         client.deleteProject(location2.getId());
      }
   }

   private void createProjectAndApplyOperations() throws Exception {
      String operation = "{\n" +
            "    \"op\": \"core/column-split\",\n" +
            "    \"description\": \"Split column ID by separator\",\n" +
            "    \"engineConfig\": {\n" +
            "      \"facets\": [],\n" +
            "      \"mode\": \"row-based\"\n" +
            "    },\n" +
            "    \"columnName\": \"ID\",\n" +
            "    \"guessCellType\": true,\n" +
            "    \"removeOriginalColumn\": true,\n" +
            "    \"mode\": \"separator\",\n" +
            "    \"separator\": \"-\",\n" +
            "    \"regex\": false,\n" +
            "    \"maxColumns\": 0\n" +
            "  }";
      try (RefineClient client = RefineClients.create(url)) {
         RefineProjectLocation location = client.createProject("Addresses", file);
         client.applyOperations(location.getId(), JsonOperation.from(operation));
      }
   }

   private void createProjectAndExpressioPreview() throws Exception {
      try (RefineClient client = RefineClients.create(url)) {
         RefineProjectLocation location = client.createProject("Addresses", file);
         List<String> expressionPreviews = client.expressionPreview(location.getId(),
               4, new long[]{0, 1},
               "grel:toLowercase(value);", false, 0);
         System.out.println(expressionPreviews);
         client.deleteProject(location.getId());
      }
   }

   private void createProjectAndAsynchProcesses() throws Exception {
      try (RefineClient client = RefineClients.create(url)) {

         RefineProjectLocation location =
               RefineExecutor.createProject()
                     .name("Addresses")
                     .file(file)
                     .execute(client);

         List<ProcessStatus> processStatuses =
               RefineExecutor.asynchProcesses()
                     .project(location)
                     .execute(client);

         System.out.println(processStatuses);

         RefineExecutor.deleteProject()
               .project(location)
               .execute(client);
      }
   }

   public static void main(String... args) throws Exception {
      Usage usage = new Usage();
      usage.createAndDeleteProject();
      usage.createAndDeleteExtendedProject();
      usage.createProjectAndExportRows();
      usage.createProjectAndGetMetadata();
      usage.createProjectAndApplyOperations();
      usage.createProjectAndExpressioPreview();
      usage.createProjectAndAsynchProcesses();
   }
}
