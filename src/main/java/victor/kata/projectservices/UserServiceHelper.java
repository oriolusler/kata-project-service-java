package victor.kata.projectservices;

public interface UserServiceHelper {
   void sendUserToServicesOnCreate(ProjectServiceDto projectServiceDTO, Project project, MessageAction messageAction, User user, ProjectUserDTO projectUser, String name);
}
