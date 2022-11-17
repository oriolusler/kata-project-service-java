package victor.kata.projectservices;

import static victor.kata.projectservices.ProjectServiceStatus.SUBSCRIBED;

public class ProjectServices {
   private ProjectServiceStatus projectServiceStatus;
   private Service service;

    public boolean isSubscribed() {
        return getProjectServiceStatus() == SUBSCRIBED;
    }

    public ProjectServiceStatus getProjectServiceStatus() {
      return projectServiceStatus;
   }


   public Service getService() {
      return service;
   }

}
