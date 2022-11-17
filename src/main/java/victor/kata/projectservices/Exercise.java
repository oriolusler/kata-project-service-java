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
        if (projectUser.getRole() == ADMIN) {
            List<ProjectServices> projectServices = projectServicesService.getProjectServicesByProjectId(project.getId());

            List<ProjectServices> subscribedServices = projectServices.stream()
                    .filter(ProjectServices::isSubscribed)
                    .collect(toList());

            List<ProjectServiceDto> dtos = projectServices.stream()
                    .filter(ProjectServices::isSubscribed)
                    .map(ps -> new ProjectServiceDto(ps.getService()))
                    .collect(toList());

            dtos.forEach(dto -> userServiceHelper.sendUserToServicesOnCreate(dto, project, messageAction, user, projectUser, ADMIN.name()));

        } else {
            List<Service> services = serviceService.findAll();

            List<Service> userServices = services.stream()
                    .filter(service -> projectUser.getServices().contains(service.getName()))
                    .collect(toList());

            List<Service> subscribedServices = new ArrayList<>();
            for (Service service : userServices) {
                Optional<ProjectServices> projectServices = projectServicesService.findByServiceAndProject(service, project);
                if (projectServices.isPresent() && projectServices.get().isSubscribed()) {
                    subscribedServices.add(service);
                }
            }

            for (Service service : subscribedServices) {
                ProjectServiceDto projectServiceDTO = new ProjectServiceDto(service);
                userServiceHelper.sendUserToServicesOnCreate(projectServiceDTO, project, messageAction, user, projectUser, projectUser.getRole().name());
            }
        }
    }
}