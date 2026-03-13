// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.bbva.gkxj.atiframework.moduletemplate;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AtiModuleBuilder extends ModuleBuilder {

    TemplateEngine templateEngine = new TemplateEngine();

    private AtiSettings atiSettings;

    public AtiModuleBuilder(AtiSettings atiSettings) {
        this.atiSettings = atiSettings;
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("TEXT"); // o "HTML" si es HTML
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    public ModuleType<AtiModuleBuilder> getModuleType() {
    return AtiModuleType.getInstance();
  }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new AtiModuleWizardStep(this.atiSettings);
    }

    @Override
    public String getName() {
        return atiSettings.getServicePublicName();
    }
    @Override
    public void setupRootModel(@NotNull ModifiableRootModel model)  {
      VirtualFile baseDir = model.getProject().getBaseDir();
        atiSettings.setServiceName(model.getProject().getName());
      if (baseDir != null) {
          try {
              createVirtualFile(baseDir, "nova.yml", "templates/nova.th");
              createVirtualFile(baseDir, "pom.xml", "templates/pom.th");
              createVirtualFile(baseDir, "README.md", "templates/README.th");
              // Crear la ruta destino
              String javaBasePath = "src/main/java/com/bbva/" + this.atiSettings.getUuaa().toLowerCase() + "/" + this.atiSettings.getServiceName().toLowerCase();
              VirtualFile javaDir = VfsUtil.createDirectoryIfMissing(baseDir, javaBasePath);
              // Crear el archivo Application.java usando la plantilla Application.th
              createVirtualFile(javaDir, "Application.java", "templates/java/Application.th");
              VirtualFile apisetupDir = VfsUtil.createDirectoryIfMissing(baseDir, javaBasePath+ "/apisetup");
              createVirtualFile(apisetupDir, "ConfigBatchconfig.java", "templates/java/apisetup/ConfigBatchconfig.th");
              createVirtualFile(apisetupDir, "ConfigBranchconfig.java", "templates/java/apisetup/ConfigBranchconfig.th");
              if(this.atiSettings.getIsNovaTransferIncluded()) {
                  createVirtualFile(apisetupDir, "ConfigFiletransfer.java", "templates/java/apisetup/ConfigFiletransfer.th");
                  createVirtualFile(apisetupDir, "FiletransferService.java", "templates/java/apisetup/FiletransferService.th");
              }
              createVirtualFile(apisetupDir, "ConfigSchedulerconfig.java", "templates/java/apisetup/ConfigSchedulerconfig.th");
              createVirtualFile(apisetupDir, "ConfigStepconfig.java", "templates/java/apisetup/ConfigStepconfig.th");
              createVirtualFile(apisetupDir, "ServiceBatchconfig.java", "templates/java/apisetup/ServiceBatchconfig.th");
              createVirtualFile(apisetupDir, "ServiceBranchconfig.java", "templates/java/apisetup/ServiceBranchconfig.th");
              createVirtualFile(apisetupDir, "ServiceSchedulerconfig.java", "templates/java/apisetup/ServiceSchedulerconfig.th");
              createVirtualFile(apisetupDir, "ServiceStepconfig.java", "templates/java/apisetup/ServiceStepconfig.th");
              createVirtualFile(apisetupDir, "WrapperServiceBatchConfig.java", "templates/java/apisetup/WrapperServiceBatchConfig.th");
              createVirtualFile(apisetupDir, "WrapperServiceBranchConfig.java", "templates/java/apisetup/WrapperServiceBranchConfig.th");
              createVirtualFile(apisetupDir, "WrapperServiceSchedulerConfig.java", "templates/java/apisetup/WrapperServiceSchedulerConfig.th");
              createVirtualFile(apisetupDir, "WrapperServiceStepConfig.java", "templates/java/apisetup/WrapperServiceStepConfig.th");

              VirtualFile asyncDir = VfsUtil.createDirectoryIfMissing(baseDir, javaBasePath+ "/async");
              createVirtualFile(asyncDir, "BatchAuditPublisher.java", "templates/java/async/BatchAuditPublisher.th");
              createVirtualFile(asyncDir, "BatchExecutorSubscriber.java", "templates/java/async/BatchExecutorSubscriber.th");
              createVirtualFile(asyncDir, "IssueAuditPublisher.java", "templates/java/async/IssueAuditPublisher.th");
              createVirtualFile(asyncDir, "SchedulerPlanPublisher.java", "templates/java/async/SchedulerPlanPublisher.th");

              VirtualFile auditDir = VfsUtil.createDirectoryIfMissing(baseDir, javaBasePath+ "/audit");
              createVirtualFile(auditDir, "ConfigBatchaudit.java", "templates/java/audit/ConfigBatchaudit.th");
              createVirtualFile(auditDir, "ConfigSchedulerplanaudit.java", "templates/java/audit/ConfigSchedulerplanaudit.th");
              createVirtualFile(auditDir, "ServiceBatchaudit.java", "templates/java/audit/ServiceBatchaudit.th");
              createVirtualFile(auditDir, "ServiceSchedulerplanaudit.java", "templates/java/audit/ServiceSchedulerplanaudit.th");
              createVirtualFile(auditDir, "WrapperServiceBatchAudit.java", "templates/java/audit/WrapperServiceBatchAudit.th");
              createVirtualFile(auditDir, "WrapperServiceSchedulerPlanAudit.java", "templates/java/audit/WrapperServiceSchedulerPlanAudit.th");

              VirtualFile callbackDir = VfsUtil.createDirectoryIfMissing(baseDir, javaBasePath+ "/callback");
              createVirtualFile(callbackDir, "BatchLauncherImpl.java", "templates/java/callback/BatchLauncherImpl.th");
              createVirtualFile(callbackDir, "BatchReexecuteImpl.java", "templates/java/callback/BatchReexecuteImpl.th");
              createVirtualFile(callbackDir, "BatchResponseImpl.java", "templates/java/callback/BatchResponseImpl.th");
              createVirtualFile(callbackDir, "BatchStopperImpl.java", "templates/java/callback/BatchStopperImpl.th");
              createVirtualFile(callbackDir, "StepLauncherImpl.java", "templates/java/callback/StepLauncherImpl.th");
              createVirtualFile(callbackDir, "StepResponseImpl.java", "templates/java/callback/StepResponseImpl.th");

              if(this.atiSettings.getIsMongoIncluded() || this.atiSettings.getIsOracleIncluded() || this.atiSettings.getIsPostgreIncluded()) {
                  VirtualFile databaseConfigurationDir = VfsUtil.createDirectoryIfMissing(baseDir, javaBasePath+ "/databaseConfiguration");
                  createVirtualFile(databaseConfigurationDir, "DatabaseConfig.java", "templates/java/databaseConfiguration/DatabaseConfig.th");
              }

              String javaResourcesPath = "src/main/";
              VirtualFile resourcesDir = VfsUtil.createDirectoryIfMissing(baseDir, javaResourcesPath+ "/resources");
              createVirtualFile(resourcesDir, "api-BatchAudit-1.1.2.yml", "templates/java/resources/api-BatchAudit-1.1.2.th");
              createVirtualFile(resourcesDir, "api-BatchConfig-1.1.1.yml", "templates/java/resources/api-BatchConfig-1.1.1.th");
              createVirtualFile(resourcesDir, "api-BranchConfig-1.1.1.yml", "templates/java/resources/api-BranchConfig-1.1.1.th");
              if(this.atiSettings.getIsNovaTransferIncluded()) {
                  createVirtualFile(resourcesDir, "api-filetransfer-1.3.0.yml", "templates/java/resources/api-filetransfer-1.3.0.th");
              }
              createVirtualFile(resourcesDir, "api-SchedulerConfig-1.1.2.yml", "templates/java/resources/api-SchedulerConfig-1.1.2.th");
              createVirtualFile(resourcesDir, "api-SchedulerPlanAudit-1.1.2.yml", "templates/java/resources/api-SchedulerPlanAudit-1.1.2.th");
              createVirtualFile(resourcesDir, "api-StepConfig-1.1.0.yml", "templates/java/resources/api-StepConfig-1.1.0.th");
              createVirtualFile(resourcesDir, "application-LOCAL.yml", "templates/java/resources/application-LOCAL.th");
              createVirtualFile(resourcesDir, "application.yml", "templates/java/resources/application.th");
              createVirtualFile(resourcesDir, "ehcache.xml", "templates/java/resources/ehcache.th");

              VirtualFile resourcesAsyncDir = VfsUtil.createDirectoryIfMissing(baseDir, javaResourcesPath+ "/resources/asyncapi");
              createVirtualFile(resourcesAsyncDir, "asyncapi-BatchAuditPublisher.yml", "templates/java/resources/asyncapi/asyncapi-BatchAuditPublisher.th");
              createVirtualFile(resourcesAsyncDir, "asyncapi-BatchExecutorSubscriber.yml", "templates/java/resources/asyncapi/asyncapi-BatchExecutorSubscriber.th");
              createVirtualFile(resourcesAsyncDir, "asyncapi-IssueAuditPublisher.yml", "templates/java/resources/asyncapi/asyncapi-IssueAuditPublisher.th");
              createVirtualFile(resourcesAsyncDir, "asyncapi-SchedulerPlanPublisher.yml", "templates/java/resources/asyncapi/asyncapi-SchedulerPlanPublisher.th");

              // Crear la ruta destino TESTS
              String testBasePath = "src/test";
              VirtualFile javaBaseTestDir = VfsUtil.createDirectoryIfMissing(baseDir, testBasePath + "/java/com/bbva/" + this.atiSettings.getUuaa().toLowerCase());

              VirtualFile apisetupTestDir = VfsUtil.createDirectoryIfMissing(javaBaseTestDir, "/apisetup");
              createVirtualFile(apisetupTestDir, "ConfigBatchconfigTest.java", "templates/test/java/apisetup/ConfigBatchconfigTest.th");
              createVirtualFile(apisetupTestDir, "ConfigBranchconfigTest.java", "templates/test/java/apisetup/ConfigBranchconfigTest.th");
              createVirtualFile(apisetupTestDir, "ConfigSchedulerconfigTest.java", "templates/test/java/apisetup/ConfigSchedulerconfigTest.th");
              createVirtualFile(apisetupTestDir, "ConfigStepconfigTest.java", "templates/test/java/apisetup/ConfigStepconfigTest.th");
              createVirtualFile(apisetupTestDir, "ServiceBatchconfigTest.java", "templates/test/java/apisetup/ServiceBatchconfigTest.th");
              createVirtualFile(apisetupTestDir, "ServiceBranchconfigTest.java", "templates/test/java/apisetup/ServiceBranchconfigTest.th");
              createVirtualFile(apisetupTestDir, "ServiceSchedulerconfigTest.java", "templates/test/java/apisetup/ServiceSchedulerconfigTest.th");
              createVirtualFile(apisetupTestDir, "ServiceStepconfigTest.java", "templates/test/java/apisetup/ServiceStepconfigTest.th");
              createVirtualFile(apisetupTestDir, "WrapperServiceBatchConfigTest.java", "templates/test/java/apisetup/WrapperServiceBatchConfigTest.th");
              createVirtualFile(apisetupTestDir, "WrapperServiceBranchConfigTest.java", "templates/test/java/apisetup/WrapperServiceBranchConfigTest.th");
              createVirtualFile(apisetupTestDir, "WrapperServiceSchedulerConfigTest.java", "templates/test/java/apisetup/WrapperServiceSchedulerConfigTest.th");
              createVirtualFile(apisetupTestDir, "WrapperServiceStepConfigTest.java", "templates/test/java/apisetup/WrapperServiceStepConfigTest.th");

              VirtualFile asyncTestDir = VfsUtil.createDirectoryIfMissing(javaBaseTestDir,  "/async");
              createVirtualFile(asyncTestDir, "BatchAuditPublisherTest.java", "templates/test/java/async/BatchAuditPublisherTest.th");
              createVirtualFile(asyncTestDir, "IssueAuditPublisherTest.java", "templates/test/java/async/IssueAuditPublisherTest.th");
              createVirtualFile(asyncTestDir, "SchedulerPlanPublisherTest.java", "templates/test/java/async/SchedulerPlanPublisherTest.th");

              VirtualFile auditTestDir = VfsUtil.createDirectoryIfMissing(javaBaseTestDir,  "/audit");
              createVirtualFile(auditTestDir, "ConfigBatchauditTest.java", "templates/test/java/audit/ConfigBatchauditTest.th");
              createVirtualFile(auditTestDir, "ConfigSchedulerplanauditTest.java", "templates/test/java/audit/ConfigSchedulerplanauditTest.th");
              createVirtualFile(auditTestDir, "ServiceBatchauditTest.java", "templates/test/java/audit/ServiceBatchauditTest.th");
              createVirtualFile(auditTestDir, "ServiceSchedulerplanauditTest.java", "templates/test/java/audit/ServiceSchedulerplanauditTest.th");
              createVirtualFile(auditTestDir, "WrapperServiceBatchAuditTest.java", "templates/test/java/audit/WrapperServiceBatchAuditTest.th");
              createVirtualFile(auditTestDir, "WrapperServiceSchedulerPlanAuditTest.java", "templates/test/java/audit/WrapperServiceSchedulerPlanAuditTest.th");

              VirtualFile callbackTestDir = VfsUtil.createDirectoryIfMissing(javaBaseTestDir, "/callback");
              createVirtualFile(callbackTestDir, "BatchLauncherImplTest.java", "templates/test/java/callback/BatchLauncherImplTest.th");
              createVirtualFile(callbackTestDir, "BatchReexecuteImplTest.java", "templates/test/java/callback/BatchReexecuteImplTest.th");
              createVirtualFile(callbackTestDir, "BatchResponseImplTest.java", "templates/test/java/callback/BatchResponseImplTest.th");
              createVirtualFile(callbackTestDir, "BatchStopperImplTest.java", "templates/test/java/callback/BatchStopperImplTest.th");
              createVirtualFile(callbackTestDir, "StepLauncherImplTest.java", "templates/test/java/callback/StepLauncherImplTest.th");
              createVirtualFile(callbackTestDir, "StepResponseImplTest.java", "templates/test/java/callback/StepResponseImplTest.th");

              if(this.atiSettings.getIsMongoIncluded() || this.atiSettings.getIsOracleIncluded() || this.atiSettings.getIsPostgreIncluded()) {
                  VirtualFile databaseConfigurationTestDir = VfsUtil.createDirectoryIfMissing(javaBaseTestDir, "/databaseConfiguration");
                  createVirtualFile(databaseConfigurationTestDir, "DatabaseConfigurationTest.java", "templates/test/java/databaseConfiguration/DatabaseConfigurationTest.th");
              }

              VirtualFile resourcesTestDir = VfsUtil.createDirectoryIfMissing(baseDir, testBasePath+ "/resources");
              createVirtualFile(resourcesTestDir, "batchAudit.json", "templates/test/resources/batchAudit.th");
              createVirtualFile(resourcesTestDir, "issueAudit.json", "templates/test/resources/issueAudit.th");
              createVirtualFile(resourcesTestDir, "schedulerPlan.json", "templates/test/resources/schedulerPlan.th");
          } catch (IOException e) {
              throw new RuntimeException(e);
          }
      }else{
          com.intellij.openapi.ui.Messages.showErrorDialog(
                  "El archivo nova.th NO ha sido creado correctamente.",
                  "Error to create ATI Client project"
          );
      }
    }

    /**
     * Crea un VirtualFile en la ruta indicada usando una plantilla thymeleaf.
     * @param parentDir Directorio padre donde crear el archivo.
     * @param fileName Nombre del archivo a crear.
     * @param fileTemplate Contenido de la plantilla thymeleaf.
     * @return VirtualFile creado.
     * @throws IOException Si ocurre un error al crear el archivo.
     */
    public void createVirtualFile(VirtualFile parentDir, String fileName, String fileTemplate) throws IOException {
        Context context = new Context();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileTemplate)) {
            if (is == null) throw new IOException("No se encontró la plantilla " + fileName);
            String templateContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Inicializar motor de plantillas Thymeleaf
            context.setVariable("atiSettings", this.atiSettings);

            // Procesar la plantilla
            String processedContent = this.templateEngine.process(templateContent, context);

            // Crear el archivo virtual
            VirtualFile virtualFile = parentDir.createChildData(this, fileName);
            VfsUtil.saveText(virtualFile, processedContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            context.clearVariables();
        }
    }
}
