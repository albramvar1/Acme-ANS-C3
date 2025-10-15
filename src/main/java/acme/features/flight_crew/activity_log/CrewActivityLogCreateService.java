
package acme.features.flight_crew.activity_log;

import java.util.Date;

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
public class CrewActivityLogCreateService extends AbstractGuiService<FlightCrew, ActivityLog> {

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
				// La etapa debe haber finalizado
				boolean legFinished = !assignment.getLeg().getScheduledArrival().after(MomentHelper.getCurrentMoment());

				// Entendemos que una asignación solo puede tener logs si: ella y la etapa son públicas y si la asignación está confirmada (para evitar incongruencias)

				authorised = super.getRequest().getPrincipal().hasRealm(assignment.getFlightCrewMember()) //
					&& assignment.getAssignmentStatus() == AssignmentStatus.CONFIRMED //
					&& !assignment.isDraftMode() //
					&& assignment.getLeg().isPublished() //
					&& legFinished;
			}
		}

		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {

		int masterId;
		FlightAssignment assignment;
		ActivityLog log;

		masterId = super.getRequest().getData("masterId", int.class);
		assignment = this.repository.findAssignmentById(masterId);

		log = new ActivityLog();
		log.setRegistrationMoment(MomentHelper.getCurrentMoment());
		log.setDraftMode(true);
		log.setFlightAssignment(assignment);

		super.getBuffer().addData(log);

	}

	@Override
	public void bind(final ActivityLog log) {
		super.bindObject(log, "typeOfIncident", "description", "severityLevel");
	}

	@Override
	public void validate(final ActivityLog log) {

		Date now = MomentHelper.getCurrentMoment();

		super.state(log.getFlightAssignment().getAssignmentStatus() == AssignmentStatus.CONFIRMED, "*", "crewMember.log.error.assignment.not-confirmed");
		super.state(!log.getFlightAssignment().isDraftMode(), "*", "crewMember.log.error.assignment.not-published");
		super.state(log.getFlightAssignment().getLeg().isPublished(), "*", "crewMember.log.error.leg.not-published");

		Date arrival = log.getFlightAssignment().getLeg().getScheduledArrival();
		super.state(!arrival.after(now), "*", "crewMember.log.error.leg.finished");
	}

	@Override
	public void perform(final ActivityLog log) {
		this.repository.save(log);
	}

	@Override
	public void unbind(final ActivityLog log) {
		Dataset dataset;

		dataset = super.unbindObject(log, "registrationMoment", "typeOfIncident", "description", "severityLevel");
		dataset.put("validDraft", !log.getFlightAssignment().isDraftMode() //
			&& log.getFlightAssignment().getLeg().isPublished());
		dataset.put("masterId", super.getRequest().getData("masterId", int.class));

		super.getResponse().addData(dataset);
	}
}
