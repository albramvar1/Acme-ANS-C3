
package acme.features.flight_crew.flight_assignment;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.controllers.AbstractGuiController;
import acme.client.controllers.GuiController;
import acme.entities.flight_assignment.FlightAssignment;
import acme.realms.FlightCrew;

@GuiController
public class CrewFlightAssignmentController extends AbstractGuiController<FlightCrew, FlightAssignment> {

	@Autowired
	private CrewFlightAssignmentListCompletedService	listCompleted;
	@Autowired
	private CrewFlightAssignmentListPlannedService		listPlanned;
	@Autowired
	private CrewFlightAssignmentShowService				show;
	@Autowired
	private CrewFlightAssignmentCreateService			create;
	@Autowired
	private CrewFlightAssignmentUpdateService			update;
	@Autowired
	private CrewFlightAssignmentDeleteService			delete;
	@Autowired
	private CrewFlightAssignmentPublishService			publish;


	@PostConstruct
	protected void initialise() {
		super.addBasicCommand("show", this.show);
		super.addBasicCommand("create", this.create);
		super.addBasicCommand("update", this.update);
		super.addBasicCommand("delete", this.delete);

		super.addCustomCommand("list-completed", "list", this.listCompleted);
		super.addCustomCommand("list-planned", "list", this.listPlanned);
		super.addCustomCommand("publish", "update", this.publish);
	}

}
