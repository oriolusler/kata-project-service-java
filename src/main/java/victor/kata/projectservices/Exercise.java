package victor.kata.projectservices;


import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class Exercise {
   private final ProjectServicesService projectServicesService;
   private final UserService userService;
   private final UserServiceHelper userServiceHelper;
   private final ServiceService serviceService;

   public Exercise(ProjectServicesService projectServicesService, UserService userService, UserServiceHelper userServiceHelper, ServiceService serviceService) {
      this.projectServicesService = projectServicesService;
      this.userService = userService;
      this.userServiceHelper = userServiceHelper;
      this.serviceService = serviceService;
   }

   public void sendUserMessageOnCreate(ProjectUserDTO projectUser, Project project, MessageAction messageAction) {
      if (projectUser.getRole().equals(ProjectUserRoleType.ADMIN)) {
         List<ProjectServices> projectServices = projectServicesService.getProjectServicesByProjectId(project.getId());
         List<ProjectServices> subscribedProjectServices = projectServices.stream()
             .filter(projectService -> projectService.getProjectServiceStatus().equals(ProjectServiceStatus.SUBSCRIBED))
             .collect(Collectors.toList());

         subscribedProjectServices.forEach(subscribedProjectService -> {
            ProjectServicesDTO projectServicesDTO = new ProjectServicesDTO();
            projectServicesDTO.setService(subscribedProjectService.getService());
            User user = userService.findByUuid(projectUser.getUuid()).get();
            userServiceHelper.sendUserToServicesOnCreate(projectServicesDTO, project, messageAction, user, projectUser, ProjectUserRoleType.ADMIN.name());
         });
      } else {
         List<String> projectServices = projectUser.getServices();
         List<Service> services = serviceService.findAll();

         for (String serviceName : projectServices) {
            for (Service service : services) {
               if (service.getName().equals(serviceName)) {
                  ProjectServices projectServices1 = projectServicesService.findByServiceAndProject(service, project);
                  if (projectServices1 != null && projectServices1.getProjectServiceStatus().equals(ProjectServiceStatus.SUBSCRIBED)) {
                     ProjectServicesDTO projectServicesDTO = new ProjectServicesDTO();
                     projectServicesDTO.setService(service);
                     User user = userService.findByUuid(projectUser.getUuid()).get();
                     if (projectUser.getRole().equals(ProjectUserRoleType.VIEW)) {
                        userServiceHelper.sendUserToServicesOnCreate(projectServicesDTO, project, messageAction, user, projectUser, ProjectUserRoleType.VIEW.name());
                     } else {
                        userServiceHelper.sendUserToServicesOnCreate(projectServicesDTO, project, messageAction, user, projectUser, ProjectUserRoleType.CONTRIBUTOR.name());
                     }
                  }
               }
            }
         }
      }
   }
}
