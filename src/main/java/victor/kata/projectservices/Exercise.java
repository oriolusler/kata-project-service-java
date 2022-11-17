package victor.kata.projectservices;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        User user = userService.findByUuid(projectUser.getUuid()).orElseThrow();
        List<Service> servicesToSend = determineServicesToSend(projectUser, project);
        comm(projectUser, project, messageAction, user, servicesToSend);
    }

    private List<Service> determineServicesToSend(ProjectUserDTO projectUser, Project project) {
        if (projectUser.getRole() == ADMIN) {
            List<ProjectServices> projectServices = projectServicesService.getProjectServicesByProjectId(project.getId());

            return projectServices.stream()
                    .filter(ProjectServices::isSubscribed)
                    .map(ProjectServices::getService)
                    .collect(toList());

        } else {
            List<Service> services = serviceService.findAll();

            return services.stream()
                    .filter(service -> projectUser.getServices().contains(service.getName()))
                    .filter(service -> hasSubscribedService(project, service))
                    .collect(toList());

        }
    }

    private void comm(ProjectUserDTO projectUser, Project project, MessageAction messageAction, User user, List<Service> servicesToSend) {
        List<ProjectServiceDto> dtos = servicesToSend.stream()
                .map(ProjectServiceDto::new)
                .collect(toList());

        for (ProjectServiceDto dto : dtos) {
            userServiceHelper.sendUserToServicesOnCreate(dto, project, messageAction, user, projectUser, projectUser.getRole().name());
        }
    }

    private boolean hasSubscribedService(Project project, Service service) {
        return projectServicesService.findByServiceAndProject(service, project)
                .filter(ProjectServices::isSubscribed)
                .isPresent();
    }
}