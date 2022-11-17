package victor.kata.projectservices;

public class ProjectServiceDto {
   private final Service service;

   public ProjectServiceDto(Service service) {
      this.service = service;
   }

   public Service getService() {
      return service;
   }
}
