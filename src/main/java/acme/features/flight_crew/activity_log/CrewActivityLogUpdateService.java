
package acme.features.flight_crew.activity_log;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.AssignmentStatus;
import acme.entities.activity_log.ActivityLog;
import acme.realms.FlightCrew;

@GuiService
public class CrewActivityLogUpdateService extends AbstractGuiService<FlightCrew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean authorised = false;
		int logId;
		ActivityLog log;

		if (super.getRequest().hasData("id", int.class)) {
			logId = super.getRequest().getData("id", int.class);
			log = this.repository.findOneById(logId);

			if (log != null) {

				// La etapa debe haber finalizado
				boolean legFinished = !log.getFlightAssignment().getLeg().getScheduledArrival().after(MomentHelper.getCurrentMoment());

				authorised = log.isDraftMode() && super.getRequest().getPrincipal().hasRealm(log.getFlightAssignment().getFlightCrewMember()) && log.getFlightAssignment().getAssignmentStatus() == AssignmentStatus.CONFIRMED
					&& !log.getFlightAssignment().isDraftMode() && log.getFlightAssignment().getLeg().isPublished() && legFinished;
			}
		}

		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {

		ActivityLog log;
		int id;

		id = super.getRequest().getData("id", int.class);
		log = this.repository.findOneById(id);

		super.getBuffer().addData(log);

	}

	@Override
	public void bind(final ActivityLog log) {
		super.bindObject(log, "typeOfIncident", "description", "severityLevel");
	}

	@Override
	public void validate(final ActivityLog log) {

		// Protección adicional contra inconsistencias (duplicado defensivo con authorise)
		super.state(log.isDraftMode(), "*", "crewMember.log.error.already-published");
		super.state(log.getFlightAssignment().getAssignmentStatus() == AssignmentStatus.CONFIRMED, "*", "crewMember.log.error.assignment.not-confirmed");
		super.state(!log.getFlightAssignment().isDraftMode(), "*", "crewMember.log.error.assignment.not-published");
		super.state(log.getFlightAssignment().getLeg().isPublished(), "*", "crewMember.log.error.leg.not-published");

		Date now = MomentHelper.getCurrentMoment();
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
		dataset.put("validDraft", log.isDraftMode() && !log.getFlightAssignment().isDraftMode() && log.getFlightAssignment().getLeg().isPublished());
		dataset.put("draftLog", log.isDraftMode());

		super.getResponse().addData(dataset);
	}
}
