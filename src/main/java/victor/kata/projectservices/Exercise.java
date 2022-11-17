package victor.kata.projectservices;


import java.util.List;

import static java.util.stream.Collectors.toList;
import static victor.kata.projectservices.ProjectUserRoleType.ADMIN;
import static victor.kata.projectservices.ProjectUserRoleType.CONTRIBUTOR;
import static victor.kata.projectservices.ProjectUserRoleType.VIEW;

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
        if (projectUser.getRole() == ADMIN) {
            List<ProjectServices> projectServices = projectServicesService.getProjectServicesByProjectId(project.getId());

            List<ProjectServices> subscribedServices = projectServices.stream()
                    .filter(ProjectServices::isSubscribed)
                    .collect(toList());

            User user = userService.findByUuid(projectUser.getUuid()).orElseThrow();

            projectServices.stream()
                    .filter(ProjectServices::isSubscribed)
                    .map(ps -> new ProjectServiceDto(ps.getService()))
                    .forEach(dto -> userServiceHelper.sendUserToServicesOnCreate(dto, project, messageAction, user, projectUser, ADMIN.name()));

        } else {
            List<String> projectServices = projectUser.getServices();
            List<Service> services = serviceService.findAll();

            for (String serviceName : projectServices) {
                for (Service service : services) {
                    if (serviceName.equals(service.getName())) {
                        ProjectServices projectServices1 = projectServicesService.findByServiceAndProject(service, project);
                        if (projectServices1 != null && projectServices1.isSubscribed()) {
                            ProjectServiceDto projectServiceDTO = new ProjectServiceDto(service);
                            User user = userService.findByUuid(projectUser.getUuid()).orElseThrow();
                            if (projectUser.getRole() == VIEW) {
                                userServiceHelper.sendUserToServicesOnCreate(projectServiceDTO, project, messageAction, user, projectUser, VIEW.name());
                            } else {
                                userServiceHelper.sendUserToServicesOnCreate(projectServiceDTO, project, messageAction, user, projectUser, CONTRIBUTOR.name());
                            }
                        }
                    }
                }
            }
        }
    }

}
