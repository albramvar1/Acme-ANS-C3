
package acme.features.flight_crew.activity_log;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.AssignmentStatus;
import acme.entities.activity_log.ActivityLog;
import acme.entities.flight_assignment.FlightAssignment;
import acme.realms.FlightCrew;

@GuiService
public class CrewActivityLogListService extends AbstractGuiService<FlightCrew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean authorised = false;

		if (super.getRequest().hasData("masterId", int.class)) {
			int assignmentId = super.getRequest().getData("masterId", int.class);
			FlightAssignment assignment = this.repository.findAssignmentById(assignmentId);

			if (assignment != null) {
				boolean legFinished = !assignment.getLeg().getScheduledArrival().after(MomentHelper.getCurrentMoment());
				authorised = super.getRequest().getPrincipal().hasRealm(assignment.getFlightCrewMember()) //
					&& assignment.getAssignmentStatus() == AssignmentStatus.CONFIRMED && !assignment.isDraftMode() //
					&& assignment.getLeg().isPublished() //
					&& legFinished;
			}
		}

		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {

		Collection<ActivityLog> logs;
		int masterId;

		masterId = super.getRequest().getData("masterId", int.class);

		logs = this.repository.findLogsByMasterId(masterId);

		super.getBuffer().addData(logs);

	}

	@Override
	public void unbind(final ActivityLog log) {
		Dataset dataset;

		dataset = super.unbindObject(log, "flightAssignment.leg.flightCode", "flightAssignment.crewRole", "typeOfIncident", "severityLevel");

		super.getResponse().addData(dataset);
	}

	@Override
	public void unbind(final Collection<ActivityLog> logs) {
		int masterId;

		masterId = super.getRequest().getData("masterId", int.class);
		FlightAssignment assignment = this.repository.findAssignmentById(masterId);

		boolean canCreate = assignment.getAssignmentStatus() == AssignmentStatus.CONFIRMED //
			&& !assignment.isDraftMode() //
			&& assignment.getLeg().isPublished() //
			&& !assignment.getLeg().getScheduledArrival().after(MomentHelper.getCurrentMoment());

		super.getResponse().addGlobal("canCreate", canCreate);
		super.getResponse().addGlobal("masterId", masterId);
	}

}
